package com.flashcards.messaging;

import com.flashcards.common.events.StudyEvents;
import com.flashcards.common.events.StudyProgressSaved;
import com.flashcards.model.OutboxEvent;
import com.flashcards.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

// Sends events waiting in the outbox to RabbitMQ, in the background.
//
// Every two seconds it sends the waiting events, oldest first, and deletes each one
// once the broker has taken it. If the broker can't be reached it stops for this
// round and tries again next time, so events wait in the database until the broker
// is back. Users never wait on this: their request finished when the event was
// stored.
//
// Delivery is at-least-once: if the service stops between sending an event and
// deleting its row, the event is sent again after restart. Consumers skip repeats
// by eventId. (Two copies of this service would also both send the same rows; at
// scale each copy would claim its rows first, e.g. with SELECT ... FOR UPDATE SKIP LOCKED.)
@Component
public class OutboxRelay {

    private static final Logger logger = LoggerFactory.getLogger(OutboxRelay.class);

    private final OutboxEventRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final JsonMapper jsonMapper;

    // Remembers an outage so it is logged once when it starts and once when it ends,
    // not every two seconds.
    private boolean brokerDown = false;

    public OutboxRelay(OutboxEventRepository outboxRepository, RabbitTemplate rabbitTemplate,
                       JsonMapper jsonMapper) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.jsonMapper = jsonMapper;
    }

    // fixedDelay: the next round starts two seconds after this one finishes, so
    // rounds never overlap.
    @Scheduled(fixedDelay = 2000)
    public void sendWaitingEvents() {
        for (OutboxEvent row : outboxRepository.findTop100ByOrderByIdAsc()) {
            StudyProgressSaved event = jsonMapper.readValue(row.getPayload(), StudyProgressSaved.class);
            try {
                // A fanout exchange ignores the routing key, so it is left empty.
                rabbitTemplate.convertAndSend(StudyEvents.EXCHANGE, "", event);
            } catch (AmqpException e) {
                if (!brokerDown) {
                    logger.warn("Message broker unreachable; study events will wait in the outbox: {}", e.getMessage());
                    brokerDown = true;
                }
                return;
            }
            outboxRepository.delete(row);

            if (brokerDown) {
                logger.info("Message broker reachable again; sending waiting study events");
                brokerDown = false;
            }
        }
    }
}

package com.flashcards.messaging;

import com.flashcards.common.events.StudyEvents;
import com.flashcards.model.OutboxEvent;
import com.flashcards.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

// Sends events waiting in the outbox to RabbitMQ, in the background.
//
// Every two seconds it sends the waiting events, oldest first. Each row is deleted
// only after RabbitMQ confirms it has stored the message (a publisher confirm), so
// an event is never deleted while it could still be lost in transit. If the broker
// can't be reached, or doesn't confirm in time, the relay stops for this round and
// tries again next time, so events wait in the database until the broker is back.
// Users never wait on this: their request finished when the event was stored.
//
// The stored JSON is sent exactly as it is, without being read back into a Java
// object, so a row can never fail to convert and hold up the rows behind it.
//
// Delivery is at-least-once: if the service stops between the confirm and deleting
// the row, the event is sent again after restart. Consumers skip repeats by eventId.
// (Two copies of this service would also both send the same rows; at scale each copy
// would claim its rows first, e.g. with SELECT ... FOR UPDATE SKIP LOCKED.)
@Component
public class OutboxRelay {

    private static final Logger logger = LoggerFactory.getLogger(OutboxRelay.class);

    // How long to wait for the broker to confirm one message before trying again later.
    private static final long CONFIRM_TIMEOUT_MS = 5000;

    private final OutboxEventRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    // Remembers an outage so it is logged once when it starts and once when it ends,
    // not every two seconds.
    private boolean brokerDown = false;

    public OutboxRelay(OutboxEventRepository outboxRepository, RabbitTemplate rabbitTemplate) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    // fixedDelay: the next round starts two seconds after this one finishes, so
    // rounds never overlap.
    @Scheduled(fixedDelay = 2000)
    public void sendWaitingEvents() {
        for (OutboxEvent row : outboxRepository.findTop100ByOrderByIdAsc()) {
            try {
                sendAndConfirm(row);
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

    // Sends one stored event and waits until the broker confirms it. Throws an
    // AmqpException if the broker can't be reached, rejects the message, or doesn't
    // confirm within the timeout.
    private void sendAndConfirm(OutboxEvent row) {
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setContentEncoding(StandardCharsets.UTF_8.name());
        properties.setMessageId(row.getEventId());
        Message message = new Message(row.getPayload().getBytes(StandardCharsets.UTF_8), properties);

        // invoke runs both steps on the same channel, which the confirm requires.
        // A fanout exchange ignores the routing key, so it is left empty.
        rabbitTemplate.invoke(operations -> {
            operations.send(StudyEvents.EXCHANGE, "", message);
            operations.waitForConfirmsOrDie(CONFIRM_TIMEOUT_MS);
            return null;
        });
    }
}

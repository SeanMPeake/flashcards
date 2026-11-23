package com.flashcards.messaging;

import com.flashcards.common.events.StudyEvents;
import com.flashcards.common.events.StudyProgressSaved;
import com.flashcards.exception.EventPublishException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

// Publishes study events to RabbitMQ.
@Component
public class StudyEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public StudyEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Sends the event to the study.events exchange. A fanout exchange ignores the
    // routing key, so it is left empty. If the broker can't be reached, this throws
    // EventPublishException (returned to the browser as 503, so it retries).
    public void publish(StudyProgressSaved event) {
        try {
            rabbitTemplate.convertAndSend(StudyEvents.EXCHANGE, "", event);
        } catch (AmqpException e) {
            throw new EventPublishException(e);
        }
    }
}

package com.flashcards.messaging;

import com.flashcards.common.events.StudyEvents;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// RabbitMQ setup for the publishing side.
//
// The deck service only declares the exchange it publishes to. It doesn't declare
// any queues: each consuming service (Review Insights, Analytics) declares and binds
// its own queue, so the publisher never needs to know who is listening.
@Configuration
public class MessagingConfig {

    // Fanout: every message is copied to every queue bound to this exchange.
    // Durable: the exchange survives a broker restart.
    @Bean
    public FanoutExchange studyEventsExchange() {
        return new FanoutExchange(StudyEvents.EXCHANGE, true, false);
    }

    // Sends event objects as JSON, so any consumer (in any language) can read them.
    // Spring Boot's RabbitTemplate picks this converter up automatically.
    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}

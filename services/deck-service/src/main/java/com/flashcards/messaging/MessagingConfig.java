package com.flashcards.messaging;

import com.flashcards.common.events.StudyEvents;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// RabbitMQ setup for the publishing side.
//
// The deck service only declares the exchange it publishes to. It doesn't declare
// any queues: each consuming service (Review Insights, Analytics) declares and binds
// its own queue, so the publisher never needs to know who is listening.
//
// Known limitation (accepted): a fanout exchange stores nothing, so events published
// while no queue is bound are dropped. A new consumer therefore receives events only
// from the moment its queue first exists, not the history before it. Replaying
// history would need a stream (e.g. a RabbitMQ stream or Kafka) instead of pub/sub;
// this project has no replay requirement.
//
// @EnableScheduling turns on @Scheduled methods, which OutboxRelay uses to send
// waiting events in the background.
@Configuration
@EnableScheduling
public class MessagingConfig {

    // Fanout: every message is copied to every queue bound to this exchange.
    // Durable: the exchange survives a broker restart.
    @Bean
    public FanoutExchange studyEventsExchange() {
        return new FanoutExchange(StudyEvents.EXCHANGE, true, false);
    }
}

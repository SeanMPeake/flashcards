package com.flashcards.messaging;

import com.flashcards.common.events.StudyEvents;
import com.flashcards.common.events.StudyProgressSaved;
import com.flashcards.model.OutboxEvent;
import com.flashcards.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

// Unit Test — OutboxRelay sends waiting events to the broker and removes each one
// only after it has been sent.
@ExtendWith(MockitoExtension.class)
class OutboxRelayTest {

    @Mock
    private OutboxEventRepository outboxRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    private OutboxRelay relay;

    @BeforeEach
    void createRelay() {
        relay = new OutboxRelay(outboxRepository, rabbitTemplate, jsonMapper);
    }

    private StudyProgressSaved event(String eventId) {
        return new StudyProgressSaved(eventId, 1L, 1L, "session-1", 5, 2, Instant.parse("2026-11-23T15:00:00Z"));
    }

    private OutboxEvent row(StudyProgressSaved event) {
        return new OutboxEvent(event.eventId(), jsonMapper.writeValueAsString(event));
    }

    @Test
    @DisplayName("Sends each waiting event to study.events, then removes it from the outbox")
    void sendsThenRemoves() {
        StudyProgressSaved first = event("batch-1");
        StudyProgressSaved second = event("batch-2");
        OutboxEvent firstRow = row(first);
        OutboxEvent secondRow = row(second);
        given(outboxRepository.findTop100ByOrderByIdAsc()).willReturn(List.of(firstRow, secondRow));

        relay.sendWaitingEvents();

        // Each event is removed only after it has been sent, in order.
        InOrder order = inOrder(rabbitTemplate, outboxRepository);
        order.verify(rabbitTemplate).convertAndSend(StudyEvents.EXCHANGE, "", first);
        order.verify(outboxRepository).delete(firstRow);
        order.verify(rabbitTemplate).convertAndSend(StudyEvents.EXCHANGE, "", second);
        order.verify(outboxRepository).delete(secondRow);
    }

    @Test
    @DisplayName("If the broker can't be reached, events stay in the outbox for the next round")
    void keepsEventsWhenBrokerIsDown() {
        given(outboxRepository.findTop100ByOrderByIdAsc()).willReturn(List.of(row(event("batch-1")), row(event("batch-2"))));
        willThrow(new AmqpConnectException(new RuntimeException("broker down")))
                .given(rabbitTemplate).convertAndSend(eq(StudyEvents.EXCHANGE), eq(""), any(Object.class));

        relay.sendWaitingEvents();

        // Stops at the first failure: one attempt, nothing removed.
        then(rabbitTemplate).should().convertAndSend(eq(StudyEvents.EXCHANGE), eq(""), any(Object.class));
        then(outboxRepository).should(never()).delete(any());
    }
}

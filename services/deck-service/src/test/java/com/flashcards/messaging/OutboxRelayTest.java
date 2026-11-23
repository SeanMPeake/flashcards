package com.flashcards.messaging;

import com.flashcards.common.events.StudyEvents;
import com.flashcards.model.OutboxEvent;
import com.flashcards.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.AmqpTimeoutException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitOperations.OperationsCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

// Unit Test — OutboxRelay sends waiting events to the broker and removes each one
// only after the broker has confirmed it.
@ExtendWith(MockitoExtension.class)
class OutboxRelayTest {

    @Mock
    private OutboxEventRepository outboxRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private OutboxRelay relay;

    // The real RabbitTemplate.invoke runs the callback on one channel; the mock runs
    // it with itself as that channel, so send and waitForConfirmsOrDie land on the mock.
    @BeforeEach
    @SuppressWarnings("unchecked")
    void createRelay() {
        given(rabbitTemplate.invoke(any())).willAnswer(inv ->
                ((OperationsCallback<Object>) inv.getArgument(0)).doInRabbit(rabbitTemplate));
        relay = new OutboxRelay(outboxRepository, rabbitTemplate);
    }

    private OutboxEvent row(String eventId) {
        return new OutboxEvent(eventId, "{\"eventId\":\"" + eventId + "\",\"cardsReviewed\":5}");
    }

    @Test
    @DisplayName("Sends the stored JSON as-is, waits for the confirm, then removes the row, in order")
    void sendsConfirmsThenRemoves() {
        OutboxEvent first = row("1:batch-1");
        OutboxEvent second = row("1:batch-2");
        given(outboxRepository.findTop100ByOrderByIdAsc()).willReturn(List.of(first, second));

        relay.sendWaitingEvents();

        InOrder order = inOrder(rabbitTemplate, outboxRepository);
        ArgumentCaptor<Message> sent = ArgumentCaptor.forClass(Message.class);
        order.verify(rabbitTemplate).send(eq(StudyEvents.EXCHANGE), eq(""), sent.capture());
        order.verify(rabbitTemplate).waitForConfirmsOrDie(anyLong());
        order.verify(outboxRepository).delete(first);
        order.verify(rabbitTemplate).send(eq(StudyEvents.EXCHANGE), eq(""), any(Message.class));
        order.verify(rabbitTemplate).waitForConfirmsOrDie(anyLong());
        order.verify(outboxRepository).delete(second);

        // The body is exactly the stored JSON, marked as JSON, and carries the event id.
        Message message = sent.getValue();
        assertEquals(first.getPayload(), new String(message.getBody(), StandardCharsets.UTF_8));
        assertEquals("application/json", message.getMessageProperties().getContentType());
        assertEquals("1:batch-1", message.getMessageProperties().getMessageId());
    }

    @Test
    @DisplayName("If the broker can't be reached, events stay in the outbox for the next round")
    void keepsEventsWhenBrokerIsDown() {
        given(outboxRepository.findTop100ByOrderByIdAsc()).willReturn(List.of(row("1:batch-1"), row("1:batch-2")));
        willThrow(new AmqpException("broker down"))
                .given(rabbitTemplate).send(eq(StudyEvents.EXCHANGE), eq(""), any(Message.class));

        relay.sendWaitingEvents();

        // Stops at the first failure: one attempt, nothing removed.
        then(rabbitTemplate).should(times(1)).send(eq(StudyEvents.EXCHANGE), eq(""), any(Message.class));
        then(outboxRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("If the broker doesn't confirm, the event is kept even though it was sent")
    void keepsEventWithoutConfirm() {
        given(outboxRepository.findTop100ByOrderByIdAsc()).willReturn(List.of(row("1:batch-1")));
        willThrow(new AmqpTimeoutException("no confirm"))
                .given(rabbitTemplate).waitForConfirmsOrDie(anyLong());

        relay.sendWaitingEvents();

        then(rabbitTemplate).should().send(eq(StudyEvents.EXCHANGE), eq(""), any(Message.class));
        then(outboxRepository).should(never()).delete(any());
    }
}

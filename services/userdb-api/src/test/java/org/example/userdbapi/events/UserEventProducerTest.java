package org.example.userdbapi.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEventProducerTest {

    @Mock
    KafkaTemplate<String, UserEvent> kafkaTemplate;

    @InjectMocks
    UserEventProducer producer;

    @Test
    void send_success_callsKafkaTemplateWithExpectedArgs_andReturnsFuture() {
        String topic = "user.account";
        ReflectionTestUtils.setField(producer, "topic", topic);

        UserEvent event = UserEvent.created(42L, "a@b.com", "userdb-api");
        String expectedKey = "42";

        CompletableFuture<SendResult<String, UserEvent>> completed =
                CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(eq(topic), eq(expectedKey), eq(event))).thenReturn(completed);

        CompletableFuture<SendResult<String, UserEvent>> future = producer.send(event);

        verify(kafkaTemplate, times(1)).send(eq(topic), eq(expectedKey), eq(event));

        assertThat(future).isSameAs(completed);
        assertThat(future).isCompleted();
    }

    @Test
    void send_failure_completesExceptionally_andLogsError() {
        String topic = "user.account";
        ReflectionTestUtils.setField(producer, "topic", topic);

        UserEvent event = UserEvent.deleted(7L, "x@y.com", "userdb-api");
        String expectedKey = "7";

        CompletableFuture<SendResult<String, UserEvent>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("boom!"));

        when(kafkaTemplate.send(eq(topic), eq(expectedKey), eq(event))).thenReturn(failed);

        CompletableFuture<SendResult<String, UserEvent>> future = producer.send(event);

        assertThat(future).isCompletedExceptionally();
        verify(kafkaTemplate).send(eq(topic), eq(expectedKey), eq(event));
    }
}
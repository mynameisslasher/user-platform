package org.example.userdbapi.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    @Value("${app.kafka.user-topic:user.account}")
    private String topic;

    public CompletableFuture<SendResult<String, UserEvent>> send(UserEvent event){
        final String key = String.valueOf(event.getUserId());
        log.info("Producing Kafka event: type={}, userId={}, email={}, key={}, topic={}",
                event.getEventType(), event.getUserId(),  event.getEmail(), key, topic);

        CompletableFuture<SendResult<String, UserEvent>> future = kafkaTemplate.send(topic, key, event);

        future.thenAccept(result -> {
            RecordMetadata m = result.getRecordMetadata();
            log.info("Kafka event sent: topic={}, partition={}, offset={}, timestamp={}",
                    m.topic(), m.partition(), m.offset(), m.timestamp());
        }).exceptionally(ex -> {
            log.error("Kafka event sent failed: topic={}, key={}, error={}", topic, key, ex.toString(), ex);
            return null;
        });
        return future;
    }
}

package org.example.userdbapi.it;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.userdbapi.dto.UserCreateDto;
import org.example.userdbapi.events.UserEvent;
import org.example.userdbapi.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@EmbeddedKafka(partitions = 1, topics = {"user.account"})
class UserEventPublishingIT {

    private static final String TOPIC = "user.account";

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private UserService userService;

    private Consumer<String, UserEvent> consumer;

    @AfterEach
    void tearDown() {
        if (consumer != null) consumer.close();
    }

    @Test
    void whenCreateUser_thenEventIsPublishedToKafka() {
        // 1) consumer для чтения события
        Map<String, Object> props = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafka);

        JsonDeserializer<UserEvent> valueDeserializer = new JsonDeserializer<>(UserEvent.class, false);
        valueDeserializer.addTrustedPackages("*");

        DefaultKafkaConsumerFactory<String, UserEvent> cf =
                new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
        consumer = cf.createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, TOPIC);

        // 2) вызов бизнес-логики
        UserCreateDto dto = new UserCreateDto("Alice", "alice@example.com", 25);
        userService.createUser(dto);

        // 3) проверка: сообщение попало в топик
        ConsumerRecord<String, UserEvent> record =
                KafkaTestUtils.getSingleRecord(consumer, TOPIC, Duration.ofSeconds(5));

        assertThat(record).isNotNull();
        UserEvent event = record.value();
        assertThat(event.getEventType()).isEqualTo(UserEvent.Type.USER_CREATED);
        assertThat(event.getEmail()).isEqualTo("alice@example.com");
        assertThat(event.getUserId()).isNotNull();
    }
}
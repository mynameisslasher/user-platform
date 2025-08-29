package org.example.notificationservice.it;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.internet.MimeMessage;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.example.notificationservice.config.AppKafkaProperties;
import org.example.notificationservice.events.UserEvent;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.mail.port=3025"   // переопределяем SMTP порт для теста
})
@EmbeddedKafka(partitions = 1, topics = {"user.account"})
class NotificationServiceKafkaMailIT {

    @Autowired
    EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    AppKafkaProperties props;

    private GreenMail greenMail;
    private KafkaTemplate<String, UserEvent> template;

    @BeforeEach
    void setUp() {
        // Поднимаем in-memory SMTP
        greenMail = new GreenMail(new ServerSetup(3025, null, "smtp"));
        greenMail.start();

        // Простой продьюсер для отправки тестового события
        Map<String, Object> cfg = Map.of(
                "bootstrap.servers", embeddedKafka.getBrokersAsString(),
                "key.serializer", StringSerializer.class,
                "value.serializer", JsonSerializer.class,
                // для JsonSerializer
                "spring.json.trusted.packages", "*"
        );
        template = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(cfg));
    }

    @AfterEach
    void tearDown() {
        if (greenMail != null) greenMail.stop();
    }

    @Test
    void whenKafkaEventReceived_thenEmailIsSent() throws Exception {
        var evt = UserEvent.builder()
                .eventType(UserEvent.Type.USER_CREATED)
                .userId(123L)
                .email("qa@example.com")
                .build();

        template.send(new ProducerRecord<>(props.getUserTopic(), String.valueOf(evt.getUserId()), evt));

        // ждём письмо
        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            MimeMessage[] messages = greenMail.getReceivedMessages();
            assertThat(messages).isNotEmpty();
            String subject = messages[0].getSubject();
            assertThat(subject).contains("Аккаунт создан");
        });
    }
}
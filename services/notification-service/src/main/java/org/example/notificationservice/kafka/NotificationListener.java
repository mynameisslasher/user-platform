package org.example.notificationservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.events.UserEvent;
import org.example.notificationservice.mail.MailService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final MailService mailService;

    @KafkaListener(topics = "${app.kafka.user-topic}")
    public void onUserEvent(UserEvent event, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.info("Kafka message received: key={}, eventType={}, email={}",
                key, event.getEventType(), event.getEmail());
        mailService.sendFor(event);
    }
}

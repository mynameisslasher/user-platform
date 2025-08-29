package org.example.notificationservice.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.events.UserEvent;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendFor(UserEvent event) {
        String subject;
        String text;

        switch(event.getEventType()) {
            case USER_CREATED -> {
                subject = "Аккаунт создан";
                text = "Здравствуйте! Ваш аккаунт на сайте был успешно создан.";
            }
            case USER_DELETED -> {
                subject = "Аккаунт удалён";
                text = "Здравствуйте! Ваш аккаунт был удалён";
            }
            default -> {
                log.warn("Unknown event type: {}", event.getEventType());
                return;
            }
        }

        sendPlain(event.getEmail(), subject, text);
    }

    public void sendPlain(String to, String subject, String text) {
        var msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        mailSender.send(msg);
        log.info("Mail sent: to={}, subject={}", to, subject);
    }
}

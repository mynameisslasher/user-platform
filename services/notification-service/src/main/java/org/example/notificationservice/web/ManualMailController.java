package org.example.notificationservice.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.notificationservice.events.UserEvent;
import org.example.notificationservice.mail.MailService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Validated
public class ManualMailController {

    private final MailService mailService;

    @PostMapping("/send-mail")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void sendMail(@Valid @RequestBody ManualMailRequest req) {
        var evt = UserEvent.builder()
                .eventType(req.operation())
                .email(req.email())
                .build();
        mailService.sendFor(evt);
    }
}

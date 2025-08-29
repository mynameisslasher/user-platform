package org.example.notificationservice.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.notificationservice.events.UserEvent;

public record ManualMailRequest(
        @Email @NotBlank String email,
        @NotNull UserEvent.Type operation
) {}
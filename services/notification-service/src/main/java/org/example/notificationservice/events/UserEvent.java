package org.example.notificationservice.events;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Getter @Setter
@Builder @Jacksonized
@NoArgsConstructor @AllArgsConstructor
@ToString
public class UserEvent {
    public enum Type { USER_CREATED, USER_DELETED }
    private String eventId;
    private Type eventType;
    private Long userId;
    private String email;
    private Instant occurredAt;
    private String source;
}
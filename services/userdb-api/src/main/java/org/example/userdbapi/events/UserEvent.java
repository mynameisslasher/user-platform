package org.example.userdbapi.events;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@Builder @Jacksonized
@NoArgsConstructor @AllArgsConstructor
@ToString
public class UserEvent {
    public enum Type{USER_CREATED, USER_DELETED}

    private String eventId;
    private Type eventType;
    private Long userId;
    private String email;
    private Instant occurredAt;
    private String source;

    public static UserEvent created(Long userId, String email, String source) {
        return UserEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(Type.USER_CREATED)
                .userId(userId)
                .email(email)
                .source(source)
                .occurredAt(Instant.now())
                .build();
    }

    public static UserEvent deleted(Long userId, String email, String source) {
        return UserEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(Type.USER_DELETED)
                .userId(userId)
                .email(email)
                .source(source)
                .occurredAt(Instant.now())
                .build();
    }
}

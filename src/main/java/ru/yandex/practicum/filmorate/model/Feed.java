package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Feed {
    private LocalDateTime timestamp;
    private int userId;
    private EventType eventType;
    private OperationType operation;
    private int eventId;
    private int entityId;
}

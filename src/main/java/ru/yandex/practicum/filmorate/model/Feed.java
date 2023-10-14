package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class Feed {
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date timestamp;
    private int userId;
    private EventType eventType;
    private OperationType operation;
    private int eventId;
    private int entityId;
}

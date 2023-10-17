package ru.yandex.practicum.filmorate.storage.feed;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.OperationType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Component
public class FeedDBStorage implements FeedStorage {

    private final JdbcTemplate jdbcTemplate;

    public FeedDBStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Feed> getFeedsList(int id) {
        String sql = "SELECT * FROM FEED WHERE user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFeed(rs), id);
    }

    private EventType getEventTypeById(int id) {
        return EventType.values()[id - 1];
    }

    private OperationType getOperationTypeById(int id) {
        return OperationType.values()[id - 1];
    }

    private Feed makeFeed(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        int eventId = rs.getInt("event_id");
        int entityId = rs.getInt("entity_id");
        EventType eventType = getEventTypeById(rs.getInt("event_type"));
        OperationType operationType = getOperationTypeById(rs.getInt("operation"));
        Date date = rs.getTimestamp("time_stamp");
        return Feed.builder()
                .userId(userId)
                .eventType(eventType)
                .operation(operationType)
                .eventId(eventId)
                .entityId(entityId)
                .timestamp(date)
                .build();
    }
}

package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Slf4j
public class UserDBStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private static final String WRONG_USER_ID = "Пользователь с указанным ID не найден";

    public UserDBStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User addUser(User user) {
        Map<String, Object> values = new HashMap<>();
        values.put("email", user.getEmail());
        values.put("login", user.getLogin());
        values.put("name", user.getName());
        values.put("birthday", user.getBirthday());

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");

        int userId = simpleJdbcInsert.executeAndReturnKey(values).intValue();

        log.debug("Пользователь успешно создан с ID = {}", userId);
        return getUser(userId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_USER_ID));
    }

    @Override
    public User updateUser(User user) {
        String sqlForUpdateUser = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        int userId = user.getId();
        if (getUser(userId).isPresent()) {
            jdbcTemplate.update(sqlForUpdateUser,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    user.getBirthday(),
                    userId);
        }
        log.debug("Пользователь с ID = {} успешно обновлен", userId);
        return getUser(userId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_USER_ID));
    }

    @Override
    public List<User> getAllUsers() {
        return jdbcTemplate.query("SELECT * FROM users", this::makeUser);
    }

    @Override
    public Optional<User> getUser(int userId) {
        String sqlUser = "SELECT * FROM users WHERE user_id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sqlUser, this::makeUser, userId);
            log.debug("Пользователь с указанным ID = {} найден", userId);
            return Optional.ofNullable(user);
        } catch (DataAccessException e) {
            log.debug("Пользователь с указанным ID = {} не найден", userId);
            return Optional.empty();
        }
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        String sqlAddFriend = "INSERT INTO user_friend (user_id, friend_id) VALUES (?,?)";

        jdbcTemplate.update(sqlAddFriend, userId, friendId);
        log.debug("Пользователь {} успешно добавил в друзья {} ", userId, friendId);
    }

    @Override
    public void deleteFriend(Integer userId, Integer friendId) {
        String sqlDeleteFriend = "DELETE FROM user_friend WHERE user_id = ? AND friend_id = ?";

        jdbcTemplate.update(sqlDeleteFriend, userId, friendId);
        log.debug("Пользователь {} успешно удалил из друзей {} ", userId, friendId);
    }

    @Override
    public List<User> getFriendsList(Integer userId) {
        String sqlFriendsList = "SELECT * " +
                "FROM users u " +
                "JOIN user_friend uf on u.user_id = uf.friend_id " +
                "WHERE uf.user_id = ?";
        return jdbcTemplate.query(sqlFriendsList, this::makeUser, userId);
    }

    @Override
    public List<User> getCommonFriends(Integer userId, Integer friendId) {
        String sqlCommonFriends = "SELECT * " +
                "FROM users u " +
                "JOIN user_friend uf on u.user_id = uf.friend_id " +
                "JOIN user_friend uf1 on uf.friend_id = uf1.friend_id " +
                "WHERE uf.user_id = ? AND uf1.user_id = ?";
        return jdbcTemplate.query(sqlCommonFriends, this::makeUser, userId, friendId);
    }

    private User makeUser(ResultSet rs, int rowNum) throws SQLException {
        int userId = rs.getInt("user_id");
        try {
            return new User(
                    userId,
                    rs.getString("email"),
                    rs.getString("login"),
                    rs.getString("name"),
                    rs.getDate("birthday").toLocalDate());
        } catch (DataAccessException e) {
            throw new EntityNotFoundException(WRONG_USER_ID);
        }
    }

    @Override
    public void deleteUserById(int userId) {
        if (!getUser(userId).isPresent()) {
            throw new EntityNotFoundException("Пользователь с id: " + userId + " не найден.");
        }
        String sql = "DELETE FROM users WHERE user_id = ?;";
        jdbcTemplate.update(sql, userId);
    }

    private EventType getEventTypeById(int id) {
        EventType eventType = null;
        switch (id) {
            case 1:
                eventType = EventType.LIKE;
                break;
            case 2:
                eventType = EventType.REVIEW;
                break;
            case 3:
                eventType = EventType.FRIEND;
                break;
        }
        return eventType;
    }

    private OperationType getOperationTypeById(int id) {
        OperationType operationType = null;
        switch (id) {
            case 1:
                operationType = OperationType.REMOVE;
                break;
            case 2:
                operationType = OperationType.ADD;
                break;
            case 3:
                operationType = OperationType.UPDATE;
                break;

        }
        return operationType;
    }

    private Feed makeFeed(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        int eventId = rs.getInt("event_id");
        int entityId = 0;
        if (rs.getInt("film_id") != 0) {
            entityId = rs.getInt("film_id");
        } else {
            entityId = rs.getInt("user_friend_id");
        }
        EventType eventType = getEventTypeById(rs.getInt("event_type"));
        OperationType operationType = getOperationTypeById(rs.getInt("operation"));
        LocalDateTime localDateTime = rs.getTimestamp("time_stamp").toLocalDateTime();
        return Feed.builder()
                .userId(userId)
                .eventType(eventType)
                .operation(operationType)
                .eventId(eventId)
                .entityId(entityId)
                .timestamp(localDateTime)
                .build();
    }

    @Override
    public List<Feed> getFeedsList(int id) {
        String sqlUserFriends = "SELECT * FROM USERS WHERE user_id IN (SELECT friend_id " +
                "FROM user_friend WHERE user_id = ?)";
        String sql = "SELECT * FROM FEED WHERE user_id = ?";
        List<User> userFriendsList = jdbcTemplate.query(sqlUserFriends, (rs, intRow) -> makeUser(rs, intRow), id);
        List<Feed> feedsList = new ArrayList<>();
        if (!userFriendsList.isEmpty()) {
            for (User user : userFriendsList) {
                feedsList.addAll(jdbcTemplate.query(sql, (rs, rowNum) -> makeFeed(rs), user.getId()));
            }
        }
        return feedsList;
    }
}

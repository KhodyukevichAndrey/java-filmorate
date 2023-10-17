package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.constants.MyConstants;
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

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("users")
                .usingGeneratedKeyColumns("user_id");

        int userId = simpleJdbcInsert.executeAndReturnKey(values).intValue();

        log.debug("Пользователь успешно создан с ID = {}", userId);
        user.setId(userId);
        return user;
    }

    @Override
    public User updateUser(User user) {
        String sqlForUpdateUser = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        int userId = user.getId();
        jdbcTemplate.update(sqlForUpdateUser, user.getEmail(), user.getLogin(),
                user.getName(), user.getBirthday(), userId);
        log.debug("Пользователь с ID = {} успешно обновлен", userId);
        return user;
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
            return Optional.of(user);
        } catch (DataAccessException e) {
            log.debug("Пользователь с указанным ID = {} не найден", userId);
            return Optional.empty();
        }
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        String sqlAddFriend = "INSERT INTO user_friend (user_id, friend_id) VALUES (?,?)";
        jdbcTemplate.update(sqlAddFriend, userId, friendId);
        jdbcTemplate.update(MyConstants.SQLFEEDUSER, userId, friendId, 1, 3, 2, LocalDateTime.now());
        log.debug("Пользователь {} успешно добавил в друзья {} ", userId, friendId);
    }

    @Override
    public void deleteFriend(Integer userId, Integer friendId) {
        String sqlDeleteFriend = "DELETE FROM user_friend WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sqlDeleteFriend, userId, friendId);
        jdbcTemplate.update(MyConstants.SQLFEEDUSER, userId, friendId, 1, 3, 1, LocalDateTime.now());
        log.debug("Пользователь {} успешно удалил из друзей {} ", userId, friendId);
    }

    @Override
    public List<User> getFriendsList(Integer userId) {
        String sqlFriendsList = "SELECT * " + "FROM users u " + "JOIN user_friend uf on u.user_id = uf.friend_id " +
                "WHERE uf.user_id = ?";
        return jdbcTemplate.query(sqlFriendsList, this::makeUser, userId);
    }

    @Override
    public List<User> getCommonFriends(Integer userId, Integer friendId) {
        String sqlCommonFriends = "SELECT * " + "FROM users u " + "JOIN user_friend uf on u.user_id = uf.friend_id " +
                "JOIN user_friend uf1 on uf.friend_id = uf1.friend_id " + "WHERE uf.user_id = ? AND uf1.user_id = ?";
        return jdbcTemplate.query(sqlCommonFriends, this::makeUser, userId, friendId);
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

    @Override
    public List<Feed> getFeedsList(int id) {
        getUser(id);
        String sql = "SELECT * FROM FEED WHERE user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFeed(rs), id);
    }

    @Override
    public Set<Integer> getSimilarLikes(int userId) {
        for (User u : getAllUsers()) {
            if (u.getId() != userId) {
                return getLikedFilmsByUserId(userId, u.getId());
            }
        }
        return Collections.emptySet();
    }

    private Set<Integer> getLikedFilmsByUserId(Integer userId, Integer id) {
        Set<Integer> userLikes = new HashSet<>();
        String sql = "SELECT f.FILM_ID FROM (SELECT FILM_ID FROM FILM_LIKES WHERE USER_ID = ?) as f LEFT JOIN\n" +
                "(SELECT FILM_ID from FILM_LIKES where USER_ID = ?) as u ON f.FILM_ID = u.FILM_ID " +
                "WHERE u.FILM_ID is null";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, id, userId);
        while (sqlRowSet.next()) {
            userLikes.add(sqlRowSet.getInt("film_id"));
        }
        return userLikes;
    }

    private User makeUser(ResultSet rs, int rowNum) throws SQLException {
        int userId = rs.getInt("user_id");
        try {
            return new User(userId, rs.getString("email"), rs.getString("login"),
                    rs.getString("name"),
                    rs.getDate("birthday").toLocalDate());
        } catch (DataAccessException e) {
            throw new EntityNotFoundException(WRONG_USER_ID);
        }
    }
}

package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public Optional<User> getUser(Integer userId) {
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
}

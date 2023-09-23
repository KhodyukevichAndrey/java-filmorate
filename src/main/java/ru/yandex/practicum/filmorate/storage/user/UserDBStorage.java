package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.stream.Collectors;

@Component
@Qualifier("UserDBStorage")
@Slf4j
public class UserDBStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private static final String WRONG_USER_ID = "Пользователь с указанным ID не найден";

    public UserDBStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User addUser(User user) {
        checkUserName(user);

        Map<String, Object> values = new HashMap<>();
        values.put("email", user.getEmail());
        values.put("login", user.getLogin());
        values.put("name", user.getName());
        values.put("birthday", user.getBirthday());

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");

        Integer userId = simpleJdbcInsert.executeAndReturnKey(values).intValue();

        log.debug("Пользователь " + user.getName() + " успешно создан с ID - " + userId);
        return getUser(userId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_USER_ID));
    }

    @Override
    public User updateUser(User user) {
        String sqlForUpdateUser = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        checkUserName(user);
        int userId = user.getId();
        if (getUser(userId).isPresent()) {
            jdbcTemplate.update(sqlForUpdateUser,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    user.getBirthday(),
                    userId);
        }
        log.debug("Пользователь " + user.getName() + " успешно обновлен");
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
            return Optional.ofNullable(user);
        } catch (DataAccessException e) {
            throw new EntityNotFoundException(WRONG_USER_ID);
        }
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        String sqlAddFriend = "INSERT INTO user_friend (user_id, friend_id, confirmation) VALUES (?,?,?)";
        String sqlUpdateFriendshipForFriend = "UPDATE user_friend SET confirmation = ? WHERE user_id = ?";

        List<Integer> friendFriendsList = getFriendsList(friendId).stream()
                .map(User::getId)
                .collect(Collectors.toList());

        if (friendFriendsList.contains(userId)) {
            jdbcTemplate.update(sqlAddFriend, userId, friendId, true);
            jdbcTemplate.update(sqlUpdateFriendshipForFriend, true, friendId);
        } else {
            jdbcTemplate.update(sqlAddFriend, userId, friendId, false);
        }
        log.debug("Статус дружбы между пользователями успешно обновлен");
    }

    @Override
    public void deleteFriend(Integer userId, Integer friendId) {
        String sqlDeleteFriend = "DELETE FROM user_friend WHERE user_id = ? AND friend_id = ?";
        String sqlUpdateFriendshipForFriend = "UPDATE user_friend SET confirmation = ? WHERE user_id = ?";

        List<Integer> friendFriendsList = getFriendsList(friendId).stream()
                .map(User::getId)
                .collect(Collectors.toList());

        jdbcTemplate.update(sqlDeleteFriend, userId, friendId);
        if (friendFriendsList.contains(userId)) {
            jdbcTemplate.update(sqlUpdateFriendshipForFriend, false, friendId);
        }
        log.debug("Статус дружбы между пользователями успешно обновлен");
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
                    rs.getDate("birthday").toLocalDate(),
                    makeFriends(userId));
        } catch (DataAccessException e) {
            throw new EntityNotFoundException(WRONG_USER_ID);
        }
    }

    private Map<Integer, Boolean> makeFriends(Integer userId) {
        Map<Integer, Boolean> friends = new HashMap<>();
        jdbcTemplate.query("SELECT * " +
                "FROM user_friend " +
                "WHERE user_id = ?", (rs, rowNum) ->
                friends.put(rs.getInt("friend_id"), rs.getBoolean("confirmation")), userId);
        return friends;
    }

    private void checkUserName(User user) {
        String name = user.getName();
        if (name == null || name.isBlank()) {
            log.debug("Имя пользователя не задано -> name = login");
            user.setName(user.getLogin());
        }
    }
}

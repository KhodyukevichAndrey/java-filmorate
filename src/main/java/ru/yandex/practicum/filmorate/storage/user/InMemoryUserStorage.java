package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();
    private int userId = 1;

    @Override
    public User addUser(User user) {
        checkUserName(user);
        user.setId(generateUserId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (users.containsKey(user.getId())) {
            checkUserName(user);
            users.put(user.getId(), user);
        } else {
            log.warn("Пользователь с указанным id не найден {}", user.getId());
            throw new UserNotFoundException();
        }
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUser(Integer id) {
        if (!users.containsKey(id)) {
            log.warn("Пользователь с указанным id не найден {}", id);
            throw new UserNotFoundException();
        }
        return users.get(id);
    }

    private int generateUserId() {
        return userId++;
    }

    private void checkUserName(User user) {
        String name = user.getName();
        if (name == null || name.isBlank()) {
            log.debug("Имя пользователя не задано -> name = login");
            user.setName(user.getLogin());
        }
    }
}

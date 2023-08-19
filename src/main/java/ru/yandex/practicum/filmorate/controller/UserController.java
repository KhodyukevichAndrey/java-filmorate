package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();
    private int userId = 1;

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.debug("Получен запрос POST /users");
        checkUserName(user);
        user.setId(generateUserId());
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.debug("Получен запрос PUT /users");
        if (users.containsKey(user.getId())) {
            checkUserName(user);
            users.put(user.getId(), user);
        } else {
            log.warn("Пользователь с указанным id не найден {}", user.getId());
            throw new ValidationException("Пользователь с таким Id не найден");
        }
        return user;
    }

    @GetMapping
    public List<User> getUsers() {
        log.debug("Получен запрос GET /users");
        return new ArrayList<>(users.values());
    }

    private int generateUserId() {
        return userId++;
    }

    private void checkUserName(User user) {
        if (user.getName() == null) {
            log.debug("Имя пользователя не задано -> name = login");
            user.setName(user.getLogin());
        }
    }
}

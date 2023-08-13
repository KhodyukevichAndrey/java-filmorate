package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
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
    public User createUser(@RequestBody User user) {
        log.debug("Получен запрос POST /users");
        if(checkUserValidation(user)) {
            user.setId(generateUserId());
            users.put(user.getId(), user);
        }
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        log.debug("Получен запрос PUT /users");
        if(checkUserValidation(user) && users.containsKey(user.getId())) {
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

    private boolean checkUserValidation(User user) {
        if(user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Недопустимый email пользователя {}", user.getEmail());
            throw new ValidationException("Значение поля email не может быть пустым и должно содержать символ @");
        }

        if(user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Недопустимый login пользователя {}", user.getLogin());
            throw new ValidationException("Значение поля login не может быть пустым и не должно содержать пробелов");
        }

        if(user.getName() == null) {
            log.debug("Имя пользователя не задано -> name = login");
            user.setName(user.getLogin());
        }

        if(user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Недопустимая дата роджения пользователя {}", user.getBirthday());
            throw new ValidationException("Дата рождения пользователя должна быть не позднее текущей даты");
        }
        return true;
    }
}

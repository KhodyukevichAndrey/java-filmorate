package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        log.debug("Получен запрос POST /users");
        return userService.addUser(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.debug("Получен запрос PUT /users");
        return userService.updateUser(user);
    }

    @GetMapping
    public List<User> getUsers() {
        log.debug("Получен запрос GET /users");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Integer id) {
        log.debug("Получен запрос GET /users/{id}");
        return userService.getUser(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        log.debug("Получен запрос PUT /users/{id}/friends/{friendId}");
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        log.debug("Получен запрос DELETE /users/{id}/friends/{friendId}");
        userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getUsersFriends(@PathVariable Integer id) {
        log.debug("Получен запрос GET /{id}/friends");
        return userService.getFriendsList(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Integer id, @PathVariable Integer otherId) {
        log.debug("Получен запрос GET /users/{id}/friends/common/{otherId}");
        return userService.getCommonFriends(id, otherId);
    }

    @DeleteMapping("/{userId}")
    public String deleteUserById(@PathVariable int userId) {
        log.info("Получен запрос DELETE/users/{id}");
        userService.deleteUserById(userId);
        return "Пользователь с id: " + userId + " удалён.";
    }

    @GetMapping("/{id}/feed")
    public List<Feed> getFeedsList(@PathVariable int id) {
        log.debug("Получен запрос GET /users/"+id+"/feed");
        return userService.getFeedsList(id);
    }
}

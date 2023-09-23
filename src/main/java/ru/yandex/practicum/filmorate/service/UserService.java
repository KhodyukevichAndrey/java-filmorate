package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
public class UserService {

    private final UserStorage userStorage;
    private static final String WRONG_USER_ID = "Пользователь с указанным ID не найден";

    @Autowired
    public UserService(@Qualifier("UserDBStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User getUser(Integer id) {
        return userStorage.getUser(id)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_USER_ID));
    }

    public void addFriend(Integer userId, Integer userFriendId) {
        userStorage.getUser(userId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_USER_ID));
        userStorage.getUser(userFriendId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_USER_ID));
        userStorage.addFriend(userId, userFriendId);
    }

    public void deleteFriend(Integer userId, Integer userFriendId) {
        userStorage.getUser(userId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_USER_ID));
        userStorage.getUser(userFriendId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_USER_ID));
        userStorage.deleteFriend(userId, userFriendId);
    }

    public List<User> getFriendsList(Integer id) {
        userStorage.getUser(id)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_USER_ID));
        return userStorage.getFriendsList(id);
    }

    public List<User> getCommonFriends(Integer currentUserId, Integer anotherUserId) {
        userStorage.getUser(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_USER_ID));
        userStorage.getUser(anotherUserId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_USER_ID));
        return userStorage.getCommonFriends(currentUserId, anotherUserId);
    }
}

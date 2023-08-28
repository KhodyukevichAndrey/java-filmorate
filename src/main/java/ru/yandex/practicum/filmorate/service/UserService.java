package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;
    private static final String WRONG_USER_ID = "Пользователь с указанным ID не найден";

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        checkUserName(user);
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        checkUserName(user);
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
        User user = userStorage.getUser(userId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_USER_ID));
        User userFriend = userStorage.getUser(userFriendId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_USER_ID));

        user.getFriends().add(userFriendId);
        userFriend.getFriends().add(userId);
    }

    public void deleteFriend(Integer userId, Integer userFriendId) {
        User user = userStorage.getUser(userId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_USER_ID));
        User userFriend = userStorage.getUser(userFriendId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_USER_ID));

        user.getFriends().remove(userFriendId);
        userFriend.getFriends().remove(userId);
    }

    public List<User> getFriendsList(Integer id) {
        User currentUser = userStorage.getUser(id)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_USER_ID));

        return currentUser.getFriends().stream()
                .map(userStorage::getUser)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Integer currentUserId, Integer anotherUserId) {
        User currentUser = userStorage.getUser(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_USER_ID));
        User anotherUser = userStorage.getUser(anotherUserId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_USER_ID));

        List<Integer> currentUserFriends = currentUser.getFriends();

        return anotherUser.getFriends().stream()
                .filter(currentUserFriends::contains)
                .map(userStorage::getUser)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private void checkUserName(User user) {
        String name = user.getName();
        if (name == null || name.isBlank()) {
            log.debug("Имя пользователя не задано -> name = login");
            user.setName(user.getLogin());
        }
    }
}

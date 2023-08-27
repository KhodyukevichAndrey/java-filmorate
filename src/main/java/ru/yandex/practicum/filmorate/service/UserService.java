package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Integer userId, Integer userFriendId) {
        User user = userStorage.getUser(userId);
        User userFriend = userStorage.getUser(userFriendId);

        if (user == null || userFriend == null) {
            log.warn("ID одного из пользователей не найден");
            throw new UserNotFoundException();
        }

        user.getFriends().add(userFriendId);
        userFriend.getFriends().add(userId);
    }

    public void deleteFriend(Integer userId, Integer userFriendId) {
        User user = userStorage.getUser(userId);
        User userFriend = userStorage.getUser(userFriendId);

        if (user == null || userFriend == null) {
            log.warn("ID одного из пользователей не найден");
            throw new UserNotFoundException();
        }

        user.getFriends().remove(userFriendId);
        userFriend.getFriends().remove(userId);
    }

    public List<User> getFriendsList(Integer id) {
        User currentUser = userStorage.getUser(id);
        if (currentUser == null) {
            log.warn("Пользователь с указанным ID не найден {}", id);
            throw new UserNotFoundException();
        }
        List<Integer> friends = currentUser.getFriends();
        return userStorage.getAllUsers().stream()
                .filter(user -> friends.contains(user.getId()))
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Integer currentUserId, Integer anotherUserId) {
        User currentUser = userStorage.getUser(currentUserId);
        User anotherUser = userStorage.getUser(anotherUserId);

        if (currentUser == null || anotherUser == null) {
            log.warn("ID одного из пользователей не найден");
            throw new UserNotFoundException();
        }

        List<Integer> currentUserFriends = currentUser.getFriends();
        List<Integer> anotherUserFriends = anotherUser.getFriends();

        return userStorage.getAllUsers().stream()
                .filter(user -> currentUserFriends.contains(user.getId()))
                .filter(user -> anotherUserFriends.contains(user.getId()))
                .collect(Collectors.toList());
    }
}

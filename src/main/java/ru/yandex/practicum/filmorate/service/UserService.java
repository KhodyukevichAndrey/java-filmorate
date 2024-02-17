package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final FeedStorage feedStorage;
    private static final String WRONG_USER_ID = "Пользователь с указанным ID не найден";

    @Autowired
    public UserService(UserStorage userStorage, FilmStorage filmStorage, FeedStorage feedStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.feedStorage = feedStorage;
    }

    public User addUser(User user) {
        checkUserName(user);
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        checkUserName(user);
        getUser(user.getId());
        return userStorage.updateUser(user);
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User getUser(int id) {
        return userStorage.getUser(id)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_USER_ID));
    }

    public void addFriend(Integer userId, Integer userFriendId) {
        getUser(userId);
        getUser(userFriendId);
        userStorage.addFriend(userId, userFriendId);
    }

    public void deleteFriend(Integer userId, Integer userFriendId) {
        getUser(userId);
        getUser(userFriendId);
        userStorage.deleteFriend(userId, userFriendId);
    }

    public List<User> getFriendsList(Integer userId) {
        getUser(userId);
        return userStorage.getFriendsList(userId);
    }

    public List<User> getCommonFriends(Integer currentUserId, Integer anotherUserId) {
        getUser(currentUserId);
        getUser(anotherUserId);
        return userStorage.getCommonFriends(currentUserId, anotherUserId);
    }

    public List<Film> getRecommendations(int userId) {
        return filmStorage.getRecommendation(userStorage.getSimilarLikes(userId));
    }

    private void checkUserName(User user) {
        String name = user.getName();
        if (name == null || name.isBlank()) {
            log.debug("Имя пользователя не задано -> name = login");
            user.setName(user.getLogin());
        }
    }

    public void deleteUserById(int userId) {
        userStorage.deleteUserById(userId);
        log.info("Пользователь с id: {} удалён.", userId);
    }

    public List<Feed> getFeedsList(int id) {
        getUser(id);
        return feedStorage.getFeedsList(id);
    }
}

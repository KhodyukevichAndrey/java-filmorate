package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {

    User addUser(User user);

    User updateUser(User user);

    List<User> getAllUsers();

    Optional<User> getUser(int userId);

    void addFriend(Integer userId, Integer friendId);

    void deleteFriend(Integer userId, Integer friendId);

    List<User> getFriendsList(Integer userId);

    List<User> getCommonFriends(Integer userId, Integer friendId);

    void deleteUserById(int userId);

    List<Film> getRecommendations(int userId);
}

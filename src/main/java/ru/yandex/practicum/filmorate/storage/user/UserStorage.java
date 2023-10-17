package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    List<Feed> getFeedsList(int id);

    Set<Integer> getSimilarLikes(int userId);
}

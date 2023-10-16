package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDBStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDBStorageTest {

    @Autowired
    private final UserDBStorage userStorage;
    private final JdbcTemplate jdbcTemplate;
    User user;
    User friend;
    User userAfterCreate;
    User friendAfterCreate;

    @BeforeEach
    void createUserTestEnvironment() {
        user = new User(0, "userEmail", "userLogin", "userName",
                LocalDate.of(1950, 1, 5));
        friend = new User(0, "friendEmail", "friendLogin", "friendName",
                LocalDate.of(1950, 2, 5));

        userAfterCreate = userStorage.addUser(user);
        friendAfterCreate = userStorage.addUser(friend);
    }

    @AfterEach
    void dropUserStorageDataBase() {
        jdbcTemplate.update("DELETE FROM user_friend");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1");
    }

    @Test
    void shouldCreateAndReturnUser() {
        assertNotNull(userAfterCreate);
        assertEquals(1, userAfterCreate.getId(), "ID пользователя не соответствует");
        assertEquals("userEmail", userAfterCreate.getEmail(), "email пользователя не соответствует");
    }

    @Test
    void shouldUpdateUser() {
        userAfterCreate.setName("nameAfterChange");
        User userAfterUpdate = userStorage.updateUser(userAfterCreate);

        assertNotNull(userAfterUpdate);
        assertEquals("nameAfterChange", userAfterUpdate.getName(),
                "Имя после изменения не соответствует");
        assertEquals(1, userAfterUpdate.getId(), "После обновления ID должен оставаться неизменным");
    }

    @Test
    void shouldReturnAllUsers() {
        List<User> allUsers = userStorage.getAllUsers();

        assertEquals(2, allUsers.size(),
                "Количество друзей в БД не соответствует кол-ву созданных пользователей");
        assertEquals(userAfterCreate, allUsers.get(0),
                "Первый созданный пользователь должен соответствовать первому пользователю из списка");
    }

    @Test
    void shouldReturnUser() {
        Optional<User> userOptional = userStorage.getUser(1);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1));
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("name", "userName"));
    }

    @Test
    void shouldReturnFriendList() {
        List<User> emptyUserFriendList = userStorage.getFriendsList(1);
        assertEquals(0, emptyUserFriendList.size(),
                "После создания пользователя список друзей должен быть пустым");

        userStorage.addFriend(1, 2);

        List<User> userFriendListAfterAddFriend = userStorage.getFriendsList(1);
        List<User> friendFriendList = userStorage.getFriendsList(2);
        assertEquals(1, userFriendListAfterAddFriend.size(),
                "После добавления друга, размер списка друзей должен увеличиться на 1");
        assertEquals(0, friendFriendList.size(), "После добавления пользователем user " +
                "в список друзей пользователя friend, размер списка друзей friend должен оставаться пустым, " +
                "т.к. дружба односторонняя");
    }

    @Test
    void shouldAddFriend() {
        userStorage.addFriend(1, 2);
        List<User> userFriendList = userStorage.getFriendsList(1);
        assertEquals(1, userFriendList.size(),
                "После добавления друга, размер списка друзей должен увеличиться на 1");

        assertEquals(friendAfterCreate, userFriendList.get(0), "После добавления user в друзья " +
                "пользователя friend, пользователь friend не должен подвергаться изменениям");
    }

    @Test
    void shouldDeleteFriend() {
        userStorage.addFriend(1, 2);
        userStorage.addFriend(2, 1);

        userStorage.deleteFriend(1, 2);

        List<User> userFriendListAfterDelete = userStorage.getFriendsList(1);
        List<User> friendFriendsListWithoutDelete = userStorage.getFriendsList(2);

        assertEquals(0, userFriendListAfterDelete.size(),
                "После удаления друга, размер списка друзей пользователя user должен уменьшиться на 1");
        assertEquals(1, friendFriendsListWithoutDelete.size(), "После удаления пользователем user " +
                "из списка друзей пользователя friend, размер списка друзей пользователя friend " +
                "не должен изменяться, т.к. дружба односторонняя");
    }

    @Test
    void shouldReturnCommonFriendsWithAnotherUser() {
        User commonFriend = new User(0, "commonUserEmail", "commonUserLogin", "commonUserName",
                LocalDate.of(1950, 1, 5));
        User commonFriendAfterCreate = userStorage.addUser(commonFriend);

        userStorage.addFriend(1, 3);
        userStorage.addFriend(2, 3);

        List<User> commonFriendsList = userStorage.getCommonFriends(1, 2);

        assertEquals(1, commonFriendsList.size(), "Размер списка общих друзей должен быть равен 1");
        assertEquals(commonFriendAfterCreate, commonFriendsList.get(0),
                "Пользователь в списке общих друзей не соответствует");
    }

    @Test
    void shouldAddFeedAddFriend() {
        userStorage.addFriend(1, 2);
        List<Feed> feddList = userStorage.getFeedsList(1);
        Feed feedUser = feddList.get(0);
        assertEquals(feedUser.getEventType(), EventType.FRIEND);
        assertEquals(feedUser.getOperation(), OperationType.ADD);
    }

    @Test
    void shouldAddFeedRemoveFriend() {
        userStorage.addFriend(1, 2);
        userStorage.deleteFriend(1, 2);
        List<Feed> feddList = userStorage.getFeedsList(1);
        Feed feedUser = feddList.get(1);
        assertEquals(feedUser.getEventType(), EventType.FRIEND);
        assertEquals(feedUser.getOperation(), OperationType.REMOVE);
    }
}

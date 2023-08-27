package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerTest {

    private UserController userController;
    private User user;
    private User userWithoutName;
    private User userAfterUpdate;
    private User userWithWrongIdForUpdate;

    @BeforeEach
    void testEnvironment() {
        UserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);
        userController = new UserController(userStorage, userService);
        user = new User("user@yandex.ru", "userLogin", LocalDate.of(1950, 1, 5));
        user.setName("userName");
        userWithoutName = new User("user@yandex.ru",
                "userLogin", LocalDate.of(1950, 1, 5));
        userAfterUpdate = new User("newUserEmail@yandex.ru", "newUserLogin",
                LocalDate.of(1970, 2, 10));
        userAfterUpdate.setId(1);
        userWithWrongIdForUpdate = new User("user@yandex.ru", "userLogin",
                LocalDate.of(1950, 1, 5));
        userWithWrongIdForUpdate.setId(15);

    }

    @Test
    void checkUserValidationForGoodExample() {
        userController.addUser(user);

        assertNotNull(user);
        assertEquals(user, userController.getUsers().get(0),
                "Пользователи должны быть эквивалентны после добавления");
        assertEquals(1, userController.getUsers().size(),
                "После добавления пользователя размер хранилища должен быть увеличен на 1");
    }

    @Test
    void checkUserValidationForUserWithoutName() {
        userController.addUser(userWithoutName);

        assertNotNull(userWithoutName);
        assertEquals(userWithoutName.getLogin(), userController.getUsers().get(0).getName(),
                "При добавлении пользователя без имени," +
                        " значение поля логин будет выступать в качестве его значения");
    }

    @Test
    void checkForUpdateUser() {
        userController.addUser(user);
        userController.updateUser(userAfterUpdate);

        assertEquals(1, userController.getUsers().size(),
                "После обновления размер хранилища не должен быть увеличен");
        assertEquals(userAfterUpdate, userController.getUsers().get(0),
                "После обновления хранилища, оно должно возвращать обновленного пользователя");
        assertEquals(1, userController.getUsers().size(), "После обновления пользователя, " +
                "размер хранилища должен оставаться неизменным");
    }

    @Test
    void checkUserValidationForUpdateWithWrongId() {
        userController.addUser(user);

        assertThrows(UserNotFoundException.class, () -> userController.updateUser(userWithWrongIdForUpdate),
                "При попытке обновить данные пользователя с несуществующим id, " +
                        "должно быть выброшено исключение валидации");
        assertEquals(user, userController.getUsers().get(0),
                "После неудачного обновления пользователя в хранилище," +
                        " оно должно возвращать прежнюю версию пользователя");
        assertEquals(1, userController.getUsers().size(), "После неудачного обновления пользователя, " +
                "размер хранилища должен оставаться неизменным");
    }
}

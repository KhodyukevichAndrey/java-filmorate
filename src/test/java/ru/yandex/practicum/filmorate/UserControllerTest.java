package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserControllerTest {

    private UserController userController;
    private User user;
    private User userWithoutEmail;
    private User userWithoutSymbolInEmail;
    private User userWithoutLogin;
    private User userLoginIncludesSpace;
    private User userWithoutName;
    private User userFromFuture;
    private User userAfterUpdate;
    private User userWithWrongIdForUpdate;

    @BeforeEach
    void testEnvironment() {
        userController = new UserController();
        user = new User("user@yandex.ru", "userLogin", LocalDate.of(1950, 1, 5));
        user.setName("userName");
        userWithoutEmail = new User("", "userLogin", LocalDate.of(1950, 1, 5));
        userWithoutSymbolInEmail = new User("useryandex.ru", "userLogin",
                LocalDate.of(1950, 1, 5));
        userWithoutLogin = new User("user@yandex.ru", "", LocalDate.of(1950, 1, 5));
        userLoginIncludesSpace = new User("user@yandex.ru", "user Login",
                LocalDate.of(1950, 1, 5));
        userWithoutName = new User("user@yandex.ru",
                "userLogin", LocalDate.of(1950, 1, 5));
        userFromFuture = new User("user@yandex.ru", "userLogin",
                LocalDate.of(2150, 1, 5));
        userAfterUpdate = new User("newUserEmail@yandex.ru", "newUserLogin",
                LocalDate.of(1970, 2, 10));
        userAfterUpdate.setId(1);
        userWithWrongIdForUpdate = new User("user@yandex.ru", "userLogin",
                LocalDate.of(1950, 1, 5));
        userWithWrongIdForUpdate.setId(15);

    }

    @Test
    void checkUserValidationForGoodExample() {
        userController.createUser(user);

        assertNotNull(user);
        assertEquals(user, userController.getUsers().get(0),
                "Пользователи должны быть эквивалентны после добавления");
        assertEquals(1, userController.getUsers().size(),
                "После добавления пользователя размер хранилища должен быть увеличен на 1");
    }

    @Test
    void checkUserValidationForWrongEmail() {
        assertThrows(ValidationException.class, () -> userController.createUser(userWithoutEmail),
                "При попытке добавить пользователя без почты, должно быть выброшено исключение валидации");
        assertThrows(ValidationException.class, () -> userController.createUser(userWithoutSymbolInEmail),
                "При попытке добавить пользователя без знака @, должно быть выброшено исключение валидации");
        assertEquals(0, userController.getUsers().size(),
                "После выброшенного исключения размер хранилища не должен изменяться");
    }

    @Test
    void checkUserValidationForWrongLogin() {
        assertThrows(ValidationException.class, () -> userController.createUser(userWithoutLogin),
                "При попытке добавить пользователя без логина, должно быть выброшено исключение валидации");
        assertThrows(ValidationException.class, () -> userController.createUser(userLoginIncludesSpace),
                "При попытке добавить пользователя с логином содержащим пробелы," +
                        " должно быть выброшено исключение валидации");
        assertEquals(0, userController.getUsers().size(),
                "После выброшенного исключения размер хранилища не должен изменяться");
    }

    @Test
    void checkUserValidationForUserWithoutName() {
        userController.createUser(userWithoutName);

        assertNotNull(userWithoutName);
        assertEquals(userWithoutName.getLogin(), userController.getUsers().get(0).getName(),
                "При добавлении пользователя без имени," +
                        " значение поля логин будет выступать в качестве его значения");
    }

    @Test
    void checkUserValidationForFutureBirthday() {
        assertNotNull(userFromFuture);
        assertThrows(ValidationException.class, () -> userController.createUser(userFromFuture),
                "При попытке добавить пользователя с датой рождения позднее текущей даты," +
                        " должно быть выброшено исключение валидации");
        assertEquals(0, userController.getUsers().size(),
                "После выброшенного исключения размер хранилища не должен изменяться");
    }

    @Test
    void checkForUpdateUser() {
        userController.createUser(user);
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
        userController.createUser(user);

        assertThrows(ValidationException.class, () -> userController.updateUser(userWithWrongIdForUpdate),
                "При попытке обновить данные пользователя с несуществующим id, " +
                        "должно быть выброшено исключение валидации");
        assertEquals(user, userController.getUsers().get(0),
                "После неудачного обновления пользователя в хранилище," +
                        " оно должно возвращать прежнюю версию пользователя");
        assertEquals(1, userController.getUsers().size(), "После неудачного обновления пользователя, " +
                "размер хранилища должен оставаться неизменным");
    }
}

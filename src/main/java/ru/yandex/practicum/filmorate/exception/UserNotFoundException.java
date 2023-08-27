package ru.yandex.practicum.filmorate.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("Пользователь с указанным ID не найден");
    }
}

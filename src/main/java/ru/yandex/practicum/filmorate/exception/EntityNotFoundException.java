package ru.yandex.practicum.filmorate.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(final String message) {
        super(message);
    }
}

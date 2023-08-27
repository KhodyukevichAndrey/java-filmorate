package ru.yandex.practicum.filmorate.exception;

public class FilmNotFoundException extends RuntimeException {

    public FilmNotFoundException() {
        super("Фильм с указанным ID не найден");
    }
}

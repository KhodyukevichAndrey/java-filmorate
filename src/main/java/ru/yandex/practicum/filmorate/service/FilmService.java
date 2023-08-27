package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Integer.compare;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public void addLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilm(filmId);
        User user = userStorage.getUser(userId);

        if (film == null) {
            log.warn("Фильм с указанным ID не найден {}", filmId);
            throw new FilmNotFoundException();
        }
        if (user == null) {
            log.warn("Пользователь с указанным ID не найден {}", userId);
            throw new UserNotFoundException();
        }
        film.getLikes().add(userId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilm(filmId);
        User user = userStorage.getUser(userId);

        if (film == null) {
            log.warn("Фильм с указанным ID не найден {}", filmId);
            throw new FilmNotFoundException();
        }
        if (user == null) {
            log.warn("Пользователь с указанным ID не найден {}", userId);
            throw new UserNotFoundException();
        }
        film.getLikes().remove(userId);
    }

    public List<Film> getPopularFilms(Integer count) {
        return filmStorage.getAllFilms().stream()
                .sorted((film0, film1) -> compare(film1.getLikes().size(), film0.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}

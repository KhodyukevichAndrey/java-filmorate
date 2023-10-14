package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.director.DirectorDBStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final DirectorDBStorage directorDBStorage;
    private static final String WRONG_USER_ID = "Пользователь с указанным ID не найден";
    private static final String WRONG_FILM_ID = "Фильм с указанным ID не найден";
    private static final String WRONG_DIRECTOR_ID = "Режиссёр с указанным ID не найден";

    @Autowired
    public FilmService(FilmStorage filmStorage,
                       UserStorage userStorage,
                       GenreStorage genreStorage,
                       MpaStorage mpaStorage,
                       DirectorDBStorage directorDBStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
        this.directorDBStorage = directorDBStorage;
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilm(int id) {
        return filmStorage.getFilm(id)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_FILM_ID));
    }

    public void addLike(int filmId, int userId) {
        getFilm(filmId);
        getUser(userId);
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        getFilm(filmId);
        getUser(userId);
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(Integer count) {
        return filmStorage.getPopularFilms(count);
    }

    public Genre getGenre(int genreId) {
        return genreStorage.getGenre(genreId)
                .orElseThrow(() -> new EntityNotFoundException("Wrong genre id"));
    }

    public List<Genre> getAllGenres() {
        return genreStorage.getAllGenres();
    }

    public Mpa getMpa(int mpaId) {
        return mpaStorage.getMpa(mpaId)
                .orElseThrow(() -> new EntityNotFoundException("Wrong mpa id"));
    }

    public List<Mpa> getAllMpa() {
        return mpaStorage.getAllMpa();
    }


    public List<Film> getSortedDirectorFilms(int directorId, String sortBy) {
        getDirector(directorId);
        return filmStorage.getDirectorFilms(directorId, sortBy);
    }


    private User getUser(int userId) {
        return userStorage.getUser(userId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_USER_ID));
    }


    public List<Film> getCommonFilms(int userId, int friendId) {
        getUser(userId);
        getUser(friendId);
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public void deleteFilmById(int filmId) {
        filmStorage.deleteFilmById(filmId);
        log.info("Фильм с id: {} удалён.", filmId);
    }

    private Director getDirector(int directorId) {
        return directorDBStorage.getDirector(directorId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_DIRECTOR_ID));
    }
}

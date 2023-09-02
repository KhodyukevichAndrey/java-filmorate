package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmControllerTest {

    private FilmController filmController;
    private Film film;
    private Film filmAfterUpdate;
    private Film filmWithWrongIdForUpdate;

    @BeforeEach
    void testEnvironment() {
        final FilmStorage filmStorage = new InMemoryFilmStorage();
        final UserStorage userStorage = new InMemoryUserStorage();
        FilmService filmService = new FilmService(filmStorage, userStorage);
        filmController = new FilmController(filmService);
        film = new Film("фильм1", "описание1",
                LocalDate.of(1900, 1, 5), 110);
        filmAfterUpdate = new Film("измененное название", "новое описание",
                LocalDate.of(1950, 4, 7), 130);
        filmAfterUpdate.setId(1);
        filmWithWrongIdForUpdate = new Film("измененное название", "новое описание",
                LocalDate.of(1950, 4, 7), 130);
        filmWithWrongIdForUpdate.setId(15);
    }

    @Test
    void checkFilmValidationForGoodExample() {
        filmController.addFilm(film);

        assertNotNull(film);
        assertEquals(1, filmController.getFilms().size(),
                "После добавления фильма размер хранилища должен увеличиваться на 1");
        assertEquals(film, filmController.getFilms().get(0),
                "Фильмы должны быть эквивалентны после добавления");
    }

    @Test
    void checkForUpdateFilm() {
        filmController.addFilm(film);
        filmController.updateFilm(filmAfterUpdate);

        assertEquals(1, filmController.getFilms().size(),
                "После обновления размер хранилища не должен быть увеличен");
        assertEquals(filmAfterUpdate, filmController.getFilms().get(0),
                "После обновления хранилища, оно должно возвращать обновленный фильм");
        assertEquals(1, filmController.getFilms().size(), "После обновления фильма, " +
                "размер хранилища должен оставаться неизменным");
    }

    @Test
    void checkFilmValidationForUpdateWithWrongId() {
        filmController.addFilm(film);

        assertThrows(EntityNotFoundException.class, () -> filmController.updateFilm(filmWithWrongIdForUpdate),
                "При попытке обновить данные фильма с несуществующим id, " +
                        "должно быть выброшено исключение валидации");
        assertEquals(film, filmController.getFilms().get(0),
                "После неудачного обновления фильма в хранилище, оно должно возвращать фильм без изменений");
        assertEquals(1, filmController.getFilms().size(), "После неудачного обновления фильма, " +
                "размер хранилища должен оставаться неизменным");
    }
}

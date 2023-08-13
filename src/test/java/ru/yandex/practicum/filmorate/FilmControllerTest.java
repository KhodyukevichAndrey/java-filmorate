package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmControllerTest {

    private FilmController filmController;
    private Film film;
    private Film filmWithoutName;
    private Film filmWithLongDescription;
    private Film filmWithWrongReleaseDate;
    private Film filmWithWrongDuration;
    private Film filmAfterUpdate;
    private Film filmWithWrongIdForUpdate;

    @BeforeEach
    void testEnvironment() {
        filmController = new FilmController();
        film = new Film("фильм1", "описание1",
                LocalDate.of(1900, 1, 5), 110);
        filmWithoutName = new Film("", "описание1",
                LocalDate.of(1900, 1, 5), 110);
        filmWithLongDescription = new Film("фильм1", "Пятеро друзей ( комик-группа «Шарло»), " +
                "приезжают в город Бризуль. Здесь они хотят разыскать господина Огюста Куглова, " +
                "который задолжал им деньги, а именно 20 миллионов. о Куглов, который за время " +
                "«своего отсутствия», стал кандидатом Коломбани.",
                LocalDate.of(1900, 1, 5), 110);
        filmWithWrongReleaseDate = new Film("фильм1", "описание1",
                LocalDate.of(1800, 1, 5), 110);
        filmWithWrongDuration = new Film("фильм1", "описание1",
                LocalDate.of(1900, 1, 5), -110);
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
    void checkFilmValidationForName() {
        assertThrows(ValidationException.class, () -> filmController.addFilm(filmWithoutName),
                "При попытке добавить фильм без названия, должно быть выброшено исключение валидации");
        assertEquals(0, filmController.getFilms().size(),
                "После выброшенного исключения размер хранилища должен оставаться неизменным");
    }

    @Test
    void checkFilmValidationForDescription() {
        assertThrows(ValidationException.class, () -> filmController.addFilm(filmWithLongDescription),
                "При попытке добавить фильм с описанием более 200 символов, " +
                        "должно быть выброшено исключение валидации");
        assertEquals(0, filmController.getFilms().size(),
                "После выброшенного исключения размер хранилища должен оставаться неизменным");
    }

    @Test
    void checkFilmValidationForReleaseDate() {
        assertThrows(ValidationException.class, () -> filmController.addFilm(filmWithWrongReleaseDate),
                "При попытке добавить фильм выпуска ранее 28.12.1895, " +
                        "должно быть выброшено исключение валидации");
        assertEquals(0, filmController.getFilms().size(),
                "После выброшенного исключения размер хранилища должен оставаться неизменным");
    }

    @Test
    void checkFilmValidationForDuration() {
        assertThrows(ValidationException.class, () -> filmController.addFilm(filmWithWrongDuration),
                "При попытке добавить фильм выпуска ранее 28.12.1895, " +
                        "должно быть выброшено исключение валидации");
        assertEquals(0, filmController.getFilms().size(),
                "После выброшенного исключения размер хранилища должен оставаться неизменным");
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

        assertThrows(ValidationException.class, () -> filmController.updateFilm(filmWithWrongIdForUpdate),
                "При попытке обновить данные фильма с несуществующим id, " +
                        "должно быть выброшено исключение валидации");
        assertEquals(film, filmController.getFilms().get(0),
                "После неудачного обновления фильма в хранилище, оно должно возвращать фильм без изменений");
        assertEquals(1, filmController.getFilms().size(), "После неудачного обновления фильма, " +
                "размер хранилища должен оставаться неизменным");
    }
}

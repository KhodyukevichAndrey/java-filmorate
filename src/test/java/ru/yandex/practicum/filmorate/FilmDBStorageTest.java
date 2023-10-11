package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDBStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDBStorage;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDBStorageTest {

    @Autowired
    private final FilmDBStorage filmStorage;
    @Autowired
    private final UserDBStorage userStorage;
    private final JdbcTemplate jdbcTemplate;
    User user;
    User anotherUser;
    Film film;
    Film anotherFilm;
    User userAfterCreate;
    User friendAfterCreate;
    Film filmAfterCreate;
    Film anotherFilmAfterCreate;

    @BeforeEach
    void createFilmTestEnvironment() {
        user = new User(0, "userEmail", "userLogin", "userName",
                LocalDate.of(1950, 1, 5));
        anotherUser = new User(0, "friendEmail", "friendLogin", "friendName",
                LocalDate.of(1950, 2, 5));
        film = new Film(0, "firstFilm", "firstDescription",
                LocalDate.of(1950, 3, 5), 100,
                new Mpa(1, null, null), new HashSet<>(), new HashSet<>());
        anotherFilm = new Film(0, "secondFilm", "secondDescription",
                LocalDate.of(1950, 4, 5), 150,
                new Mpa(2, null, null), new HashSet<>(), new HashSet<>());

        userAfterCreate = userStorage.addUser(user);
        friendAfterCreate = userStorage.addUser(anotherUser);
        filmAfterCreate = filmStorage.addFilm(film);
        anotherFilmAfterCreate = filmStorage.addFilm(anotherFilm);
    }

    @AfterEach
    void dropFilmAndUserStorageDataBase() {
        jdbcTemplate.update("DELETE FROM film_likes");
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1");
    }

    @Test
    void shouldCreateAndReturnFilm() {
        Film newFilmAfterCreate = filmStorage.addFilm(film);
        assertEquals(3, newFilmAfterCreate.getId(), "Фильма пользователя не соответствует");
        assertEquals("firstFilm", newFilmAfterCreate.getName(), "Название фильма не соответствует");
    }

    @Test
    void shouldUpdateFilm() {
        Film filmForUpdate = new Film(1, "newName", "firstDescription",
                LocalDate.of(1950, 3, 5), 100,
                new Mpa(1, null, null), new HashSet<>(), new HashSet<>());
        Film filmAfterUpdate = filmStorage.updateFilm(filmForUpdate);

        assertNotNull(filmAfterUpdate);
        assertEquals("newName", filmAfterUpdate.getName(),
                "Имя после изменения не соответствует");
        assertEquals(1, filmAfterUpdate.getId(), "После обновления ID должен оставаться неизменным");
    }

    @Test
    void shouldReturnAllFilms() {
        List<Film> allFilms = filmStorage.getAllFilms();

        assertEquals(2, allFilms.size(),
                "Количество фильмов не соответствует кол-ву созданных фильмов");
        assertEquals(filmAfterCreate, allFilms.get(0),
                "Первый созданный фильм должен соответствовать первому фильму из списка");
    }

    @Test
    void shouldReturnFilmById() {
        Optional<Film> filmOptional = filmStorage.getFilm(1);

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", 1));
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("name", "firstFilm"));
    }

    @Test
    void shouldAddLikeToFilm() {
        List<Integer> likes = jdbcTemplate.query("SELECT user_id FROM film_likes WHERE film_id = ?",
                (rs, rowNum) -> rs.getInt("user_id"), 1);

        assertEquals(0, likes.size(), "Количество лайков должно быть равно 0");

        filmStorage.addLike(1, 1);

        List<Integer> likesAfterAddLike = jdbcTemplate.query("SELECT user_id FROM film_likes WHERE film_id = ?",
                (rs, rowNum) -> rs.getInt("user_id"), 1);

        assertEquals(1, likesAfterAddLike.size(), "После добавления лайка, " +
                "их количество должно быть увеличено на 1");
        assertEquals(1, likesAfterAddLike.get(0), "ID пользователя, " +
                "который поставил лайк не соответствует");
    }

    @Test
    void shouldRemoveLikeFromFilm() {
        List<Integer> likes = jdbcTemplate.query("SELECT user_id FROM film_likes WHERE film_id = ?",
                (rs, rowNum) -> rs.getInt("user_id"), 1);

        assertEquals(0, likes.size(), "Количество лайков должно быть равно 0");

        filmStorage.addLike(1, 1);
        filmStorage.addLike(1, 2);

        filmStorage.removeLike(1, 1);

        List<Integer> likesAfterRemoveLike = jdbcTemplate.query("SELECT user_id FROM film_likes WHERE film_id = ?",
                (rs, rowNum) -> rs.getInt("user_id"), 1);

        assertEquals(1, likesAfterRemoveLike.size(),
                "Количество оставшихся лайков должно быть равно 1");
        assertEquals(2, likesAfterRemoveLike.get(0), "ID пользователя, " +
                "от которого остался лайк не соответствует");
    }

    @Test
    void shouldReturnPopularFilms() {
        filmStorage.addLike(1, 1);
        filmStorage.addLike(2, 2);
        filmStorage.addLike(2, 1);

        List<Film> popularFilms = filmStorage.getPopularFilms(1);

        Optional<Film> filmOptional = filmStorage.getFilm(2);

        assertEquals(2, popularFilms.get(0).getId(),
                "Список должен содержать фильм c ID с наибольшим кол-вом лайков");
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying((film1 ->
                        assertEquals(film1, popularFilms.get(0),
                                "Фильм полученный по ID и фильм полученный " +
                                        "в списке самых популярных должны быть эквивалентны")));
    }
}

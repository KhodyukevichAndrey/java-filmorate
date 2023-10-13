package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.HashSet;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ReviewDBStorageTest {
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
                new Mpa(1, null, null), new HashSet<>());
        anotherFilm = new Film(0, "secondFilm", "secondDescription",
                LocalDate.of(1950, 4, 5), 150,
                new Mpa(2, null, null), new HashSet<>());

        userAfterCreate = userStorage.addUser(user);
        friendAfterCreate = userStorage.addUser(anotherUser);
        filmAfterCreate = filmStorage.addFilm(film);
        anotherFilmAfterCreate = filmStorage.addFilm(anotherFilm);
    }
}

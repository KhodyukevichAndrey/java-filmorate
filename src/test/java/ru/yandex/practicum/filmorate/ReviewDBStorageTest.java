package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.film.FilmDBStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewDBStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDBStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ReviewDBStorageTest {
    @Autowired
    private final FilmDBStorage filmStorage;
    @Autowired
    private final UserDBStorage userStorage;
    @Autowired
    private final ReviewDBStorage reviewDBStorage;
    private final JdbcTemplate jdbcTemplate;
    User user;
    User anotherUser;
    Film film;
    Film anotherFilm;
    User userAfterCreate;
    User friendAfterCreate;
    Film filmAfterCreate;
    Film anotherFilmAfterCreate;
    Review review;

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
        review = new Review();
        review.setFilmId(1);
        review.setUserId(1);
        review.setContent("So bad");
        review.setIsPositive(true);
        review.setUseful(0);
        userAfterCreate = userStorage.addUser(user);
        friendAfterCreate = userStorage.addUser(anotherUser);
        filmAfterCreate = filmStorage.addFilm(film);
        anotherFilmAfterCreate = filmStorage.addFilm(anotherFilm);
    }

    @AfterEach
    void dropFilmAndUserStorageDataBase() {
        jdbcTemplate.update("DELETE FROM user_friend;");
        jdbcTemplate.update("DELETE FROM feed;");
        jdbcTemplate.update("DELETE FROM reviews;");
        jdbcTemplate.update("ALTER TABLE reviews ALTER COLUMN review_id RESTART WITH 1");
        jdbcTemplate.update("DELETE FROM users;");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1");
        jdbcTemplate.update("DELETE FROM films;");
        jdbcTemplate.update("ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1");

    }

    @Test
    void shouldAddFeedCreateReview() {
        reviewDBStorage.addReview(review);
        List<Feed> feddList = userStorage.getFeedsList(1);
        Feed feedUser = feddList.get(0);
        assertEquals(feedUser.getEventType(), EventType.REVIEW);
        assertEquals(feedUser.getOperation(), OperationType.ADD);
    }

    @Test
    void shouldAddFeedRemoveReview() {
        reviewDBStorage.addReview(review);
        reviewDBStorage.deleteReview(1);
        List<Feed> feddList = userStorage.getFeedsList(1);
        Feed feedUser = feddList.get(1);
        assertEquals(feedUser.getEventType(), EventType.REVIEW);
        assertEquals(feedUser.getOperation(), OperationType.REMOVE);
    }

    @Test
    void shouldAddFeedUpdateReview() {
        reviewDBStorage.addReview(review);
        review.setContent("So Good");
        review.setReviewId(1);
        reviewDBStorage.updateReview(review);
        List<Feed> feddList = userStorage.getFeedsList(1);
        Feed feedUser = feddList.get(1);
        assertEquals(feedUser.getEventType(), EventType.REVIEW);
        assertEquals(feedUser.getOperation(), OperationType.UPDATE);
    }
}


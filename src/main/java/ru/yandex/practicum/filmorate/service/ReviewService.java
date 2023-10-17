package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    private static final String WRONG_USER_ID = "Пользователь с указанным ID не найден";
    private static final String WRONG_FILM_ID = "Фильм с указанным ID не найден";

    private static final String WRONG_REVIEW_ID = "Отзыв с указанным ID не найден";

    @Autowired
    public ReviewService(ReviewStorage reviewStorage,
                         FilmStorage filmStorage,
                         UserStorage userStorage) {
        this.reviewStorage = reviewStorage;
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Review addReview(Review review) {
        getUser(review.getUserId());
        getFilm(review.getFilmId());
        return reviewStorage.addReview(review);
    }

    public Review updateReview(Review review) {
        getReview(review.getReviewId());
        getUser(review.getUserId());
        getFilm(review.getFilmId());
        return reviewStorage.updateReview(review);
    }

    public void deleteReview(int reviewId) {
        reviewStorage.deleteReview(reviewId);
    }

    public Review getReview(int reviewId) {
        return reviewStorage.getReview(reviewId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_REVIEW_ID));
    }

    public List<Review> getAllReviews() {
        return reviewStorage.getAllReviews();
    }

    public List<Review> getReviewsByFilmId(Optional<Integer> filmId, Optional<Integer> count) {
        return reviewStorage.getReviewsByFilmId(filmId, count);
    }

    public Review likeReview(int reviewId, int userId, boolean likeValue) {
        return reviewStorage.likeReview(reviewId, userId, likeValue);
    }

    public Review deleteLikeReview(int reviewId, int userId, boolean likeValue) {
        return reviewStorage.deleteLikeReview(reviewId, userId, likeValue);
    }

    private User getUser(int userId) {
        return userStorage.getUser(userId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_USER_ID));
    }

    public Film getFilm(int filmId) {
        return filmStorage.getFilm(filmId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_FILM_ID));
    }

}

package ru.yandex.practicum.filmorate.storage.review;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.constants.MyConstants;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class ReviewDBStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;
    private static final String WRONG_REVIEW_ID = "Отзыв с указанным ID не найден";

    public ReviewDBStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Review> addReview(Review review) {
        Map<String, Object> values = new HashMap<>();
        values.put("film_id", review.getFilmId());
        values.put("user_id", review.getUserId());
        values.put("review_body", review.getContent());
        values.put("is_positive", review.getIsPositive());
        values.put("useful", 0);
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reviews")
                .usingGeneratedKeyColumns("review_id");
        int reviewId = simpleJdbcInsert.executeAndReturnKey(values).intValue();
        jdbcTemplate.update(MyConstants.SQLFEEDREVIEW, review.getUserId(), reviewId, 3, 2, 2,
                LocalDateTime.now());
        log.info("Отзыв успешно создан с ID - {}", reviewId);
        return Optional.ofNullable(getReview(reviewId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_REVIEW_ID)));
    }

    @Override
    public Optional<Review> updateReview(Review review) {
        Review reviewOld = getReview(review.getReviewId()).get();
        int reviewId = review.getReviewId();
        String sqlUpdateReview = "UPDATE reviews " +
                "SET review_body = ?, is_positive = ? WHERE review_id = ?";
        Optional<Review> reviewOptional = getReview(reviewId);
        if (reviewOptional.isPresent()) {
            jdbcTemplate.update(sqlUpdateReview,
                    review.getContent(),
                    review.getIsPositive(),
                    reviewId);
        }
        jdbcTemplate.update(MyConstants.SQLFEEDREVIEW, reviewOld.getUserId(), reviewOld.getReviewId(), 3, 2, 3,
                LocalDateTime.now());
        log.info("Отзыв успешно обновлен по указанном ID = {}", reviewId);

        return Optional.ofNullable(getReview(reviewId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_REVIEW_ID)));
    }

    @Override
    public Optional<Review> getReview(Integer id) {
        String sqlReview = "SELECT * " +
                "FROM reviews " +
                "WHERE review_id = ? ";
        try {
            Review review = jdbcTemplate.queryForObject(sqlReview, this::makeReview, id);
            log.info("Отзыв с указанным ID = {} найден", id);
            return Optional.ofNullable(review);
        } catch (DataAccessException e) {
            log.info("Отзыв с указанным ID = {} не найден", id);
            return Optional.empty();
        }
    }

    @Override
    public List<Review> getAllReviews() {
        String sqlReviews = "SELECT * " +
                "FROM reviews " +
                "ORDER BY useful DESC";
        return jdbcTemplate.query(sqlReviews, this::makeReview);
    }

    @Override
    public List<Review> getReviewsByFilmId(Optional<Integer> filmId, Optional<Integer> count) {
        if (filmId.isPresent()) {
            String sqlReviewsByFilm = "SELECT * " +
                    "FROM reviews " +
                    "WHERE film_id = ? " +
                    "ORDER BY useful DESC " +
                    "LIMIT ?";
            return jdbcTemplate.query(sqlReviewsByFilm, this::makeReview, filmId.get(), count.get());
        } else {
            String sqlReviewsByFilm = "SELECT * " +
                    "FROM reviews " +
                    "ORDER BY useful DESC " +
                    "LIMIT ?";
            return jdbcTemplate.query(sqlReviewsByFilm, this::makeReview, count.get());
        }

    }

    private Review makeReview(ResultSet rs, int rowNum) throws SQLException {
        int reviewId = rs.getInt("review_id");
        try {
            return new Review(
                    reviewId,
                    rs.getInt("film_id"),
                    rs.getInt("user_id"),
                    rs.getString("review_body"),
                    rs.getBoolean("is_positive"),
                    rs.getInt("useful"));
        } catch (DataAccessException e) {
            throw new EntityNotFoundException(WRONG_REVIEW_ID);
        }
    }

    @Override
    public void deleteReview(int reviewId) {
        try {
            Review review = getReview(reviewId).get();
            jdbcTemplate.update(MyConstants.SQLFEEDREVIEW, review.getUserId(), reviewId, 3, 2, 1,
                    LocalDateTime.now());
            String sqlQuery = "DELETE " +
                    "FROM reviews " +
                    "WHERE review_id = ?";
            jdbcTemplate.update(sqlQuery, reviewId);
        } catch (DataAccessException e) {
            throw new EntityNotFoundException(WRONG_REVIEW_ID);
        }

    }

    @Override
    public Optional<Review> likeReview(int reviewId, int userId, boolean likeValue) {
        jdbcTemplate.update("INSERT INTO review_likes (review_id, user_id,is_like) VALUES(?,?,?)",
                reviewId,
                userId,
                likeValue);
        String sqlReview = "SELECT * " +
                "FROM reviews " +
                "WHERE review_id = ? ";
        Review review = jdbcTemplate.queryForObject(sqlReview, this::makeReview, reviewId);
        String sqlUpdateReview = "UPDATE reviews " +
                "SET useful = ? WHERE review_id = ?";
        if (likeValue) {
            jdbcTemplate.update(sqlUpdateReview,
                    review.getUseful() + 1,
                    reviewId);
            log.info("Лайк пользователя c ID = {} к отзыву с ID = {} успешно добавлен", userId, reviewId);
        } else {
            jdbcTemplate.update(sqlUpdateReview,
                    review.getUseful() - 1,
                    reviewId);
            log.info("Дизлайк пользователя c ID = {} к отзыву с ID = {} успешно добавлен", userId, reviewId);
        }
        return Optional.ofNullable(getReview(reviewId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_REVIEW_ID)));
    }

    @Override
    public Optional<Review> deleteLikeReview(int reviewId, int userId, boolean likeValue) {
        String sqlQuery =
                "DELETE " +
                        "FROM review_likes " +
                        "WHERE review_id = ?" +
                        "AND user_id = ?";
        jdbcTemplate.update(sqlQuery, reviewId, userId);
        String sqlReview = "SELECT * " +
                "FROM reviews " +
                "WHERE review_id = ? ";
        Review review = jdbcTemplate.queryForObject(sqlReview, this::makeReview, reviewId);
        String sqlUpdateReview = "UPDATE reviews " +
                "SET useful = ? WHERE review_id = ?";
        if (likeValue == true) {
            jdbcTemplate.update(sqlUpdateReview,
                    review.getUseful() - 1,
                    reviewId);
            log.info("Лайк пользователя c ID = {} к отзыву с ID = {} успешно удален", userId, reviewId);
        } else {
            jdbcTemplate.update(sqlUpdateReview,
                    review.getUseful() + 1,
                    reviewId);
            log.info("Дизлайк пользователя c ID = {} к отзыву с ID = {} успешно удален", userId, reviewId);
        }
        return Optional.ofNullable(getReview(reviewId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_REVIEW_ID)));
    }

}

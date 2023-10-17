package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review addReview(Review review);

    Optional <Review> getReview(Integer id);

    List<Review> getAllReviews();

    Review updateReview(Review review);

    void deleteReview(int reviewId);

    List<Review> getReviewsByFilmId(Optional<Integer> filmId, Optional<Integer> count);

    Review likeReview(int reviewId, int userId, boolean likeValue);

    Review deleteLikeReview(int reviewId, int userId, boolean likeValue);
}

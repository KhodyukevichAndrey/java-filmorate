package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.Optional;

@Validated
@RestController
@RequestMapping("/reviews")
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }


    @PostMapping
    public Review addReview(@Valid @RequestBody Review review) {
        log.info("Получен запрос POST /reviews");
        return reviewService.addReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        log.info("Получен запрос PUT /reviews");
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{reviewId}")
    public void deleteReview(@PathVariable("reviewId") @Min(1) int id) {
        log.info("Получен запрос DELETE /reviews/{reviewId}");
        reviewService.deleteReview(id);
    }

    @GetMapping("/{reviewId}")
    public Review getReviewById(@PathVariable("reviewId") @Min(1) int id) {
        log.info("Получен запрос GET /reviews/{reviewId}");
        return reviewService.getReview(id);
    }

    @GetMapping
    public List<Review> getReviews(@RequestParam(required = false) Optional<Integer> filmId,
                                   @RequestParam(required = false) Optional<Integer> count) {
        if (filmId.isEmpty() && count.isEmpty()) {
            log.info("Получен запрос GET /reviews");
            return reviewService.getAllReviews();
        } else if (filmId.isPresent() && count.isEmpty()) {
            log.info("Получен запрос GET /reviews?filmId={filmId}&count={count}");
            return reviewService.getReviewsByFilmId(filmId, Optional.of(10));
        } else {
            log.info("Получен запрос GET /reviews?filmId={filmId}&count={count}");
            return reviewService.getReviewsByFilmId(filmId, count);
        }
    }

    @PutMapping("/{reviewId}/like/{userId}")
    public Review likeReview(@PathVariable("reviewId") @Min(1) int reviewId,
                                       @PathVariable("userId") @Min(1) int userId) {
        log.info("Получен запрос PUT /reviews/{id}/like/{userId}");
        return reviewService.likeReview(reviewId, userId, true);
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    public Review dislikeReview(@PathVariable("reviewId") @Min(1) int reviewId,
                                          @PathVariable("userId") @Min(1) int userId) {
        log.info("Получен запрос PUT /reviews/{id}/dislike/{userId}");
        return reviewService.likeReview(reviewId, userId, false);
    }

    @DeleteMapping("/{reviewId}/like/{userId}")
    public Review deleteLikeReview(@PathVariable("reviewId") @Min(1) int reviewId,
                                             @PathVariable("userId") @Min(1) int userId) {
        log.info("Получен запрос DELETE /reviews/{id}/like/{userId}");
        return reviewService.deleteLikeReview(reviewId, userId, true);
    }

    @DeleteMapping("/{reviewId}/dislike/{userId}")
    public Review deleteDislikeReview(@PathVariable("reviewId") @Min(1) int reviewId,
                                                @PathVariable("userId") @Min(1) int userId) {
        log.info("Получен запрос DELETE /reviews/{id}/dislike/{userId}");
        return reviewService.deleteLikeReview(reviewId, userId, false);
    }
}
package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review {
    private int reviewId;
    @NotNull
    private Integer filmId;
    @NotNull
    private Integer userId;
    @NotNull
    private String content;
    @NotNull
    private Boolean isPositive;
    private int useful;
}

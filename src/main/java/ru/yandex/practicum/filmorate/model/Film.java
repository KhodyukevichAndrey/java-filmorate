package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import ru.yandex.practicum.filmorate.validator.ReleaseDate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private int id;
    @NotBlank
    private final String name;
    @NotNull
    @Size(max = 200)
    private final String description;
    @NotNull
    @ReleaseDate
    private final LocalDate releaseDate;
    @NotNull
    @Positive
    private final int duration;
    private Set<Integer> likes = new HashSet<>();
}

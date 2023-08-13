package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();
    private int filmId = 1;
    private final static int MAX_DESCRIPTION_LENGTH = 200;
    private final static LocalDate EARLIEST_FILM_DATE = LocalDate.of(1895, 12, 28);

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        log.debug("Получен запрос POST /films");
        if(checkFilmValidation(film)) {
            film.setId(generateFilmId());
            films.put(film.getId(), film);
        }
        return film;
    }

    @PutMapping
    public Film updateFilm (@RequestBody Film film) {
        log.debug("Получен запрос PUT /films");
        if(checkFilmValidation(film) && films.containsKey(film.getId())) {
            films.put(film.getId(), film);
        } else {
            log.warn("Фильм с указанным id не найден {}", film.getId());
            throw new ValidationException("Фильм с указанным id не найден");
        }
        return film;
    }

    @GetMapping
    public List<Film> getFilms() {
        log.debug("Получен запрос GET /films");
        return new ArrayList<>(films.values());
    }

    private int generateFilmId() {
        return filmId++;
    }

    private boolean checkFilmValidation(Film film) {
        if(film.getName().isBlank()) {
            log.warn("Недопустимое название фильма {}", film.getName());
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if(film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            log.warn("Недопустимое количество символов описания для фильма {}", film.getDescription().length());
            throw new ValidationException("Максимальная длина описания должна быть не более 200 символов");
        }

        if(film.getReleaseDate().isBefore(EARLIEST_FILM_DATE)) {
            log.warn("Недопустимая дата релиза фильма {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза фильма должна быть не ранее 28.12.1895");
        }

        if (film.getDuration() <= 0) {
            log.warn("Недопустимая продолжительность фильма {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма не может быть меньше 0");
        }
        return true;
    }
}

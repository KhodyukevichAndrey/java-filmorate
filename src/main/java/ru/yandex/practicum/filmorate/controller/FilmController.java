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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.util.List;

@Validated
@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.debug("Получен запрос POST /films");
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.debug("Получен запрос PUT /films");
        return filmService.updateFilm(film);
    }

    @GetMapping
    public List<Film> getFilms() {
        log.debug("Получен запрос GET /films");
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable int id) {
        log.debug("Получен запрос GET /films/{id}");
        return filmService.getFilm(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable Integer userId) {
        log.debug("Получен запрос PUT /films/{id}/like/{userId}");
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable Integer userId) {
        log.debug("Получен запрос DELETE /films/{id}/like/{userId}");
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@Valid @Positive @RequestParam(defaultValue = "10") Integer count,
                                      @RequestParam(required = false) Integer genreId,
                                      @Valid @Min(1895) @RequestParam(required = false) Integer year) {
        log.debug("Получен запрос GET /films/popular?count={count}&genreId{genreId}&year={year}");
        return filmService.getPopularFilms(count, genreId, year);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam int userId, @RequestParam int friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @DeleteMapping("/{filmId}")
    public String deleteFilmById(@PathVariable int filmId) {
        log.info("Получен запрос DELETE/{filmId}");
        filmService.deleteFilmById(filmId);
        return "Фильм с id: " + filmId + " удалён.";
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getSortedFilmsByDirector(@Valid @Positive @PathVariable Integer directorId,
                                               @RequestParam(defaultValue = "noSort") String sortBy) {
        log.debug("Получен запрос GET /films/director/{directorId}?sortBy{likes|year}");
        return filmService.getSortedDirectorFilms(directorId, sortBy);
    }

    @GetMapping("/search")
    public List<Film> getFilmsBySearch(@Valid @NotBlank @RequestParam("query") String query,
                                       @RequestParam("by") String by) {
        log.debug("Получен запрос GET /films/search");
        return filmService.getFilmsBySearch(query, by);
    }
}

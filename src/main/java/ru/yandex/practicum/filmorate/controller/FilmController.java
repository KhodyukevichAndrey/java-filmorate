package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final FilmStorage filmStorage;
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmStorage inMemoryFilmStorage, FilmService filmService) {
        this.filmStorage = inMemoryFilmStorage;
        this.filmService = filmService;
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.debug("Получен запрос POST /films");
        return filmStorage.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.debug("Получен запрос PUT /films");
        return filmStorage.updateFilm(film);
    }

    @GetMapping
    public List<Film> getFilms() {
        log.debug("Получен запрос GET /films");
        return filmStorage.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable Integer id) {
        log.debug("Получен запрос GET /films/{id}");
        return filmStorage.getFilm(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.debug("Получен запрос PUT /films/{id}/like/{userId}");
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.debug("Получен запрос DELETE /films/{id}/like/{userId}");
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10", required = false) Integer count) {
        log.debug("Получен запрос GET /films/popular?count={count}");
        return filmService.getPopularFilms(count);
    }
}

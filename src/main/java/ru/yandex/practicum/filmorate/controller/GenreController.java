package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/genres")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreController {

    private final FilmService filmService;

    @GetMapping("/{id}")
    public Genre getGenre(@PathVariable Integer id) {
        log.debug("Получен запрос GET /genres/{id}");
        return filmService.getGenre(id);
    }

    @GetMapping
    public List<Genre> getAllGenres() {
        log.debug("Получен запрос GET /genres");
        return filmService.getAllGenres();
    }
}

package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaController {

    private final FilmService filmService;

    @GetMapping("/{id}")
    public Mpa getMpa(@PathVariable Integer id) {
        log.debug("Получен запрос GET /mpa/{id}");
        return filmService.getMpa(id);
    }

    @GetMapping
    public List<Mpa> getAllMpa() {
        log.debug("Получен запрос GET /mpa");
        return filmService.getAllMpa();
    }
}

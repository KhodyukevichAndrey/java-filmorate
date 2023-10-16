package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/directors")
@Slf4j
public class DirectorController {

    private final DirectorService directorService;

    @Autowired
    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    @PostMapping
    public Director addDirector(@Valid @RequestBody Director director) {
        log.debug("Получен запрос POST /directors");
        return directorService.addDirector(director);
    }

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director director) {
        log.debug("Получен запрос PUT /directors");
        return directorService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable Integer id) {
        log.debug("Получен запрос DELETE /directors/{id}");
        directorService.deleteDirector(id);
    }

    @GetMapping("/{id}")
    public Director getDirector(@PathVariable Integer id) {
        log.debug("Получен запрос GET /directors/{id}");
        return directorService.getDirector(id);
    }

    @GetMapping
    public List<Director> getAllDirectors() {
        log.debug("Получен запрос GET /directors");
        return directorService.getAllDirectors();
    }
}

package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Integer, Film> films = new HashMap<>();
    private int filmId = 1;

    @Override
    public Film addFilm(Film film) {
        film.setId(generateFilmId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
        } else {
            log.warn("Фильм с указанным ID не найден {}", film.getId());
            throw new FilmNotFoundException();
        }
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilm(Integer id) {
        if(!films.containsKey(id)) {
            log.warn("Фильм с указанным ID не найден {}", id);
            throw new FilmNotFoundException();
        }
        return films.get(id);
    }

    private int generateFilmId() {
        return filmId++;
    }
}

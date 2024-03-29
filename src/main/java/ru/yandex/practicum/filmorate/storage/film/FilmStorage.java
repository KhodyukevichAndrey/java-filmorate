package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getAllFilms();

    Optional<Film> getFilm(int id);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    List<Film> getPopularFilms(Integer count, Integer genreId, Integer year);

    List<Film> getCommonFilms(int userId, int friendId);

    void deleteFilmById(int filmId);

    List<Film> getDirectorFilms(Integer directorId, String sortBy);

    List<Film> getFilmsBySearch(String query, String by);

    List<Film> getRecommendation(Set<Integer> userId);
}

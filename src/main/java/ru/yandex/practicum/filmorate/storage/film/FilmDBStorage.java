package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
@Qualifier("FilmDBStorage")
@Slf4j
public class FilmDBStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private static final String WRONG_FILM_ID = "Фильм с указанным ID не найден";

    public FilmDBStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film addFilm(Film film) {
        Map<String, Object> values = new HashMap<>();
        values.put("name", film.getName());
        values.put("description", film.getDescription());
        values.put("release_date", film.getReleaseDate());
        values.put("duration", film.getDuration());
        values.put("mpa_id", film.getMpa().getId());

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");

        Integer filmId = simpleJdbcInsert.executeAndReturnKey(values).intValue();

        updateFilmGenres(film, filmId);

        log.debug("Фильм " + film.getName() + " успешно создан с ID - " + filmId);
        return getFilm(filmId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_FILM_ID));
    }

    @Override
    public Film updateFilm(Film film) {
        String sqlUpdateFilm = "UPDATE films " +
                "SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
        int filmId = film.getId();
        if (getFilm(filmId).isPresent()) {
            jdbcTemplate.update(sqlUpdateFilm,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getMpa().getId(),
                    filmId);
            updateFilmGenres(film, filmId);
        }
        log.debug("Фильм " + film.getName() + " успешно обновлен");
        return getFilm(filmId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_FILM_ID));
    }

    @Override
    public List<Film> getAllFilms() {
        return jdbcTemplate.query("SELECT * FROM films", this::makeFilm);
    }

    @Override
    public Optional<Film> getFilm(Integer id) {
        String sqlFilm = "SELECT * FROM films WHERE film_id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sqlFilm, this::makeFilm, id);
            log.debug("Фильм с ID " + id + " найден");
            return Optional.ofNullable(film);
        } catch (DataAccessException e) {
            throw new EntityNotFoundException(WRONG_FILM_ID);
        }
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        jdbcTemplate.update("INSERT INTO film_likes (film_id, user_id) VALUES(?,?)",
                filmId,
                userId);
        log.debug("Лайк пользователя " + userId + " к фильму " + filmId + " успешно добавлен");
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        jdbcTemplate.update("DELETE FROM film_likes WHERE film_id = ? AND user_id = ?",
                filmId,
                userId);
        log.debug("Лайк пользователя " + userId + " к фильму " + filmId + " успешно удален");
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        String sqlPopularFilms = "SELECT f.* " +
                "FROM films f " +
                "LEFT JOIN film_likes fl on f.film_id = fl.film_id " +
                "GROUP BY f.film_id, fl.film_id " +
                "ORDER BY COUNT(fl.user_id) DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sqlPopularFilms,
                this::makeFilm, count);
    }

    private Film makeFilm(ResultSet rs, int rowNum) throws SQLException {
        int filmId = rs.getInt("film_id");
        int mpaId = rs.getInt("mpa_id");
        try {
            return new Film(
                    filmId,
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("release_date").toLocalDate(),
                    rs.getInt("duration"),
                    new HashSet<>(makeFilmLikes(filmId)),
                    makeMpa(mpaId).orElseThrow(() -> new EntityNotFoundException("Неверный Mpa id")),
                    new HashSet<>(makeFilmGenres(filmId)));
        } catch (DataAccessException e) {
            throw new EntityNotFoundException(WRONG_FILM_ID);
        }
    }

    private Optional<Mpa> makeMpa(int mpaId) {
        String sqlMpa = "SELECT * FROM mpa WHERE mpa_id = ?";
        SqlRowSet mpaRow = jdbcTemplate.queryForRowSet(sqlMpa, mpaId);

        if (mpaRow.next()) {
            log.debug("Mpa по указанному ID " + mpaId + " найден");
            return Optional.of(new Mpa(
                    mpaRow.getInt("mpa_id"),
                    mpaRow.getString("name"),
                    mpaRow.getString("description")));
        } else {
            log.debug("Mpa по указанному ID " + mpaId + " не найден");
            return Optional.empty();
        }
    }

    private List<Genre> makeFilmGenres(int filmId) {
        String sqlGenres = "SELECT g.genre_id, g.name " +
                "FROM film_genres fg " +
                "JOIN genres g on fg.genre_id = g.genre_id " +
                "WHERE film_id = ? " +
                "ORDER BY g.genre_id";
        return jdbcTemplate.query(sqlGenres, this::makeGenre, filmId);
    }

    private Genre makeGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getInt("genre_id"), rs.getString("name"));
    }

    private List<Integer> makeFilmLikes(int filmId) {
        String sqlFilmLikes = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return jdbcTemplate.query(sqlFilmLikes, this::makeLike, filmId);
    }

    private int makeLike(ResultSet rs, int rowNum) throws SQLException {
        return rs.getInt("user_id");
    }

    private void updateFilmGenres(Film film, Integer filmId) {
        String sqlDeleteGenres = "DELETE FROM film_genres WHERE film_id = ?";
        String sqlUpdateGenres = "INSERT INTO film_genres (film_id, genre_id) VALUES (?,?)";

        jdbcTemplate.update(sqlDeleteGenres, filmId);

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(sqlUpdateGenres, filmId, genre.getId());
            }
        }
    }
}
package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
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

        int filmId = simpleJdbcInsert.executeAndReturnKey(values).intValue();

        if (film.getGenres() != null) {
            addGenres(filmId, film.getGenres());
        }

        log.debug("Фильм успешно создан с ID - {}", filmId);
        return getFilm(filmId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_FILM_ID));
    }

    @Override
    public Film updateFilm(Film film) {
        String sqlUpdateFilm = "UPDATE films " +
                "SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
        int filmId = film.getId();
        Optional<Film> filmOptional = getFilm(filmId);

        if (filmOptional.isPresent()) {
            jdbcTemplate.update(sqlUpdateFilm,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getMpa().getId(),
                    filmId);
            removeGenres(filmId, filmOptional.get().getGenres());
        }

        if (film.getGenres() != null) {
            addGenres(filmId, film.getGenres());
        }

        log.debug("Фильм успешно обновлен по указанном ID = {}", filmId);
        return getFilm(filmId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_FILM_ID));
    }

    @Override
    public List<Film> getAllFilms() {
        String sqlFilms = "SELECT f.*, m.* " +
                "FROM films f " +
                "JOIN mpa m ON f.mpa_id = m.mpa_id";
        List<Film> filmsWithoutGenres = jdbcTemplate.query(sqlFilms, this::makeFilm);
        return makeFilmsWithGenres(filmsWithoutGenres);
    }

    @Override
    public Optional<Film> getFilm(Integer id) {
        String sqlFilm = "SELECT f.*, m.* " +
                "FROM films f " +
                "JOIN mpa m ON f.mpa_id = m.mpa_id " +
                "WHERE f.film_id = ? ";
        try {
            Film film = jdbcTemplate.queryForObject(sqlFilm, this::makeFilm, id);
            makeFilmsWithGenres(List.of(film));
            log.debug("Фильм с указанным ID = {} найден", id);
            return Optional.ofNullable(film);
        } catch (DataAccessException e) {
            log.debug("Фильм с указанным ID = {} не найден", id);
            return Optional.empty();
        }
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        jdbcTemplate.update("INSERT INTO film_likes (film_id, user_id) VALUES(?,?)",
                filmId,
                userId);
        log.debug("Лайк пользователя c ID = {} к фильму с ID = {} успешно добавлен", userId, filmId);
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        jdbcTemplate.update("DELETE FROM film_likes WHERE film_id = ? AND user_id = ?",
                filmId,
                userId);
        log.debug("Лайк пользователя c ID = {} к фильму с ID = {} успешно удален", userId, filmId);
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        String sqlPopularFilms = "SELECT f.*, m.* " +
                "FROM films f " +
                "LEFT JOIN film_likes fl on f.film_id = fl.film_id " +
                "JOIN mpa m ON f.mpa_id = m.mpa_id " +
                "GROUP BY f.film_id, fl.film_id " +
                "ORDER BY COUNT(fl.user_id) DESC " +
                "LIMIT ?";
        List<Film> filmsWithoutGenres = jdbcTemplate.query(sqlPopularFilms, this::makeFilm, count);
        return makeFilmsWithGenres(filmsWithoutGenres);
    }

    private Film makeFilm(ResultSet rs, int rowNum) throws SQLException {
        int filmId = rs.getInt("film_id");
        try {
            return new Film(
                    filmId,
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("release_date").toLocalDate(),
                    rs.getInt("duration"),
                    makeMpa(rs),
                    new HashSet<>());
        } catch (DataAccessException e) {
            throw new EntityNotFoundException(WRONG_FILM_ID);
        }
    }

    private List<Film> makeFilmsWithGenres(List<Film> films) {
        final Map<Integer, Film> filmById = films.stream().collect(Collectors.toMap(Film::getId, Function.identity()));
        String sqlGenres = "SELECT * " +
                "FROM film_genres fg " +
                "JOIN genres g on fg.genre_id = g.genre_id " +
                "WHERE fg.film_id IN (%s) " +
                "ORDER BY g.genre_id";
        String inSql = String.join(",", Collections.nCopies(films.size(), "?"));

        jdbcTemplate.query(String.format(sqlGenres, inSql),
                (rs, rowNum) -> addGenreToFilm(rs, filmById.get(makeFilmId(rs))),
                filmById.keySet().toArray());

        return new ArrayList<>(filmById.values());
    }

    private Film addGenreToFilm(ResultSet rs, Film film) throws SQLException {
        Genre genre = new Genre(rs.getInt("genres.genre_id"), rs.getString("genres.name"));
        film.getGenres().add(genre);
        return film;
    }

    private Integer makeFilmId(ResultSet rs) throws SQLException {
        return rs.getInt("film_genres.film_id");
    }

    private Mpa makeMpa(ResultSet rs) throws SQLException {
        return new Mpa(
                rs.getInt("mpa.mpa_id"),
                rs.getString("mpa.name"),
                rs.getString("mpa.description"));
    }

    private void removeGenres(int filmId, Set<Genre> genres) {
        String sqlDeleteGenres = "DELETE FROM film_genres WHERE film_id = ? AND genre_id = ?";
        List<Integer> genresId = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(sqlDeleteGenres,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, filmId);
                        ps.setInt(2, genresId.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return genresId.size();
                    }
                });
    }

    private void addGenres(int filmId, Set<Genre> genres) {
        String sqlUpdateGenres = "INSERT INTO film_genres (film_id, genre_id) VALUES (?,?)";
        List<Integer> genresId = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(sqlUpdateGenres,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, filmId);
                        ps.setInt(2, genresId.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return genresId.size();
                    }
                });
    }
}

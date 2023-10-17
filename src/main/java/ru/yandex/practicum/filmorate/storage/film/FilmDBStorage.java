package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.constants.MyConstants;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FilmDBStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static final String WRONG_FILM_ID = "Фильм с указанным ID не найден";
    private static final String SQL_ADD_GENRE_CONDITION =
            "WHERE f.film_id IN " + "(SELECT film_id FROM film_genres WHERE genre_id = :genre_id) ";
    private static final String SQL_ADD_YEAR_CONDITION = "WHERE EXTRACT(YEAR FROM " +
            "cast(f.release_date AS date)) = :year ";

    public FilmDBStorage(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Film addFilm(Film film) {
        Map<String, Object> values = new HashMap<>();
        values.put("name", film.getName());
        values.put("description", film.getDescription());
        values.put("release_date", film.getReleaseDate());
        values.put("duration", film.getDuration());
        values.put("mpa_id", film.getMpa().getId());

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("films")
                .usingGeneratedKeyColumns("film_id");

        int filmId = simpleJdbcInsert.executeAndReturnKey(values).intValue();

        if (film.getGenres() != null) {
            addGenres(filmId, film.getGenres());
        }

        if (film.getDirectors() != null) {
            addDirectors(filmId, film.getDirectors());
        }

        log.debug("Фильм успешно создан с ID - {}", filmId);
        return getFilm(filmId).orElseThrow(() -> new EntityNotFoundException(WRONG_FILM_ID));
    }

    @Override
    public Film updateFilm(Film film) {
        String sqlUpdateFilm =
                "UPDATE films " + "SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                        "WHERE film_id = ?";
        int filmId = film.getId();
        Optional<Film> filmOptional = getFilm(filmId);

        if (filmOptional.isPresent()) {
            jdbcTemplate.update(sqlUpdateFilm, film.getName(), film.getDescription(),
                    film.getReleaseDate(), film.getDuration(),
                    film.getMpa().getId(), filmId);
            removeGenres(filmId, filmOptional.get().getGenres());
            removeDirectors(filmId, filmOptional.get().getDirectors());
        }

        if (film.getGenres() != null) {
            addGenres(filmId, film.getGenres());
        }

        if (film.getDirectors() != null) {
            addDirectors(filmId, film.getDirectors());
        }

        log.debug("Фильм успешно обновлен по указанном ID = {}", filmId);
        return getFilm(filmId).orElseThrow(() -> new EntityNotFoundException(WRONG_FILM_ID));
    }

    @Override
    public List<Film> getAllFilms() {
        String sqlFilms = "SELECT f.*, m.* " + "FROM films f " + "JOIN mpa m ON f.mpa_id = m.mpa_id";
        List<Film> filmsWithoutGenres = jdbcTemplate.query(sqlFilms, this::makeFilm);
        makeFilmsWithDirectors(filmsWithoutGenres);
        return makeFilmsWithGenres(filmsWithoutGenres);
    }

    @Override
    public Optional<Film> getFilm(int id) {
        String sqlFilm = "SELECT f.*, m.* " + "FROM films f " +
                "JOIN mpa m ON f.mpa_id = m.mpa_id " + "WHERE f.film_id = ? ";
        try {
            Film film = jdbcTemplate.queryForObject(sqlFilm, this::makeFilm, id);
            makeFilmsWithDirectors(List.of(film));
            makeFilmsWithGenres(List.of(film));
            log.debug("Фильм с указанным ID = {} найден", id);
            return Optional.of(film);
        } catch (DataAccessException e) {
            log.debug("Фильм с указанным ID = {} не найден", id);
            return Optional.empty();
        }
    }

    @Override
    public void addLike(int filmId, int userId) {
        try {
            jdbcTemplate.update(MyConstants.SQLFEEDFILM, userId, filmId, 2, 1, 2, LocalDateTime.now());
            jdbcTemplate.update("INSERT INTO film_likes (film_id, user_id) VALUES(?,?)", filmId, userId);
            log.debug("Лайк пользователя c ID = {} к фильму с ID = {} успешно добавлен", userId, filmId);
        } catch (DuplicateKeyException e) {
            // (ничего не делать) Подгон под тесты, т.к. постман и гит требуют 200 код даже в случае не добавленного в
            // FILM_LIKES дублированного значения
        }
    }

    @Override
    public void removeLike(int filmId, int userId) {
        jdbcTemplate.update(MyConstants.SQLFEEDFILM, userId, filmId, 2, 1, 1, LocalDateTime.now());
        jdbcTemplate.update("DELETE FROM film_likes WHERE film_id = ? AND user_id = ?", filmId, userId);
        log.debug("Лайк пользователя c ID = {} к фильму с ID = {} успешно удален", userId, filmId);
    }

    @Override
    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        List<Film> popularFilmsWithSort;
        SqlParameterSource parameters = new MapSqlParameterSource("count", count)
                .addValue("genre_id", genreId).addValue("year", year);


        String sqlPopularFilms = "SELECT f.*, m.* " + "FROM films f " +
                "LEFT JOIN film_likes fl on f.film_id = fl.film_id " +
                "JOIN mpa m ON f.mpa_id = m.mpa_id ";

        if (genreId != null && year != null) {
            sqlPopularFilms =
                    sqlPopularFilms + SQL_ADD_GENRE_CONDITION + "AND EXTRACT(YEAR FROM " +
                            "cast(f.release_date AS date)) = :year ";
        } else if (genreId != null && year == null) {
            sqlPopularFilms = sqlPopularFilms + SQL_ADD_GENRE_CONDITION;
        } else if (genreId == null && year != null) {
            sqlPopularFilms = sqlPopularFilms + SQL_ADD_YEAR_CONDITION;
        }

        String endSql = "GROUP BY f.film_id, fl.film_id " + "ORDER BY COUNT(fl.user_id) DESC " + "LIMIT :count";
        sqlPopularFilms = sqlPopularFilms + endSql;

        popularFilmsWithSort = namedParameterJdbcTemplate.query(sqlPopularFilms, parameters, this::makeFilm);
        makeFilmsWithDirectors(popularFilmsWithSort);
        makeFilmsWithGenres(popularFilmsWithSort);
        return popularFilmsWithSort;
    }

    @Override
    public List<Film> getDirectorFilms(Integer directorId, String sortBy) {
        List<Film> sortedFilms;
        String sqlWithoutSort = "SELECT f.*, m.* " + "FROM films f " + "LEFT JOIN mpa m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN film_directors fd ON f.film_id = fd.film_id " + "WHERE director_id = ?";
        String sqlSortByYear = "SELECT f.*, m.* " + "FROM films f " + "LEFT JOIN mpa m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN film_directors fd ON f.film_id = fd.film_id " + "WHERE fd.director_id = ? " +
                "ORDER BY f.release_date ASC";
        String sqlSortByLikes = "SELECT f.*, m.* " + "FROM films f " + "LEFT JOIN mpa m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN film_directors fd ON f.film_id = fd.film_id " +
                "LEFT JOIN film_likes fl ON f.film_id = fl.film_id " +
                "WHERE fd.director_id = ? " + "GROUP BY f.film_id " + "ORDER BY COUNT (fl.user_id) DESC";

        if (sortBy.equals("year")) {
            sortedFilms = jdbcTemplate.query(sqlSortByYear, this::makeFilm, directorId);
        } else if (sortBy.equals("likes")) {
            sortedFilms = jdbcTemplate.query(sqlSortByLikes, this::makeFilm, directorId);
        } else {
            sortedFilms = jdbcTemplate.query(sqlWithoutSort, this::makeFilm, directorId);
        }

        makeFilmsWithDirectors(sortedFilms);
        makeFilmsWithGenres(sortedFilms);

        return sortedFilms;
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        List<Film> sortedCommonFilms;
        String sql =
                "SELECT *\n" + "FROM FILMS \n" + "INNER JOIN mpa m ON FILMS.mpa_id = m.mpa_id \n" +
                        "WHERE FILM_ID IN (SELECT FILM_ID \n" +
                        "FROM FILM_LIKES \n" + "WHERE USER_ID = ?);";

        String sqlSorted = "SELECT f.*, m.* " + "FROM films f " +
                "LEFT JOIN film_likes fl on f.film_id = fl.film_id " +
                "JOIN mpa m ON f.mpa_id = m.mpa_id " + "GROUP BY f.film_id, fl.film_id " +
                "ORDER BY COUNT(fl.user_id) DESC ";

        List<Film> userFilms = jdbcTemplate.query(sql, (rs, row) -> makeFilm(rs, row), userId);
        List<Film> friendFilms = jdbcTemplate.query(sql, (rs, row) -> makeFilm(rs, row), friendId);
        friendFilms.retainAll(userFilms);
        sortedCommonFilms = jdbcTemplate.query(sqlSorted, (rs, row) -> makeFilm(rs, row));
        sortedCommonFilms.retainAll(friendFilms);
        return sortedCommonFilms;
    }

    @Override
    public void deleteFilmById(int filmId) {
        if (!getFilm(filmId).isPresent()) {
            throw new EntityNotFoundException("Фильм с id: " + filmId + " не найден.");
        }
        String sql = "DELETE FROM films WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    @Override
    public List<Film> getRecommendation(Set<Integer> usersId) {
        String[] idStrings = usersId.stream().map(String::valueOf).toArray(String[]::new);

        String joinedIds = String.join(",", idStrings);
        String sql = "SELECT f.*, m.* FROM FILMS f JOIN MPA m ON f.mpa_id = m.mpa_id where FILM_ID in " +
                "(" + joinedIds + ")";
        List<Film> films = jdbcTemplate.query(sql, this::makeFilm);
        makeFilmsWithDirectors(films);
        return makeFilmsWithGenres(films);
    }

    @Override
    public List<Film> getFilmsBySearch(String query, String by) {
        String sql;
        List<Film> films;
        if (by.equals("title,director") || by.equals("director,title")) {
            sql = "SELECT f.FILM_ID as film_id, f.NAME as film_name, f.DESCRIPTION as description, " +
                    "RELEASE_DATE, DURATION, m.MPA_ID as mpa_id, m.NAME as mpa_name, " +
                    "m.DESCRIPTION as mpa_description " +
                    "FROM FILMS f left join FILM_DIRECTORS FD on f.FILM_ID = FD.FILM_ID " +
                    "left join mpa m on f.MPA_ID = m.MPA_ID " +
                    "left join FILM_LIKES b on f.FILM_ID = b.FILM_ID " +
                    "left join DIRECTORS D on D.DIRECTOR_ID = FD.DIRECTOR_ID " +
                    "WHERE LOWER(d.NAME) LIKE LOWER('%" + query +
                    "%') or LOWER(f.NAME) LIKE LOWER('%" + query + "%') " +
                    "GROUP BY f.FILM_ID " +
                    "ORDER BY COUNT(b.USER_ID) DESC";
        } else if (by.equals("director")) {
            sql = "SELECT * FROM (FILMS f\n" +
                    "join FILM_DIRECTORS fd ON f.FILM_ID = fd.FILM_ID\n" +
                    "join DIRECTORS d on fd.DIRECTOR_ID = d.DIRECTOR_ID join MPA ON f.MPA_ID = MPA.MPA_ID)\n" +
                    "where LOWER(d.NAME) like LOWER('%" + query + "%')";
        } else if (by.equals("title")) {
            sql = "SELECT * FROM" +
                    "(SELECT * FROM FILMS f where LOWER(f.NAME) like LOWER('%" + query +
                    "%')) b join MPA ON b.MPA_ID = MPA.MPA_ID";
        } else {
            return Collections.emptyList();
        }
        films = jdbcTemplate.query(sql, this::makeFilm);
        makeFilmsWithDirectors(films);
        makeFilmsWithGenres(films);
        return films;
    }

    private Film makeFilm(ResultSet rs, int rowNum) throws SQLException {
        int filmId = rs.getInt("film_id");
        try {
            return new Film(filmId, rs.getString("name"), rs.getString("description"),
                    rs.getDate("release_date").toLocalDate(),
                    rs.getInt("duration"), makeMpa(rs), new HashSet<>(), new HashSet<>());
        } catch (DataAccessException e) {
            throw new EntityNotFoundException(WRONG_FILM_ID);
        }
    }

    private List<Film> makeFilmsWithGenres(List<Film> films) {
        final Map<Integer, Film> filmById = films.stream().collect(Collectors.toMap(Film::getId, Function.identity()));
        String sqlGenres =
                "SELECT * " + "FROM film_genres fg " + "JOIN genres g on fg.genre_id = g.genre_id " +
                        "WHERE fg.film_id IN (%s) " +
                        "ORDER BY fg.FILM_ID";
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

    private List<Film> makeFilmsWithDirectors(List<Film> films) {
        final Map<Integer, Film> filmById = films.stream().collect(Collectors.toMap(Film::getId, Function.identity()));
        String sqlDirectors = "SELECT * " + "FROM film_directors fd " +
                "JOIN directors d on fd.director_id = d.director_id " +
                "WHERE fd.film_id IN (%s) " +
                "ORDER BY d.director_id";
        String inSql = String.join(",", Collections.nCopies(films.size(), "?"));

        jdbcTemplate.query(String.format(sqlDirectors, inSql), (rs, rowNum) -> addDirectorToFilm(rs,
                        filmById.get(makeFilmId(rs))),
                filmById.keySet().toArray());

        return new ArrayList<>(filmById.values());
    }

    private Film addDirectorToFilm(ResultSet rs, Film film) throws SQLException {
        Director director = new Director(rs.getInt("directors.director_id"),
                rs.getString("directors.name"));
        film.getDirectors().add(director);
        return film;
    }

    private Integer makeFilmId(ResultSet rs) throws SQLException {
        return rs.getInt("film_id");
    }

    private Mpa makeMpa(ResultSet rs) throws SQLException {
        return new Mpa(rs.getInt("mpa.mpa_id"), rs.getString("mpa.name"),
                rs.getString("mpa.description"));
    }

    private void removeGenres(int filmId, Set<Genre> genres) {
        String sqlDeleteGenres = "DELETE FROM film_genres WHERE film_id = ? AND genre_id = ?";
        List<Integer> genresId = genres.stream().map(Genre::getId).collect(Collectors.toList());

        jdbcTemplate.batchUpdate(sqlDeleteGenres, new BatchPreparedStatementSetter() {
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
        List<Integer> genresId = genres.stream().map(Genre::getId).collect(Collectors.toList());

        jdbcTemplate.batchUpdate(sqlUpdateGenres, new BatchPreparedStatementSetter() {
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

    private void removeDirectors(int filmId, Set<Director> directors) {
        String sqlDeleteDirectors = "DELETE FROM film_directors WHERE film_id = ? AND director_id = ?";
        List<Integer> directorsId = directors.stream().map(Director::getId).collect(Collectors.toList());

        jdbcTemplate.batchUpdate(sqlDeleteDirectors, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, filmId);
                ps.setInt(2, directorsId.get(i));
            }

            @Override
            public int getBatchSize() {
                return directorsId.size();
            }
        });
    }

    private void addDirectors(int filmId, Set<Director> directors) {
        String sqlUpdateDirectors = "INSERT INTO film_directors (film_id, director_id) VALUES(?,?)";
        List<Integer> directorsId = directors.stream().map(Director::getId).collect(Collectors.toList());

        jdbcTemplate.batchUpdate(sqlUpdateDirectors, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, filmId);
                ps.setInt(2, directorsId.get(i));
            }

            @Override
            public int getBatchSize() {
                return directorsId.size();
            }
        });
    }
}

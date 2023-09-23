package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Component
@Qualifier("GenreDBStorage")
public class GenreDBStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    public GenreDBStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Genre> getGenre(int genreId) {
        String sqlGenre = "SELECT * FROM genres WHERE genre_id = ?";
        SqlRowSet genreRow = jdbcTemplate.queryForRowSet(sqlGenre, genreId);

        if (genreRow.next()) {
            return Optional.of(new Genre(
                    genreRow.getInt("genre_id"),
                    genreRow.getString("name")));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<Genre> getAllGenres() {
        return jdbcTemplate.query("SELECT * FROM genres",
                (rs, rowNum) -> new Genre(
                        rs.getInt("genre_id"),
                        rs.getString("name")));
    }
}

package ru.yandex.practicum.filmorate.storage.director;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class DirectorDBStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;
    private static final String WRONG_DIRECTOR_ID = "Режиссёр с указанным ID = {} не найден";

    public DirectorDBStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Director> getDirector(int directorId) {
        String sqlDirector = "SELECT * FROM directors WHERE director_id = ?";
        try {
            Director director = jdbcTemplate.queryForObject(sqlDirector, (rs, rowNum) -> makeDirector(rs), directorId);
            log.debug("Режиссёр с указанным ID = {} найден", directorId);
            return Optional.of(director);
        } catch (DataAccessException e) {
            log.debug(WRONG_DIRECTOR_ID, directorId);
            return Optional.empty();
        }
    }

    @Override
    public List<Director> getAllDirectors() {
        String sqlDirectors = "SELECT * FROM directors";
        return jdbcTemplate.query(sqlDirectors, (rs, rowNum) -> makeDirector(rs));
    }

    @Override
    public Director addDirector(Director director) {
        Map<String, Object> values = new HashMap<>();
        values.put("name", director.getName());

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("directors")
                .usingGeneratedKeyColumns("director_id");

        int directorId = simpleJdbcInsert.executeAndReturnKey(values).intValue();

        log.debug("Режиссёр успешно создан с ID = {}", directorId);

        director.setId(directorId);
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        String sqlForUpdateDirector = "UPDATE directors SET name = ? WHERE director_id = ?";
        int directorId = director.getId();

        jdbcTemplate.update(sqlForUpdateDirector, director.getName(), directorId);

        log.debug("Режиссёр с ID = {} успешно обновлен", directorId);
        return director;
    }

    @Override
    public void deleteDirector(int directorId) {
        String sqlDirector = "DELETE FROM directors WHERE director_id = ?";
        jdbcTemplate.update(sqlDirector, directorId);
        log.debug("Режиссёр с ID - {} успешно удален", directorId);
    }

    private Director makeDirector(ResultSet rs) throws SQLException {
        return new Director(
                rs.getInt("director_id"),
                rs.getString("name"));
    }
}

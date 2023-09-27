package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class MpaDBStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    public MpaDBStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Mpa> getMpa(int mpaId) {
        String sqlMpa = "SELECT * FROM mpa WHERE mpa_id = ?";
        try {
            Mpa mpa = jdbcTemplate.queryForObject(sqlMpa, (rs, rowNum) -> makeMpa(rs), mpaId);
            log.debug("Mpa с указанным ID = {} найден", mpaId);
            return Optional.ofNullable(mpa);
        } catch (DataAccessException e) {
            log.debug("Mpa с указанным ID = {} не найден", mpaId);
            return Optional.empty();
        }
    }

    @Override
    public List<Mpa> getAllMpa() {
        String sqlMpa = "SELECT * FROM mpa";
        return jdbcTemplate.query(sqlMpa, (rs, rowNum) -> makeMpa(rs));
    }

    private Mpa makeMpa(ResultSet rs) throws SQLException {
        return new Mpa(
                rs.getInt("mpa_id"),
                rs.getString("name"),
                rs.getString("description"));
    }
}

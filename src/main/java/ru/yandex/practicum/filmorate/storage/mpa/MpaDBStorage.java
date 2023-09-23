package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Component
@Qualifier("MpaDBStorage")
public class MpaDBStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    public MpaDBStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Mpa> getMpa(int mpaId) {
        String sqlMpa = "SELECT * FROM mpa WHERE mpa_id = ?";
        SqlRowSet mpaRow = jdbcTemplate.queryForRowSet(sqlMpa, mpaId);

        if (mpaRow.next()) {
            return Optional.of(new Mpa(
                    mpaRow.getInt("mpa_id"),
                    mpaRow.getString("name"),
                    mpaRow.getString("description")));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<Mpa> getAllMpa() {
        return jdbcTemplate.query("SELECT * FROM mpa",
                (rs, rowNum) -> new Mpa(
                        rs.getInt("mpa_id"),
                        rs.getString("name"),
                        rs.getString("description")));
    }
}

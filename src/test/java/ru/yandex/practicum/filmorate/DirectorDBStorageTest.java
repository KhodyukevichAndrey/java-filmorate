package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorDBStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DirectorDBStorageTest {

    @Autowired
    private final DirectorDBStorage directorDBStorage;
    private final JdbcTemplate jdbcTemplate;
    Director firstDirector;
    Director directorAfterCreate;
    Director directorForUpdate;

    @BeforeEach
    void createDirectorTestEnvironment() {
        firstDirector = new Director(999, "FirstDirectorName");
        directorForUpdate = new Director(1, "NameAfterUpdate");

        directorAfterCreate = directorDBStorage.addDirector(firstDirector);
    }

    @AfterEach
    void dropFilmAndUserStorageDataBase() {
        jdbcTemplate.update("DELETE FROM directors");
        jdbcTemplate.update("ALTER TABLE directors ALTER COLUMN director_id RESTART WITH 1");
    }

    @Test
    void shouldCreateAndReturnDirector() {
        assertEquals(1, directorDBStorage.getAllDirectors().size(),
                "В коллекции должен быть только 1 режиссёр");

        Director secondDirectorAfterCreate = directorDBStorage.addDirector(firstDirector);

        assertEquals(2, directorDBStorage.getAllDirectors().size(),
                "После создания режиссёра размер коллекции должен быть увеличен на 1");
        assertEquals(2, secondDirectorAfterCreate.getId(), "ID Режиссёра не соответствует");
        assertEquals("FirstDirectorName", secondDirectorAfterCreate.getName(),
                "После добавления режиссёра его имя не должно быть изменено");
    }

    @Test
    void shouldUpdateAndReturnDirector() {
        Director directorAfterUpdate = directorDBStorage.updateDirector(directorForUpdate);

        assertNotNull(directorAfterUpdate);
        assertEquals("NameAfterUpdate", directorAfterUpdate.getName(),
                "Имя режиссера после обновления не соответствует");
        assertEquals(1, directorAfterUpdate.getId(),
                "После обновления ID должен оставаться неизменным");
        assertEquals(1, directorDBStorage.getAllDirectors().size(),
                "После обновления режиссёра размер коллекции должен оставаться без изменений");
    }

    @Test
    void shouldDeleteDirector() {
        Director directorAfterCreate = directorDBStorage.addDirector(firstDirector);
        assertEquals(2, directorDBStorage.getAllDirectors().size(),
                "После создания режиссёра размер коллекции должен быть увеличен на 1");

        directorDBStorage.deleteDirector(directorAfterCreate.getId());
        Optional<Director> directorOptional = directorDBStorage.getDirector(2);

        assertEquals(1, directorDBStorage.getAllDirectors().size(),
                "После удаления режиссёра размер коллекции должен быть уменьшен на 1");
        assertFalse(directorOptional.isPresent(),
                "После удаления режиссёра по ID, DB должна вернуть по ID пустой Optional");
    }

    @Test
    void shouldReturnDirectorById() {
        Optional<Director> directorOptional = directorDBStorage.getDirector(1);

        assertThat(directorOptional)
                .isPresent()
                .hasValueSatisfying(director ->
                        assertThat(director).hasFieldOrPropertyWithValue("id", 1));

        assertThat(directorOptional)
                .isPresent()
                .hasValueSatisfying(director ->
                        assertThat(director).hasFieldOrPropertyWithValue("Name", "FirstDirectorName"));
    }

    @Test
    void shouldReturnAllDirectors() {
        List<Director> allDirectors = directorDBStorage.getAllDirectors();

        assertEquals(1, allDirectors.size(),
                "Количество режиссёров не соответствует кол-ву созданных");
        assertEquals(directorAfterCreate, allDirectors.get(0),
                "Первый созданный режиссёр должен соответствовать первому режиссёру из списка");
    }
}

package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaDBStorageTest {

    @Autowired
    private final MpaStorage mpaStorage;

    @Test
    void shouldReturnMpaById() {
        Optional<Mpa> mpaOptional = mpaStorage.getMpa(1);

        assertThat(mpaOptional)
                .isPresent()
                .hasValueSatisfying(mpa ->
                        assertThat(mpa).hasFieldOrPropertyWithValue("name", "G"));

        assertThat(mpaOptional)
                .isPresent()
                .hasValueSatisfying(mpa ->
                        assertThat(mpa).hasFieldOrPropertyWithValue("description",
                                "Без возрастных ограничений"));
    }

    @Test
    void shouldReturnMpaList() {
        List<Mpa> mpa = mpaStorage.getAllMpa();

        assertEquals(5, mpa.size(), "Количество всех mpa не соответствует");
        assertEquals("Детям рекомендован просмотр в присутствии родителей", mpa.get(1).getDescription(),
                "Описание mpa не соответствует");
    }
}

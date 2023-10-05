package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDBStorageTest {

    @Autowired
    private final GenreStorage genreStorage;

    @Test
    void shouldReturnGenreById() {
        Optional<Genre> genreOptional = genreStorage.getGenre(1);

        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(genre ->
                        assertThat(genre).hasFieldOrPropertyWithValue("name", "Комедия"));
    }

    @Test
    void shouldReturnGenresList() {
        List<Genre> genres = genreStorage.getAllGenres();

        assertEquals(6, genres.size(), "Количество всех жанров не соответствует");
        assertEquals("Комедия", genres.get(0).getName(), "Название жанра не соответствует");
    }
}

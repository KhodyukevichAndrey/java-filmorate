package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

public interface DirectorStorage {

    Optional<Director> getDirector(int directorId);

    List<Director> getAllDirectors();

    Director addDirector(Director director);

    Director updateDirector(Director director);

    void deleteDirector(int directorId);
}

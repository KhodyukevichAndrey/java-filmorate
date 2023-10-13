package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorDBStorage;

import java.util.List;

@Service
public class DirectorService {

    private static final String WRONG_DIRECTOR_ID = "Режиссёр с указанным не найден";
    private final DirectorDBStorage directorStorage;

    @Autowired
    public DirectorService(DirectorDBStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public Director addDirector(Director director) {
        return directorStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        return directorStorage.updateDirector(director);
    }

    public void deleteDirector(int directorId) {
        directorStorage.deleteDirector(directorId);
    }

    public Director getDirector(int directorId) {
        return directorStorage.getDirector(directorId)
                .orElseThrow(() -> new EntityNotFoundException(WRONG_DIRECTOR_ID));
    }

    public List<Director> getAllDirectors() {
        return directorStorage.getAllDirectors();
    }
}

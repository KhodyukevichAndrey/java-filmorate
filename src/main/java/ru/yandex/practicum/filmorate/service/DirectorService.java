package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Service
public class DirectorService {

    private static final String WRONG_DIRECTOR_ID = "Режиссёр с указанным не найден";
    private final DirectorStorage directorStorage;

    @Autowired
    public DirectorService(DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public Director addDirector(Director director) {
        return directorStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        getDirector(director.getId());
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

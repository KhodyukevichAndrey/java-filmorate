package ru.yandex.practicum.filmorate.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class ReleaseDateConstraintValidator implements ConstraintValidator<ReleaseDate, LocalDate> {

    private LocalDate earliestDate;

    @Override
    public void initialize(ReleaseDate date) {
        this.earliestDate = LocalDate.parse(date.earliestDate());
    }

    @Override
    public boolean isValid(LocalDate releaseDate, ConstraintValidatorContext cxt) {
        return releaseDate == null || !releaseDate.isBefore(earliestDate);
    }
}

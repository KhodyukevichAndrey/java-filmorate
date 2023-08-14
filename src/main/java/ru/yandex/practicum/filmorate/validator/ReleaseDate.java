package ru.yandex.practicum.filmorate.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ReleaseDateConstraintValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ReleaseDate {

    String earliestDate() default "1895-12-28";

    String message() default "{Дата должна быть позднее указанного периода}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class User {
    private int id;
    @NotEmpty
    @Email
    private final String email;
    @NotBlank
    @Pattern(regexp = "[^ ]*")
    private final String login;
    private String name;
    @NotNull
    @PastOrPresent
    private final LocalDate birthday;
}

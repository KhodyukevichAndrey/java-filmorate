package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
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
    private List<Integer> friends = new ArrayList<>();
}

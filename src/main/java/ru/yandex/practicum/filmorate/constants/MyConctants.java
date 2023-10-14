package ru.yandex.practicum.filmorate.constants;

public class MyConctants {
    public static final String SQLFEEDFILM = "INSERT INTO feed" +
            "(user_id, film_id, EVENT_TYPE, OPERATION, TIME_STAMP) VALUES (?,?,?,?,?);";
    public static final String SQLFEEDUSER = "INSERT INTO feed" +
            "(user_id, user_friend_id, EVENT_TYPE, OPERATION, TIME_STAMP) VALUES (?,?,?,?,?);";
}

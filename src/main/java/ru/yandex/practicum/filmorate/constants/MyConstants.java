package ru.yandex.practicum.filmorate.constants;

public class MyConstants {
    public static final String SQLFEEDFILM = "INSERT INTO feed" +
            "(user_id, entity_id, entity_type_id, EVENT_TYPE, OPERATION, TIME_STAMP) VALUES (?,?,?,?,?,?);";
    public static final String SQLFEEDUSER = "INSERT INTO feed" +
            "(user_id, entity_id, entity_type_id, EVENT_TYPE, OPERATION, TIME_STAMP) VALUES (?,?,?,?,?,?);";
    public static final String SQLFEEDREVIEW = "INSERT INTO feed " +
            "(user_id, entity_id, entity_type_id, EVENT_TYPE, OPERATION, TIME_STAMP) VALUES (?,?,?,?,?,?);";
}

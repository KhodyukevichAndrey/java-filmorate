INSERT INTO GENRES (name)
VALUES ('Комедия'),
('Драма'),
('Мультфильм'),
('Триллер'),
('Документальный'),
('Боевик');

INSERT INTO MPA (name, description)
VALUES ('G', 'Без возрастных ограничений'),
('PG', 'Детям рекомендован просмотр в присутствии родителей'),
('PG-13', 'Детям до 13 лет просмотр не желателен'),
('R', 'Лицам до 17 лет рекомендован просмотр в присутствии родителей'),
('NC-17', 'Лицам до 18 лет просмотр запрещен');

INSERT INTO EVENT(EVENT_TYPE_ID, EVENT_NAME)
VALUES (1, 'LIKE'),
       (2, 'REVIEW'),
       (3, 'FRIEND');

INSERT INTO OPERATION(OPERATION_ID, OPERATION_NAME)
VALUES (1, 'REMOVE'),
       (2, 'ADD'),
       (3, 'UPDATE');
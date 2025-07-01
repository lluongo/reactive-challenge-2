DROP TABLE IF EXISTS health;

CREATE TABLE health
(
    id INT PRIMARY KEY,
    up BOOLEAN
);

INSERT INTO health
    (id, up)
VALUES (1, true);
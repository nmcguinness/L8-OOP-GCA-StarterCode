-- Create the database
CREATE DATABASE IF NOT EXISTS taskhub
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE taskhub;

-- Create the tasks table
CREATE TABLE IF NOT EXISTS tasks (
                                     id INT NOT NULL AUTO_INCREMENT,
                                     title VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    PRIMARY KEY (id)
    );

-- Optional starter data
INSERT INTO tasks (title, status) VALUES
                                      ('Prepare lab example', 'TODO'),
                                      ('Test JSON server', 'DOING'),
                                      ('Write DAO notes', 'DONE');
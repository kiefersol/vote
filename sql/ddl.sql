DROP TABLE IF EXISTS vote;
DROP TABLE IF EXISTS vote_option;
DROP TABLE IF EXISTS vote_record;
DROP TABLE IF EXISTS vote_result;

CREATE TABLE vote (
    vote_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL
);

CREATE TABLE vote_option (
    option_id INT AUTO_INCREMENT PRIMARY KEY,
    vote_id INT NOT NULL,
    option_text VARCHAR(255) NOT NULL,
    FOREIGN KEY (vote_id) REFERENCES vote(vote_id) ON DELETE CASCADE
);

CREATE TABLE vote_record (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    vote_id INT NOT NULL,
    option_id INT NOT NULL,
    voted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vote_id) REFERENCES vote(vote_id) ON DELETE CASCADE,
    FOREIGN KEY (option_id) REFERENCES vote_option(option_id) ON DELETE CASCADE
);

CREATE TABLE vote_result (
    vote_id INT NOT NULL,
    option_id INT NOT NULL,
    redis_count INT NOT NULL DEFAULT 0,
    db_count INT NOT NULL DEFAULT 0,
    is_matched BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (vote_id, option_id),
    FOREIGN KEY (vote_id) REFERENCES vote(vote_id) ON DELETE CASCADE,
    FOREIGN KEY (option_id) REFERENCES vote_option(option_id) ON DELETE CASCADE
);
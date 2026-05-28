CREATE TABLE IF NOT EXISTS users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(100)  NOT NULL,
    password   VARCHAR(255)  NOT NULL,
    nickname   VARCHAR(50)   DEFAULT NULL,
    created_at DATETIME      DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS forum_posts (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT        NOT NULL,
    nickname    VARCHAR(50)   NOT NULL,
    content     TEXT          NOT NULL,
    dish_name   VARCHAR(100)  DEFAULT NULL,
    likes           INT           DEFAULT 0,
    liked_user_ids  TEXT          DEFAULT NULL,
    created_at  DATETIME      DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_forum_user_id (user_id),
    CONSTRAINT fk_forum_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS lunches (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    description VARCHAR(500)  DEFAULT NULL,
    tags        VARCHAR(255)  DEFAULT NULL,
    user_id     BIGINT        NOT NULL,
    weight      INT           DEFAULT 1,
    created_at  DATETIME      DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    CONSTRAINT fk_lunch_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE lunches ADD COLUMN weight INT DEFAULT 1 AFTER user_id;

CREATE TABLE IF NOT EXISTS lunch_history (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id           BIGINT        NOT NULL,
    lunch_name        VARCHAR(100)  NOT NULL,
    lunch_description VARCHAR(500)  DEFAULT NULL,
    tags              VARCHAR(255)  DEFAULT NULL,
    created_at        DATETIME      DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_history_user_id (user_id),
    CONSTRAINT fk_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS forum_comments (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id     BIGINT        NOT NULL,
    user_id     BIGINT        NOT NULL,
    nickname    VARCHAR(50)   NOT NULL,
    content     TEXT          NOT NULL,
    created_at  DATETIME      DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_comment_post_id (post_id),
    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES forum_posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

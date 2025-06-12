CREATE TABLE IF NOT EXISTS users
(
    user_id    BIGINT AUTO_INCREMENT,
    username   VARCHAR(20)                      NOT NULL,
    password   VARCHAR(255)                     NOT NULL,
    role       enum ('ROLE_USER', 'ROLE_ADMIN') NOT NULL,
    created_at TIMESTAMP                        NOT NULL,
    updated_at TIMESTAMP                        NOT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT unique_username UNIQUE (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

# TEAM
CREATE TABLE IF NOT EXISTS teams
(
    team_id    BIGINT AUTO_INCREMENT,
    code       ENUM ('LG', 'KT', 'OB', 'HT', 'SS', 'LT', 'NC', 'SK', 'HH', 'WO') NOT NULL,
    created_at TIMESTAMP                                                         NOT NULL,
    updated_at TIMESTAMP                                                         NOT NULL,
    PRIMARY KEY (team_id),
    CONSTRAINT unique_team_code UNIQUE (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

# Matches
CREATE TABLE IF NOT EXISTS matches
(
    match_id     BIGINT AUTO_INCREMENT,
    home_team_id BIGINT                                                        NOT NULL,
    away_team_id BIGINT                                                        NOT NULL,
    match_result ENUM ('HOME_WIN', 'AWAY_WIN', 'DRAW', 'CANCEL', 'NOT_PLAYED') NULL,
    home_score   INT                                                           NULL,
    away_score   INT                                                           NULL,
    remark       VARCHAR(255)                                                  NULL,
    created_at   TIMESTAMP                                                     NOT NULL,
    updated_at   TIMESTAMP                                                     NOT NULL,
    PRIMARY KEY (match_id),
    CONSTRAINT fk_matches_home_team
        FOREIGN KEY (home_team_id) REFERENCES teams (team_id)
            ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_matches_away_team
        FOREIGN KEY (away_team_id) REFERENCES teams (team_id)
            ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

# live_boards
CREATE TABLE IF NOT EXISTS live_boards
(
    live_board_id BIGINT AUTO_INCREMENT,
    match_id      BIGINT       NOT NULL,
    name          VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL,
    PRIMARY KEY (live_board_id),
    CONSTRAINT unique_match_id UNIQUE (match_id),
    CONSTRAINT fk_live_boards_match
        FOREIGN KEY (match_id) REFERENCES matches (match_id)
            ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

# live_board_chat
CREATE TABLE IF NOT EXISTS live_board_chat_messages
(
    live_board_chat_message_id BIGINT AUTO_INCREMENT,
    message                    TEXT         NOT NULL,
    live_board_id              BIGINT       NOT NULL,
    user_id                    BIGINT       NULL,
    session_id                 VARCHAR(100) NULL,
    display_nickname           VARCHAR(255) NOT NULL,
    created_at                 TIMESTAMP    NOT NULL,
    updated_at                 TIMESTAMP    NOT NULL,
    PRIMARY KEY (live_board_chat_message_id),
    CONSTRAINT fk_chat_messages_live_board
        FOREIGN KEY (live_board_id) REFERENCES live_boards (live_board_id)
            ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_chat_messages_user
        FOREIGN KEY (user_id) REFERENCES message_user (user_id)
            ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


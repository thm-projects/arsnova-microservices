CREATE TABLE sessions (
    id uuid NOT NULL,
    keyword VARCHAR(8) NOT NULL,
    user_id uuid NOT NULL,
    title VARCHAR(255) NOT NULL,
    short_name VARCHAR(255) NOT NULL,
    last_owner_activity varchar(30) NOT NULL,
    creation_time varchar(30) NOT NULL,
    active boolean NOT NULL DEFAULT false,
    feedback_lock boolean NOT NULL DEFAULT true,
    flip_flashcards boolean NOT NULL DEFAULT true,
    PRIMARY KEY(id)
);
CREATE EXTENSION IF NOT EXISTS pgcrypto;  -- для gen_random_uuid()

CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    native_lang   VARCHAR(10)  NOT NULL,
    target_lang   VARCHAR(10)  NOT NULL,
    level         SMALLINT     NOT NULL DEFAULT 1
                    CHECK (level BETWEEN 1 AND 5),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_native_target_differ CHECK (native_lang <> target_lang)
);
-- ============================================
-- ToDo App - PostgreSQL Schema
-- ============================================

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255),
    name            VARCHAR(255) NOT NULL,
    phone           VARCHAR(50),
    address         VARCHAR(500),
    department      VARCHAR(255),
    role            VARCHAR(20)  NOT NULL DEFAULT 'USER',
    profile_picture_url VARCHAR(1000),
    bio             TEXT,
    email_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Todos table
CREATE TABLE IF NOT EXISTS todos (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    priority    VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    category    VARCHAR(255),
    due_date    TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Verification tokens table
CREATE TABLE IF NOT EXISTS verification_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(512) NOT NULL,
    expires_at  TIMESTAMPTZ  NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ============================================
-- Indexes
-- ============================================
CREATE INDEX IF NOT EXISTS idx_todos_user_id       ON todos(user_id);
CREATE INDEX IF NOT EXISTS idx_todos_status        ON todos(status);
CREATE INDEX IF NOT EXISTS idx_todos_priority      ON todos(priority);
CREATE INDEX IF NOT EXISTS idx_todos_due_date      ON todos(due_date);
CREATE INDEX IF NOT EXISTS idx_verification_token  ON verification_tokens(token);
CREATE INDEX IF NOT EXISTS idx_verification_user   ON verification_tokens(user_id);

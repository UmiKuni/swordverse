CREATE TABLE users (
    id uuid PRIMARY KEY,
    username varchar(50) NOT NULL,
    password_hash varchar(255) NOT NULL,
    display_name varchar(100),
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT chk_users_username_length CHECK (length(username) >= 3)
);

CREATE TABLE sessions (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL,
    status varchar(20) NOT NULL,
    access_token_expires_at timestamp with time zone NOT NULL,
    refresh_token_expires_at timestamp with time zone NOT NULL,
    revoked_at timestamp with time zone,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT fk_sessions_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_sessions_status
        CHECK (status IN ('ACTIVE', 'REVOKED', 'EXPIRED')),
    CONSTRAINT chk_sessions_expiration
        CHECK (access_token_expires_at <= refresh_token_expires_at),
    CONSTRAINT chk_sessions_revocation
        CHECK (status <> 'REVOKED' OR revoked_at IS NOT NULL)
);

CREATE INDEX idx_sessions_user_id ON sessions(user_id);
CREATE INDEX idx_sessions_status ON sessions(status);

CREATE TABLE refresh_tokens (
    id uuid PRIMARY KEY,
    session_id uuid NOT NULL,
    token_hash varchar(255) NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    revoked_at timestamp with time zone,
    created_at timestamp with time zone NOT NULL,
    CONSTRAINT uq_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_session
        FOREIGN KEY (session_id) REFERENCES sessions(id),
    CONSTRAINT chk_refresh_tokens_expiration
        CHECK (expires_at > created_at)
);

CREATE INDEX idx_refresh_tokens_session_id ON refresh_tokens(session_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);

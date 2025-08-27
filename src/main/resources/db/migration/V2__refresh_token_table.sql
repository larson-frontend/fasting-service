-- V2 Refresh Token table
CREATE TABLE IF NOT EXISTS refresh_token (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash VARCHAR(128) NOT NULL UNIQUE,
  expires_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  revoked BOOLEAN NOT NULL DEFAULT FALSE,
  user_agent_hash VARCHAR(128),
  ip_hash VARCHAR(128)
);

CREATE INDEX IF NOT EXISTS idx_refresh_token_user ON refresh_token(user_id);

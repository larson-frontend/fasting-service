-- V1 Baseline Core Schema
-- This migration establishes the initial schema for users and fasting sessions.
-- It mirrors the current JPA model essentials (simplified) and adds constraints.

CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(255) NOT NULL UNIQUE,
  email VARCHAR(255) UNIQUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  last_login_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  language VARCHAR(2) DEFAULT 'en' CHECK (language IN ('en','de')),
  theme VARCHAR(10) DEFAULT 'system' CHECK (theme IN ('light','dark','system')),
  fasting_reminders BOOLEAN DEFAULT true,
  meal_reminders BOOLEAN DEFAULT true,
  progress_updates BOOLEAN DEFAULT true
);

CREATE TABLE IF NOT EXISTS fast_session (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
  start_at TIMESTAMPTZ NOT NULL,
  end_at TIMESTAMPTZ,
  goal_hours INTEGER DEFAULT 16,
  CONSTRAINT ck_fast_session_time CHECK (end_at IS NULL OR end_at >= start_at)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_fast_session_user_id ON fast_session(user_id);
CREATE INDEX IF NOT EXISTS idx_fast_session_user_active ON fast_session(user_id, end_at) WHERE end_at IS NULL;

-- Goal hours constraint
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint WHERE conname = 'check_goal_hours_range'
  ) THEN
    EXECUTE 'ALTER TABLE fast_session ADD CONSTRAINT check_goal_hours_range CHECK (goal_hours >= 1 AND goal_hours <= 48)';
  END IF;
END$$;

COMMENT ON TABLE users IS 'User accounts with preferences + notification flags';
COMMENT ON TABLE fast_session IS 'Fasting sessions associated with users';
COMMENT ON COLUMN fast_session.goal_hours IS 'Target hours for the fasting session (1-48)';

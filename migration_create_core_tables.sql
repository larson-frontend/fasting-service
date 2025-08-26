-- Core Schema Migration (Idempotent)
-- This script creates the essential tables and constraints for users and fasting sessions.
-- It is safe to run multiple times; IF NOT EXISTS / conditional guards prevent duplicate objects.

-- Users table (simplified core fields)
CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  email VARCHAR(100),
  created_at timestamptz DEFAULT now(),
  language VARCHAR(2),
  theme VARCHAR(10),
  notifications_enabled BOOLEAN
);

-- Fasting session table
CREATE TABLE IF NOT EXISTS fast_session (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
  start_at timestamptz NOT NULL,
  end_at timestamptz,
  goal_hours INTEGER,
  CONSTRAINT ck_fast_session_time CHECK (end_at IS NULL OR end_at >= start_at)
);

-- Ensure only one active session per user (end_at IS NULL)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes WHERE schemaname = 'public' AND indexname = 'ux_fast_session_active'
    ) THEN
        EXECUTE 'CREATE UNIQUE INDEX ux_fast_session_active ON fast_session(user_id) WHERE end_at IS NULL';
    END IF;
END$$;

-- Optionally add a range constraint for goal_hours if column is used
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'fast_session' AND column_name = 'goal_hours'
    ) THEN
        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint WHERE conname = 'check_goal_hours_range'
        ) THEN
            EXECUTE 'ALTER TABLE fast_session ADD CONSTRAINT check_goal_hours_range CHECK (goal_hours >= 1 AND goal_hours <= 48)';
        END IF;
    END IF;
END$$;

-- Comments (optional, run once)
COMMENT ON TABLE users IS 'Application users (simplified core schema)';
COMMENT ON TABLE fast_session IS 'User fasting sessions';
COMMENT ON COLUMN fast_session.start_at IS 'Session start timestamp (UTC)';
COMMENT ON COLUMN fast_session.end_at IS 'Session end timestamp (UTC) or NULL if active';
COMMENT ON COLUMN fast_session.goal_hours IS 'Target fasting duration in hours (1-48)';

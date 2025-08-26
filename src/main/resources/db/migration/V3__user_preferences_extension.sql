-- V3 User preferences extension
-- Adds missing user preference and defaults columns to align with JPA embedded structures

ALTER TABLE users ADD COLUMN IF NOT EXISTS notifications_enabled BOOLEAN DEFAULT true;
ALTER TABLE users ADD COLUMN IF NOT EXISTS goal_achievements BOOLEAN DEFAULT true;
ALTER TABLE users ADD COLUMN IF NOT EXISTS weekly_reports BOOLEAN DEFAULT false;
ALTER TABLE users ADD COLUMN IF NOT EXISTS default_goal_hours INTEGER DEFAULT 16;
ALTER TABLE users ADD COLUMN IF NOT EXISTS preferred_fasting_type VARCHAR(50) DEFAULT '16:8';
ALTER TABLE users ADD COLUMN IF NOT EXISTS auto_start_next_fast BOOLEAN DEFAULT false;
ALTER TABLE users ADD COLUMN IF NOT EXISTS timezone VARCHAR(100);

-- Ensure goal hours constraint exists
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name = 'users' AND column_name = 'default_goal_hours'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM pg_constraint WHERE conname = 'check_users_default_goal_hours_range'
    ) THEN
      EXECUTE 'ALTER TABLE users ADD CONSTRAINT check_users_default_goal_hours_range CHECK (default_goal_hours >= 1 AND default_goal_hours <= 48)';
    END IF;
  END IF;
END$$;

COMMENT ON COLUMN users.notifications_enabled IS 'Master notification enable switch';
COMMENT ON COLUMN users.goal_achievements IS 'Enable goal achievement notifications';
COMMENT ON COLUMN users.weekly_reports IS 'Enable weekly progress reports';
COMMENT ON COLUMN users.default_goal_hours IS 'Default goal hours for new fasting sessions (1-48 hours)';
COMMENT ON COLUMN users.preferred_fasting_type IS 'Preferred fasting type (e.g., 16:8, 18:6, 24h)';
COMMENT ON COLUMN users.auto_start_next_fast IS 'Automatically start next fasting session when current ends';
COMMENT ON COLUMN users.timezone IS 'User timezone for date/time calculations';

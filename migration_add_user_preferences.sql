-- Database Migration: Add missing user preference columns
-- This script adds the missing columns for fasting defaults and timezone

-- Add timezone column
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS timezone VARCHAR(100);

-- Add fasting defaults columns
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS default_goal_hours INTEGER DEFAULT 16;

ALTER TABLE users 
ADD COLUMN IF NOT EXISTS preferred_fasting_type VARCHAR(50) DEFAULT '16:8';

ALTER TABLE users 
ADD COLUMN IF NOT EXISTS auto_start_next_fast BOOLEAN DEFAULT false;

-- Add constraints for the new columns
ALTER TABLE users 
ADD CONSTRAINT IF NOT EXISTS check_default_goal_hours_range 
CHECK (default_goal_hours >= 1 AND default_goal_hours <= 48);

-- Add comment for documentation
COMMENT ON COLUMN users.timezone IS 'User timezone for date/time calculations';
COMMENT ON COLUMN users.default_goal_hours IS 'Default goal hours for new fasting sessions (1-48 hours)';
COMMENT ON COLUMN users.preferred_fasting_type IS 'Preferred fasting type (e.g., 16:8, 18:6, 24h)';
COMMENT ON COLUMN users.auto_start_next_fast IS 'Automatically start next fasting session when current ends';

-- Verification query to check if all columns exist
SELECT 
    table_name, 
    column_name, 
    data_type,
    column_default,
    is_nullable
FROM information_schema.columns 
WHERE table_name = 'users' 
AND column_name IN ('timezone', 'default_goal_hours', 'preferred_fasting_type', 'auto_start_next_fast')
ORDER BY column_name;

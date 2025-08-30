-- Migration: Add enhanced notification preferences
-- Add new notification preference columns to users table

ALTER TABLE users ADD COLUMN IF NOT EXISTS notifications_enabled BOOLEAN DEFAULT true;
ALTER TABLE users ADD COLUMN IF NOT EXISTS goal_achievements BOOLEAN DEFAULT true;
ALTER TABLE users ADD COLUMN IF NOT EXISTS weekly_reports BOOLEAN DEFAULT false;

-- Update existing progress_updates default to false for better UX
ALTER TABLE users ALTER COLUMN progress_updates SET DEFAULT false;

-- Update existing records that don't have the new fields
UPDATE users 
SET 
    notifications_enabled = true,
    goal_achievements = true,
    weekly_reports = false
WHERE notifications_enabled IS NULL;

-- Ensure all new fields are NOT NULL
ALTER TABLE users ALTER COLUMN notifications_enabled SET NOT NULL;
ALTER TABLE users ALTER COLUMN goal_achievements SET NOT NULL;
ALTER TABLE users ALTER COLUMN weekly_reports SET NOT NULL;

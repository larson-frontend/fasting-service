-- Database Migration: Add goal_hours column to fast_session table
-- This script adds the goal_hours column with a default value of 16

-- Add the goal_hours column with default value 16
ALTER TABLE fast_session 
ADD COLUMN IF NOT EXISTS goal_hours INTEGER DEFAULT 16;

-- Add constraint to ensure goal_hours is between 1 and 48
ALTER TABLE fast_session 
ADD CONSTRAINT check_goal_hours_range 
CHECK (goal_hours >= 1 AND goal_hours <= 48);

-- Update existing sessions without goal_hours to have default value 16
UPDATE fast_session 
SET goal_hours = 16 
WHERE goal_hours IS NULL;

-- Add comment to the column
COMMENT ON COLUMN fast_session.goal_hours IS 'Target hours for the fasting session (1-48 hours)';

-- Verify the migration
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default 
FROM information_schema.columns 
WHERE table_name = 'fast_session' 
    AND column_name = 'goal_hours';

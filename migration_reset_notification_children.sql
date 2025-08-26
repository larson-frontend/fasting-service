-- Migration: Reset all notification children properties to false
-- Purpose: Ensure clean state for feature flag rollout where detailed notifications are hidden
-- Date: 2025-08-24
-- Description: Sets all detailed notification preferences to false while preserving the main 'enabled' flag

-- Update all existing users' notification preferences
-- Keep the main 'enabled' flag as is, but set all children to false
UPDATE users 
SET 
    fasting_reminders = false,
    meal_reminders = false,
    progress_updates = false,
    goal_achievements = false,
    weekly_reports = false
WHERE id IS NOT NULL;

-- Verify the update
SELECT 
    id,
    username,
    notifications_enabled,
    fasting_reminders,
    meal_reminders,
    progress_updates,
    goal_achievements,
    weekly_reports
FROM users;

-- Output summary
SELECT 
    COUNT(*) as total_users_updated,
    COUNT(CASE WHEN notifications_enabled = true THEN 1 END) as users_with_notifications_enabled,
    COUNT(CASE WHEN fasting_reminders = false THEN 1 END) as users_with_fasting_reminders_false,
    COUNT(CASE WHEN meal_reminders = false THEN 1 END) as users_with_meal_reminders_false,
    COUNT(CASE WHEN progress_updates = false THEN 1 END) as users_with_progress_updates_false,
    COUNT(CASE WHEN goal_achievements = false THEN 1 END) as users_with_goal_achievements_false,
    COUNT(CASE WHEN weekly_reports = false THEN 1 END) as users_with_weekly_reports_false
FROM users;

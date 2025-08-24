-- Migration script to add user management tables
-- Run this against your PostgreSQL database

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_login_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    -- User preferences (embedded)
    language VARCHAR(2) DEFAULT 'en' CHECK (language IN ('en', 'de')),
    theme VARCHAR(10) DEFAULT 'system' CHECK (theme IN ('light', 'dark', 'system')),
    
    -- Notification preferences
    fasting_reminders BOOLEAN DEFAULT true,
    meal_reminders BOOLEAN DEFAULT true,
    progress_updates BOOLEAN DEFAULT true
);

-- Add user_id column to fast_session table
ALTER TABLE fast_session 
ADD COLUMN IF NOT EXISTS user_id BIGINT REFERENCES users(id);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_fast_session_user_id ON fast_session(user_id);
CREATE INDEX IF NOT EXISTS idx_fast_session_user_active ON fast_session(user_id, end_at) WHERE end_at IS NULL;

-- Insert a default user for testing
INSERT INTO users (username, email, language, theme) 
VALUES ('default_user', 'user@example.com', 'en', 'system')
ON CONFLICT (username) DO NOTHING;

COMMENT ON TABLE users IS 'User accounts with authentication and preferences';
COMMENT ON TABLE fast_session IS 'Fasting sessions associated with users';
COMMENT ON COLUMN users.language IS 'UI language preference: en or de';
COMMENT ON COLUMN users.theme IS 'UI theme preference: light, dark, or system';
COMMENT ON COLUMN fast_session.user_id IS 'Foreign key reference to users table';

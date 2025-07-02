-- Drop table if exists
DROP TABLE IF EXISTS authorized_users;

-- Create authorized_users table
CREATE TABLE IF NOT EXISTS authorized_users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert some initial data
INSERT INTO authorized_users (username, email, active, created_at, updated_at)
VALUES ('admin', 'admin@example.com', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

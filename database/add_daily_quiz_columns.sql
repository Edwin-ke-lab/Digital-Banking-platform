-- Add daily quiz tracking columns to customers table
ALTER TABLE customers ADD daily_quiz_count INT DEFAULT 0;
ALTER TABLE customers ADD daily_quiz_date DATE NULL;

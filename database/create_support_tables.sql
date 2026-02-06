-- Create table for storing complaints
CREATE TABLE complaints (
    id SERIAL PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL,
    complaint_description TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'Pending',
    response TEXT DEFAULT NULL
);
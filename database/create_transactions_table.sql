CREATE TABLE transactions (
    id SERIAL PRIMARY KEY,
    account_number TEXT NOT NULL,
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description TEXT NOT NULL,
    amount NUMERIC(18,2) NOT NULL,
    loan_id INT
);
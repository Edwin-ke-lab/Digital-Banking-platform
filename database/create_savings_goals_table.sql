CREATE TABLE savings_goals (
    id SERIAL PRIMARY KEY,
    account_number TEXT NOT NULL,
    goal_name TEXT NOT NULL,
    target_amount NUMERIC(18,2) NOT NULL,
    current_amount NUMERIC(18,2) DEFAULT 0,
    auto_save_frequency TEXT DEFAULT NULL
);
CREATE TABLE rewards (
    reward_id INT IDENTITY(1,1) PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL,
    reward_type VARCHAR(50) NOT NULL,
    reward_amount DECIMAL(10, 2) NOT NULL,
    reward_description VARCHAR(255),
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_rewards_account FOREIGN KEY (account_number) REFERENCES accounts(account_number) ON DELETE CASCADE ON UPDATE CASCADE
);
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'loan_requests')
BEGIN
    CREATE TABLE loan_requests (
        id INT IDENTITY(1,1) PRIMARY KEY,
        account_number NVARCHAR(50) NOT NULL,
        loan_type NVARCHAR(50) NOT NULL,
        interest_rate DECIMAL(5, 2) NOT NULL,
        repayment_period INT NOT NULL,
        loan_amount DECIMAL(18, 2) NOT NULL,
        status NVARCHAR(20) DEFAULT 'Pending',
        created_at DATETIME DEFAULT GETDATE(),
        repayment_date DATETIME NULL
    );
END
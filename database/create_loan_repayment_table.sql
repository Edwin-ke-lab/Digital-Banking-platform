CREATE TABLE repayments (
  id BIGINT NOT NULL IDENTITY(1,1) PRIMARY KEY,
  loan_id BIGINT NOT NULL,
  repayment_amount DECIMAL(10,2) NOT NULL,
  repayment_date DATETIME NOT NULL DEFAULT GETDATE(),
  CONSTRAINT FK_LoanRepayments FOREIGN KEY (loan_id)
    REFERENCES loan_requests(id) ON DELETE CASCADE
);

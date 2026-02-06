-- Add loan limit field to customers table
ALTER TABLE customers ADD loan_limit DECIMAL(10, 2) DEFAULT 50000.00;

-- Update existing customers to have a default loan limit of 50000
UPDATE customers SET loan_limit = 50000.00 WHERE loan_limit IS NULL;


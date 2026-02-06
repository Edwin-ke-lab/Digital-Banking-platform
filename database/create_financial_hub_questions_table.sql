CREATE TABLE financial_hub_questions (
    id INT IDENTITY(1,1) PRIMARY KEY,
    question_text NVARCHAR(255) NOT NULL,
    correct_answer NVARCHAR(10) NOT NULL
);
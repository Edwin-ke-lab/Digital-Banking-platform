package main;

import GUI.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bank {
    // Timer for autosave processing
    private java.util.Timer autosaveTimer;

    // Start the autosave processor (call this once, e.g. on app startup)
    public void startAutoSaveProcessor() {
        if (autosaveTimer != null) return; // Prevent multiple timers
        autosaveTimer = new java.util.Timer(true); // Daemon thread
        // Run every hour (can adjust as needed)
        autosaveTimer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                processAutoSave();
            }
        }, 0, 60 * 60 * 1000); // every hour
    }

    // Process autosave for all accounts and goals
    private void processAutoSave() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Ensure last_auto_save column exists (best-effort)
            try {
                PreparedStatement check = conn.prepareStatement("SELECT last_auto_save FROM savings_goals LIMIT 1");
                check.executeQuery();
            } catch (SQLException ex) {
                try (PreparedStatement alter = conn.prepareStatement("ALTER TABLE savings_goals ADD COLUMN last_auto_save TIMESTAMP NULL")) {
                    alter.executeUpdate();
                    System.err.println("Added last_auto_save column to savings_goals table");
                } catch (SQLException ignore) {
                    System.err.println("Could not add last_auto_save column: " + ignore.getMessage());
                }
            }

            String query = "SELECT account_number, goal_name, auto_save_frequency, auto_save_amount, last_auto_save FROM savings_goals WHERE auto_save_frequency IS NOT NULL AND auto_save_amount > 0";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
            while (rs.next()) {
                String accountNumber = rs.getString("account_number");
                String goalName = rs.getString("goal_name");
                String frequency = rs.getString("auto_save_frequency");
                double amount = rs.getDouble("auto_save_amount"); // Use user-set autosave amount
                java.sql.Timestamp lastSave = rs.getTimestamp("last_auto_save");

                boolean shouldSave = false;
                if (frequency != null && lastSave != null) {
                    long diff = now.getTime() - lastSave.getTime();
                    if (frequency.equalsIgnoreCase("daily") && diff >= 24L * 60 * 60 * 1000) {
                        shouldSave = true;
                    } else if (frequency.equalsIgnoreCase("weekly") && diff >= 7L * 24 * 60 * 60 * 1000) {
                        shouldSave = true;
                    }
                } else if (frequency != null && lastSave == null) {
                    shouldSave = true; // First time
                }

                if (shouldSave) {
                    try {
                        contributeToGoal(accountNumber, goalName, amount);
                        // Update last_auto_save
                        PreparedStatement updateStmt = conn.prepareStatement("UPDATE savings_goals SET last_auto_save = ? WHERE account_number = ? AND goal_name = ?");
                        updateStmt.setTimestamp(1, now);
                        updateStmt.setString(2, accountNumber);
                        updateStmt.setString(3, goalName);
                        updateStmt.executeUpdate();
                    } catch (Exception ex) {
                        System.err.println("AutoSave failed for " + accountNumber + " goal " + goalName + ": " + ex.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in AutoSave processor: " + e.getMessage());
        }
    }

    // Fetch the current balance for the given account number
    public double getBalance(String accountNumber) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT balance FROM customers WHERE account_number = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("balance");
            } else {
                throw new IllegalArgumentException("Account not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database error: " + e.getMessage());
        }
    }

    // Deposit money into the account
    public void deposit(String accountNumber, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero.");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE customers SET balance = balance + ? WHERE account_number = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setDouble(1, amount);
            stmt.setString(2, accountNumber);
            stmt.executeUpdate();

            // Directly record the transaction in the database
            String transactionQuery = "INSERT INTO transactions (account_number, description, amount, date) VALUES (?, ?, ?, ?)";
            PreparedStatement transactionStmt = conn.prepareStatement(transactionQuery);
            transactionStmt.setString(1, accountNumber);
            transactionStmt.setString(2, "Deposit");
            transactionStmt.setDouble(3, amount);
            transactionStmt.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
            transactionStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database error: " + e.getMessage());
        }
    }

    // Withdraw money from the account
    public void withdraw(String accountNumber, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero.");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String checkQuery = "SELECT balance FROM customers WHERE account_number = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, accountNumber);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                double currentBalance = rs.getDouble("balance");

                if (currentBalance >= amount) {
                    String updateQuery = "UPDATE customers SET balance = balance - ? WHERE account_number = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                    updateStmt.setDouble(1, amount);
                    updateStmt.setString(2, accountNumber);
                    updateStmt.executeUpdate();

                    // Directly record the transaction in the database
                    String transactionQuery = "INSERT INTO transactions (account_number, description, amount, date) VALUES (?, ?, ?, ?)";
                    PreparedStatement transactionStmt = conn.prepareStatement(transactionQuery);
                    transactionStmt.setString(1, accountNumber);
                    transactionStmt.setString(2, "Withdrawal");
                    transactionStmt.setDouble(3, -amount);
                    transactionStmt.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
                    transactionStmt.executeUpdate();
                } else {
                    throw new IllegalArgumentException("Insufficient balance.");
                }
            } else {
                throw new IllegalArgumentException("Account not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database error: " + e.getMessage());
        }
    }

    public void transfer(String accountNumber, String recipient, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero.");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Deduct from sender
            String deductQuery = "UPDATE customers SET balance = balance - ? WHERE account_number = ?";
            PreparedStatement deductStmt = conn.prepareStatement(deductQuery);
            deductStmt.setDouble(1, amount);
            deductStmt.setString(2, accountNumber);
            deductStmt.executeUpdate();

            // Add to recipient
            String addQuery = "UPDATE customers SET balance = balance + ? WHERE account_number = ?";
            PreparedStatement addStmt = conn.prepareStatement(addQuery);
            addStmt.setDouble(1, amount);
            addStmt.setString(2, recipient);
            addStmt.executeUpdate();

            // Directly record the transactions in the database
            String transactionQuery = "INSERT INTO transactions (account_number, description, amount, date) VALUES (?, ?, ?, ?)";
            PreparedStatement transactionStmt = conn.prepareStatement(transactionQuery);

            // Record sender transaction
            transactionStmt.setString(1, accountNumber);
            transactionStmt.setString(2, "Transfer to " + recipient);
            transactionStmt.setDouble(3, -amount);
            transactionStmt.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
            transactionStmt.executeUpdate();

            // Record recipient transaction
            transactionStmt.setString(1, recipient);
            transactionStmt.setString(2, "Transfer from " + accountNumber);
            transactionStmt.setDouble(3, amount);
            transactionStmt.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
            transactionStmt.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database error: " + e.getMessage());
        }
    }

    public String getTransactionHistory(String accountNumber) {
        StringBuilder history = new StringBuilder();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT date, description, amount FROM transactions WHERE account_number = ? ORDER BY date DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String date = rs.getString("date");
                String description = rs.getString("description");
                double amount = rs.getDouble("amount");
                history.append(String.format("%s - %s: $%.2f\n", date, description, amount));
            }
        } catch (Exception e) {
            history.append("Error fetching transaction history: ").append(e.getMessage());
        }
        return history.toString();
    }

    public void createAccount(String accountNumber, String name, double balance) {
        throw new UnsupportedOperationException("Unimplemented method 'createAccount'");
    }

    public void showBalance(String accountNumber) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT balance FROM customers WHERE account_number = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double balance = rs.getDouble("balance");
                System.out.println("Current Balance: " + balance);
            } else {
                System.out.println("Account not found.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching balance: " + e.getMessage());
        }
    }

    // Apply for a loan
    public void applyForLoan(String accountNumber, String loanType, String interestRate, String loanTermMonths, double amount) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO loan_requests (account_number, loan_type, interest_rate, loan_term_months, loan_amount, status) VALUES (?, ?, ?, ?, ?, 'Pending')";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, accountNumber);
            stmt.setString(2, loanType);
            stmt.setString(3, interestRate.replace("%", "")); // store as number
            stmt.setString(4, loanTermMonths.replace("months", "")); // store as number
            stmt.setDouble(5, amount);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Database error: " + ex.getMessage());
        }
    }

    public void requestLoan(String accountNumber, double loanAmount, double interestRate) {
        
        throw new UnsupportedOperationException("Unimplemented method 'requestLoan'");
    }

    public String generateStatement(String accountNumber) {
        
        throw new UnsupportedOperationException("Unimplemented method 'generateStatement'");
    }

    public void financialAdvisor(String accountNumber) {
       
        throw new UnsupportedOperationException("Unimplemented method 'financialAdvisor'");
    }

    // Fetch available loan options
    public Object[][] getLoanOptions() {
        return new Object[][] {
            {"Personal Loan", "12%", "5 years"},
            {"Home Loan", "8%", "20 years"},
            {"Car Loan", "10%", "7 years"},
            {"Education Loan", "9%", "10 years"},
            {"Business Loan", "15%", "10 years"}
        };
    }

    public void recordTransaction(String accountNumber, String description, double amount) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("Recording transaction for account: " + accountNumber + ", Description: " + description + ", Amount: " + amount);
            String query = "INSERT INTO transactions (account_number, description, amount) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, accountNumber);
            stmt.setString(2, description);
            stmt.setDouble(3, amount);
            stmt.executeUpdate();
            System.out.println("Transaction recorded successfully.");
        } catch (SQLException e) {
            System.err.println("Error recording transaction: " + e.getMessage());
            throw new RuntimeException("Error recording transaction: " + e.getMessage());
        }
    }

    /**
     * Add points to customer's financial-hub points. When points reach or exceed 100,
     * award a reward (deposit a small bonus) and increase the customer's loan limit by 1000.
     * This method is safe to call repeatedly; it will create the reward_points column if missing.
     */
    public void addFinancialHubPoints(String accountNumber, int pointsToAdd) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Ensure reward_points column exists (best-effort)
            try {
                PreparedStatement check = conn.prepareStatement("SELECT reward_points FROM customers WHERE account_number = ?");
                check.setString(1, accountNumber);
                check.executeQuery();
            } catch (SQLException colEx) {
                // Try to add the column if it doesn't exist
                try (PreparedStatement alter = conn.prepareStatement("ALTER TABLE customers ADD COLUMN reward_points INT DEFAULT 0")) {
                    alter.executeUpdate();
                } catch (SQLException ignore) {
                    // If ALTER fails, continue; subsequent queries may fail and will be handled below
                }
            }

            // Add points
            PreparedStatement add = conn.prepareStatement("UPDATE customers SET reward_points = COALESCE(reward_points,0) + ? WHERE account_number = ?");
            add.setInt(1, pointsToAdd);
            add.setString(2, accountNumber);
            add.executeUpdate();

            // Read back new points
            int points = 0;
            try (PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(reward_points,0) AS pts FROM customers WHERE account_number = ?")) {
                ps.setString(1, accountNumber);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) points = rs.getInt("pts");
            }

            // When reaching threshold, give reward and increase loan limit
            final int THRESHOLD = 100;
            if (points >= THRESHOLD) {
                // Subtract threshold points (keep remainder)
                try (PreparedStatement dec = conn.prepareStatement("UPDATE customers SET reward_points = reward_points - ? WHERE account_number = ?")) {
                    dec.setInt(1, THRESHOLD);
                    dec.setString(2, accountNumber);
                    dec.executeUpdate();
                }

                // Give monetary reward (e.g., $50)
                double rewardAmount = 50.0;
                try (PreparedStatement updBal = conn.prepareStatement("UPDATE customers SET balance = balance + ? WHERE account_number = ?")) {
                    updBal.setDouble(1, rewardAmount);
                    updBal.setString(2, accountNumber);
                    updBal.executeUpdate();
                }

                // Record reward transaction
                try (PreparedStatement rec = conn.prepareStatement("INSERT INTO transactions (account_number, description, amount, date) VALUES (?, ?, ?, ?)")) {
                    rec.setString(1, accountNumber);
                    rec.setString(2, "Financial Hub Reward");
                    rec.setDouble(3, rewardAmount);
                    rec.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
                    rec.executeUpdate();
                }

                // Increase loan limit by 1000 (add column if necessary)
                try {
                    PreparedStatement checkLoan = conn.prepareStatement("SELECT loan_limit FROM customers WHERE account_number = ?");
                    checkLoan.setString(1, accountNumber);
                    checkLoan.executeQuery();
                } catch (SQLException ex) {
                    try (PreparedStatement alter = conn.prepareStatement("ALTER TABLE customers ADD COLUMN loan_limit DECIMAL(10,2) DEFAULT 50000.00")) {
                        alter.executeUpdate();
                    } catch (SQLException ignore) {}
                }

                try (PreparedStatement inc = conn.prepareStatement("UPDATE customers SET loan_limit = COALESCE(loan_limit,50000) + 1000 WHERE account_number = ?")) {
                    inc.setString(1, accountNumber);
                    inc.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating financial hub points: " + e.getMessage());
        }
    }

    public int getRewardPoints(String accountNumber) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(reward_points,0) AS pts FROM customers WHERE account_number = ?")) {
                ps.setString(1, accountNumber);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return rs.getInt("pts");
            }
        } catch (SQLException e) {
            // swallow and return 0
        }
        return 0;
    }

    public void createSavingsGoal(String accountNumber, String goalName, double targetAmount, double autoSaveAmount) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO savings_goals (account_number, goal_name, target_amount, current_amount,auto_save_amount) VALUES (?, ?, ?, 0,?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, accountNumber);
            stmt.setString(2, goalName);
            stmt.setDouble(3, targetAmount);
            stmt.setDouble(4, autoSaveAmount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating savings goal: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> viewActiveGoals(String accountNumber) {
        List<Map<String, Object>> goals = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT goal_name, target_amount, current_amount FROM savings_goals WHERE account_number = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> goal = new HashMap<>();
                goal.put("goal_name", rs.getString("goal_name"));
                goal.put("target_amount", rs.getDouble("target_amount"));
                goal.put("current_amount", rs.getDouble("current_amount"));
                goals.add(goal);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching active goals: " + e.getMessage());
        }
        return goals;
    }

    public void contributeToGoal(String accountNumber, String goalName, double amount) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Verify account existence and balance
            String checkAccountQuery = "SELECT balance FROM customers WHERE account_number = ?";
            double currentBalance;
            try (PreparedStatement checkAccountStmt = conn.prepareStatement(checkAccountQuery)) {
                checkAccountStmt.setString(1, accountNumber);
                ResultSet rs = checkAccountStmt.executeQuery();

                if (rs.next()) {
                    currentBalance = rs.getDouble("balance");
                    if (currentBalance < amount) {
                        conn.rollback();
                        throw new RuntimeException("Insufficient balance.");
                    }
                } else {
                    conn.rollback();
                    throw new RuntimeException("Account not found.");
                }
            }

            // Deduct from account balance
            String updateBalanceQuery = "UPDATE customers SET balance = balance - ? WHERE account_number = ?";
            try (PreparedStatement updateBalanceStmt = conn.prepareStatement(updateBalanceQuery)) {
                updateBalanceStmt.setDouble(1, amount);
                updateBalanceStmt.setString(2, accountNumber);
                updateBalanceStmt.executeUpdate();
            }

            // Update savings goal
            String updateGoalQuery = "UPDATE savings_goals SET current_amount = current_amount + ? WHERE account_number = ? AND goal_name = ?";
            try (PreparedStatement updateGoalStmt = conn.prepareStatement(updateGoalQuery)) {
                updateGoalStmt.setDouble(1, amount);
                updateGoalStmt.setString(2, accountNumber);
                updateGoalStmt.setString(3, goalName);

                int rowsAffected = updateGoalStmt.executeUpdate();
                if (rowsAffected == 0) {
                    conn.rollback();
                    throw new RuntimeException("Savings goal not found.");
                }
            }

            // Record transaction
            String insertTransactionQuery = "INSERT INTO transactions (account_number, amount, description, date) VALUES (?, ?, ?, NOW())";
            try (PreparedStatement insertTransactionStmt = conn.prepareStatement(insertTransactionQuery)) {
                insertTransactionStmt.setString(1, accountNumber);
                insertTransactionStmt.setDouble(2, -amount); // Negative for deduction
                insertTransactionStmt.setString(3, "Auto-save contribution to: " + goalName);
                int txRows = insertTransactionStmt.executeUpdate();
                System.out.println("Transaction recorded for auto-save: " + accountNumber + ", amount: -" + amount + ", rows: " + txRows);
            }

            conn.commit();
            System.out.println("Auto-save completed for account: " + accountNumber + ", goal: " + goalName + ", amount: " + amount);
        } catch (SQLException e) {
            System.err.println("Error contributing to savings goal: " + e.getMessage());
            throw new RuntimeException("Error contributing to savings goal: " + e.getMessage());
        }
    }


    public Map<String, Double> viewGoalProgress(String accountNumber, String goalName) {
        Map<String, Double> progress = new HashMap<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT target_amount, current_amount FROM savings_goals WHERE account_number = ? AND goal_name = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, accountNumber);
            stmt.setString(2, goalName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                progress.put("target_amount", rs.getDouble("target_amount"));
                progress.put("current_amount", rs.getDouble("current_amount"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error viewing goal progress: " + e.getMessage());
        }
        return progress;
    }

    public void addTransaction(String accountNumber, String description, double amount) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO transactions (account_number, description, amount) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, accountNumber);
            stmt.setString(2, description);
            stmt.setDouble(3, amount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding transaction: " + e.getMessage());
        }
    }

    public void manageNotifications(String accountNumber, String preference) {
        // Logic to update notification preferences in the database
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE accounts SET notification_preference = ? WHERE account_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, preference);
                stmt.setString(2, accountNumber);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error managing notifications: " + e.getMessage());
        }
    }

    public void updateSecuritySettings(String accountNumber, String securityOption) {
        // Logic to update security settings in the database
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE accounts SET security_settings = ? WHERE account_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, securityOption);
                stmt.setString(2, accountNumber);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating security settings: " + e.getMessage());
        }
    }

    public void updateAppPreferences(String accountNumber, String theme, String language) {
        // Logic to update app preferences in the database
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE accounts SET theme = ?, language = ? WHERE account_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, theme);
                stmt.setString(2, language);
                stmt.setString(3, accountNumber);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating app preferences: " + e.getMessage());
        }
    }

    public void linkAccountService(String accountNumber, String service) {
        // Logic to link an external service to the user's account
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO linked_services (account_number, service_name) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, accountNumber);
                stmt.setString(2, service);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error linking account service: " + e.getMessage());
        }
    }

    public void updateAccessibilitySettings(String accountNumber, String accessibilityOption) {
        // Logic to update accessibility settings in the database
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE accounts SET accessibility_settings = ? WHERE account_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, accessibilityOption);
                stmt.setString(2, accountNumber);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating accessibility settings: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> getCustomerLoans(String accountNumber) {
        List<Map<String, Object>> loans = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT id AS loan_id, loan_amount - COALESCE(SUM(repayment_amount), 0) AS remaining_balance FROM loan_requests LEFT JOIN repayments ON loan_requests.id = repayments.loan_id WHERE account_number = ? AND status = 'Approved' GROUP BY loan_requests.id, loan_requests.loan_amount";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, accountNumber);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    Map<String, Object> loan = new HashMap<>();
                    loan.put("loan_id", rs.getString("loan_id"));
                    loan.put("remaining_balance", rs.getDouble("remaining_balance"));
                    loans.add(loan);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching customer loans: " + e.getMessage());
        }
        return loans;
    }

    public List<Map<String, Object>> getApprovedCustomerLoans(String accountNumber) {
        List<Map<String, Object>> loans = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT id, loan_type, loan_amount, remaining_balance FROM loan_requests WHERE account_number = ? AND status = 'Approved'";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, accountNumber);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    Map<String, Object> loan = new HashMap<>();
                    loan.put("id", rs.getInt("id"));
                    loan.put("loan_type", rs.getString("loan_type"));
                    loan.put("loan_amount", rs.getDouble("loan_amount"));
                    loan.put("remaining_balance", rs.getDouble("remaining_balance"));
                    loans.add(loan);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching approved customer loans: " + e.getMessage());
        }
        return loans;
    }

    // Repay a loan and update the customer's balance
    public void repayLoan(String accountNumber, int loanId, double repaymentAmount) {
        if (repaymentAmount <= 0) {
            throw new IllegalArgumentException("Repayment amount must be greater than zero.");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Check if the loan exists
            String checkLoanQuery = "SELECT loan_amount FROM loan_requests WHERE id = ? AND account_number = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkLoanQuery)) {
                checkStmt.setInt(1, loanId);
                checkStmt.setString(2, accountNumber);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    // Insert repayment into the repayments table
                    String insertRepaymentQuery = "INSERT INTO repayments (loan_id, repayment_amount, date) VALUES (?, ?, NOW())";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertRepaymentQuery)) {
                        insertStmt.setInt(1, loanId);
                        insertStmt.setDouble(2, repaymentAmount);
                        insertStmt.executeUpdate();
                    }

                    // Commit the transaction
                    conn.commit();
                } else {
                    throw new IllegalArgumentException("Loan not found.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error processing loan repayment: " + e.getMessage());
        }
    }

    public void updateProfile(String accountNumber, String newUsername, String newEmail) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE customers SET username = COALESCE(?, username), email = COALESCE(?, email) WHERE account_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, newUsername);
                stmt.setString(2, newEmail);
                stmt.setString(3, accountNumber);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating profile: " + e.getMessage());
        }
    }

    public void changePassword(String accountNumber, String newPassword) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE customers SET password = ? WHERE account_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, newPassword);
                stmt.setString(2, accountNumber);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error changing password: " + e.getMessage());
        }
    }

    public void setAutoSaveFrequency(String accountNumber, String autoSaveGoal, String frequency,
            double autoSaveAmount) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE savings_goals SET auto_save_frequency = ?, auto_save_amount = ? WHERE account_number = ? AND goal_name = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, frequency);
            stmt.setDouble(2, autoSaveAmount);
            stmt.setString(3, accountNumber);
            stmt.setString(4, autoSaveGoal);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error setting auto-save frequency and amount: " + e.getMessage());
        }
    }

    /**
     * Get the number of quizzes the customer has completed today.
     * Returns 0-2+ (resets daily at midnight).
     */
    public int getDailyQuizzesTaken(String accountNumber) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Ensure columns exist (best-effort)
            try {
                PreparedStatement check = conn.prepareStatement("SELECT daily_quiz_count, daily_quiz_date FROM customers WHERE account_number = ? LIMIT 1");
                check.setString(1, accountNumber);
                check.executeQuery();
            } catch (SQLException ex) {
                try (PreparedStatement alter1 = conn.prepareStatement("ALTER TABLE customers ADD COLUMN daily_quiz_count INT DEFAULT 0")) {
                    alter1.executeUpdate();
                } catch (SQLException ignore) {}
                try (PreparedStatement alter2 = conn.prepareStatement("ALTER TABLE customers ADD COLUMN daily_quiz_date DATE NULL")) {
                    alter2.executeUpdate();
                } catch (SQLException ignore) {}
            }

            java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
            int count = 0;

            try (PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(daily_quiz_count, 0) AS cnt, daily_quiz_date FROM customers WHERE account_number = ?")) {
                ps.setString(1, accountNumber);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    java.sql.Date lastDate = rs.getDate("daily_quiz_date");
                    count = rs.getInt("cnt");

                    // If date is not today, reset count
                    if (lastDate == null || !lastDate.equals(today)) {
                        try (PreparedStatement reset = conn.prepareStatement("UPDATE customers SET daily_quiz_count = 0, daily_quiz_date = ? WHERE account_number = ?")) {
                            reset.setDate(1, today);
                            reset.setString(2, accountNumber);
                            reset.executeUpdate();
                        } catch (SQLException ignore) {}
                        count = 0;
                    }
                }
            }

            return count;
        } catch (SQLException e) {
            System.err.println("Error getting daily quizzes taken: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Consume one daily quiz attempt. Returns true if a quiz slot was available and consumed,
     * false if the customer has already taken 2 quizzes today.
     */
    public synchronized boolean consumeDailyQuizAttempt(String accountNumber) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Ensure columns exist (best-effort)
            try {
                PreparedStatement check = conn.prepareStatement("SELECT daily_quiz_count, daily_quiz_date FROM customers WHERE account_number = ? LIMIT 1");
                check.setString(1, accountNumber);
                check.executeQuery();
            } catch (SQLException ex) {
                try (PreparedStatement alter1 = conn.prepareStatement("ALTER TABLE customers ADD COLUMN daily_quiz_count INT DEFAULT 0")) {
                    alter1.executeUpdate();
                } catch (SQLException ignore) {}
                try (PreparedStatement alter2 = conn.prepareStatement("ALTER TABLE customers ADD COLUMN daily_quiz_date DATE NULL")) {
                    alter2.executeUpdate();
                } catch (SQLException ignore) {}
            }

            java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
            final int MAX_QUIZZES_PER_DAY = 2;

            try (PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(daily_quiz_count, 0) AS cnt, daily_quiz_date FROM customers WHERE account_number = ?")) {
                ps.setString(1, accountNumber);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    java.sql.Date lastDate = rs.getDate("daily_quiz_date");
                    int count = rs.getInt("cnt");

                    // If date is not today, set count to 1
                    if (lastDate == null || !lastDate.equals(today)) {
                        try (PreparedStatement reset = conn.prepareStatement("UPDATE customers SET daily_quiz_count = 1, daily_quiz_date = ? WHERE account_number = ?")) {
                            reset.setDate(1, today);
                            reset.setString(2, accountNumber);
                            reset.executeUpdate();
                        } catch (SQLException ignore) {}
                        return true;
                    }

                    // If count < 2, increment and return true
                    if (count < MAX_QUIZZES_PER_DAY) {
                        try (PreparedStatement inc = conn.prepareStatement("UPDATE customers SET daily_quiz_count = daily_quiz_count + 1 WHERE account_number = ?")) {
                            inc.setString(1, accountNumber);
                            inc.executeUpdate();
                        }
                        return true;
                    }

                    // Limit reached
                    return false;
                }
            } catch (SQLException e) {
                System.err.println("Error consuming quiz attempt: " + e.getMessage());
                return true; // Allow attempt on error
            }

            return true;
        } catch (SQLException e) {
            System.err.println("Error in consumeDailyQuizAttempt: " + e.getMessage());
            return true; // Allow attempt on error (fail open)
        }
    }

    /**
     * Get the number of quizzes answered today for the given account.
     * This returns the count of unique questions answered today.
     */
    public int getDailyQuizzesAnswered(String accountNumber) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Ensure the table and columns exist (best-effort)
            try {
                PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM daily_quiz_answers WHERE account_number = ?");
                check.setString(1, accountNumber);
                check.executeQuery();
            } catch (SQLException ex) {
                try (PreparedStatement create = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS daily_quiz_answers (" +
                    "  id INT AUTO_INCREMENT PRIMARY KEY," +
                    "  account_number VARCHAR(50) NOT NULL," +
                    "  question TEXT NOT NULL," +
                    "  answer_date DATE NOT NULL," +
                    "  UNIQUE KEY unique_daily_answer (account_number, question, answer_date)" +
                    ")"
                )) {
                    create.executeUpdate();
                } catch (SQLException ignore) {}
            }

            java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) AS cnt FROM daily_quiz_answers WHERE account_number = ? AND answer_date = ?"
            )) {
                ps.setString(1, accountNumber);
                ps.setDate(2, today);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getInt("cnt");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting daily quizzes answered: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Check if the customer has already answered a specific question today.
     */
    public boolean hasAnsweredQuestionToday(String accountNumber, String question) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Ensure the table exists (best-effort)
            try {
                PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM daily_quiz_answers WHERE account_number = ?");
                check.setString(1, accountNumber);
                check.executeQuery();
            } catch (SQLException ex) {
                try (PreparedStatement create = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS daily_quiz_answers (" +
                    "  id INT AUTO_INCREMENT PRIMARY KEY," +
                    "  account_number VARCHAR(50) NOT NULL," +
                    "  question TEXT NOT NULL," +
                    "  answer_date DATE NOT NULL," +
                    "  UNIQUE KEY unique_daily_answer (account_number, question, answer_date)" +
                    ")"
                )) {
                    create.executeUpdate();
                } catch (SQLException ignore) {}
            }

            java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) AS cnt FROM daily_quiz_answers WHERE account_number = ? AND question = ? AND answer_date = ?"
            )) {
                ps.setString(1, accountNumber);
                ps.setString(2, question);
                ps.setDate(3, today);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getInt("cnt") > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking if question answered today: " + e.getMessage());
        }
        return false;
    }

    /**
     * Record that the customer answered a specific question today.
     */
    public void recordQuestionAnswered(String accountNumber, String question) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Ensure the table exists (best-effort)
            try {
                PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM daily_quiz_answers WHERE account_number = ?");
                check.setString(1, accountNumber);
                check.executeQuery();
            } catch (SQLException ex) {
                try (PreparedStatement create = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS daily_quiz_answers (" +
                    "  id INT AUTO_INCREMENT PRIMARY KEY," +
                    "  account_number VARCHAR(50) NOT NULL," +
                    "  question TEXT NOT NULL," +
                    "  answer_date DATE NOT NULL," +
                    "  UNIQUE KEY unique_daily_answer (account_number, question, answer_date)" +
                    ")"
                )) {
                    create.executeUpdate();
                } catch (SQLException ignore) {}
            }

            java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
            try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO daily_quiz_answers (account_number, question, answer_date) VALUES (?, ?, ?)"
            )) {
                ps.setString(1, accountNumber);
                ps.setString(2, question);
                ps.setDate(3, today);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error recording question answered: " + e.getMessage());
        }
    }
}
package GUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import java.awt.*;
import main.Bank;
import java.sql.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class AdminDashboard {

    private JFrame frame;
    private JPanel contentPanel; // Main content area for dynamic updates
    private JLabel notificationLabel; // Notification label for complaints

    public AdminDashboard(Bank bank) {
        initialize();
        startComplaintCheckingThread(); // Start checking for new complaints
    }

    private void initialize() {
        // Frame settings
        frame = new JFrame("");
        frame.setBackground(new Color(54, 57, 63));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        
        // Enable full screen mode
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(false); // Keep window decorations for minimize/close buttons
        
        // Center the frame on screen
        frame.setLocationRelativeTo(null);

        // Sidebar (Navigation Bar)
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(54, 57, 63));
        sidebar.setPreferredSize(new Dimension(200, 600));

        // Add a left navigation panel similar to MenuPage
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setPreferredSize(new Dimension(200, 600));
        leftPanel.setLayout(new BorderLayout());

        // Top menu panel for buttons
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(7, 1, 0, 0));
        menuPanel.setBackground(Color.WHITE);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Arrange all options vertically on the left side (without Logout)
        String[] leftMenuItems = {"Manage Accounts", "Approve / Reject Loans", "View All Transactions", "Generate Reports", "Financial Hub Management", "Smart Savings Management", "Customer Support"};
        
        Color[] buttonColors = {
            new Color(52, 152, 219),      // Manage Accounts - Blue
            new Color(220, 20, 60),       // Approve / Reject Loans - Crimson
            new Color(34, 139, 34),       // View All Transactions - Green
            new Color(255, 140, 0),       // Generate Reports - Orange
            new Color(0, 123, 255),       // Financial Hub Management - Bright Blue
            new Color(155, 89, 182),      // Smart Savings Management - Purple
            new Color(70, 130, 180)       // Customer Support - Steel Blue
        };

        JButton[] adminMenuButtons = new JButton[leftMenuItems.length];

        for (int i = 0; i < leftMenuItems.length; i++) {
            String item = leftMenuItems[i];
            JButton btn = createRoundedAdminButton(item, buttonColors[i]);
            adminMenuButtons[i] = btn;

            // Add action listeners for each button
            if (item.equals("Customer Support")) {
                btn.addActionListener(e -> {
                    resetAllAdminButtonColors(adminMenuButtons);
                    btn.setForeground(new Color(0, 123, 255));
                    String[] supportOptions = {"View Complaints", "Respond to Messages", "Reset Customer Passwords"};
                    String choice = (String) JOptionPane.showInputDialog(
                        frame,
                        "Select a support option:",
                        "Customer Support",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        supportOptions,
                        supportOptions[0]
                    );

                    if (choice != null) {
                        switch (choice) {
                            case "View Complaints":
                                // Logic to view complaints
                                try (Connection conn = DatabaseConnection.getConnection()) {
                                    String query = "SELECT id, account_number, complaint_description, response FROM complaints";
                                    PreparedStatement stmt = conn.prepareStatement(query);
                                    ResultSet rs = stmt.executeQuery();

                                    // Create table model
                                    String[] columnNames = {"ID", "Account Number", "Complaint", "Response"};
                                    DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

                                    // Populate table model with data from ResultSet
                                    while (rs.next()) {
                                        Object[] row = {
                                            rs.getInt("id"),
                                            rs.getString("account_number"),
                                            rs.getString("complaint_description"),
                                            rs.getString("response")
                                        };
                                        tableModel.addRow(row);
                                    }

                                    // Create table and display in a scroll pane
                                    JTable complaintsTable = new JTable(tableModel);
                                    JScrollPane scrollPane = new JScrollPane(complaintsTable);
                                    scrollPane.setPreferredSize(new Dimension(800, 400));

                                    // Add a button to respond to complaints
                                    JButton respondButton = new JButton("Respond to Complaint");
                                    respondButton.addActionListener(e1 -> {
                                        int selectedRow = complaintsTable.getSelectedRow();
                                        if (selectedRow != -1) {
                                            int complaintId = (int) tableModel.getValueAt(selectedRow, 0);
                                            String response = JOptionPane.showInputDialog(frame, "Enter your response:");

                                            if (response != null && !response.trim().isEmpty()) {
                                                try {
                                                    // Update the response and status in the database
                                                    String updateQuery = "UPDATE complaints SET response = ?, status = 'Received' WHERE id = ?";
                                                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                                                    updateStmt.setString(1, response);
                                                    updateStmt.setInt(2, complaintId);
                                                    updateStmt.executeUpdate();

                                                    JOptionPane.showMessageDialog(frame, "Response submitted successfully and status updated to 'Received'.", "Success", JOptionPane.INFORMATION_MESSAGE);

                                                    // Refresh the table
                                                    tableModel.setValueAt(response, selectedRow, 3);
                                                } catch (SQLException ex) {
                                                    JOptionPane.showMessageDialog(frame, "Error submitting response: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                                }
                                            } else {
                                                JOptionPane.showMessageDialog(frame, "Response cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                                            }
                                        } else {
                                            JOptionPane.showMessageDialog(frame, "Please select a complaint to respond to.", "Warning", JOptionPane.WARNING_MESSAGE);
                                        }
                                    });

                                    JPanel panel = new JPanel();
                                    panel.setLayout(new BorderLayout());
                                    panel.add(scrollPane, BorderLayout.CENTER);
                                    panel.add(respondButton, BorderLayout.SOUTH);

                                    JOptionPane.showMessageDialog(frame, panel, "Customer Complaints", JOptionPane.INFORMATION_MESSAGE);
                                } catch (SQLException ex) {
                                    JOptionPane.showMessageDialog(frame, "Error fetching complaints: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                break;
                            case "Respond to Messages":
                                // Logic to respond to messages
                                break;
                            case "Reset Customer Passwords":
                                // Logic to reset passwords
                                break;
                            case "Unlock/Block Accounts":
                                // Logic to unlock/block accounts
                                break;
                        }
                    }
                });
            }

            // Update Financial Hub Management options
            if (item.equals("Financial Hub Management")) {
                btn.addActionListener(e -> {
                    resetAllAdminButtonColors(adminMenuButtons);
                    btn.setForeground(new Color(0, 123, 255));
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        String query = "SELECT id, question_text, correct_answer FROM financial_hub_questions";
                        PreparedStatement stmt = conn.prepareStatement(query);
                        ResultSet rs = stmt.executeQuery();

                        // Create table model
                        String[] columnNames = {"ID", "Question", "Answer"};
                        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

                        // Populate table model with data from ResultSet
                        while (rs.next()) {
                            Object[] row = {
                                rs.getInt("id"),
                                rs.getString("question_text"),
                                rs.getString("correct_answer")
                            };
                            tableModel.addRow(row);
                        }

                        // Create table and display in a scroll pane
                        JTable quizTable = new JTable(tableModel);
                        JScrollPane scrollPane = new JScrollPane(quizTable);
                        scrollPane.setPreferredSize(new Dimension(800, 400));

                        int option = JOptionPane.showOptionDialog(
                            frame,
                            scrollPane,
                            "Manage Financial Hub Questions",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            new String[]{"Add Question", "Edit Selected", "Delete Selected", "Close"},
                            "Close"
                        );

                        switch (option) {
                            case 0: // Add Question
                                String newQuestion = JOptionPane.showInputDialog(frame, "Enter the new question:");
                                String newAnswer = JOptionPane.showInputDialog(frame, "Enter the correct answer (Yes/No):");
                                if (newQuestion != null && newAnswer != null && !newQuestion.trim().isEmpty() && !newAnswer.trim().isEmpty()) {
                                    String insertQuery = "INSERT INTO financial_hub_questions (question_text, correct_answer) VALUES (?, ?)";
                                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                                        insertStmt.setString(1, newQuestion);
                                        insertStmt.setString(2, newAnswer);
                                        insertStmt.executeUpdate();
                                        JOptionPane.showMessageDialog(frame, "Question added successfully.");
                                    }
                                }
                                break;
                            case 1: // Edit Selected
                                int selectedRow = quizTable.getSelectedRow();
                                if (selectedRow != -1) {
                                    int questionId = (int) tableModel.getValueAt(selectedRow, 0);
                                    String currentQuestion = (String) tableModel.getValueAt(selectedRow, 1);
                                    String currentAnswer = (String) tableModel.getValueAt(selectedRow, 2);
                                    String updatedQuestion = JOptionPane.showInputDialog(frame, "Edit the question:", currentQuestion);
                                    String updatedAnswer = JOptionPane.showInputDialog(frame, "Edit the answer:", currentAnswer);
                                    if (updatedQuestion != null && updatedAnswer != null && !updatedQuestion.trim().isEmpty() && !updatedAnswer.trim().isEmpty()) {
                                        String updateQuery = "UPDATE financial_hub_questions SET question_text = ?, correct_answer = ? WHERE id = ?";
                                        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                                            updateStmt.setString(1, updatedQuestion);
                                            updateStmt.setString(2, updatedAnswer);
                                            updateStmt.setInt(3, questionId);
                                            updateStmt.executeUpdate();
                                            JOptionPane.showMessageDialog(frame, "Question updated successfully.");
                                        }
                                    }
                                }
                                break;
                            case 2: // Delete Selected
                                int rowToDelete = quizTable.getSelectedRow();
                                if (rowToDelete != -1) {
                                    int questionId = (int) tableModel.getValueAt(rowToDelete, 0);
                                    String deleteQuery = "DELETE FROM financial_hub_questions WHERE id = ?";
                                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                                        deleteStmt.setInt(1, questionId);
                                        deleteStmt.executeUpdate();
                                        JOptionPane.showMessageDialog(frame, "Question deleted successfully.");
                                    }
                                }
                                break;
                            default:
                                // Close or no action
                                break;
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(frame, "Error managing quizzes: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }

            // Update Smart Savings Management options
            if (item.equals("Smart Savings Management")) {
                btn.addActionListener(e -> {
                    resetAllAdminButtonColors(adminMenuButtons);
                    btn.setForeground(new Color(0, 123, 255));
                    String[] options = {"View Goals & Progress"};
                    String choice = (String) JOptionPane.showInputDialog(
                        frame,
                        "Select an option:",
                        "Smart Savings Management",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        options,
                        options[0]
                    );

                    if (choice != null) {
                        switch (choice) {
                            case "View Goals & Progress":
                                // Logic to view customer goals
                                try (Connection conn = DatabaseConnection.getConnection()) {
                                    String query = "SELECT account_number, id, goal_name, target_amount, current_amount FROM savings_goals";
                                    PreparedStatement stmt = conn.prepareStatement(query);
                                    ResultSet rs = stmt.executeQuery();

                                    // Create table model
                                    String[] columnNames = {"id", "Goal Name", "Target Amount", "Current Amount", "account_number"};
                                    DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

                                    // Populate table model with data from ResultSet
                                    while (rs.next()) {
                                        Object[] row = {
                                            rs.getInt("id"),
                                            rs.getString("goal_name"),
                                            rs.getDouble("target_amount"),
                                            rs.getDouble("current_amount"),
                                            rs.getString("account_number")
                                        };
                                        tableModel.addRow(row);
                                    }

                                    // Create table and display in a scroll pane
                                    JTable goalsTable = new JTable(tableModel);
                                    JScrollPane scrollPane = new JScrollPane(goalsTable);
                                    scrollPane.setPreferredSize(new Dimension(800, 400));

                                    // Add a button to deposit funds
                                    JButton depositButton = new JButton("Deposit Funds");
                                    depositButton.addActionListener(e1 -> {
                                        int selectedRow = goalsTable.getSelectedRow();
                                        if (selectedRow != -1) {
                                            int goalId = (int) tableModel.getValueAt(selectedRow, 0);
                                            double targetAmount = (double) tableModel.getValueAt(selectedRow, 2);
                                            double currentAmount = (double) tableModel.getValueAt(selectedRow, 3);
                                            String accountNumber = (String) tableModel.getValueAt(selectedRow, 4);

                                            if (currentAmount >= targetAmount) {
                                                try {
                                                    // Update the account balance
                                                    String updateQuery = "UPDATE Customers SET balance = balance + ? WHERE account_number = ?";
                                                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                                                    updateStmt.setDouble(1, targetAmount);
                                                    updateStmt.setString(2, accountNumber);
                                                    updateStmt.executeUpdate();

                                                    // Remove the goal from savings_goals table
                                                    String deleteQuery = "DELETE FROM savings_goals WHERE id = ?";
                                                    PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
                                                    deleteStmt.setInt(1, goalId);
                                                    deleteStmt.executeUpdate();

                                                    JOptionPane.showMessageDialog(frame, "Funds deposited successfully and goal removed.", "Success", JOptionPane.INFORMATION_MESSAGE);
                                                } catch (SQLException ex) {
                                                    JOptionPane.showMessageDialog(frame, "Error depositing funds: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                                }
                                            } else {
                                                JOptionPane.showMessageDialog(frame, "Current amount is less than the target amount. Cannot deposit funds.", "Warning", JOptionPane.WARNING_MESSAGE);
                                            }
                                        } else {
                                            JOptionPane.showMessageDialog(frame, "Please select a goal to deposit funds.", "Warning", JOptionPane.WARNING_MESSAGE);
                                        }
                                    });

                                    JPanel panel = new JPanel();
                                    panel.setLayout(new BorderLayout());
                                    panel.add(scrollPane, BorderLayout.CENTER);
                                    panel.add(depositButton, BorderLayout.SOUTH);

                                    JOptionPane.showMessageDialog(frame, panel, "Customer Goals", JOptionPane.INFORMATION_MESSAGE);
                                } catch (SQLException ex) {
                                    JOptionPane.showMessageDialog(frame, "Error fetching customer goals: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                break;
                        }
                    }
                });
            }

            // Add functionality to Manage Accounts
            if (item.equals("Manage Accounts")) {
                btn.addActionListener(e -> {
                    resetAllAdminButtonColors(adminMenuButtons);
                    btn.setForeground(new Color(0, 123, 255));
                    String[] options = {"See a list of all accounts", "Permanently deactivate an account", "Edit account details", "View account transactions"};
                    String choice = (String) JOptionPane.showInputDialog(
                        frame,
                        "Select an option:",
                        "Manage Accounts",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        options,
                        options[0]
                    );
                    if (choice != null) {
                        switch (choice) {
                            case "See a list of all accounts":
                                // Logic to display all accounts
                                try (Connection conn = DatabaseConnection.getConnection()) {
                                    // Update the query to fetch data from the Customers table
                                    String query = "SELECT id, first_name, last_name, id_number, phone_number, email, account_type, account_number, balance FROM Customers";
                                    PreparedStatement stmt = conn.prepareStatement(query);
                                    ResultSet rs = stmt.executeQuery();

                                    // Create table model
                                    String[] columnNames = {"ID", "First Name", "Last Name", "ID Number", "Phone Number", "Email", "Account Type", "Account Number", "Balance"};
                                    DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

                                    // Populate table model with data from ResultSet
                                    while (rs.next()) {
                                        Object[] row = {
                                            rs.getInt("id"),
                                            rs.getString("first_name"),
                                            rs.getString("last_name"),
                                            rs.getString("id_number"),
                                            rs.getString("phone_number"),
                                            rs.getString("email"),
                                            rs.getString("account_type"),
                                            rs.getString("account_number"),
                                            rs.getDouble("balance")
                                        };
                                        tableModel.addRow(row);
                                    }

                                    // Create table and display in a scroll pane
                                    JTable customersTable = new JTable(tableModel);
                                    JScrollPane scrollPane = new JScrollPane(customersTable);
                                    scrollPane.setPreferredSize(new Dimension(800, 400));

                                    JOptionPane.showMessageDialog(frame, scrollPane, "All Customers", JOptionPane.INFORMATION_MESSAGE);
                                } catch (SQLException ex) {
                                    JOptionPane.showMessageDialog(frame, "Error fetching accounts: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                break;
                            case "Permanently deactivate an account":
                                // Logic to permanently deactivate an account
                                String accountNumber = JOptionPane.showInputDialog(frame, "Enter the account number to deactivate:", "Deactivate Account", JOptionPane.PLAIN_MESSAGE);
                                if (accountNumber != null && !accountNumber.trim().isEmpty()) {
                                    try (Connection conn = DatabaseConnection.getConnection()) {
                                        String query = "DELETE FROM Customers WHERE account_number = ?";
                                        PreparedStatement stmt = conn.prepareStatement(query);
                                        stmt.setString(1, accountNumber);
                                        int rowsAffected = stmt.executeUpdate();

                                        if (rowsAffected > 0) {
                                            JOptionPane.showMessageDialog(frame, "Account successfully deactivated.", "Success", JOptionPane.INFORMATION_MESSAGE);
                                        } else {
                                            JOptionPane.showMessageDialog(frame, "Account not found.", "Error", JOptionPane.ERROR_MESSAGE);
                                        }
                                    } catch (SQLException ex) {
                                        JOptionPane.showMessageDialog(frame, "Error deactivating account: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(frame, "Account number cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                break;
                            case "Edit account details":
                                // Logic to edit account details like phone number and email
                                String accountNumberEdit = JOptionPane.showInputDialog(frame, "Enter the account number to edit:", "Edit Account Details", JOptionPane.PLAIN_MESSAGE);
                                if (accountNumberEdit != null && !accountNumberEdit.trim().isEmpty()) {
                                    try (Connection conn = DatabaseConnection.getConnection()) {
                                        String newPhoneNumber = JOptionPane.showInputDialog(frame, "Enter the new phone number:", "Edit Account Details", JOptionPane.PLAIN_MESSAGE);
                                        String newEmail = JOptionPane.showInputDialog(frame, "Enter the new email:", "Edit Account Details", JOptionPane.PLAIN_MESSAGE);

                                        if (newPhoneNumber != null && newEmail != null && !newPhoneNumber.trim().isEmpty() && !newEmail.trim().isEmpty()) {
                                            String query = "UPDATE Customers SET phone_number = ?, email = ? WHERE account_number = ?";
                                            PreparedStatement stmt = conn.prepareStatement(query);
                                            stmt.setString(1, newPhoneNumber);
                                            stmt.setString(2, newEmail);
                                            stmt.setString(3, accountNumberEdit);
                                            int rowsAffected = stmt.executeUpdate();

                                            if (rowsAffected > 0) {
                                                JOptionPane.showMessageDialog(frame, "Account details updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                                            } else {
                                                JOptionPane.showMessageDialog(frame, "Account not found.", "Error", JOptionPane.ERROR_MESSAGE);
                                            }
                                        } else {
                                            JOptionPane.showMessageDialog(frame, "Phone number and email cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                                        }
                                    } catch (SQLException ex) {
                                        JOptionPane.showMessageDialog(frame, "Error updating account details: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(frame, "Account number cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                break;
                            case "View account transactions":
                                // Logic to view account transactions
                                String transactionAccountNumber = JOptionPane.showInputDialog(frame, "Enter the account number to view transactions:", "View Account Transactions", JOptionPane.PLAIN_MESSAGE);
                                if (transactionAccountNumber != null && !transactionAccountNumber.trim().isEmpty()) {
                                    try (Connection conn = DatabaseConnection.getConnection()) {
                                        String query = "SELECT date, description, amount FROM transactions WHERE account_number = ? ORDER BY date DESC";
                                        PreparedStatement stmt = conn.prepareStatement(query);
                                        stmt.setString(1, transactionAccountNumber);
                                        ResultSet rs = stmt.executeQuery();

                                        // Create table model
                                        String[] columnNames = {"Date", "Description", "Amount"};
                                        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

                                        // Populate table model with data from ResultSet
                                        while (rs.next()) {
                                            Object[] row = {
                                                rs.getTimestamp("date"),
                                                rs.getString("description"),
                                                rs.getDouble("amount")
                                            };
                                            tableModel.addRow(row);
                                        }

                                        // Create table and display in a scroll pane
                                        JTable transactionsTable = new JTable(tableModel);
                                        JScrollPane scrollPane = new JScrollPane(transactionsTable);
                                        scrollPane.setPreferredSize(new Dimension(800, 400));

                                        JOptionPane.showMessageDialog(frame, scrollPane, "Account Transactions", JOptionPane.INFORMATION_MESSAGE);
                                    } catch (SQLException ex) {
                                        JOptionPane.showMessageDialog(frame, "Error fetching transactions: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(frame, "Account number cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                break;
                        }
                    }
                });
            }

            // Add functionality to Approve / Reject Loans
            if (item.equals("Approve / Reject Loans")) {
                btn.addActionListener(e -> {
                    resetAllAdminButtonColors(adminMenuButtons);
                    btn.setForeground(new Color(0, 123, 255));
                    // Logic to approve or reject pending loans
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        String query = "SELECT id, account_number, loan_type, loan_amount, status FROM loan_requests WHERE status = 'Pending'";
                        PreparedStatement stmt = conn.prepareStatement(query);
                        ResultSet rs = stmt.executeQuery();

                        // Create table model
                        String[] columnNames = {"Loan ID", "Account Number", "Loan Type", "Loan Amount", "Status"};
                        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

                        // Populate table model with data from ResultSet
                        while (rs.next()) {
                            Object[] row = {
                                rs.getInt("id"),
                                rs.getString("account_number"),
                                rs.getString("loan_type"),
                                rs.getDouble("loan_amount"),
                                rs.getString("status")
                            };
                            tableModel.addRow(row);
                        }

                        // Create table and display in a scroll pane
                        JTable loansTable = new JTable(tableModel);
                        JScrollPane scrollPane = new JScrollPane(loansTable);
                        scrollPane.setPreferredSize(new Dimension(800, 400));

                        int option = JOptionPane.showConfirmDialog(frame, scrollPane, "Pending Loan Requests", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                        if (option == JOptionPane.OK_OPTION) {
                            String loanId = JOptionPane.showInputDialog(frame, "Enter the Loan ID to approve/reject:", "Loan Approval", JOptionPane.PLAIN_MESSAGE);
                            if (loanId != null && !loanId.trim().isEmpty()) {
                                String[] actions = {"Approve", "Reject"};
                                String action = (String) JOptionPane.showInputDialog(frame, "Select an action:", "Loan Approval", JOptionPane.PLAIN_MESSAGE, null, actions, actions[0]);

                                if (action != null) {
                                    // Get loan details BEFORE updating status
                                    String loanDetailsQuery = "SELECT account_number, loan_amount FROM loan_requests WHERE id = ?";
                                    PreparedStatement detailsStmt = conn.prepareStatement(loanDetailsQuery);
                                    detailsStmt.setInt(1, Integer.parseInt(loanId));
                                    ResultSet loanDetails = detailsStmt.executeQuery();
                                    
                                    String accountNumber = null;
                                    double loanAmount = 0;
                                    
                                    if (loanDetails.next()) {
                                        accountNumber = loanDetails.getString("account_number");
                                        loanAmount = loanDetails.getDouble("loan_amount");
                                    }
                                    
                                    // Update loan status
                                    String updateQuery = "UPDATE loan_requests SET status = ? WHERE id = ?";
                                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                                    updateStmt.setString(1, action);
                                    updateStmt.setInt(2, Integer.parseInt(loanId));
                                    int rowsAffected = updateStmt.executeUpdate();

                                    if (rowsAffected > 0) {
                                        // If approved, deposit the loan amount into customer account
                                        if (action.equals("Approve") && accountNumber != null && loanAmount > 0) {
                                            // Update customer balance
                                            String updateBalanceQuery = "UPDATE customers SET balance = balance + ? WHERE account_number = ?";
                                            PreparedStatement balanceStmt = conn.prepareStatement(updateBalanceQuery);
                                            balanceStmt.setDouble(1, loanAmount);
                                            balanceStmt.setString(2, accountNumber);
                                            balanceStmt.executeUpdate();
                                            
                                            // Record transaction
                                            String transactionQuery = "INSERT INTO transactions (account_number, date, description, amount) VALUES (?, NOW(), ?, ?)";
                                            PreparedStatement transactionStmt = conn.prepareStatement(transactionQuery);
                                            transactionStmt.setString(1, accountNumber);
                                            transactionStmt.setString(2, "Loan Approval - " + loanAmount);
                                            transactionStmt.setDouble(3, loanAmount);
                                            transactionStmt.executeUpdate();
                                            
                                            JOptionPane.showMessageDialog(frame, "Loan approved and $" + loanAmount + " has been deposited into account " + accountNumber + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
                                        } else if (action.equals("Reject")) {
                                            JOptionPane.showMessageDialog(frame, "Loan rejected successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                                        } else {
                                            JOptionPane.showMessageDialog(frame, "Loan " + action.toLowerCase() + "d successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                                        }
                                    } else {
                                        JOptionPane.showMessageDialog(frame, "Loan ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            } else {
                                JOptionPane.showMessageDialog(frame, "Loan ID cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(frame, "Error fetching or updating loans: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }

            // Add functionality to View All Transactions
            if (item.equals("View All Transactions")) {
                btn.addActionListener(e -> {
                    resetAllAdminButtonColors(adminMenuButtons);
                    btn.setForeground(new Color(0, 123, 255));
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        String query = "SELECT account_number, date, description, amount FROM transactions ORDER BY date DESC";
                        PreparedStatement stmt = conn.prepareStatement(query);
                        ResultSet rs = stmt.executeQuery();

                        // Create table model
                        String[] columnNames = {"Account Number", "Date", "Description", "Amount"};
                        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

                        // Populate table model with data from ResultSet
                        while (rs.next()) {
                            Object[] row = {
                                rs.getString("account_number"),
                                rs.getTimestamp("date"),
                                rs.getString("description"),
                                rs.getDouble("amount")
                            };
                            tableModel.addRow(row);
                        }

                        // Create table and display in a scroll pane
                        JTable transactionsTable = new JTable(tableModel);
                        JScrollPane scrollPane = new JScrollPane(transactionsTable);
                        scrollPane.setPreferredSize(new Dimension(800, 400));

                        JOptionPane.showMessageDialog(frame, scrollPane, "All Transactions", JOptionPane.INFORMATION_MESSAGE);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(frame, "Error fetching transactions: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }

            // Add functionality to Generate Reports
            if (item.equals("Generate Reports")) {
                btn.addActionListener(e -> {
                    resetAllAdminButtonColors(adminMenuButtons);
                    btn.setForeground(new Color(0, 123, 255));
                    String[] reportOptions = {
                        "Account Status Report",
                        "Transaction Summary Report",
                        "Bank Balance Summary",
                        "Loan Repayment Report"
                    };

                    String selectedReport = (String) JOptionPane.showInputDialog(
                        frame,
                        "Select a report to generate:",
                        "Generate Reports",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        reportOptions,
                        reportOptions[0]
                    );

                    if (selectedReport != null) {
                        switch (selectedReport) {
                            case "Account Status Report":
                                // Logic to generate Account Status Report
                                try (Connection conn = DatabaseConnection.getConnection()) {
                                    // Fix the SQL query string concatenation in "Account Status Report"
                                    String query = "SELECT account_number, account_type, balance, role, " +
                                                   "CASE WHEN balance > 0 THEN 'Active' " +
                                                   "WHEN balance = 0 THEN 'Inactive' " +
                                                   "ELSE 'Suspended' END AS status " +
                                                   "FROM Customers";
                                    PreparedStatement stmt = conn.prepareStatement(query);
                                    ResultSet rs = stmt.executeQuery();

                                    // Create table model
                                    String[] columnNames = {"Account Number", "Account Type", "Balance", "Role", "Status"};
                                    DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

                                    // Populate table model with data from ResultSet
                                    while (rs.next()) {
                                        Object[] row = {
                                            rs.getString("account_number"),
                                            rs.getString("account_type"),
                                            rs.getDouble("balance"),
                                            rs.getString("role"),
                                            rs.getString("status")
                                        };
                                        tableModel.addRow(row);
                                    }

                                    // Create table and display in a scroll pane
                                    JTable statusTable = new JTable(tableModel);
                                    JScrollPane scrollPane = new JScrollPane(statusTable);
                                    scrollPane.setPreferredSize(new Dimension(800, 400));

                                    // Create a panel with table and download button
                                    JPanel reportPanel = new JPanel(new BorderLayout());
                                    reportPanel.add(scrollPane, BorderLayout.CENTER);
                                    
                                    JButton downloadBtn = new JButton("Download as CSV");
                                    downloadBtn.addActionListener(ev -> downloadReportAsCSV(tableModel, "Account_Status_Report"));
                                    reportPanel.add(downloadBtn, BorderLayout.SOUTH);

                                    JOptionPane.showMessageDialog(frame, reportPanel, "Account Status Report", JOptionPane.INFORMATION_MESSAGE);
                                } catch (SQLException ex) {
                                    JOptionPane.showMessageDialog(frame, "Error generating report: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                break;
                            case "Transaction Summary Report":
                                // Logic to generate transaction summary report
                                try (Connection conn = DatabaseConnection.getConnection()) {
                                    // Prompt admin to select a customer
                                    String accountNumber = JOptionPane.showInputDialog(frame, "Enter Account Number:", "Transaction Summary Report", JOptionPane.PLAIN_MESSAGE);
                                    
                                    if (accountNumber == null || accountNumber.trim().isEmpty()) {
                                        return;
                                    }
                                    
                                    // Show date picker dialog
                                    String[] dates = showDatePickerDialog("Select Date Range");
                                    if (dates == null) {
                                        return; // User cancelled
                                    }
                                    
                                    String startDate = dates[0];
                                    String endDate = dates[1];
                                    
                                    // Query to fetch transaction summary
                                    String query = "SELECT description,amount,date FROM transactions WHERE account_number = ? AND date BETWEEN ? AND ?";
                                    PreparedStatement stmt = conn.prepareStatement(query);
                                    stmt.setString(1, accountNumber);
                                    stmt.setString(2, startDate);
                                    stmt.setString(3, endDate);
                                    ResultSet rs = stmt.executeQuery();

                                    // Create table model
                                    String[] columnNames = {"description","Amount", "date"};
                                    DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

                                    // Populate table model with data from ResultSet
                                    while (rs.next()) {
                                        Object[] row = {
                                            rs.getString("description"),
                                            rs.getDouble("amount"),
                                            rs.getDate("date")
                                        };
                                        tableModel.addRow(row);
                                    }

                                    // Create table and display in a scroll pane
                                    JTable transactionsTable = new JTable(tableModel);
                                    JScrollPane scrollPane = new JScrollPane(transactionsTable);
                                    scrollPane.setPreferredSize(new Dimension(800, 400));

                                    // Create a panel with table and download button
                                    JPanel reportPanel = new JPanel(new BorderLayout());
                                    reportPanel.add(scrollPane, BorderLayout.CENTER);
                                    
                                    JButton downloadBtn = new JButton("Download as CSV");
                                    downloadBtn.addActionListener(ev -> downloadReportAsCSV(tableModel, "Transaction_Summary_Report"));
                                    reportPanel.add(downloadBtn, BorderLayout.SOUTH);

                                    JOptionPane.showMessageDialog(frame, reportPanel, "Transaction Summary Report", JOptionPane.INFORMATION_MESSAGE);
                                } catch (SQLException ex) {
                                    JOptionPane.showMessageDialog(frame, "Error fetching transaction summary: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                break;
                            case "Bank Balance Summary":
                                // Logic to calculate and display comprehensive bank summary
                                try (Connection conn = DatabaseConnection.getConnection()) {
                                    StringBuilder summary = new StringBuilder();
                                    summary.append("COMPREHENSIVE BANK SUMMARY REPORT\n");
                                    summary.append("=====================================\n\n");
                                    
                                    // 1. Total Number of Customers
                                    String customerCountQuery = "SELECT COUNT(*) AS total_customers FROM customers";
                                    PreparedStatement customerStmt = conn.prepareStatement(customerCountQuery);
                                    ResultSet customerRs = customerStmt.executeQuery();
                                    int totalCustomers = 0;
                                    if (customerRs.next()) {
                                        totalCustomers = customerRs.getInt("total_customers");
                                    }
                                    summary.append(String.format("Total Number of Customers: %d\n", totalCustomers));
                                    
                                    // 2. Total Number of Active Accounts
                                    String activeAccountsQuery = "SELECT COUNT(*) AS active_accounts FROM customers WHERE balance > 0";
                                    PreparedStatement activeStmt = conn.prepareStatement(activeAccountsQuery);
                                    ResultSet activeRs = activeStmt.executeQuery();
                                    int activeAccounts = 0;
                                    if (activeRs.next()) {
                                        activeAccounts = activeRs.getInt("active_accounts");
                                    }
                                    summary.append(String.format("Total Number of Active Accounts: %d\n", activeAccounts));
                                    
                                    // 3. Total Deposits
                                    String depositQuery = "SELECT SUM(amount) AS total_deposits FROM transactions WHERE description LIKE '%Deposit%'";
                                    PreparedStatement depositStmt = conn.prepareStatement(depositQuery);
                                    ResultSet depositRs = depositStmt.executeQuery();
                                    double totalDeposits = 0;
                                    if (depositRs.next()) {
                                        totalDeposits = depositRs.getDouble("total_deposits");
                                    }
                                    summary.append(String.format("Total Deposits: $%.2f\n", totalDeposits));
                                    
                                    // 4. Total Withdrawals
                                    String withdrawalQuery = "SELECT SUM(amount) AS total_withdrawals FROM transactions WHERE description LIKE '%Withdrawal%'";
                                    PreparedStatement withdrawalStmt = conn.prepareStatement(withdrawalQuery);
                                    ResultSet withdrawalRs = withdrawalStmt.executeQuery();
                                    double totalWithdrawals = 0;
                                    if (withdrawalRs.next()) {
                                        totalWithdrawals = withdrawalRs.getDouble("total_withdrawals");
                                    }
                                    summary.append(String.format("Total Withdrawals: $%.2f\n", totalWithdrawals));
                                    
                                    // 5. Net Bank Balance (Deposits - Withdrawals)
                                    double netBalance = totalDeposits - totalWithdrawals;
                                    summary.append(String.format("Net Bank Balance: $%.2f\n", netBalance));
                                    
                                    // 6. Number of Loans Issued
                                    String loansIssuedQuery = "SELECT COUNT(*) AS loans_issued FROM loan_requests";
                                    PreparedStatement loansIssuedStmt = conn.prepareStatement(loansIssuedQuery);
                                    ResultSet loansIssuedRs = loansIssuedStmt.executeQuery();
                                    int loansIssued = 0;
                                    if (loansIssuedRs.next()) {
                                        loansIssued = loansIssuedRs.getInt("loans_issued");
                                    }
                                    summary.append(String.format("Number of Loans Issued: %d\n", loansIssued));
                                    
                                    // 7. Number of Active Loans
                                    String activeLoansQuery = "SELECT COUNT(*) AS active_loans FROM loan_requests WHERE status = 'Approve'";
                                    PreparedStatement activeLoansStmt = conn.prepareStatement(activeLoansQuery);
                                    ResultSet activeLoansRs = activeLoansStmt.executeQuery();
                                    int activeLoans = 0;
                                    if (activeLoansRs.next()) {
                                        activeLoans = activeLoansRs.getInt("active_loans");
                                    }
                                    summary.append(String.format("Number of Active Loans: %d\n", activeLoans));
                                    
                                    // 8. Number of Completed Loans
                                    String completedLoansQuery = "SELECT COUNT(DISTINCT lr.id) AS completed_loans FROM loan_requests lr " +
                                                               "LEFT JOIN repayments r ON lr.id = r.loan_id " +
                                                               "WHERE lr.status = 'Approve' " +
                                                               "GROUP BY lr.id, lr.loan_amount, lr.interest_rate, lr.loan_term_months " +
                                                               "HAVING (lr.loan_amount + (lr.loan_amount * lr.interest_rate / 100 * lr.loan_term_months / 12)) <= COALESCE(SUM(r.repayment_amount), 0)";
                                    PreparedStatement completedLoansStmt = conn.prepareStatement(completedLoansQuery);
                                    ResultSet completedLoansRs = completedLoansStmt.executeQuery();
                                    int completedLoans = 0;
                                    while (completedLoansRs.next()) {
                                        completedLoans++;
                                    }
                                    summary.append(String.format("Number of Completed Loans: %d\n", completedLoans));
                                    
                                    // 9. Total Value of Outstanding Loans
                                    String outstandingLoansQuery = "SELECT lr.loan_amount, lr.interest_rate, lr.loan_term_months, " +
                                                                 "COALESCE(SUM(r.repayment_amount), 0) AS total_repaid " +
                                                                 "FROM loan_requests lr " +
                                                                 "LEFT JOIN repayments r ON lr.id = r.loan_id " +
                                                                 "WHERE lr.status = 'Approve' " +
                                                                 "GROUP BY lr.id, lr.loan_amount, lr.interest_rate, lr.loan_term_months";
                                    PreparedStatement outstandingStmt = conn.prepareStatement(outstandingLoansQuery);
                                    ResultSet outstandingRs = outstandingStmt.executeQuery();
                                    double totalOutstanding = 0;
                                    while (outstandingRs.next()) {
                                        double principal = outstandingRs.getDouble("loan_amount");
                                        double interestRate = outstandingRs.getDouble("interest_rate");
                                        int termMonths = outstandingRs.getInt("loan_term_months");
                                        double totalRepaid = outstandingRs.getDouble("total_repaid");
                                        double totalWithInterest = principal + (principal * interestRate / 100 * termMonths / 12);
                                        double remaining = totalWithInterest - totalRepaid;
                                        if (remaining > 0) {
                                            totalOutstanding += remaining;
                                        }
                                    }
                                    summary.append(String.format("Total Value of Outstanding Loans: $%.2f\n", totalOutstanding));
                                    
                                    // 10. Total Value of Repaid Loans
                                    String repaidLoansQuery = "SELECT SUM(r.repayment_amount) AS total_repaid FROM repayments r " +
                                                            "JOIN loan_requests lr ON r.loan_id = lr.id " +
                                                            "WHERE lr.status = 'Approve'";
                                    PreparedStatement repaidStmt = conn.prepareStatement(repaidLoansQuery);
                                    ResultSet repaidRs = repaidStmt.executeQuery();
                                    double totalRepaid = 0;
                                    if (repaidRs.next()) {
                                        totalRepaid = repaidRs.getDouble("total_repaid");
                                    }
                                    summary.append(String.format("Total Value of Repaid Loans: $%.2f\n", totalRepaid));
                                    
                                    summary.append("\nReport Generated: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

                                    // Create a panel with text and download button
                                    JPanel reportPanel = new JPanel(new BorderLayout());
                                    JTextArea textArea = new JTextArea(summary.toString());
                                    textArea.setEditable(false);
                                    textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                                    reportPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);
                                    
                                    JButton downloadBtn = new JButton("Download as TXT");
                                    downloadBtn.addActionListener(ev -> downloadTextReport(summary.toString(), "Comprehensive_Bank_Summary"));
                                    reportPanel.add(downloadBtn, BorderLayout.SOUTH);

                                    JOptionPane.showMessageDialog(frame, reportPanel, "Comprehensive Bank Summary", JOptionPane.INFORMATION_MESSAGE);
                                } catch (SQLException ex) {
                                    JOptionPane.showMessageDialog(frame, "Error fetching bank summary: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                break;
                            case "Loan Repayment Report":
                                String[] loanReportOptions = {
                                    "Individual Account",
                                    "All Accounts",
                                    "Back"
                                };
                                String loanReportChoice = (String) JOptionPane.showInputDialog(
                                    frame,
                                    "Select loan repayment report type:",
                                    "Loan Repayment Report",
                                    JOptionPane.PLAIN_MESSAGE,
                                    null,
                                    loanReportOptions,
                                    loanReportOptions[0]
                                );
                                if (loanReportChoice == null || loanReportChoice.equals("Back")) return;

                                try (Connection conn = DatabaseConnection.getConnection()) {
                                    StringBuilder report = new StringBuilder();
                                    if (loanReportChoice.equals("Individual Account")) {
                                        String accNum = JOptionPane.showInputDialog(frame, "Enter account number:");
                                        String query = "SELECT lr.id, lr.loan_type, lr.loan_amount, lr.interest_rate, lr.loan_term_months, " +
                                                       "COALESCE(SUM(r.repayment_amount), 0) AS total_repaid " +
                                                       "FROM loan_requests lr " +
                                                       "LEFT JOIN repayments r ON lr.id = r.loan_id " +
                                                       "WHERE lr.account_number = ? " +
                                                       "GROUP BY lr.id, lr.loan_type, lr.loan_amount, lr.interest_rate, lr.loan_term_months";
                                        PreparedStatement stmt = conn.prepareStatement(query);
                                        stmt.setString(1, accNum);
                                        ResultSet rs = stmt.executeQuery();
                                        report.append("Loan Repayment Report for Account: ").append(accNum).append("\n\n");
                                        while (rs.next()) {
                                            double principal = rs.getDouble("loan_amount");
                                            double interestRate = rs.getDouble("interest_rate");
                                            int termMonths = rs.getInt("loan_term_months");
                                            double totalRepaid = rs.getDouble("total_repaid");
                                            double totalInterest = principal * (interestRate / 100.0) * (termMonths / 12.0);
                                            double totalWithInterest = principal + totalInterest;
                                            double remaining = totalWithInterest - totalRepaid;
                                            report.append(String.format(
                                                "Loan ID: %d\nType: %s\nPrincipal: %.2f\nInterest Rate: %.2f%%\nTerm: %d months\nTotal (With Interest): %.2f\nTotal Repaid: %.2f\nRemaining Balance: %.2f\n\n",
                                                rs.getInt("id"), rs.getString("loan_type"), principal, interestRate, termMonths, totalWithInterest, totalRepaid, remaining
                                            ));
                                        }
                                    } else if (loanReportChoice.equals("All Accounts")) {
                                        String query = "SELECT lr.account_number, lr.id, lr.loan_type, lr.loan_amount, lr.interest_rate, lr.loan_term_months, " +
                                                       "COALESCE(SUM(r.repayment_amount), 0) AS total_repaid " +
                                                       "FROM loan_requests lr " +
                                                       "LEFT JOIN repayments r ON lr.id = r.loan_id " +
                                                       "GROUP BY lr.account_number, lr.id, lr.loan_type, lr.loan_amount, lr.interest_rate, lr.loan_term_months";
                                        PreparedStatement stmt = conn.prepareStatement(query);
                                        ResultSet rs = stmt.executeQuery();
                                        
                                        // Create table model for searchable table
                                        String[] columnNames = {"Account Number", "Loan ID", "Loan Type", "Principal", "Interest Rate (%)", "Term (Months)", "Total (With Interest)", "Total Repaid", "Remaining Balance"};
                                        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
                                        
                                        // Populate table model with data from ResultSet
                                        while (rs.next()) {
                                            double principal = rs.getDouble("loan_amount");
                                            double interestRate = rs.getDouble("interest_rate");
                                            int termMonths = rs.getInt("loan_term_months");
                                            double totalRepaid = rs.getDouble("total_repaid");
                                            double totalInterest = principal * (interestRate / 100.0) * (termMonths / 12.0);
                                            double totalWithInterest = principal + totalInterest;
                                            double remaining = totalWithInterest - totalRepaid;
                                            
                                            Object[] row = {
                                                rs.getString("account_number"),
                                                rs.getInt("id"),
                                                rs.getString("loan_type"),
                                                String.format("%.2f", principal),
                                                String.format("%.2f", interestRate),
                                                termMonths,
                                                String.format("%.2f", totalWithInterest),
                                                String.format("%.2f", totalRepaid),
                                                String.format("%.2f", remaining)
                                            };
                                            tableModel.addRow(row);
                                        }
                                        
                                        // Create searchable table with search bar
                                        JTable loanTable = new JTable(tableModel);
                                        JScrollPane scrollPane = new JScrollPane(loanTable);
                                        scrollPane.setPreferredSize(new Dimension(1000, 400));
                                        
                                        // Create search panel
                                        JPanel searchPanel = new JPanel(new BorderLayout());
                                        JLabel searchLabel = new JLabel("Search:");
                                        JTextField searchField = new JTextField();
                                        searchField.setColumns(20);
                                        
                                        // Add search functionality
                                        searchField.getDocument().addDocumentListener(new DocumentListener() {
                                            @Override
                                            public void insertUpdate(DocumentEvent e) {
                                                filterTable();
                                            }
                                            
                                            @Override
                                            public void removeUpdate(DocumentEvent e) {
                                                filterTable();
                                            }
                                            
                                            @Override
                                            public void changedUpdate(DocumentEvent e) {
                                                filterTable();
                                            }
                                            
                                            private void filterTable() {
                                                String searchText = searchField.getText().toLowerCase();
                                                javax.swing.table.TableRowSorter<DefaultTableModel> sorter = 
                                                    new javax.swing.table.TableRowSorter<>(tableModel);
                                                loanTable.setRowSorter(sorter);
                                                
                                                if (searchText.trim().isEmpty()) {
                                                    sorter.setRowFilter(null);
                                                } else {
                                                    sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + searchText));
                                                }
                                            }
                                        });
                                        
                                        JPanel searchInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                                        searchInputPanel.add(searchLabel);
                                        searchInputPanel.add(searchField);
                                        
                                        searchPanel.add(searchInputPanel, BorderLayout.NORTH);
                                        searchPanel.add(scrollPane, BorderLayout.CENTER);
                                        
                                        // Create a panel with search functionality and download button
                                        JPanel reportPanel = new JPanel(new BorderLayout());
                                        reportPanel.add(searchPanel, BorderLayout.CENTER);
                                        
                                        JButton downloadBtn = new JButton("Download as CSV");
                                        downloadBtn.addActionListener(ev -> downloadReportAsCSV(tableModel, "Loan_Repayment_Report_All_Accounts"));
                                        reportPanel.add(downloadBtn, BorderLayout.SOUTH);
                                        
                                    
                                        JOptionPane.showMessageDialog(frame, reportPanel, "Loan Repayment Report - All Accounts", JOptionPane.INFORMATION_MESSAGE);
                                        return; // Exit early for All Accounts to avoid text area creation
                                    }
                                    JTextArea txtArea = new JTextArea(report.toString());
                                    txtArea.setEditable(false);
                                    JScrollPane sp = new JScrollPane(txtArea);
                                    sp.setPreferredSize(new Dimension(800, 400));
                                    
                                    // Create a panel with text area and download button
                                    JPanel reportPanel = new JPanel(new BorderLayout());
                                    reportPanel.add(sp, BorderLayout.CENTER);
                                    
                                    JButton downloadBtn = new JButton("Download as TXT");
                                    downloadBtn.addActionListener(ev -> downloadTextReport(report.toString(), "Loan_Repayment_Report"));
                                    reportPanel.add(downloadBtn, BorderLayout.SOUTH);
                                    
                                    JOptionPane.showMessageDialog(frame, reportPanel, "Loan Repayment Report", JOptionPane.INFORMATION_MESSAGE);
                                } catch (SQLException ex) {
                                    JOptionPane.showMessageDialog(frame, "Error generating loan repayment report: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                break;
                            case "Defaulted Loans Report":
                                // Logic to display all unpaid loans
                                try (Connection conn = DatabaseConnection.getConnection()) {
                                    String query = "SELECT account_number, loan_type, loan_amount, due_date FROM loan_requests WHERE status = 'Unpaid'";
                                    PreparedStatement stmt = conn.prepareStatement(query);
                                    ResultSet rs = stmt.executeQuery();

                                    // Create table model
                                    String[] columnNames = {"Account Number", "Loan Type", "Loan Amount", "Due Date"};
                                    DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

                                    // Populate table model with data from ResultSet
                                    while (rs.next()) {
                                        Object[] row = {
                                            rs.getString("account_number"),
                                            rs.getString("loan_type"),
                                            rs.getDouble("loan_amount"),
                                            rs.getDate("due_date")
                                        };
                                        tableModel.addRow(row);
                                    }

                                    // Create table and display in a scroll pane
                                    JTable loansTable = new JTable(tableModel);
                                    JScrollPane scrollPane = new JScrollPane(loansTable);
                                    scrollPane.setPreferredSize(new Dimension(800, 400));

                                    // Create a panel with table and download button
                                    JPanel reportPanel = new JPanel(new BorderLayout());
                                    reportPanel.add(scrollPane, BorderLayout.CENTER);
                                    
                                    JButton downloadBtn = new JButton("Download as CSV");
                                    downloadBtn.addActionListener(ev -> downloadReportAsCSV(tableModel, "Defaulted_Loans_Report"));
                                    reportPanel.add(downloadBtn, BorderLayout.SOUTH);

                                    JOptionPane.showMessageDialog(frame, reportPanel, "Defaulted Loans Report", JOptionPane.INFORMATION_MESSAGE);
                                } catch (SQLException ex) {
                                    JOptionPane.showMessageDialog(frame, "Error fetching defaulted loans: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                break;
                            case "Admin Activity Log":
                                // Logic to display admin actions on loans
                                try (Connection conn = DatabaseConnection.getConnection()) {
                                    String query = "SELECT status, id FROM loan_requests WHERE status IN ('Approve', 'Reject')";
                                    PreparedStatement stmt = conn.prepareStatement(query);
                                    ResultSet rs = stmt.executeQuery();

                                    // Create table model
                                    String[] columnNames = {"user_id", "status", "id"};
                                    DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

                                    // Populate table model with data from ResultSet
                                    while (rs.next()) {
                                        Object[] row = {
                                            rs.getString("user_id"),
                                            rs.getString("status"),
                                            rs.getInt("id"),
                                        };
                                        tableModel.addRow(row);
                                    }

                                    // Create table and display in a scroll pane
                                    JTable actionsTable = new JTable(tableModel);
                                    JScrollPane scrollPane = new JScrollPane(actionsTable);
                                    scrollPane.setPreferredSize(new Dimension(800, 400));

                                    JOptionPane.showMessageDialog(frame, scrollPane, "Admin Activity Log", JOptionPane.INFORMATION_MESSAGE);
                                } catch (SQLException ex) {
                                    JOptionPane.showMessageDialog(frame, "Error fetching admin activity log: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                break;
                        }
                    }
                });
            }

            menuPanel.add(btn);
        }

        // Logout button panel at the bottom
        JPanel logoutPanel = new JPanel();
        logoutPanel.setLayout(new GridLayout(1, 1, 0, 0));
        logoutPanel.setBackground(Color.WHITE);
        logoutPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JButton btnLogout = new JButton("🚪 Logout") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Check if button is hovered
                boolean isHovered = Boolean.TRUE.equals(getClientProperty("isHovered"));
                
                // Draw rounded background
                if (isHovered || getModel().isPressed()) {
                    g2.setColor(new Color(230, 230, 230));
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                
                super.paintComponent(g);
            }
        };
        
        btnLogout.setBackground(Color.WHITE);
        btnLogout.setForeground(Color.BLACK);
        btnLogout.setFocusPainted(false);
        btnLogout.setFont(new Font("Segoe UI Symbol", Font.BOLD, 12));
        btnLogout.setOpaque(false);
        btnLogout.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        btnLogout.setHorizontalAlignment(SwingConstants.LEFT);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        btnLogout.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnLogout.putClientProperty("isHovered", true);
                btnLogout.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnLogout.putClientProperty("isHovered", false);
                btnLogout.repaint();
            }
        });
        
        btnLogout.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "You have been logged out.");
            frame.setVisible(false);
            new LoginPage();
        });
        logoutPanel.add(btnLogout);

        // Add menu panel and logout panel to left panel
        leftPanel.add(menuPanel, BorderLayout.NORTH);
        leftPanel.add(logoutPanel, BorderLayout.SOUTH);

        // Create notification panel at the top
        JPanel notificationPanel = new JPanel(new BorderLayout());
        notificationPanel.setBackground(new Color(240, 248, 255));
        notificationPanel.setBorder(BorderFactory.createLineBorder(new Color(0, 123, 255), 2));
        notificationPanel.setPreferredSize(new Dimension(0, 60));

        notificationLabel = new JLabel("System Ready - Monitoring for new complaints...");
        notificationLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        notificationLabel.setForeground(new Color(0, 123, 255));
        notificationLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        notificationPanel.add(notificationLabel, BorderLayout.CENTER);

        // Add mouse listener to notification label for clicking to view complaints
        notificationLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showComplaintsView();
            }
        });

        // Main content area (dashboard display)
        JPanel mainContentArea = new JPanel(new BorderLayout());
        
        // Create dashboard panels
        contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(2, 2, 10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create loan performance chart panel
        JPanel loanChartPanel = createLoanPerformanceChart();
        contentPanel.add(loanChartPanel);
        
    // Create financial hub engagement chart panel
    JPanel financialHubChartPanel = createFinancialHubEngagementChart();
    contentPanel.add(financialHubChartPanel);

    // Create Smart Savings Progress panel (radial / donut)
    JPanel smartSavingsPanel = createSmartSavingsProgressPanel();
    contentPanel.add(smartSavingsPanel);

    // Add summary stats panel
    JPanel summaryPanel = createSummaryStatsPanel();
    contentPanel.add(summaryPanel);

        // Add content panel to main content area
        mainContentArea.add(contentPanel, BorderLayout.CENTER);
        
        // Add notification panel at the bottom
        mainContentArea.add(notificationPanel, BorderLayout.SOUTH);

        // Add everything to the frame
        frame.add(leftPanel, BorderLayout.WEST);
        frame.add(mainContentArea, BorderLayout.CENTER);
    }

    public void show() {
        frame.setVisible(true);
    }

    // Utility method to download reports as CSV files
    private void downloadReportAsCSV(DefaultTableModel tableModel, String reportName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        
        // Set default filename with timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String timestamp = dateFormat.format(new Date());
        fileChooser.setSelectedFile(new File(reportName + "_" + timestamp + ".csv"));
        
        int userSelection = fileChooser.showSaveDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(fileToSave))) {
                // Write headers
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    writer.print(tableModel.getColumnName(i));
                    if (i < tableModel.getColumnCount() - 1) {
                        writer.print(",");
                    }
                }
                writer.println();
                
                // Write data rows
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        Object value = tableModel.getValueAt(i, j);
                        writer.print(value != null ? value.toString() : "");
                        if (j < tableModel.getColumnCount() - 1) {
                            writer.print(",");
                        }
                    }
                    writer.println();
                }
                
                JOptionPane.showMessageDialog(frame, 
                    "Report downloaded successfully to: " + fileToSave.getAbsolutePath(), 
                    "Download Complete", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, 
                    "Error saving file: " + ex.getMessage(), 
                    "Save Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Utility method to download text reports as TXT files
    private void downloadTextReport(String reportContent, String reportName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
        
        // Set default filename with timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String timestamp = dateFormat.format(new Date());
        fileChooser.setSelectedFile(new File(reportName + "_" + timestamp + ".txt"));
        
        int userSelection = fileChooser.showSaveDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(fileToSave))) {
                writer.print(reportContent);
                
                JOptionPane.showMessageDialog(frame, 
                    "Report downloaded successfully to: " + fileToSave.getAbsolutePath(), 
                    "Download Complete", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, 
                    "Error saving file: " + ex.getMessage(), 
                    "Save Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Utility method to show date picker dialog
    private String[] showDatePickerDialog(String title) {
        JDialog dialog = new JDialog(frame, title, true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Start Date
        JLabel startDateLabel = new JLabel("Start Date:");
        JSpinner startDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor startDateEditor = new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd");
        startDateSpinner.setEditor(startDateEditor);
        startDateSpinner.setValue(new Date());

        // End Date
        JLabel endDateLabel = new JLabel("End Date:");
        JSpinner endDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor endDateEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");
        endDateSpinner.setEditor(endDateEditor);
        endDateSpinner.setValue(new Date());

        mainPanel.add(startDateLabel);
        mainPanel.add(startDateSpinner);
        mainPanel.add(endDateLabel);
        mainPanel.add(endDateSpinner);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        final String[] result = new String[2];
        final boolean[] cancelled = {false};

        okButton.addActionListener(e -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            result[0] = dateFormat.format(startDateSpinner.getValue());
            result[1] = dateFormat.format(endDateSpinner.getValue());
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> {
            cancelled[0] = true;
            dialog.dispose();
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);

        return cancelled[0] ? null : result;
    }

    // Loan Performance Chart - Pie Chart
    private JPanel createLoanPerformanceChart() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Loan Performance Chart"));
        panel.setBackground(Color.WHITE);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT status, COUNT(*) as count FROM loan_requests GROUP BY status";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            List<String> statuses = new ArrayList<>();
            List<Integer> counts = new ArrayList<>();

            while (rs.next()) {
                statuses.add(rs.getString("status"));
                counts.add(rs.getInt("count"));
            }

            LoanPerformanceChart chart = new LoanPerformanceChart(statuses, counts);
            panel.add(chart, BorderLayout.CENTER);

        } catch (SQLException ex) {
            JLabel errorLabel = new JLabel("Error loading loan data: " + ex.getMessage());
            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(errorLabel, BorderLayout.CENTER);
        }

        return panel;
    }

    // Financial Hub Engagement Chart - Pie Chart
    private JPanel createFinancialHubEngagementChart() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Financial Hub Engagement"));
        panel.setBackground(Color.WHITE);

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get total number of customers
            String totalCustomersQuery = "SELECT COUNT(*) as total FROM Customers";
            PreparedStatement totalStmt = conn.prepareStatement(totalCustomersQuery);
            ResultSet totalRs = totalStmt.executeQuery();
            int totalCustomers = 0;
            if (totalRs.next()) {
                totalCustomers = totalRs.getInt("total");
            }

            // Get number of customers who have participated in financial hub (have transactions with financial hub bonus)
            String participatedQuery = "SELECT COUNT(DISTINCT account_number) as participated " +
                                     "FROM transactions " +
                                     "WHERE description LIKE '%Financial Hub%' OR description LIKE '%financial hub%'";
            PreparedStatement participatedStmt = conn.prepareStatement(participatedQuery);
            ResultSet participatedRs = participatedStmt.executeQuery();
            int participated = 0;
            if (participatedRs.next()) {
                participated = participatedRs.getInt("participated");
            }

            int notParticipated = Math.max(0, totalCustomers - participated);

            List<String> categories = new ArrayList<>();
            List<Integer> counts = new ArrayList<>();

            categories.add("Participated");
            counts.add(participated);
            categories.add("Not Participated");
            counts.add(notParticipated);

            FinancialHubEngagementChart chart = new FinancialHubEngagementChart(categories, counts);
            panel.add(chart, BorderLayout.CENTER);

        } catch (SQLException ex) {
            JLabel errorLabel = new JLabel("Error loading financial hub data: " + ex.getMessage());
            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(errorLabel, BorderLayout.CENTER);
        }

        return panel;
    }

    // Financial Hub Engagement Chart Component
    class FinancialHubEngagementChart extends JPanel {
        private List<String> categories;
        private List<Integer> counts;

        public FinancialHubEngagementChart(List<String> categories, List<Integer> counts) {
            this.categories = categories;
            this.counts = counts;
            setPreferredSize(new Dimension(400, 300));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            if (categories.isEmpty() || counts.isEmpty()) {
                g2d.setColor(Color.GRAY);
                g2d.drawString("No financial hub data available", width/2 - 100, height/2);
                return;
            }

            // Calculate total count
            int totalCount = counts.stream().mapToInt(Integer::intValue).sum();
            if (totalCount == 0) {
                g2d.setColor(Color.GRAY);
                g2d.drawString("No financial hub data available", width/2 - 100, height/2);
                return;
            }

            // Draw pie chart
            int centerX = width / 2;
            int centerY = height / 2;
            int radius = Math.min(width, height) / 3;

            double startAngle = 0;
            Color[] colors = {Color.GREEN, Color.RED};

            for (int i = 0; i < categories.size(); i++) {
                double angle = (counts.get(i) * 360.0) / totalCount;
                
                g2d.setColor(colors[i % colors.length]);
                g2d.fillArc(centerX - radius, centerY - radius, 2 * radius, 2 * radius, 
                           (int) startAngle, (int) angle);

                // Draw label
                double labelAngle = Math.toRadians(startAngle + angle / 2);
                int labelX = centerX + (int) (radius * 1.4 * Math.cos(labelAngle));
                int labelY = centerY + (int) (radius * 1.4 * Math.sin(labelAngle));
                
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                g2d.drawString(categories.get(i) + " (" + counts.get(i) + ")", labelX, labelY);

                startAngle += angle;
            }

            // Draw title
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.setColor(Color.BLACK);

            // Draw legend
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.setColor(Color.BLACK);
            g2d.drawString("Legend:", 20, 50);
            
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            int legendY = 70;
            for (int i = 0; i < categories.size(); i++) {
                g2d.setColor(colors[i % colors.length]);
                g2d.fillRect(20, legendY - 8, 12, 12);
                g2d.setColor(Color.BLACK);
                g2d.drawString(categories.get(i) + " (" + counts.get(i) + ")", 40, legendY);
                legendY += 20;
            }

            // Draw percentage information
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.setColor(Color.BLACK);
            if (totalCount > 0) {
                double participationRate = (counts.get(0) * 100.0) / totalCount;
                g2d.drawString("Participation Rate: " + String.format("%.1f", participationRate) + "%", 
                              width/2 - 80, height - 0);
            }
        }
    }


    // Smart Savings Progress Panel (radial / donut)
    private JPanel createSmartSavingsProgressPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Smart Savings Progress"));
        panel.setBackground(Color.WHITE);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT COALESCE(SUM(current_amount),0) AS total_saved, COALESCE(SUM(target_amount),0) AS total_goal FROM savings_goals";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            double totalSaved = 0.0;
            double totalGoal = 0.0;
            if (rs.next()) {
                totalSaved = rs.getDouble("total_saved");
                totalGoal = rs.getDouble("total_goal");
            }

            SmartSavingsProgressChart chart = new SmartSavingsProgressChart(totalSaved, totalGoal);
            panel.add(chart, BorderLayout.CENTER);

        } catch (SQLException ex) {
            JLabel errorLabel = new JLabel("Error loading savings data: " + ex.getMessage());
            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(errorLabel, BorderLayout.CENTER);
        }

        return panel;
    }

    // Smart Savings Progress Chart (donut)
    class SmartSavingsProgressChart extends JPanel {
        private double totalSaved;
        private double totalGoal;

        public SmartSavingsProgressChart(double totalSaved, double totalGoal) {
            this.totalSaved = totalSaved;
            this.totalGoal = totalGoal;
            // allow layout managers to size this panel; painting will adapt to available rectangle
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = Math.max(1, getWidth());
            int h = Math.max(1, getHeight());

            // padding and space reserved for legend/label below the donut
            int padding = 22;
            int legendSpace = 52;

            // compute the largest diameter that fits inside available rectangle
            int availW = w - padding * 2;
            int availH = h - padding * 2 - legendSpace;
            // reduce height slightly so the circle doesn't take the full vertical space
            int availHAdjusted = (int) (availH * 0.75);
            int diameter = Math.max(40, Math.min(availW, availHAdjusted));

            int outer = diameter;
            int thickness = Math.max(10, outer / 3);
            int inner = outer - thickness;

            // center the donut in the available area (above the legend)
            int cx = w / 2;
            int cy = padding + (availH) / 2;

            int arcX = cx - outer / 2;
            int arcY = cy - outer / 2;

            double pct = 0.0;
            if (totalGoal > 0.0001) pct = Math.max(0.0, Math.min(1.0, totalSaved / totalGoal));

            // Background ring
            g2d.setColor(new Color(230, 230, 230));
            Stroke old = g2d.getStroke();
            g2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawArc(arcX, arcY, outer, outer, 0, 360);

            // Determine color by threshold (you can swap to gradient if desired)
            Color fillColor;
            if (pct < 0.5) {
                fillColor = new Color(220, 53, 69); // red
            } else if (pct < 0.75) {
                fillColor = new Color(255, 159, 67); // orange
            } else {
                fillColor = new Color(40, 167, 69); // green
            }

            // Filled arc
            g2d.setColor(fillColor);
            int angle = (int) Math.round(360 * pct);
            g2d.drawArc(arcX, arcY, outer, outer, 90, -angle);

            g2d.setStroke(old);

            // Inner circle (to create donut hole)
            int innerX = cx - inner / 2;
            int innerY = cy - inner / 2;
            g2d.setColor(getBackground());
            g2d.fillOval(innerX, innerY, inner, inner);

            // Center text: percentage and amounts
            g2d.setColor(Color.DARK_GRAY);
            String pctStr = String.format("%.0f%%", pct * 100);
            int pctFont = Math.max(14, inner / 4);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, pctFont));
            FontMetrics fm = g2d.getFontMetrics();
            int pctW = fm.stringWidth(pctStr);
            g2d.drawString(pctStr, cx - pctW / 2, cy - 6);

            g2d.setFont(new Font("Segoe UI", Font.PLAIN, Math.max(10, inner / 10)));
            String amtStr = String.format("$%.2f / $%.2f", totalSaved, totalGoal);
            FontMetrics fm2 = g2d.getFontMetrics();
            int amtW = fm2.stringWidth(amtStr);
            g2d.drawString(amtStr, cx - amtW / 2, cy + fm.getHeight());

            // Small legend/label below the donut
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2d.setColor(Color.GRAY);
            String label = "Total Saved vs Total Goals (all users)";
            int labelY = cy + outer / 2 + 18;
            g2d.drawString(label, cx - g2d.getFontMetrics().stringWidth(label) / 2, labelY);
        }
    }

    // Summary Stats Panel
    private JPanel createSummaryStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Summary Statistics"));
        panel.setBackground(Color.WHITE);

        try (Connection conn = DatabaseConnection.getConnection()) {
            StringBuilder stats = new StringBuilder();
            stats.append("<html><body style='font-family: Arial; font-size: 12px;'>");

            // Total customers
            String customerQuery = "SELECT COUNT(*) as total FROM Customers";
            PreparedStatement customerStmt = conn.prepareStatement(customerQuery);
            ResultSet customerRs = customerStmt.executeQuery();
            if (customerRs.next()) {
                stats.append("<b>Total Customers:</b> ").append(customerRs.getInt("total")).append("<br><br>");
            }

            // Total balance
            String balanceQuery = "SELECT SUM(balance) as total FROM Customers";
            PreparedStatement balanceStmt = conn.prepareStatement(balanceQuery);
            ResultSet balanceRs = balanceStmt.executeQuery();
            if (balanceRs.next()) {
                stats.append("<b>Total Bank Balance:</b> $").append(String.format("%.2f", balanceRs.getDouble("total"))).append("<br><br>");
            }

            // Active loans
            String loanQuery = "SELECT COUNT(*) as active FROM loan_requests WHERE status = 'Approve'";
            PreparedStatement loanStmt = conn.prepareStatement(loanQuery);
            ResultSet loanRs = loanStmt.executeQuery();
            if (loanRs.next()) {
                stats.append("<b>Active Loans:</b> ").append(loanRs.getInt("active")).append("<br><br>");
            }

            // Pending loans
            String pendingQuery = "SELECT COUNT(*) as pending FROM loan_requests WHERE status = 'Pending'";
            PreparedStatement pendingStmt = conn.prepareStatement(pendingQuery);
            ResultSet pendingRs = pendingStmt.executeQuery();
            if (pendingRs.next()) {
                stats.append("<b>Pending Loans:</b> ").append(pendingRs.getInt("pending")).append("<br><br>");
            }

            stats.append("</body></html>");

            JLabel statsLabel = new JLabel(stats.toString());
            statsLabel.setVerticalAlignment(SwingConstants.TOP);
            panel.add(statsLabel, BorderLayout.CENTER);

        } catch (SQLException ex) {
            JLabel errorLabel = new JLabel("Error loading summary data: " + ex.getMessage());
            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(errorLabel, BorderLayout.CENTER);
        }

        return panel;
    }

    // Loan Performance Chart Component
    class LoanPerformanceChart extends JPanel {
        private List<String> statuses;
        private List<Integer> counts;

        public LoanPerformanceChart(List<String> statuses, List<Integer> counts) {
            this.statuses = statuses;
            this.counts = counts;
            setPreferredSize(new Dimension(500, 400));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            if (statuses.isEmpty() || counts.isEmpty()) {
                g2d.setColor(Color.GRAY);
                g2d.drawString("No loan data available", width/2 - 80, height/2);
                return;
            }

            // Calculate total count
            int totalCount = counts.stream().mapToInt(Integer::intValue).sum();
            if (totalCount == 0) {
                g2d.setColor(Color.GRAY);
                g2d.drawString("No loan data available", width/2 - 80, height/2);
                return;
            }

            // Draw pie chart
            int centerX = width / 2;
            int centerY = height / 2;
            int radius = Math.min(width, height) / 3;

            double startAngle = 0;
            Color[] colors = {Color.ORANGE, Color.GREEN, Color.BLUE, Color.RED, Color.MAGENTA, Color.CYAN};

            for (int i = 0; i < statuses.size(); i++) {
                double angle = (counts.get(i) * 360.0) / totalCount;
                
                g2d.setColor(colors[i % colors.length]);
                g2d.fillArc(centerX - radius, centerY - radius, 2 * radius, 2 * radius, 
                           (int) startAngle, (int) angle);

                // Draw label
                double labelAngle = Math.toRadians(startAngle + angle / 2);
                int labelX = centerX + (int) (radius * 1.4 * Math.cos(labelAngle));
                int labelY = centerY + (int) (radius * 1.4 * Math.sin(labelAngle));
                
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                g2d.drawString(statuses.get(i) + " (" + counts.get(i) + ")", labelX, labelY);

                startAngle += angle;
            }

            // Draw title
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.setColor(Color.BLACK);

            // Draw legend
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.setColor(Color.BLACK);
            g2d.drawString("Legend:", 20, 50);
            
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            int legendY = 70;
            for (int i = 0; i < statuses.size(); i++) {
                g2d.setColor(colors[i % colors.length]);
                g2d.fillRect(20, legendY - 8, 12, 12);
                g2d.setColor(Color.BLACK);
                g2d.drawString(statuses.get(i) + " (" + counts.get(i) + ")", 40, legendY);
                legendY += 20;
            }
        }
    }

    // Helper method to reset all admin menu buttons
    private void resetAllAdminButtonColors(JButton[] buttons) {
        for (JButton btn : buttons) {
            btn.setForeground(Color.BLACK);
            btn.putClientProperty("isHovered", false);
            btn.repaint();
        }
    }

    // Helper method to create rounded buttons for admin panel
    private JButton createRoundedAdminButton(String text, Color bgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Check if button is hovered
                boolean isHovered = Boolean.TRUE.equals(getClientProperty("isHovered"));
                
                // Draw rounded background
                if (isHovered || getModel().isPressed()) {
                    g2.setColor(new Color(230, 230, 230));
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                
                super.paintComponent(g);
            }
        };
        
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 12));
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.putClientProperty("isHovered", true);
                btn.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!btn.getForeground().equals(new Color(0, 123, 255))) {
                    btn.putClientProperty("isHovered", false);
                }
                btn.repaint();
            }
        });
        
        // Add click action to change text color to blue
        btn.addActionListener(e -> {
            btn.setForeground(new Color(0, 123, 255));
        });
        
        return btn;
    }

    // Method to display complaints view
    private void showComplaintsView() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT id, account_number, complaint_description, response FROM complaints";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            // Create table model
            String[] columnNames = {"ID", "Account Number", "Complaint", "Response"};
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

            // Populate table model with data from ResultSet
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("account_number"),
                    rs.getString("complaint_description"),
                    rs.getString("response")
                };
                tableModel.addRow(row);
            }

            // Create table and display in a scroll pane
            JTable complaintsTable = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(complaintsTable);
            scrollPane.setPreferredSize(new Dimension(800, 400));

            // Add a button to respond to complaints
            JButton respondButton = new JButton("Respond to Complaint");
            respondButton.addActionListener(e1 -> {
                int selectedRow = complaintsTable.getSelectedRow();
                if (selectedRow != -1) {
                    int complaintId = (int) tableModel.getValueAt(selectedRow, 0);
                    String response = JOptionPane.showInputDialog(frame, "Enter your response:");

                    if (response != null && !response.trim().isEmpty()) {
                        try {
                            // Update the response and status in the database
                            String updateQuery = "UPDATE complaints SET response = ?, status = 'Received' WHERE id = ?";
                            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                            updateStmt.setString(1, response);
                            updateStmt.setInt(2, complaintId);
                            updateStmt.executeUpdate();

                            JOptionPane.showMessageDialog(frame, "Response submitted successfully and status updated to 'Received'.", "Success", JOptionPane.INFORMATION_MESSAGE);

                            // Refresh the table
                            tableModel.setValueAt(response, selectedRow, 3);
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(frame, "Error submitting response: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(frame, "Response cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Please select a complaint to respond to.", "Warning", JOptionPane.WARNING_MESSAGE);
                }
            });

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(respondButton, BorderLayout.SOUTH);

            JOptionPane.showMessageDialog(frame, panel, "Customer Complaints", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Error fetching complaints: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Thread to check for new complaints periodically
    private void startComplaintCheckingThread() {
        new Thread(() -> {
            int lastComplaintCount = 0;
            while (true) {
                try {
                    // Check for new complaints every 5 seconds
                    Thread.sleep(5000);
                    
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        String query = "SELECT COUNT(*) AS count FROM complaints WHERE response IS NULL";
                        PreparedStatement stmt = conn.prepareStatement(query);
                        ResultSet rs = stmt.executeQuery();
                        
                        if (rs.next()) {
                            int unrespondedCount = rs.getInt("count");
                            
                            if (unrespondedCount > lastComplaintCount && unrespondedCount > 0) {
                                // Get the latest complaint details
                                String latestComplaintQuery = "SELECT account_number, complaint_description FROM complaints WHERE response IS NULL ORDER BY id DESC LIMIT 1";
                                PreparedStatement latestStmt = conn.prepareStatement(latestComplaintQuery);
                                ResultSet latestRs = latestStmt.executeQuery();
                                
                                if (latestRs.next()) {
                                    String accountNumber = latestRs.getString("account_number");
                                    String complaintDesc = latestRs.getString("complaint_description");
                                    
                                    // Show popup alert
                                    SwingUtilities.invokeLater(() -> {
                                        String message = "New Complaint Received!\n\n" +
                                            "Customer Account: " + accountNumber + "\n" +
                                            "Complaint: " + complaintDesc.substring(0, Math.min(50, complaintDesc.length())) + 
                                            (complaintDesc.length() > 50 ? "..." : "");
                                        
                                        JOptionPane.showMessageDialog(frame, message, 
                                            "⚠️ New Complaint Alert", JOptionPane.WARNING_MESSAGE);
                                    });
                                }
                                
                                // Update notification label with new complaint count
                                SwingUtilities.invokeLater(() -> {
                                    notificationLabel.setText("⚠️ NEW COMPLAINT ALERT! " + unrespondedCount + " unresponded complaint(s) - Click to view");
                                    notificationLabel.setForeground(new Color(220, 20, 60));
                                    // Make it clickable to view complaints
                                    notificationLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                                });
                                lastComplaintCount = unrespondedCount;
                            } else if (unrespondedCount == 0 && lastComplaintCount > 0) {
                                // All complaints have been responded to
                                SwingUtilities.invokeLater(() -> {
                                    notificationLabel.setText("✓ All complaints have been responded to");
                                    notificationLabel.setForeground(new Color(34, 139, 34));
                                });
                                lastComplaintCount = 0;
                            }
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Bank bank = new Bank(); // Assuming you have a Bank class
                AdminDashboard window = new AdminDashboard(bank);
                window.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}

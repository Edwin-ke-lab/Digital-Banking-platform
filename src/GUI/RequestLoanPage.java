package GUI;

import javax.swing.*;
import java.awt.*;
import main.Bank;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RequestLoanPage {

    private JFrame frame;
    private String accountNumber;
    private Bank bank;
    private JTextField txtAmount; // Declare txtAmount at the class level
    private double loanLimit = 50000.0; // default, will be loaded from DB if present

    public RequestLoanPage(String accountNumber, Bank bank) {
        this.accountNumber = accountNumber;
        this.bank = bank;
        
        // Check if customer meets minimum balance requirement
        if (!checkMinimumBalance()) {
            JOptionPane.showMessageDialog(null, 
                "You need a minimum balance of $5,000 to apply for a loan.\nYour current balance is insufficient.", 
                "Insufficient Balance", 
                JOptionPane.WARNING_MESSAGE);
            return; // Exit without showing the loan page
        }
        
        // Load loan limit from DB (if column exists)
        loadLoanLimit();
        
        initialize();
    }

    // Load loan_limit from customers table; fallback to default if missing
    private void loadLoanLimit() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT loan_limit FROM customers WHERE account_number = ?");
            ps.setString(1, accountNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                try {
                    loanLimit = rs.getDouble("loan_limit");
                    if (rs.wasNull()) loanLimit = 50000.0;
                } catch (SQLException ignore) {
                    loanLimit = 50000.0;
                }
            }
        } catch (SQLException ex) {
            // If column doesn't exist or any error, keep default loanLimit
            loanLimit = 50000.0;
        }
    }

    // Method to check if customer has minimum balance of $5,000
    private boolean checkMinimumBalance() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT balance FROM customers WHERE account_number = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                double balance = rs.getDouble("balance");
                return balance >= 5000.0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error checking balance: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    private void initialize() {
        frame = new JFrame("Request Loan");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel("Available Loan Options", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        // Add information panel about loan requirements
        JPanel infoPanel = new JPanel(new BorderLayout());
    JLabel lblInfo = new JLabel(String.format("<html><center>Loan Requirements:<br/>• Minimum balance: $5,000<br/>• Maximum loan amount: $%.2f</center></html>", loanLimit));
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfo.setForeground(new Color(0, 100, 0));
        lblInfo.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        infoPanel.add(lblInfo, BorderLayout.CENTER);
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(lblTitle, BorderLayout.NORTH);
        titlePanel.add(infoPanel, BorderLayout.SOUTH);
        
        frame.add(titlePanel, BorderLayout.NORTH);

        // Loan options table
        String[] columnNames = {"Loan Type", "Interest Rate", "loan_term_months"};
        Object[][] data = {
            {"Personal Loan", "12%", "5"},
            {"Home Loan", "8%", "12"},
            {"Car Loan", "10%", "12"},
            {"Education Loan", "9%", "8"},
            {"Business Loan", "15%", "12"}
        };

        JTable loanTable = new JTable(data, columnNames);
        loanTable.setEnabled(false);
        // Enable row selection in the loan options table
        loanTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loanTable.setEnabled(true);
        JScrollPane scrollPane = new JScrollPane(loanTable);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Back button
        JButton btnBack = new JButton("Back");
        btnBack.addActionListener(e -> frame.dispose());
        frame.add(btnBack, BorderLayout.SOUTH);

        // Add Apply button
        JButton btnApply = new JButton("Apply for Loan");
        btnApply.addActionListener(e -> {
            int selectedRow = loanTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame, "Please select a loan option to apply.", "No Selection", JOptionPane.WARNING_MESSAGE);
            } else {
                String loanType = (String) loanTable.getValueAt(selectedRow, 0);
                String interestRate = (String) loanTable.getValueAt(selectedRow, 1);
                Object termObj = loanTable.getValueAt(selectedRow, 2);
                int loan_term_months;
                if (termObj instanceof Integer) {
                    loan_term_months = (Integer) termObj;
                } else {
                    // If it's a string like "5months", extract the number
                    String termStr = termObj.toString().replaceAll("[^0-9]", "");
                    loan_term_months = Integer.parseInt(termStr);
                }
                String amountStr = txtAmount.getText();

                try {
                    double amount = Double.parseDouble(amountStr);

                    // Validate loan amount (maximum = loanLimit)
                    if (amount > loanLimit) {
                        JOptionPane.showMessageDialog(frame, 
                            String.format("Loan amount cannot exceed $%.2f.\nPlease enter a smaller amount.", loanLimit), 
                            "Loan Amount Too High", 
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    if (amount <= 0) {
                        JOptionPane.showMessageDialog(frame, 
                            "Loan amount must be greater than $0.", 
                            "Invalid Amount", 
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Send loan application request to admin
                    bank.applyForLoan(accountNumber, loanType, interestRate, String.valueOf(loan_term_months), amount);
                    JOptionPane.showMessageDialog(frame, "Your loan application has been submitted.", "Application Submitted", JOptionPane.INFORMATION_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid loan amount.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        frame.add(btnApply, BorderLayout.EAST);

        // Add input field for loan amount
        JPanel inputPanel = new JPanel(new FlowLayout());
        JLabel lblAmount = new JLabel("Enter Loan Amount: ");
        txtAmount = new JTextField(10); // Initialize txtAmount
        inputPanel.add(lblAmount);
        inputPanel.add(txtAmount);
        frame.add(inputPanel, BorderLayout.SOUTH);

        // Disable the loan amount input field initially
        txtAmount.setEnabled(false);

        // Enable the loan amount input field only after a loan option is selected
        loanTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && loanTable.getSelectedRow() != -1) {
                txtAmount.setEnabled(true);
            }
        });
    }

    public void show() {
        if (frame != null) {
            frame.setVisible(true);
        }
        // If frame is null (insufficient balance), nothing happens - no page is shown
    }

    // accountNumber and bank are currently unused but may be used for future enhancements.
}
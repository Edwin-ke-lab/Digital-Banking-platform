package main;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard {

    private JFrame frame;

    public AdminDashboard() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Admin Dashboard");
        frame.setBounds(100, 100, 600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        JLabel lblTitle = new JLabel("Admin Dashboard");
        lblTitle.setFont(new Font("Tahoma", Font.BOLD, 18));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setBounds(150, 30, 300, 30);
        frame.getContentPane().add(lblTitle);

        JButton btnUserManagement = new JButton("User & Account Management");
        btnUserManagement.setBounds(200, 100, 200, 30);
        frame.getContentPane().add(btnUserManagement);

        JButton btnLoanManagement = new JButton("Loan Management");
        btnLoanManagement.setBounds(200, 150, 200, 30);
        frame.getContentPane().add(btnLoanManagement);

        JButton btnTransactionOversight = new JButton("Transaction Oversight");
        btnTransactionOversight.setBounds(200, 200, 200, 30);
        frame.getContentPane().add(btnTransactionOversight);

        JButton btnReports = new JButton("Reporting & Analytics");
        btnReports.setBounds(200, 250, 200, 30);
        frame.getContentPane().add(btnReports);
    }

    public void show() {
        frame.setVisible(true);
    }
}

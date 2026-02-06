package main;

import javax.swing.*;
import GUI.DatabaseConnection;
import GUI.LoginPage;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegistrationPage {

    public JFrame frame;

    public RegistrationPage() {
        initialize();
    }

    private void initialize() {
        // Create a full-screen undecorated frame and add custom symbol-only controls
        frame = new JFrame("Banking System - Register");
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setSize(screenSize);
        frame.setLocationRelativeTo(null);

        // Gradient background panel for an appealing look
        JPanel background = new JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
                // match customer login background: teal -> soft light
                java.awt.GradientPaint gp = new java.awt.GradientPaint(
                    0, 0, new Color(72, 209, 204),
                    0, getHeight(), new Color(230, 230, 250)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        background.setLayout(null);
        background.setBounds(0, 0, screenSize.width, screenSize.height);
        frame.setContentPane(background);

        JLabel lblTitle = new JLabel("Register a New Account");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        int panelWidth = 700;
        int panelHeight = 520;
        int centerX = (screenSize.width - panelWidth) / 2;
        int centerY = (screenSize.height - panelHeight) / 2;
        lblTitle.setBounds(centerX, centerY - 60, panelWidth, 36);
        background.add(lblTitle);

        // Custom symbol-only window controls (black symbols)
        javax.swing.JPanel topControls = new javax.swing.JPanel(null);
        topControls.setOpaque(false);
        int topBarHeight = 36;
        topControls.setBounds(0, 0, screenSize.width, topBarHeight);

        javax.swing.JButton btnMinimize = new javax.swing.JButton("\u2013");
        btnMinimize.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnMinimize.setForeground(Color.BLACK);
        btnMinimize.setBorder(null);
        btnMinimize.setContentAreaFilled(false);
        btnMinimize.setFocusPainted(false);
        btnMinimize.setBounds(screenSize.width - 80, 4, 34, 28);
        btnMinimize.addActionListener(e -> frame.setState(JFrame.ICONIFIED));

        javax.swing.JButton btnClose = new javax.swing.JButton("\u2715");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setForeground(Color.BLACK);
        btnClose.setBorder(null);
        btnClose.setContentAreaFilled(false);
        btnClose.setFocusPainted(false);
        btnClose.setBounds(screenSize.width - 40, 6, 34, 24);
        btnClose.addActionListener(e -> System.exit(0));

        topControls.add(btnMinimize);
        topControls.add(btnClose);
        frame.getLayeredPane().add(topControls, javax.swing.JLayeredPane.PALETTE_LAYER);

    // form area (centered panel)
    JPanel formPanel = new JPanel(null);
    formPanel.setOpaque(false);
    int formW = 520;
    int formH = 420;
    int formX = centerX + (panelWidth - formW) / 2;
    int formY = centerY - 20;
    formPanel.setBounds(formX, formY, formW, formH);
    background.add(formPanel);

    JLabel lblFirstName = new JLabel("First Name:");
    lblFirstName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    lblFirstName.setBounds(20, 10, 120, 25);
    formPanel.add(lblFirstName);

    RoundedTextField txtFirstName = new RoundedTextField();
    txtFirstName.setBounds(150, 10, 340, 32);
    formPanel.add(txtFirstName);

    JLabel lblLastName = new JLabel("Last Name:");
    lblLastName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    lblLastName.setBounds(20, 55, 120, 25);
    formPanel.add(lblLastName);

    RoundedTextField txtLastName = new RoundedTextField();
    txtLastName.setBounds(150, 55, 340, 32);
    formPanel.add(txtLastName);

    JLabel lblIDNumber = new JLabel("ID Number:");
    lblIDNumber.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    lblIDNumber.setBounds(20, 100, 120, 25);
    formPanel.add(lblIDNumber);

    RoundedTextField txtIDNumber = new RoundedTextField();
    txtIDNumber.setBounds(150, 100, 340, 32);
    formPanel.add(txtIDNumber);

    JLabel lblPhoneNumber = new JLabel("Phone Number:");
    lblPhoneNumber.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    lblPhoneNumber.setBounds(20, 145, 120, 25);
    formPanel.add(lblPhoneNumber);

    RoundedTextField txtPhoneNumber = new RoundedTextField();
    txtPhoneNumber.setBounds(150, 145, 340, 32);
    formPanel.add(txtPhoneNumber);

    JLabel lblEmail = new JLabel("Email:");
    lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    lblEmail.setBounds(20, 190, 120, 25);
    formPanel.add(lblEmail);

    RoundedTextField txtEmail = new RoundedTextField();
    txtEmail.setBounds(150, 190, 340, 32);
    formPanel.add(txtEmail);

    JLabel lblUsername = new JLabel("Username:");
    lblUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    lblUsername.setBounds(20, 235, 120, 25);
    formPanel.add(lblUsername);

    RoundedTextField txtUsername = new RoundedTextField();
    txtUsername.setBounds(150, 235, 340, 32);
    formPanel.add(txtUsername);

    JLabel lblPassword = new JLabel("Password:");
    lblPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    lblPassword.setBounds(20, 280, 120, 25);
    formPanel.add(lblPassword);

    RoundedPasswordField txtPassword = new RoundedPasswordField();
    txtPassword.setBounds(150, 280, 340, 32);
    formPanel.add(txtPassword);

    JLabel lblAccountType = new JLabel("Account Type:");
    lblAccountType.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    lblAccountType.setBounds(20, 325, 120, 25);
    formPanel.add(lblAccountType);

    String[] accountTypes = { "Current" };
    JComboBox<String> cmbAccountType = new JComboBox<>(accountTypes);
    cmbAccountType.setBounds(150, 325, 200, 28);
    formPanel.add(cmbAccountType);

    JButton btnRegister = new JButton("Register");
    btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 14));
    btnRegister.setBackground(new Color(72, 209, 204));
    btnRegister.setForeground(Color.WHITE);
    btnRegister.setBounds(150, 370, 140, 36);
    formPanel.add(btnRegister);

    // Back Button next to Register button
    JButton btnBack = new JButton("Back");
    btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    btnBack.setBackground(new Color(220, 220, 220));
    btnBack.setForeground(new Color(44, 62, 80));
    btnBack.setBounds(310, 370, 140, 36);
    formPanel.add(btnBack);

    btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
        String firstName = txtFirstName.getText();
        String lastName = txtLastName.getText();
        String idNumber = txtIDNumber.getText();
        String phoneNumber = txtPhoneNumber.getText();
        String email = txtEmail.getText();
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());
        String accountType = (String) cmbAccountType.getSelectedItem();

                if (firstName.isEmpty() || lastName.isEmpty() || idNumber.isEmpty() || phoneNumber.isEmpty() ||
                        email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please fill in all fields", "Registration Failed", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                    JOptionPane.showMessageDialog(frame, "Invalid email format", "Registration Failed", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!phoneNumber.matches("\\d{10}")) {
                    JOptionPane.showMessageDialog(frame, "Phone number must be 10 digits", "Registration Failed", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (Connection conn = DatabaseConnection.getConnection()) {
                    String checkQuery = "SELECT * FROM customers WHERE username = ? OR id_number = ?";
                    PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                    checkStmt.setString(1, username);
                    checkStmt.setString(2, idNumber);
                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next()) {
                        JOptionPane.showMessageDialog(frame, "Username or ID Number already exists", "Registration Failed", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String query = "INSERT INTO customers (first_name, last_name, id_number, phone_number, email, username, password, account_type, account_number, balance) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    String accountNumber = "ACC" + String.format("%012d", (int)(Math.random() * 10000000));
                    stmt.setString(1, firstName);
                    stmt.setString(2, lastName);
                    stmt.setString(3, idNumber);
                    stmt.setString(4, phoneNumber);
                    stmt.setString(5, email);
                    stmt.setString(6, username);
                    stmt.setString(7, password);
                    stmt.setString(8, accountType);
                    stmt.setString(9, accountNumber);
                    stmt.setDouble(10, 0.0);

                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(frame, "Registration Successful! Your Account Number: " + accountNumber);
                    frame.setVisible(false);
                    new LoginPage().frame.setVisible(true);
                } catch (SQLException ex) {
                    if (ex.getMessage().contains("Duplicate entry")) {
                        JOptionPane.showMessageDialog(frame, "Username or ID Number already exists", "Registration Failed", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // ActionListener for Back Button
        btnBack.addActionListener(e -> {
            new LoginPage().frame.setVisible(true);
            frame.setVisible(false);
        });
    }

    // Rounded text field for a modern look
    private static class RoundedTextField extends JTextField {
        private static final long serialVersionUID = 1L;
        private int arc = 16;

        public RoundedTextField() {
            super();
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // background
            g2.setColor(new Color(255, 255, 255, 230));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            // subtle border
            g2.setColor(new Color(200, 200, 200, 180));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedPasswordField extends JPasswordField {
        private static final long serialVersionUID = 1L;
        private int arc = 16;

        public RoundedPasswordField() {
            super();
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 230));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setColor(new Color(200, 200, 200, 180));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
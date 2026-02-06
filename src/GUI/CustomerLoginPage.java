package GUI;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JPanel;

import main.Bank;
import main.RegistrationPage;

public class CustomerLoginPage {

    public JFrame frame;

    public CustomerLoginPage() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Customer Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        // Set frame to full screen
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        // use a custom symbol-only control bar (minimize & close) instead of native title
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setSize(screenSize);
        frame.setLocationRelativeTo(null);

        // Soft gradient background using a JPanel
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
                java.awt.GradientPaint gp = new java.awt.GradientPaint(
                    0, 0, new Color(72, 209, 204),
                    0, getHeight(), new Color(230, 230, 250)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        backgroundPanel.setLayout(null);
        backgroundPanel.setBounds(0, 0, screenSize.width, screenSize.height);
        frame.setContentPane(backgroundPanel);

        int panelWidth = 500;
        int panelHeight = 400;
        int centerX = (screenSize.width - panelWidth) / 2;
        int centerY = (screenSize.height - panelHeight) / 2;

        JPanel loginPanel = new JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                int arc = 24;
                // card background
                g2.setColor(new Color(255, 255, 255, 230));
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                // border
                g2.setColor(new Color(200, 200, 200, 200));
                g2.setStroke(new java.awt.BasicStroke(2f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
                g2.dispose();
            }
        };
        loginPanel.setLayout(null);
        loginPanel.setOpaque(false); // keep true painting done in paintComponent
        loginPanel.setBounds(centerX, centerY, panelWidth, panelHeight);
        // inner padding so elements don't touch the rounded border
        loginPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 20, 18, 20));
        backgroundPanel.add(loginPanel);

        JLabel lblTitle = new JLabel("Customer Login");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(44, 62, 80));
        lblTitle.setBounds(0, 30, panelWidth, 40);
        lblTitle.setHorizontalAlignment(JLabel.CENTER);
        loginPanel.add(lblTitle);

        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblUsername.setForeground(new Color(44, 62, 80));
        lblUsername.setBounds(100, 100, 100, 25);
        loginPanel.add(lblUsername);

    RoundedTextField txtUsername = new RoundedTextField();
    txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    txtUsername.setBounds(200, 100, 200, 30);
    loginPanel.add(txtUsername);

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblPassword.setForeground(new Color(44, 62, 80));
        lblPassword.setBounds(100, 150, 100, 25);
        loginPanel.add(lblPassword);

    RoundedPasswordField txtPassword = new RoundedPasswordField();
    txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    txtPassword.setBounds(200, 150, 200, 30);
    loginPanel.add(txtPassword);

    // Forgot password shown as an underlined link (clickable JLabel)
    javax.swing.JLabel lblForgot = new javax.swing.JLabel("<html><u>Forgot Password?</u></html>");
    lblForgot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    lblForgot.setForeground(new Color(0, 102, 204)); // link-blue
    lblForgot.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    // place the forgot link below the registration area (arranged with the register label/button)
    lblForgot.setBounds(200, 295, 200, 25);
    loginPanel.add(lblForgot);

    RoundedButton btnLogin = new RoundedButton("Login", new Color(72, 209, 204));
    btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 16));
    btnLogin.setForeground(Color.WHITE);
    btnLogin.setFocusPainted(false);
    btnLogin.setBounds(200, 200, 200, 35);
    btnLogin.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10));
    loginPanel.add(btnLogin);

        JLabel lblNoAccount = new JLabel("Don't have an account?");
        lblNoAccount.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNoAccount.setForeground(new Color(44, 62, 80));
        lblNoAccount.setBounds(100, 255, 130, 30);
        loginPanel.add(lblNoAccount);

    RoundedButton btnRegister = new RoundedButton("Register", new Color(100, 149, 237));
    btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 14));
    btnRegister.setForeground(Color.WHITE);
    btnRegister.setFocusPainted(false);
    btnRegister.setBounds(230, 255, 100, 30);
    btnRegister.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10));
    loginPanel.add(btnRegister);

        // Add Back Button at the bottom center of the login panel
        JButton btnBack = new JButton("Back");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnBack.setBackground(new Color(220, 220, 220));
        btnBack.setForeground(new Color(44, 62, 80));
        btnBack.setFocusPainted(false);
    btnBack.setBounds(200, 340, 100, 30);
        btnBack.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(72, 209, 204), 1));
        loginPanel.add(btnBack);

    // --- Custom symbol-only window controls (minimize & close) ---
    javax.swing.JPanel topControls = new javax.swing.JPanel(null);
    topControls.setOpaque(false);
    int topBarHeight = 36;
    topControls.setBounds(0, 0, screenSize.width, topBarHeight);

    javax.swing.JButton btnMinimize = new javax.swing.JButton("\u2013");
    btnMinimize.setFont(new Font("Segoe UI", Font.BOLD, 16));
    btnMinimize.setForeground(Color.WHITE);
    btnMinimize.setBorder(null);
    btnMinimize.setContentAreaFilled(false);
    btnMinimize.setFocusPainted(false);
    btnMinimize.setBounds(screenSize.width - 80, 4, 34, 28);
    btnMinimize.addActionListener(e -> frame.setState(JFrame.ICONIFIED));

    javax.swing.JButton btnClose = new javax.swing.JButton("\u2715");
    btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
    btnClose.setForeground(Color.WHITE);
    btnClose.setBorder(null);
    btnClose.setContentAreaFilled(false);
    btnClose.setFocusPainted(false);
    btnClose.setBounds(screenSize.width - 40, 6, 34, 24);
    btnClose.addActionListener(e -> System.exit(0));

    topControls.add(btnMinimize);
    topControls.add(btnClose);
    frame.getLayeredPane().add(topControls, javax.swing.JLayeredPane.PALETTE_LAYER);

        // ActionListener for Login Button
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = txtUsername.getText();
                String password = new String(txtPassword.getPassword());

                try (Connection conn = DatabaseConnection.getConnection()) {
                    String query = "SELECT * FROM customers WHERE username = ? AND password = ? AND role = 'customer'";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        String accountNumber = rs.getString("account_number");
                        JOptionPane.showMessageDialog(frame, "Customer Login Successful!");
                        frame.setVisible(false);

                        // Open the Customer Menu
                        Bank bank = new Bank();
                        bank.startAutoSaveProcessor(); // Start background auto-save processor
                        MenuPage menuPage = new MenuPage(accountNumber, bank);
                        menuPage.show();
                    } else {
                        JOptionPane.showMessageDialog(frame, "Invalid Username or Password", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // ActionListener for Register Button
        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open the Registration Page
                RegistrationPage registrationPage = new RegistrationPage();
                registrationPage.frame.setVisible(true);
                frame.dispose();
            }
        });

        // Action: Forgot Password (clickable label)
        lblForgot.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JTextField userField = new JTextField();
                JTextField emailField = new JTextField();
                Object[] message = {
                    "Username:", userField,
                    "Email (used at registration):", emailField
                };
                int option = JOptionPane.showConfirmDialog(frame, message, "Reset Password", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    String user = userField.getText().trim();
                    String email = emailField.getText().trim();
                    if (user.isEmpty() || email.isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "Please enter both username and email.", "Input Required", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    try (Connection conn = DatabaseConnection.getConnection()) {
                        String checkQuery = "SELECT * FROM customers WHERE username = ? AND email = ?";
                        PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                        checkStmt.setString(1, user);
                        checkStmt.setString(2, email);
                        ResultSet rs = checkStmt.executeQuery();
                        if (!rs.next()) {
                            JOptionPane.showMessageDialog(frame, "No matching account found for the provided username and email.", "Not Found", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        // Prompt for new password
                        JPasswordField newPass = new JPasswordField();
                        JPasswordField confirmPass = new JPasswordField();
                        Object[] passMsg = {
                            "New Password:", newPass,
                            "Confirm Password:", confirmPass
                        };
                        int passOption = JOptionPane.showConfirmDialog(frame, passMsg, "Set New Password", JOptionPane.OK_CANCEL_OPTION);
                        if (passOption == JOptionPane.OK_OPTION) {
                            String p1 = new String(newPass.getPassword());
                            String p2 = new String(confirmPass.getPassword());
                            if (p1.isEmpty()) {
                                JOptionPane.showMessageDialog(frame, "Password cannot be empty.", "Invalid Password", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                            if (!p1.equals(p2)) {
                                JOptionPane.showMessageDialog(frame, "Passwords do not match.", "Mismatch", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            // Update password in DB
                            String updateQuery = "UPDATE customers SET password = ? WHERE username = ? AND email = ?";
                            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                            updateStmt.setString(1, p1);
                            updateStmt.setString(2, user);
                            updateStmt.setString(3, email);
                            int updated = updateStmt.executeUpdate();
                            if (updated > 0) {
                                JOptionPane.showMessageDialog(frame, "Password reset successful. You may now login with your new password.", "Success", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(frame, "Failed to reset password. Please contact support.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }

                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        btnBack.addActionListener(e -> {
            LoginPage loginPage = new LoginPage();
            loginPage.frame.setVisible(true);
            frame.setVisible(false);
        });
    }

    // Rounded input controls
    private static class RoundedTextField extends JTextField {
        private static final long serialVersionUID = 1L;
        private int arc = 14;
        public RoundedTextField() {
            super();
            setOpaque(false);
            setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 10));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255,255,255,230));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setColor(new Color(200,200,200,180));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedPasswordField extends JPasswordField {
        private static final long serialVersionUID = 1L;
        private int arc = 14;
        public RoundedPasswordField() {
            super();
            setOpaque(false);
            setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 10));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255,255,255,230));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setColor(new Color(200,200,200,180));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Rounded button for login/register
    private static class RoundedButton extends JButton {
        private static final long serialVersionUID = 1L;
        private int arc = 18;
        private Color bg;

        public RoundedButton(String text, Color background) {
            super(text);
            this.bg = background == null ? new Color(100, 149, 237) : background;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w, h, arc, arc);
            g2.setColor(bg.darker());
            g2.setStroke(new java.awt.BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
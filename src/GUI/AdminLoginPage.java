package GUI;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

public class AdminLoginPage {

    public JFrame frame;

    public AdminLoginPage() {
        initialize();
    }

    private void initialize() {
        // Set frame to full screen
        frame = new JFrame("Admin Login");
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        // we'll show a custom symbol-only control bar (minimize & close)
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setSize(screenSize);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Gradient background panel
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
                java.awt.GradientPaint gp = new java.awt.GradientPaint(
                    0, 0, new Color(255, 99, 71),
                    0, getHeight(), new Color(255, 228, 225)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        backgroundPanel.setLayout(null);
        backgroundPanel.setBounds(0, 0, screenSize.width, screenSize.height);
        frame.setContentPane(backgroundPanel);

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
                g2.setColor(new Color(255, 255, 255, 240));
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                // border
                g2.setColor(new Color(200, 200, 200, 200));
                g2.setStroke(new java.awt.BasicStroke(2f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
                g2.dispose();
            }
        };
        loginPanel.setLayout(null);
        loginPanel.setOpaque(false);
        loginPanel.setBounds(centerX, centerY, panelWidth, panelHeight);
        loginPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 20, 18, 20));
        backgroundPanel.add(loginPanel);

        JLabel lblTitle = new JLabel("Admin Login");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
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

        JButton btnLogin = new JButton("Login") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnLogin.setBackground(new Color(255, 99, 71));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setBounds(200, 200, 200, 35);
        btnLogin.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10));
        loginPanel.add(btnLogin);

        // Add Reset Password Link
        JLabel lblForgotPassword = new JLabel("<html><u>Forgot Password?</u></html>");
        lblForgotPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblForgotPassword.setForeground(new Color(0, 102, 204));
        lblForgotPassword.setBounds(200, 245, 200, 20);
        lblForgotPassword.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblForgotPassword.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                String newPassword = JOptionPane.showInputDialog(frame, "Enter your new password:", "");
                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    String username = txtUsername.getText();
                    if (username.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "Please enter your username first.", "Input Error", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        String query = "UPDATE users SET password = ? WHERE username = ? AND role = ?";
                        PreparedStatement stmt = conn.prepareStatement(query);
                        stmt.setString(1, newPassword);
                        stmt.setString(2, username);
                        stmt.setString(3, "admin");
                        int rowsUpdated = stmt.executeUpdate();
                        if (rowsUpdated > 0) {
                            JOptionPane.showMessageDialog(frame, "Password reset successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(frame, "Username not found.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        loginPanel.add(lblForgotPassword);

        // Add Back Button at the bottom center of the login panel
        JButton btnBack = new JButton("Back") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(new Color(255, 99, 71));
                g2.setStroke(new java.awt.BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnBack.setBackground(new Color(220, 220, 220));
        btnBack.setForeground(new Color(44, 62, 80));
        btnBack.setFocusPainted(false);
        btnBack.setContentAreaFilled(false);
        btnBack.setBounds(200, 320, 100, 30);
        btnBack.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        loginPanel.add(btnBack);

        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = txtUsername.getText();
                String password = new String(txtPassword.getPassword());

                try (Connection conn = DatabaseConnection.getConnection()) {
                    String query = "SELECT * FROM users WHERE username = ? AND password = ? AND role = ?";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    stmt.setString(3, "admin");
                    ResultSet rs = stmt.executeQuery();

                    if (rs.next()) {
                        JOptionPane.showMessageDialog(frame, "Admin Login Successful!");
                        frame.setVisible(false);

                        // Open the Admin Dashboard
                        AdminDashboard adminDashboard = new AdminDashboard(null);
                        adminDashboard.show();
                    } else {
                        JOptionPane.showMessageDialog(frame, "Invalid Username or Password", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
}
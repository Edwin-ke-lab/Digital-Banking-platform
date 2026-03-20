package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class LoginPage {

    public JFrame frame;

    // Custom Rounded Button Class
    class RoundedButton extends JButton {
        private Color backgroundColor;
        private int cornerRadius = 25;

        public RoundedButton(String text) {
            super(text);
            setOpaque(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);

            // Set default colors
            backgroundColor = getBackground();
            backgroundColor.brighter();
            backgroundColor.darker();


        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw rounded rectangle background
            g2d.setColor(getBackground());
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

            // Draw border
            g2d.setColor(getBackground().darker());
            g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

            g2d.dispose();

            // Draw text
            super.paintComponent(g);
        }

        @Override
        public void setBackground(Color bg) {
            super.setBackground(bg);
            backgroundColor = bg;
            bg.brighter();
            bg.darker();
        }
    }

    /**
     * Create the application.
     */
    public LoginPage() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        // Get screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // Create the frame (we'll use a custom, symbol-only top bar)
        frame = new JFrame("Banking System - Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // remove native title bar so we can show only symbol buttons on top-right
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setSize(screenSize);
        frame.setLocationRelativeTo(null);

        // Set layout manager
        frame.getContentPane().setLayout(null);

        // Background image label (scaled)
        JLabel background = new JLabel();
        background.setBounds(0, 0, screenSize.width, screenSize.height);
        ImageIcon icon = new ImageIcon("C:\\Users\\EDWIN NYANDORO\\Downloads\\project.jpg");
        java.awt.Image img = icon.getImage().getScaledInstance(screenSize.width, screenSize.height,
                java.awt.Image.SCALE_SMOOTH);
        background.setIcon(new ImageIcon(img));
        frame.setContentPane(background);
        background.setLayout(new GridBagLayout());

        // --- Custom symbol-only window controls (minimize & close) ---
        // placed in the layered pane so they float above the background
        javax.swing.JPanel topControls = new javax.swing.JPanel(null);
        topControls.setOpaque(false);
        int topBarHeight = 36;
        topControls.setBounds(0, 0, screenSize.width, topBarHeight);

        javax.swing.JButton btnMinimize = new javax.swing.JButton("\u2013"); // en-dash looks like a minimize symbol
        btnMinimize.setFont(new Font("Tahoma", Font.BOLD, 16));
        // show as black symbol per request
        btnMinimize.setForeground(Color.BLACK);
        btnMinimize.setBorder(null);
        btnMinimize.setContentAreaFilled(false);
        btnMinimize.setFocusPainted(false);
        btnMinimize.setBounds(screenSize.width - 80, 4, 34, 28);
        btnMinimize.addActionListener(e -> frame.setState(JFrame.ICONIFIED));

        javax.swing.JButton btnClose = new javax.swing.JButton("\u2715"); // heavy multiplication x
        btnClose.setFont(new Font("Tahoma", Font.BOLD, 14));
        // show as black symbol per request
        btnClose.setForeground(Color.BLACK);
        btnClose.setBorder(null);
        btnClose.setContentAreaFilled(false);
        btnClose.setFocusPainted(false);
        btnClose.setBounds(screenSize.width - 40, 6, 34, 24);
        btnClose.addActionListener(e -> System.exit(0));

        topControls.add(btnMinimize);
        topControls.add(btnClose);
        frame.getLayeredPane().add(topControls, javax.swing.JLayeredPane.PALETTE_LAYER);

        // Panel to hold components with transparent background
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(15, 0, 15, 0);

        // Title Label
        JLabel lblTitle = new JLabel("Welcome to Digital Banking & Finance Platform");
        lblTitle.setFont(new Font("Tahoma", Font.BOLD, 24));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        panel.add(lblTitle, gbc);

        // Customer Login Button
        RoundedButton btnCustomerLogin = new RoundedButton("Customer Login");
        btnCustomerLogin.setFont(new Font("Tahoma", Font.BOLD, 18));
        btnCustomerLogin.setBackground(new Color(72, 209, 204));
        btnCustomerLogin.setForeground(Color.WHITE);
        btnCustomerLogin.setPreferredSize(new Dimension(200, 50));
        gbc.gridy = 1;
        panel.add(btnCustomerLogin, gbc);

        // Admin Login Button
        RoundedButton btnAdminLogin = new RoundedButton("Admin Login");
        btnAdminLogin.setFont(new Font("Tahoma", Font.BOLD, 18));
        btnAdminLogin.setBackground(new Color(255, 99, 71));
        btnAdminLogin.setForeground(Color.WHITE);
        btnAdminLogin.setPreferredSize(new Dimension(200, 50));
        gbc.gridy = 2;
        panel.add(btnAdminLogin, gbc);

        // Add panel to background (centered)
        background.add(panel, new GridBagConstraints());

        // ActionListener for Customer Login Button
        btnCustomerLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open the Customer Login Form
                CustomerLoginPage customerLoginPage = new CustomerLoginPage();
                customerLoginPage.frame.setVisible(true);
                frame.setVisible(false);
            }
        });

        // ActionListener for Admin Login Button
        btnAdminLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open the Admin Login Form
                AdminLoginPage adminLoginPage = new AdminLoginPage();
                adminLoginPage.frame.setVisible(true);
                frame.setVisible(false);
            }
        });
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                LoginPage window = new LoginPage();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
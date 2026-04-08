package GUI;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import main.Bank;

public class MenuPage {

    private JFrame frame;
    private String accountNumber;
    private Bank bank;
    private JTextArea txtTransactions; // right panel transactions area
    private String customerFirstName; // Store customer's first name
    private JPanel rightPanel; // Reference to right panel for content switching

    public MenuPage(String accountNumber, Bank bank) {
        this.accountNumber = accountNumber;
        this.bank = bank;
        this.customerFirstName = getCustomerFirstName();
        initialize();
    }

    // Method to fetch customer's first name from database
    private String getCustomerFirstName() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT first_name FROM customers WHERE account_number = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("first_name");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "Customer"; // Default fallback if name not found
    }

    private void initialize() {
        frame = new JFrame("");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);
    

        // 🔹 Left Navigation Panel
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setPreferredSize(new Dimension(200, 600));

        // Top panel for menu items
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(6, 1, 0, 0));
        menuPanel.setBackground(Color.WHITE);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        String[] menuItems = {"Overview", "Financial hub", "Smart savings", "Repay Loan", "Support / Help Center", "Settings"};
        String[] menuIcons = {"🏠", "🎓", "📈", "💳", "🎧", "⚙️"};

        JButton[] menuButtons = new JButton[menuItems.length];

        for (int i = 0; i < menuItems.length; i++) {
            String item = menuItems[i];
            String icon = menuIcons[i];
            
            JButton btn = new JButton(icon + " " + item) {
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

            menuButtons[i] = btn;
            // Use a larger font size and fallback fonts that support emojis
            Font emojiFont = new Font("Segoe UI Symbol", Font.BOLD, 14);
            if (emojiFont.canDisplayUpTo(icon) == -1) {
                btn.setFont(emojiFont);
            } else {
                btn.setFont(new Font("Arial Unicode MS", Font.BOLD, 14));
            }
            btn.setForeground(Color.BLACK);
            btn.setBackground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setContentAreaFilled(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            final JButton[] btns = menuButtons;

            // Add hover effect
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    ((JButton)e.getComponent()).putClientProperty("isHovered", true);
                    btn.repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    ((JButton)e.getComponent()).putClientProperty("isHovered", false);
                    btn.repaint();
                }
            });

            // Overview view - already open by default
            if (item.equals("Overview")) {
                btn.setForeground(new Color(0, 123, 255));
                btn.setOpaque(true);
                btn.addActionListener(e -> {
                    resetAllButtonColors(btns);
                    btn.setForeground(new Color(0, 123, 255));
                    btn.setOpaque(true);
                    showOverviewPanel();
                });
            } else if (item.equals("Smart savings")) {
                btn.addActionListener(e -> {
                    resetAllButtonColors(btns);
                    btn.setForeground(new Color(0, 123, 255));
                    btn.setOpaque(true);
                    showSmartSavingsPanel();
                });
            } else if (item.equals("Financial hub")) {
                btn.addActionListener(e -> {
                    resetAllButtonColors(btns);
                    btn.setForeground(new Color(0, 123, 255));
                    btn.setOpaque(true);
                    showFinancialHubPanel();
                });
            } else if (item.equals("Support / Help Center")) {
                btn.addActionListener(e -> {
                    resetAllButtonColors(btns);
                    btn.setForeground(new Color(0, 123, 255));
                    btn.setOpaque(true);
                    showSupportPanel();
                });
            } else if (item.equals("Repay Loan")) {
                btn.addActionListener(e -> {
                    resetAllButtonColors(btns);
                    btn.setForeground(new Color(0, 123, 255));
                    btn.setOpaque(true);
                    showRepayLoanPanel();
                });
            } else if (item.equals("Settings")) {
                btn.addActionListener(e -> {
                    resetAllButtonColors(btns);
                    btn.setForeground(new Color(0, 123, 255));
                    btn.setOpaque(true);
                    showSettingsPanel();
                });
            }

            menuPanel.add(btn);
        }

        // Bottom panel for Logout button
        JPanel logoutPanel = new JPanel(new BorderLayout());
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
        
        btnLogout.setFont(new Font("Segoe UI Symbol", Font.BOLD, 14));
        btnLogout.setForeground(Color.BLACK);
        btnLogout.setBackground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        btnLogout.setContentAreaFilled(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
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
            new LoginPage().frame.setVisible(true);
        });
        logoutPanel.add(btnLogout);

        // Add menu panel and logout panel to left panel
        leftPanel.add(menuPanel, BorderLayout.NORTH);
        leftPanel.add(logoutPanel, BorderLayout.SOUTH);

        // Initialize txtTransactions (for compatibility with existing button actions)
        txtTransactions = new JTextArea();
        txtTransactions.setEditable(false);

        // 🔹 Right Panel (Account Overview - Full Width)
        rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(new Color(245, 245, 245));
        
        // Create overview content in right panel
        createOverviewInPanel(rightPanel);

        // Add Panels to Frame
        frame.add(leftPanel, BorderLayout.WEST);
        frame.add(rightPanel, BorderLayout.CENTER);
        
        // Display the frame in full screen
        frame.setVisible(true);
    }

    // Display Overview Panel in the right panel
    private void showOverviewPanel() {
        rightPanel.removeAll();
        createOverviewInPanel(rightPanel);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    // Helper method to reset all button colors to black
    private void resetAllButtonColors(JButton[] buttons) {
        for (JButton btn : buttons) {
            btn.setForeground(Color.BLACK);
            btn.setOpaque(false);
        }
    }

    // Display Financial Hub Panel in the right panel
    private void showFinancialHubPanel() {
        rightPanel.removeAll();
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 245));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JLabel headerLabel = new JLabel("Financial Learning Hub");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(0, 123, 255));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));

        // Get current points
        int currentPoints = bank.getRewardPoints(accountNumber);
        JLabel pointsLabel = new JLabel("Your Points: " + currentPoints + "/100");
        pointsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pointsLabel.setForeground(new Color(255, 140, 0));

        // Progress bar
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(currentPoints);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        progressBar.setForeground(new Color(34, 139, 34));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 245, 245));
        headerPanel.add(headerLabel, BorderLayout.NORTH);
        
        JPanel pointsPanel = new JPanel(new BorderLayout(10, 5));
        pointsPanel.setBackground(new Color(245, 245, 245));
        pointsPanel.add(pointsLabel, BorderLayout.WEST);
        pointsPanel.add(progressBar, BorderLayout.CENTER);
        headerPanel.add(pointsPanel, BorderLayout.CENTER);

        // Content panel with options
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 255), 2),
            "Financial Education",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(0, 123, 255)
        ));

        // Fetch quizzes from database
        java.util.List<String[]> quizzes = new java.util.ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT question_text, correct_answer FROM financial_hub_questions";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String question = rs.getString("question_text");
                String answer = rs.getString("correct_answer");
                quizzes.add(new String[]{question, answer});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JLabel errorLabel = new JLabel("Error loading questions from database: " + ex.getMessage());
            errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            errorLabel.setForeground(new Color(220, 20, 60));
            contentPanel.add(errorLabel);
        }

        int answeredCount = bank.getDailyQuizzesAnswered(accountNumber);
        final int MAX_ATTEMPTS_PER_DAY = 4;
        
        if (answeredCount >= MAX_ATTEMPTS_PER_DAY) {
            JLabel limitLabel = new JLabel("Daily Limit Reached!");
            limitLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            limitLabel.setForeground(new Color(220, 20, 60));
            limitLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            contentPanel.add(limitLabel);
            
            JLabel nextLabel = new JLabel("You have reached your daily limit of 4 questions. Please try again tomorrow.");
            nextLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            nextLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
            contentPanel.add(nextLabel);
        } else {
            // Find available questions
            java.util.List<Integer> availableIndices = new java.util.ArrayList<>();
            for (int j = 0; j < quizzes.size(); j++) {
                if (!bank.hasAnsweredQuestionToday(accountNumber, quizzes.get(j)[0])) {
                    availableIndices.add(j);
                }
            }

            int remainingAttempts = MAX_ATTEMPTS_PER_DAY - answeredCount;
            JLabel attemptsLabel = new JLabel("Attempts Remaining: " + remainingAttempts + " of " + MAX_ATTEMPTS_PER_DAY);
            attemptsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            attemptsLabel.setForeground(new Color(255, 140, 0));
            attemptsLabel.setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 20));
            contentPanel.add(attemptsLabel);

            JLabel infoLabel = new JLabel("Answer a quiz question and earn 25 points! (Each answer counts as 1 attempt)");
            infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            infoLabel.setForeground(new Color(100, 100, 100));
            infoLabel.setBorder(BorderFactory.createEmptyBorder(5, 20, 15, 20));
            contentPanel.add(infoLabel);

            for (int idx : availableIndices) {
                String question = quizzes.get(idx)[0];
                String correctAnswer = quizzes.get(idx)[1];

                JButton quizButton = createStyledButton(question, new Color(52, 152, 219));
                quizButton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
                quizButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
                quizButton.setAlignmentX(Component.LEFT_ALIGNMENT);

                quizButton.addActionListener(e -> {
                    String[] options = {"Yes", "No", "Cancel"};
                    int choice = JOptionPane.showOptionDialog(
                        frame,
                        question,
                        "Financial Hub Quiz",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[2]
                    );

                    if (choice >= 0 && choice < 2) {
                        String userAnswer = options[choice];
                        bank.recordQuestionAnswered(accountNumber, question);

                        if (userAnswer.equalsIgnoreCase(correctAnswer)) {
                            int pointsEarned = 25;
                            try {
                                bank.addFinancialHubPoints(accountNumber, pointsEarned);
                                int after = bank.getRewardPoints(accountNumber);

                                showSuccessDialog("Correct!", "You earned " + pointsEarned + " points!\nTotal: " + after + "/100");
                                
                                if (after >= 100) {
                                    showSuccessDialog("Milestone!", "Congratulations! A $50 reward has been credited and your loan limit increased by $1,000!");
                                }

                                // Refresh the Financial Hub panel
                                showFinancialHubPanel();
                                loadTransactions(txtTransactions);
                            } catch (RuntimeException ex) {
                                showErrorDialog("Error", "Error processing reward: " + ex.getMessage());
                            }
                        } else {
                            showErrorDialog("Incorrect", "The correct answer is: " + correctAnswer + "\nTry again next time.");
                        }
                    }
                });

                contentPanel.add(quizButton);
                contentPanel.add(Box.createVerticalStrut(8));
            }
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        rightPanel.add(mainPanel, BorderLayout.CENTER);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    // Display Smart Savings Panel in the right panel
    private void showSmartSavingsPanel() {
        rightPanel.removeAll();
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 245));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JLabel headerLabel = new JLabel("Smart Savings Goals");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(0, 123, 255));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 245, 245));
        headerPanel.add(headerLabel, BorderLayout.NORTH);
        JPanel actionButtonPanel = new JPanel();
        actionButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        actionButtonPanel.setBackground(new Color(245, 245, 245));
        actionButtonPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 255), 2),
            "Actions",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(0, 123, 255)
        ));

        JButton btnCreateGoal = createRoundedButton("Create Goal", new Color(0, 123, 255));
        JButton btnViewGoals = createRoundedButton("View Goals", new Color(0, 123, 255));
        JButton btnContribute = createRoundedButton("Contribute", new Color(0, 123, 255));
        JButton btnSetAutoSave = createRoundedButton("Auto-Save", new Color(0, 123, 255));
        JButton btnViewProgress = createRoundedButton("Progress", new Color(0, 123, 255));

        btnCreateGoal.setPreferredSize(new Dimension(120, 40));
        btnViewGoals.setPreferredSize(new Dimension(120, 40));
        btnContribute.setPreferredSize(new Dimension(120, 40));
        btnSetAutoSave.setPreferredSize(new Dimension(120, 40));
        btnViewProgress.setPreferredSize(new Dimension(120, 40));

        actionButtonPanel.add(btnCreateGoal);
        actionButtonPanel.add(btnViewGoals);
        actionButtonPanel.add(btnContribute);
        actionButtonPanel.add(btnSetAutoSave);
        actionButtonPanel.add(btnViewProgress);

        headerPanel.add(actionButtonPanel, BorderLayout.CENTER);

        // Content area with doughnut charts
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 255), 2),
            "Active Savings Goals",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(0, 123, 255)
        ));

        // Load and display active goals with doughnut charts
        try {
            List<Map<String, Object>> activeGoals = bank.viewActiveGoals(accountNumber);
            
            if (activeGoals.isEmpty()) {
                JLabel noGoalsLabel = new JLabel("No active savings goals yet. Create one to get started!");
                noGoalsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                noGoalsLabel.setForeground(new Color(100, 100, 100));
                noGoalsLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                contentPanel.add(noGoalsLabel);
            } else {
                for (Map<String, Object> goal : activeGoals) {
                    JPanel goalPanel = new JPanel(new BorderLayout(15, 10));
                    goalPanel.setBackground(new Color(240, 248, 255));
                    goalPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 220, 240), 1),
                        BorderFactory.createEmptyBorder(15, 15, 15, 15)
                    ));

                    String goalName = (String) goal.get("goal_name");
                    double targetAmount = ((Number) goal.get("target_amount")).doubleValue();
                    double currentAmount = ((Number) goal.get("current_amount")).doubleValue();
                    double progress = (currentAmount / targetAmount) * 100;

                    // Left: Goal info
                    JPanel infoPanel = new JPanel();
                    infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
                    infoPanel.setBackground(new Color(240, 248, 255));

                    JLabel goalNameLabel = new JLabel(goalName);
                    goalNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    goalNameLabel.setForeground(new Color(0, 0, 0));
                    infoPanel.add(goalNameLabel);
                    infoPanel.add(Box.createVerticalStrut(5));

                    JLabel amountLabel = new JLabel(String.format("$%.2f / $%.2f", currentAmount, targetAmount));
                    amountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    amountLabel.setForeground(new Color(100, 100, 100));
                    infoPanel.add(amountLabel);
                    infoPanel.add(Box.createVerticalStrut(5));

                    JLabel progressLabel = new JLabel(String.format("Progress: %.1f%%", progress));
                    progressLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    progressLabel.setForeground(new Color(0, 123, 255));
                    infoPanel.add(progressLabel);

                    // Right: Doughnut chart
                    JPanel chartPanel = new JPanel() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            drawDoughnutChart(g, (int) progress, getWidth(), getHeight());
                        }
                    };
                    chartPanel.setBackground(new Color(240, 248, 255));
                    chartPanel.setPreferredSize(new Dimension(120, 120));

                    goalPanel.add(infoPanel, BorderLayout.WEST);
                    goalPanel.add(chartPanel, BorderLayout.CENTER);

                    contentPanel.add(goalPanel);
                    contentPanel.add(Box.createVerticalStrut(10));
                }
            }
        } catch (Exception ex) {
            JLabel errorLabel = new JLabel("Error loading goals: " + ex.getMessage());
            errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            errorLabel.setForeground(new Color(220, 20, 60));
            errorLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            contentPanel.add(errorLabel);
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Button actions
        btnCreateGoal.addActionListener(e -> {
            String goalName = JOptionPane.showInputDialog(frame, "Enter goal name:");
            if (goalName != null && !goalName.trim().isEmpty()) {
                String targetStr = JOptionPane.showInputDialog(frame, "Enter target amount:");
                if (targetStr != null) {
                    try {
                        double targetAmount = Double.parseDouble(targetStr);
                        String autoSaveStr = JOptionPane.showInputDialog(frame, "Enter auto-save amount (or leave blank):");
                        double autoSaveAmount = 0.0;
                        if (autoSaveStr != null && !autoSaveStr.trim().isEmpty()) {
                            autoSaveAmount = Double.parseDouble(autoSaveStr);
                        }
                        bank.createSavingsGoal(accountNumber, goalName, targetAmount, autoSaveAmount);
                        showSuccessDialog("Success", "Savings goal created successfully!");
                        showSmartSavingsPanel();
                    } catch (NumberFormatException ex) {
                        showErrorDialog("Invalid Input", "Please enter valid numbers.");
                    } catch (Exception ex) {
                        showErrorDialog("Error", ex.getMessage());
                    }
                }
            }
        });

        btnViewGoals.addActionListener(e -> showSmartSavingsPanel());

        btnContribute.addActionListener(e -> {
            String goalName = JOptionPane.showInputDialog(frame, "Enter goal name:");
            if (goalName != null && !goalName.trim().isEmpty()) {
                String amountStr = JOptionPane.showInputDialog(frame, "Enter contribution amount:");
                if (amountStr != null) {
                    try {
                        double amount = Double.parseDouble(amountStr);
                        bank.contributeToGoal(accountNumber, goalName, amount);
                        showSuccessDialog("Success", "Contribution added to goal!");
                        showSmartSavingsPanel();
                    } catch (NumberFormatException ex) {
                        showErrorDialog("Invalid Input", "Please enter a valid amount.");
                    } catch (Exception ex) {
                        showErrorDialog("Error", ex.getMessage());
                    }
                }
            }
        });

        btnSetAutoSave.addActionListener(e -> {
            String goalName = JOptionPane.showInputDialog(frame, "Enter goal name:");
            if (goalName != null && !goalName.trim().isEmpty()) {
                String frequencyStr = JOptionPane.showInputDialog(frame, "Enter frequency (daily/weekly):");
                if (frequencyStr != null && !frequencyStr.trim().isEmpty()) {
                    String amountStr = JOptionPane.showInputDialog(frame, "Enter auto-save amount:");
                    if (amountStr != null) {
                        try {
                            double amount = Double.parseDouble(amountStr);
                            bank.setAutoSaveFrequency(accountNumber, goalName, frequencyStr, amount);
                            showSuccessDialog("Success", "Auto-save frequency set successfully!");
                            showSmartSavingsPanel();
                        } catch (NumberFormatException ex) {
                            showErrorDialog("Invalid Input", "Please enter a valid amount.");
                        } catch (Exception ex) {
                            showErrorDialog("Error", ex.getMessage());
                        }
                    }
                }
            }
        });

        btnViewProgress.addActionListener(e -> {
            String goalName = JOptionPane.showInputDialog(frame, "Enter goal name:");
            if (goalName != null && !goalName.trim().isEmpty()) {
                try {
                    Map<String, Double> progress = bank.viewGoalProgress(accountNumber, goalName);
                    if (progress.isEmpty()) {
                        showErrorDialog("Not Found", "Goal not found.");
                    } else {
                        double target = progress.get("target_amount");
                        double current = progress.get("current_amount");
                        double percent = (current / target) * 100;
                        showSuccessDialog("Goal Progress", String.format(
                            "Goal: %s\nTarget: $%.2f\nCurrent: $%.2f\nProgress: %.1f%%",
                            goalName, target, current, percent
                        ));
                    }
                } catch (Exception ex) {
                    showErrorDialog("Error", ex.getMessage());
                }
            }
        });

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        rightPanel.add(mainPanel, BorderLayout.CENTER);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    // Helper method to draw doughnut chart
    private void drawDoughnutChart(Graphics g, int progressPercent, int width, int height) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = width / 2;
        int centerY = height / 2;
        int outerRadius = Math.min(width, height) / 2 - 10;
        int innerRadius = outerRadius - 12;

        // Draw completed portion (green)
        g2.setColor(new Color(34, 139, 34));
        g2.fillArc(centerX - outerRadius, centerY - outerRadius,
                   outerRadius * 2, outerRadius * 2,
                   -90, (int) (3.6 * progressPercent));

        // Draw remaining portion (light gray)
        g2.setColor(new Color(200, 200, 200));
        g2.fillArc(centerX - outerRadius, centerY - outerRadius,
                   outerRadius * 2, outerRadius * 2,
                   (int) (-90 + 3.6 * progressPercent), (int) (360 - 3.6 * progressPercent));

        // Draw inner circle (white) to create doughnut effect
        g2.setColor(new Color(240, 248, 255));
        g2.fillOval(centerX - innerRadius, centerY - innerRadius,
                    innerRadius * 2, innerRadius * 2);

        // Draw percentage text in center
        g2.setColor(new Color(34, 139, 34));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        String percentText = progressPercent + "%";
        FontMetrics fm = g2.getFontMetrics();
        int textX = centerX - fm.stringWidth(percentText) / 2;
        int textY = centerY + (fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(percentText, textX, textY);
    }

    // Display Support/Help Centre Panel in the right panel
    private void showSupportPanel() {
        rightPanel.removeAll();

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 245));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JLabel headerLabel = new JLabel("Support & Help Centre");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(new Color(0, 123, 255));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Action buttons panel - HORIZONTAL layout at top
        JPanel actionButtonPanel = new JPanel();
        actionButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        actionButtonPanel.setBackground(new Color(245, 245, 245));
        actionButtonPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 255), 2),
            "Quick Actions",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(0, 123, 255)
        ));

        JButton btnSubmitComplaint = createRoundedButton("Submit Complaint", new Color(0, 123, 255));
        JButton btnViewResponses = createRoundedButton("View Admin Responses", new Color(0, 123, 255));
        JButton btnFAQ = createRoundedButton("FAQ", new Color(0, 123, 255));
        JButton btnContactInfo = createRoundedButton("Contact Information", new Color(0, 123, 255));

        btnSubmitComplaint.setPreferredSize(new Dimension(130, 40));
        btnViewResponses.setPreferredSize(new Dimension(150, 40));
        btnFAQ.setPreferredSize(new Dimension(100, 40));
        btnContactInfo.setPreferredSize(new Dimension(130, 40));

        actionButtonPanel.add(btnSubmitComplaint);
        actionButtonPanel.add(btnViewResponses);
        actionButtonPanel.add(btnFAQ);
        actionButtonPanel.add(btnContactInfo);

        // Top panel with header and buttons
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 245, 245));
        topPanel.add(headerLabel, BorderLayout.NORTH);
        topPanel.add(actionButtonPanel, BorderLayout.CENTER);

        // Content area
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 255), 2),
            "Support Resources",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(0, 123, 255)
        ));

        // Welcome section
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(new Color(240, 248, 255));
        welcomePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 220, 240), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel welcomeTitle = new JLabel("Welcome to Support Centre");
        welcomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        welcomeTitle.setForeground(new Color(0, 123, 255));

        JLabel welcomeText = new JLabel("<html>We're here to help! Select an action above to get started or browse the resources below.</html>");
        welcomeText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        welcomeText.setForeground(new Color(100, 100, 100));

        welcomePanel.add(welcomeTitle, BorderLayout.NORTH);
        welcomePanel.add(welcomeText, BorderLayout.CENTER);

        contentPanel.add(welcomePanel);
        contentPanel.add(Box.createVerticalStrut(15));

        // FAQ Section
        JPanel faqPanel = new JPanel(new BorderLayout());
        faqPanel.setBackground(new Color(240, 248, 255));
        faqPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 220, 240), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel faqTitle = new JLabel("Frequently Asked Questions");
        faqTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        faqTitle.setForeground(new Color(34, 139, 34));

        String faqText = "<html>" +
            "<b>Q: How do I reset my password?</b><br>A: Go to the login page and click 'Forgot Password' to receive a reset link.<br><br>" +
            "<b>Q: How long does a transfer take?</b><br>A: Transfers usually take 1-2 business days.<br><br>" +
            "<b>Q: Is my data secure?</b><br>A: Yes, we use industry-standard encryption to protect your data.<br><br>" +
            "<b>Q: How do I check my account balance?</b><br>A: Click the 'Show Balance' button in the Overview section.<br><br>" +
            "<b>Q: Can I set up automatic savings?</b><br>A: Yes, visit the Smart Savings section to configure auto-save." +
            "</html>";

        JLabel faqContent = new JLabel(faqText);
        faqContent.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        faqContent.setForeground(new Color(80, 80, 80));
        faqContent.setVerticalAlignment(JLabel.TOP);

        faqPanel.add(faqTitle, BorderLayout.NORTH);
        faqPanel.add(faqContent, BorderLayout.CENTER);

        contentPanel.add(faqPanel);
        contentPanel.add(Box.createVerticalStrut(15));

        // Contact Information Section
        JPanel contactPanel = new JPanel(new BorderLayout());
        contactPanel.setBackground(new Color(240, 248, 255));
        contactPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 220, 240), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel contactTitle = new JLabel("Contact Information");
        contactTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        contactTitle.setForeground(new Color(220, 20, 60));

        String contactText = "<html>" +
            "<b>Email:</b> support@digitalbanking.com<br>" +
            "<b>Phone:</b> +1-800-BANK-123 (Available 24/7)<br>" +
            "<b>Office Hours:</b> Monday to Friday, 8:00 AM - 6:00 PM<br>" +
            "<b>Address:</b> 123 Finance Street, Banking City, BC 12345<br>" +
            "<b>Live Chat:</b> Available during office hours" +
            "</html>";

        JLabel contactContent = new JLabel(contactText);
        contactContent.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        contactContent.setForeground(new Color(80, 80, 80));
        contactContent.setVerticalAlignment(JLabel.TOP);

        contactPanel.add(contactTitle, BorderLayout.NORTH);
        contactPanel.add(contactContent, BorderLayout.CENTER);

        contentPanel.add(contactPanel);
        contentPanel.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Button actions
        btnSubmitComplaint.addActionListener(e -> {
            String complaint = JOptionPane.showInputDialog(frame, "Enter your complaint (max 500 characters):", "");
            if (complaint != null && !complaint.trim().isEmpty()) {
                if (complaint.length() > 500) {
                    showErrorDialog("Input Too Long", "Complaint must be 500 characters or less.");
                    return;
                }
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String query = "INSERT INTO complaints (account_number, complaint_description, response) VALUES (?, ?, NULL)";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setString(1, accountNumber);
                    stmt.setString(2, complaint);
                    stmt.executeUpdate();
                    showSuccessDialog("Success", "Your complaint has been submitted successfully!\nWe will respond within 24-48 hours.");
                    showSupportPanel();
                } catch (SQLException ex) {
                    showErrorDialog("Error", "Failed to submit complaint: " + ex.getMessage());
                }
            }
        });

        btnViewResponses.addActionListener(e -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                // Fetch all complaints (both with and without responses)
                String query = "SELECT id, complaint_description, response FROM complaints WHERE account_number = ? ORDER BY id DESC";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, accountNumber);
                ResultSet rs = stmt.executeQuery();

                StringBuilder responses = new StringBuilder();
                boolean hasComplaints = false;
                while (rs.next()) {
                    hasComplaints = true;
                    int complaintId = rs.getInt("id");
                    String complaintDesc = rs.getString("complaint_description");
                    String adminResponse = rs.getString("response");

                    responses.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                    responses.append("COMPLAINT ID: ").append(complaintId).append("\n\n");
                    responses.append("YOUR COMPLAINT:\n").append(complaintDesc).append("\n\n");
                    responses.append("ADMIN RESPONSE:\n");
                    if (adminResponse != null && !adminResponse.isEmpty()) {
                        responses.append(adminResponse).append("\n");
                    } else {
                        responses.append("[Response Pending - Admin is reviewing your complaint]\n");
                    }
                    responses.append("\n");
                }

                if (!hasComplaints) {
                    showSuccessDialog("No Complaints", "You have not submitted any complaints yet.");
                } else {
                    JTextArea textArea = new JTextArea(responses.toString());
                    textArea.setEditable(false);
                    textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                    textArea.setLineWrap(true);
                    textArea.setWrapStyleWord(true);
                    textArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
                    textArea.setBackground(new Color(240, 248, 255));
                    textArea.setForeground(new Color(50, 50, 50));

                    JScrollPane scrollPane2 = new JScrollPane(textArea);
                    
                    // Create a dialog to display responses centered on screen
                    JDialog dialog = new JDialog(frame, "Your Complaints & Responses", true);
                    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    dialog.setSize(700, 600);
                    dialog.setLocationRelativeTo(frame); // Center on frame
                    dialog.add(scrollPane2, BorderLayout.CENTER);
                    
                    // Add a close button at the bottom
                    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
                    JButton closeBtn = new JButton("Close");
                    closeBtn.addActionListener(ae -> {
                        dialog.dispose();
                       // Remove red dot when dialog is closed
                    });
                    buttonPanel.add(closeBtn);
                    dialog.add(buttonPanel, BorderLayout.SOUTH);
                    
                    dialog.setVisible(true);
                }
            } catch (SQLException ex) {
                showErrorDialog("Error", "Failed to retrieve responses: " + ex.getMessage());
            }
        });

        btnFAQ.addActionListener(e -> {
            String faqContent2 = "FREQUENTLY ASKED QUESTIONS\n\n" +
                "1. How do I reset my password?\n" +
                "   Go to login and click 'Forgot Password' to receive a reset link.\n\n" +
                "2. How long does a transfer take?\n" +
                "   Transfers usually take 1-2 business days.\n\n" +
                "3. Is my data secure?\n" +
                "   Yes, we use industry-standard encryption.\n\n" +
                "4. How do I check my account balance?\n" +
                "   Click 'Show Balance' in the Overview section.\n\n" +
                "5. Can I set up automatic savings?\n" +
                "   Yes, visit Smart Savings to configure auto-save.\n\n" +
                "6. What are the transaction limits?\n" +
                "   Daily limit: $10,000 | Monthly limit: $100,000\n\n" +
                "7. How do I dispute a transaction?\n" +
                "   Submit a complaint in the Support centre.\n\n" +
                "8. Are fees applicable?\n" +
                "   Standard transactions are fee-free.";

            JTextArea textArea = new JTextArea(faqContent2);
            textArea.setEditable(false);
            textArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            textArea.setBackground(new Color(240, 248, 255));
            textArea.setForeground(new Color(50, 50, 50));

            JScrollPane scrollPane2 = new JScrollPane(textArea);
            JOptionPane.showMessageDialog(frame, scrollPane2, "FAQ", JOptionPane.INFORMATION_MESSAGE);
        });

        btnContactInfo.addActionListener(e -> {
            String contactDetails = "CONTACT INFORMATION\n\n" +
                "📧 EMAIL:\n   support@digitalbanking.com\n\n" +
                "📞 PHONE:\n   +1-800-BANK-123 (24/7 Support)\n\n" +
                "🏢 PHYSICAL ADDRESS:\n   123 Finance Street\n   Banking City, BC 12345\n\n" +
                "⏰ OFFICE HOURS:\n   Monday - Friday: 8:00 AM - 6:00 PM\n   Saturday - Sunday: Closed\n\n" +
                "💬 LIVE CHAT:\n   Available during office hours on our website\n\n" +
                "📱 MOBILE APP SUPPORT:\n   In-app help is available 24/7\n\n" +
                "⚠️ EMERGENCY SUPPORT:\n   For urgent matters, call our hotline at any time.";

            JTextArea textArea = new JTextArea(contactDetails);
            textArea.setEditable(false);
            textArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            textArea.setBackground(new Color(240, 248, 255));
            textArea.setForeground(new Color(50, 50, 50));

            JScrollPane scrollPane2 = new JScrollPane(textArea);
            JOptionPane.showMessageDialog(frame, scrollPane2, "Contact Information", JOptionPane.INFORMATION_MESSAGE);
        });

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        rightPanel.add(mainPanel, BorderLayout.CENTER);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    // Display Repay Loan Panel in the right panel
    private void showRepayLoanPanel() {
        rightPanel.removeAll();

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 245));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JLabel headerLabel = new JLabel("Repay Loan");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(new Color(0, 123, 255));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Action buttons panel
        JPanel actionButtonPanel = new JPanel();
        actionButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        actionButtonPanel.setBackground(new Color(245, 245, 245));
        actionButtonPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 255), 2),
            "Quick Actions",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(0, 123, 255)
        ));

        JButton btnRepayLoan = createRoundedButton("Make Payment", new Color(0, 123, 255));
        JButton btnViewBalance = createRoundedButton("View Balance", new Color(0, 123, 255));
        JButton btnLoanHistory = createRoundedButton("Payment History", new Color(0, 123, 255));

        btnRepayLoan.setPreferredSize(new Dimension(130, 40));
        btnViewBalance.setPreferredSize(new Dimension(130, 40));
        btnLoanHistory.setPreferredSize(new Dimension(130, 40));

        actionButtonPanel.add(btnRepayLoan);
        actionButtonPanel.add(btnViewBalance);
        actionButtonPanel.add(btnLoanHistory);

        // Top panel with header and buttons
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 245, 245));
        topPanel.add(headerLabel, BorderLayout.NORTH);
        topPanel.add(actionButtonPanel, BorderLayout.CENTER);

        // Content area - Loans table
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 255), 2),
            "Your Active Loans",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(0, 123, 255)
        ));

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT lr.id, lr.loan_type, lr.loan_amount, lr.interest_rate, lr.loan_term_months, " +
                "COALESCE(SUM(r.repayment_amount), 0) AS total_repaid " +
                "FROM loan_requests lr " +
                "LEFT JOIN repayments r ON lr.id = r.loan_id " +
                "WHERE lr.account_number = ? AND lr.status = 'Approve' " +
                "GROUP BY lr.id, lr.loan_type, lr.loan_amount, lr.interest_rate, lr.loan_term_months"
            );
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            List<Object[]> loanData = new java.util.ArrayList<>();
            while (rs.next()) {
                double principal = rs.getDouble("loan_amount");
                double interestRate = rs.getDouble("interest_rate");
                int termMonths = rs.getInt("loan_term_months");
                double totalRepaid = rs.getDouble("total_repaid");

                double totalInterest = principal * (interestRate / 100.0) * (termMonths / 12.0);
                double totalWithInterest = principal + totalInterest;
                double remaining = totalWithInterest - totalRepaid;

                if (remaining > 0.01) {
                    loanData.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("loan_type"),
                        principal,
                        interestRate,
                        termMonths,
                        totalWithInterest,
                        remaining,
                        totalRepaid
                    });
                }
            }

            if (loanData.isEmpty()) {
                JLabel noLoansLabel = new JLabel("No active loans to repay!");
                noLoansLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                noLoansLabel.setForeground(new Color(100, 100, 100));
                noLoansLabel.setHorizontalAlignment(JLabel.CENTER);
                noLoansLabel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));
                contentPanel.add(noLoansLabel, BorderLayout.CENTER);
            } else {
                String[] columnNames = {"Loan ID", "Type", "Principal", "Rate (%)", "Term (Mo.)", "Total", "Remaining", "Paid"};
                Object[][] data = loanData.toArray(new Object[0][]);
                JTable table = new JTable(data, columnNames);
                table.setRowHeight(25);
                table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
                table.getTableHeader().setBackground(new Color(0, 123, 255));
                table.getTableHeader().setForeground(Color.WHITE);
                table.setSelectionBackground(new Color(100, 180, 255));

                JScrollPane scrollPane = new JScrollPane(table);
                scrollPane.setBorder(BorderFactory.createEmptyBorder());
                contentPanel.add(scrollPane, BorderLayout.CENTER);

                // Button actions
                btnRepayLoan.addActionListener(e -> {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow == -1) {
                        showErrorDialog("No Selection", "Please select a loan from the table to make a payment.");
                        return;
                    }

                    int loanId = (int) data[selectedRow][0];
                    double remainingBalance = (double) data[selectedRow][6];

                    String repaymentAmountStr = JOptionPane.showInputDialog(frame,
                        "Enter payment amount (Max: $" + String.format("%.2f", remainingBalance) + "):", "");

                    if (repaymentAmountStr == null || repaymentAmountStr.trim().isEmpty()) return;

                    try {
                        double repaymentAmount = Double.parseDouble(repaymentAmountStr);
                        if (repaymentAmount <= 0 || repaymentAmount > remainingBalance) {
                            showErrorDialog("Invalid Amount", "Please enter an amount between $0.01 and $" + String.format("%.2f", remainingBalance));
                            return;
                        }

                        try (Connection conn2 = DatabaseConnection.getConnection()) {
                            // Update customer balance
                            String updateCustomerQuery = "UPDATE customers SET balance = balance - ? WHERE account_number = ?";
                            try (PreparedStatement updateCustomerStmt = conn2.prepareStatement(updateCustomerQuery)) {
                                updateCustomerStmt.setDouble(1, repaymentAmount);
                                updateCustomerStmt.setString(2, accountNumber);
                                updateCustomerStmt.executeUpdate();
                            }

                            // Insert repayment record
                            String insertRepaymentQuery = "INSERT INTO repayments (loan_id, repayment_amount, repayment_date) VALUES (?, ?, NOW())";
                            try (PreparedStatement insertRepaymentStmt = conn2.prepareStatement(insertRepaymentQuery)) {
                                insertRepaymentStmt.setInt(1, loanId);
                                insertRepaymentStmt.setDouble(2, repaymentAmount);
                                insertRepaymentStmt.executeUpdate();
                            }

                            // Insert transaction record
                            String insertTransactionQuery = "INSERT INTO transactions (account_number, amount, description, date) VALUES (?, ?, ?, NOW())";
                            try (PreparedStatement insertTransactionStmt = conn2.prepareStatement(insertTransactionQuery)) {
                                insertTransactionStmt.setString(1, accountNumber);
                                insertTransactionStmt.setDouble(2, repaymentAmount);
                                insertTransactionStmt.setString(3, "Loan Repayment for Loan ID: " + loanId);
                                insertTransactionStmt.executeUpdate();
                            }

                            showSuccessDialog("Payment Successful", "Payment of $" + String.format("%.2f", repaymentAmount) + " has been processed!");
                            showRepayLoanPanel();
                        }
                    } catch (NumberFormatException ex) {
                        showErrorDialog("Invalid Input", "Please enter a valid amount.");
                    } catch (SQLException ex) {
                        showErrorDialog("Error", "Failed to process payment: " + ex.getMessage());
                    }
                });

                btnViewBalance.addActionListener(e -> {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow == -1) {
                        showErrorDialog("No Selection", "Please select a loan to view balance.");
                        return;
                    }

                    String loanType = (String) data[selectedRow][1];
                    double principal = (double) data[selectedRow][2];
                    double rate = (double) data[selectedRow][3];
                    double total = (double) data[selectedRow][5];
                    double remaining = (double) data[selectedRow][6];
                    double paid = (double) data[selectedRow][7];

                    String balanceInfo = String.format(
                        "LOAN DETAILS\n\n" +
                        "Loan Type: %s\n" +
                        "Principal Amount: $%.2f\n" +
                        "Interest Rate: %.2f%%\n\n" +
                        "Total Amount (with Interest): $%.2f\n" +
                        "Amount Paid So Far: $%.2f\n" +
                        "Remaining Balance: $%.2f\n\n" +
                        "Payment Progress: %.1f%%",
                        loanType, principal, rate, total, paid, remaining,
                        (paid / total) * 100
                    );

                    JTextArea textArea = new JTextArea(balanceInfo);
                    textArea.setEditable(false);
                    textArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    textArea.setBackground(new Color(240, 248, 255));
                    textArea.setForeground(new Color(50, 50, 50));

                    JScrollPane scrollPane2 = new JScrollPane(textArea);
                    JOptionPane.showMessageDialog(frame, scrollPane2, "Loan Balance Details", JOptionPane.INFORMATION_MESSAGE);
                });

                btnLoanHistory.addActionListener(e -> {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow == -1) {
                        showErrorDialog("No Selection", "Please select a loan to view payment history.");
                        return;
                    }

                    int loanId = (int) data[selectedRow][0];

                    try (Connection conn2 = DatabaseConnection.getConnection()) {
                        String historyQuery = "SELECT repayment_amount, repayment_date FROM repayments WHERE loan_id = ? ORDER BY repayment_date DESC";
                        PreparedStatement historyStmt = conn2.prepareStatement(historyQuery);
                        historyStmt.setInt(1, loanId);
                        ResultSet historyRs = historyStmt.executeQuery();

                        StringBuilder history = new StringBuilder("PAYMENT HISTORY\n\n");
                        boolean hasPayments = false;
                        double totalPaid = 0;

                        while (historyRs.next()) {
                            hasPayments = true;
                            double amount = historyRs.getDouble("repayment_amount");
                            String date = historyRs.getString("repayment_date");
                            totalPaid += amount;
                            history.append("Date: ").append(date).append("\n")
                                   .append("Amount: $").append(String.format("%.2f", amount)).append("\n\n");
                        }

                        if (!hasPayments) {
                            history.append("No payments made yet on this loan.");
                        } else {
                            history.insert(0, "Total Payments: $" + String.format("%.2f", totalPaid) + "\n\n");
                        }

                        JTextArea textArea = new JTextArea(history.toString());
                        textArea.setEditable(false);
                        textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
                        textArea.setLineWrap(true);
                        textArea.setWrapStyleWord(true);
                        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                        textArea.setBackground(new Color(240, 248, 255));
                        textArea.setForeground(new Color(50, 50, 50));

                        JScrollPane scrollPane2 = new JScrollPane(textArea);
                        JOptionPane.showMessageDialog(frame, scrollPane2, "Payment History - Loan #" + loanId, JOptionPane.INFORMATION_MESSAGE);
                    } catch (SQLException ex) {
                        showErrorDialog("Error", "Failed to retrieve payment history: " + ex.getMessage());
                    }
                });
            }
        } catch (SQLException ex) {
            JLabel errorLabel = new JLabel("Error loading loans: " + ex.getMessage());
            errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            errorLabel.setForeground(new Color(220, 20, 60));
            errorLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            contentPanel.add(errorLabel, BorderLayout.CENTER);
        }

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        rightPanel.add(mainPanel, BorderLayout.CENTER);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    // Display Settings Panel in the right panel
    private void showSettingsPanel() {
        rightPanel.removeAll();

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 245));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JLabel headerLabel = new JLabel("Settings");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(new Color(0, 123, 255));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Action buttons panel - HORIZONTAL layout at top
        JPanel actionButtonPanel = new JPanel();
        actionButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        actionButtonPanel.setBackground(new Color(245, 245, 245));
        actionButtonPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 255), 2),
            "Quick Actions",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(0, 123, 255)
        ));

        JButton btnChangeEmail = createRoundedButton("Change Email", new Color(0, 123, 255));
        JButton btnChangeUsername = createRoundedButton("Change Username", new Color(0, 123, 255));
        JButton btnChangePassword = createRoundedButton("Change Password", new Color(0, 123, 255));
        JButton btnPreferences = createRoundedButton("Preferences", new Color(0, 123, 255));

        btnChangeEmail.setPreferredSize(new Dimension(130, 40));
        btnChangeUsername.setPreferredSize(new Dimension(130, 40));
        btnChangePassword.setPreferredSize(new Dimension(130, 40));
        btnPreferences.setPreferredSize(new Dimension(130, 40));

        actionButtonPanel.add(btnChangeEmail);
        actionButtonPanel.add(btnChangeUsername);
        actionButtonPanel.add(btnChangePassword);
        actionButtonPanel.add(btnPreferences);

        // Top panel with header and buttons
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 245, 245));
        topPanel.add(headerLabel, BorderLayout.NORTH);
        topPanel.add(actionButtonPanel, BorderLayout.CENTER);

        // Content area
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 255), 2),
            "Settings Information",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(0, 123, 255)
        ));

        // Account Information Section
        JPanel accountPanel = new JPanel(new BorderLayout());
        accountPanel.setBackground(new Color(240, 245, 250));
        accountPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 220, 240), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel accountTitle = new JLabel("Account Information");
        accountTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        accountTitle.setForeground(new Color(0, 123, 255));

        String accountInfo = "<html>" +
            "<b>Account Number:</b> " + accountNumber + "<br><br>" +
            "<b>Account Status:</b> Active<br><br>" +
            "<b>Member Since:</b> 2024<br><br>" +
            "<b>Account Type:</b> Premium" +
            "</html>";

        JLabel accountContent = new JLabel(accountInfo);
        accountContent.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        accountContent.setForeground(new Color(80, 80, 80));

        accountPanel.add(accountTitle, BorderLayout.NORTH);
        accountPanel.add(accountContent, BorderLayout.CENTER);

        contentPanel.add(accountPanel);
        contentPanel.add(Box.createVerticalStrut(15));

        // Security Section
        JPanel securityPanel = new JPanel(new BorderLayout());
        securityPanel.setBackground(new Color(255, 240, 245));
        securityPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(240, 200, 220), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel securityTitle = new JLabel("Security & Privacy");
        securityTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        securityTitle.setForeground(new Color(220, 20, 60));

        String securityInfo = "<html>" +
            "🔐 <b>Two-Factor Authentication:</b> Not Enabled<br><br>" +
            "🛡️ <b>Security Level:</b> High<br><br>" +
            "📱 <b>Login Devices:</b> 1 active device<br><br>" +
            "🔔 <b>Security Alerts:</b> Enabled" +
            "</html>";

        JLabel securityContent = new JLabel(securityInfo);
        securityContent.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        securityContent.setForeground(new Color(80, 80, 80));

        securityPanel.add(securityTitle, BorderLayout.NORTH);
        securityPanel.add(securityContent, BorderLayout.CENTER);

        contentPanel.add(securityPanel);
        contentPanel.add(Box.createVerticalStrut(15));

        // Notification Preferences Section
        JPanel notificationPanel = new JPanel(new BorderLayout());
        notificationPanel.setBackground(new Color(240, 250, 240));
        notificationPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 240, 200), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel notificationTitle = new JLabel("Notification Preferences");
        notificationTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        notificationTitle.setForeground(new Color(34, 139, 34));

        String notificationInfo = "<html>" +
            "📧 <b>Email Notifications:</b> Enabled<br><br>" +
            "📲 <b>SMS Alerts:</b> Enabled<br><br>" +
            "🔔 <b>Transaction Alerts:</b> Enabled<br><br>" +
            "💬 <b>Marketing Communications:</b> Disabled" +
            "</html>";

        JLabel notificationContent = new JLabel(notificationInfo);
        notificationContent.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        notificationContent.setForeground(new Color(80, 80, 80));

        notificationPanel.add(notificationTitle, BorderLayout.NORTH);
        notificationPanel.add(notificationContent, BorderLayout.CENTER);

        contentPanel.add(notificationPanel);
        contentPanel.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Button actions
        btnChangeEmail.addActionListener(e -> {
            String newEmail = JOptionPane.showInputDialog(frame, "Enter your new email address:", "");
            if (newEmail != null && !newEmail.trim().isEmpty()) {
                if (!newEmail.contains("@")) {
                    showErrorDialog("Invalid Email", "Please enter a valid email address.");
                    return;
                }
                try {
                    bank.updateProfile(accountNumber, null, newEmail);
                    showSuccessDialog("Success", "Email updated successfully!\nPlease verify your new email address.");
                    showSettingsPanel();
                } catch (Exception ex) {
                    showErrorDialog("Error", "Failed to update email: " + ex.getMessage());
                }
            }
        });

        btnChangeUsername.addActionListener(e -> {
            String newUsername = JOptionPane.showInputDialog(frame, "Enter your new username (3-20 characters):", "");
            if (newUsername != null && !newUsername.trim().isEmpty()) {
                if (newUsername.length() < 3 || newUsername.length() > 20) {
                    showErrorDialog("Invalid Username", "Username must be between 3 and 20 characters.");
                    return;
                }
                try {
                    bank.updateProfile(accountNumber, newUsername, null);
                    showSuccessDialog("Success", "Username updated successfully!");
                    showSettingsPanel();
                } catch (Exception ex) {
                    showErrorDialog("Error", "Failed to update username: " + ex.getMessage());
                }
            }
        });

        btnChangePassword.addActionListener(e -> {
            JPasswordField oldPasswordField = new JPasswordField();
            JPasswordField newPasswordField = new JPasswordField();
            JPasswordField confirmPasswordField = new JPasswordField();

            Object[] fields = {
                "Current Password:", oldPasswordField,
                "New Password:", newPasswordField,
                "Confirm Password:", confirmPasswordField
            };

            int result = JOptionPane.showConfirmDialog(frame, fields, "Change Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String oldPassword = new String(oldPasswordField.getPassword());
                String newPassword = new String(newPasswordField.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());

                if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    showErrorDialog("Empty Fields", "All password fields are required.");
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    showErrorDialog("Password Mismatch", "New password and confirmation do not match.");
                    return;
                }

                if (newPassword.length() < 6) {
                    showErrorDialog("Weak Password", "Password must be at least 6 characters long.");
                    return;
                }

                try {
                    bank.changePassword(accountNumber, newPassword);
                    showSuccessDialog("Success", "Password changed successfully!\nPlease log in again with your new password.");
                    showSettingsPanel();
                } catch (Exception ex) {
                    showErrorDialog("Error", "Failed to change password: " + ex.getMessage());
                }
            }
        });

        btnPreferences.addActionListener(e -> {
            String preferencesInfo = "ACCOUNT PREFERENCES\n\n" +
                "NOTIFICATION SETTINGS:\n" +
                "✓ Email Notifications - Disabled\n" +
                "✓ SMS Alerts - Disabled\n" +
                "✓ Transaction Alerts - Disabled\n" +
                "✗ Marketing Communications - Disabled\n\n" +
                "PRIVACY SETTINGS:\n" +
                "✓ Show Account Balance - Public (in your profile)\n" +
                "✓ Allow Contact - Friends Only\n\n" +
                "SECURITY SETTINGS:\n" +
                "✓ Two-Factor Authentication - Not Active\n" +
                "✓ Session Timeout - 30 minutes\n" +
                "✓ Login Alerts - Enabled\n\n" +
                "LANGUAGE & DISPLAY:\n" +
                "Language: English\n" +
                "Theme: Light\n" +
                "Date Format: MM/DD/YYYY\n" +
                "Currency: USD ($)";

            JTextArea textArea = new JTextArea(preferencesInfo);
            textArea.setEditable(false);
            textArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            textArea.setBackground(new Color(240, 245, 250));
            textArea.setForeground(new Color(50, 50, 50));

            JScrollPane scrollPane2 = new JScrollPane(textArea);
            JOptionPane.showMessageDialog(frame, scrollPane2, "Account Preferences", JOptionPane.INFORMATION_MESSAGE);
        });

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        rightPanel.add(mainPanel, BorderLayout.CENTER);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    // 🔹 Fetch and display first 5 transactions from DB
    private void loadTransactions(JTextArea txtTransactions) {
        try {
            String history = bank.getTransactionHistory(accountNumber);
            String[] lines = history.split("\n");
            StringBuilder first5 = new StringBuilder();
            int count = 0;
            for (String line : lines) {
                if (count >= 5) break;
                if (!line.trim().isEmpty()) {
                    first5.append(line).append("\n");
                    count++;
                }
            }
            txtTransactions.setText(first5.toString().isEmpty() ? "No transactions yet." : first5.toString());
        } catch (Exception e) {
            txtTransactions.setText("Error loading transactions: " + e.getMessage());
        }
    }

    // Create Overview content in the right panel (embedded, not dialog)
    private void createOverviewInPanel(JPanel rightPanel) {
        // Query customer and account tables
        String firstName = "N/A", lastName = "", email = "N/A", phone_number = "N/A";
        String accountType = "N/A";
        double balance = 0.0;

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT first_name, last_name, email, phone_number FROM customers WHERE account_number = ?");
            ps.setString(1, accountNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                firstName = rs.getString("first_name") != null ? rs.getString("first_name") : firstName;
                lastName = rs.getString("last_name") != null ? rs.getString("last_name") : lastName;
                email = rs.getString("email") != null ? rs.getString("email") : email;
                phone_number = rs.getString("phone_number") != null ? rs.getString("phone_number") : phone_number;
            }

            PreparedStatement ps2 = conn.prepareStatement("SELECT account_type, balance FROM customers WHERE account_number = ?");
            ps2.setString(1, accountNumber);
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) {
                accountType = rs2.getString("account_type") != null ? rs2.getString("account_type") : accountType;
                try { balance = rs2.getDouble("balance"); } catch (Exception ignore) {}
            }
        } catch (SQLException ex) {
            JLabel errorLabel = new JLabel("Unable to load overview");
            errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            rightPanel.add(errorLabel, BorderLayout.CENTER);
            return;
        }

        // Create main panel with scroll capability
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(245, 245, 245));

        // Welcome message (Top)
        JLabel lblTitle = new JLabel("Welcome to Your Account, " + customerFirstName + "!", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
        lblTitle.setForeground(new Color(0, 123, 255));

        // Account info section (Top)
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 15, 12));
        infoPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0, 123, 255), 2), "Account Information", 0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(0, 123, 255)));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setOpaque(true);

        JLabel[] labels = {
            createStyledLabel("Name:", 12, Font.BOLD),
            createStyledLabel(firstName + (lastName.isEmpty() ? "" : " " + lastName), 12, Font.PLAIN),
            createStyledLabel("Account Number:", 12, Font.BOLD),
            createStyledLabel(accountNumber, 12, Font.PLAIN),
            createStyledLabel("Account Type:", 12, Font.BOLD),
            createStyledLabel(accountType, 12, Font.PLAIN),
            createStyledLabel("Balance:", 12, Font.BOLD),
            createStyledLabel(String.format("$%.2f", balance), 12, Font.PLAIN, new Color(34, 139, 34)),
            createStyledLabel("Email:", 12, Font.BOLD),
            createStyledLabel(email, 12, Font.PLAIN),
            createStyledLabel("Phone:", 12, Font.BOLD),
            createStyledLabel(phone_number, 12, Font.PLAIN)
        };

        for (JLabel lbl : labels) {
            infoPanel.add(lbl);
        }

        // Button actions panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        buttonPanel.setBackground(new Color(245, 245, 245));

        JButton btnShowBalance = createRoundedButton("💰 Show Balance", new Color(52, 152, 219));
        JButton btnDeposit = createRoundedButton("⬇️ Deposit", new Color(52, 152, 219));
        JButton btnWithdraw = createRoundedButton("⬆️ Withdraw", new Color(52, 152, 219));
        JButton btnTransfer = createRoundedButton("➡️ Transfer", new Color(52, 152, 219));
        JButton btnRequestLoan = createRoundedButton("💳 Request Loan", new Color(52, 152, 219));

        // Use array to hold Runnable reference for transactions
        final Runnable[] loadOverviewTransactionsRef = new Runnable[1];

        // Button listeners
        btnShowBalance.addActionListener(e -> {
            try {
                double currentBalance = bank.getBalance(accountNumber);
                JPanel balancePanel = new JPanel(new BorderLayout(15, 15));
                balancePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                balancePanel.setBackground(new Color(240, 248, 255));

                JLabel balanceLabel = new JLabel("Your Current Balance:");
                balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

                JLabel amountLabel = new JLabel(String.format("$%.2f", currentBalance));
                amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
                amountLabel.setForeground(new Color(34, 139, 34));
                amountLabel.setHorizontalAlignment(SwingConstants.CENTER);

                balancePanel.add(balanceLabel, BorderLayout.NORTH);
                balancePanel.add(amountLabel, BorderLayout.CENTER);

                JOptionPane.showMessageDialog(frame, balancePanel, "Account Balance", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                showErrorDialog("Error", "Unable to fetch balance: " + ex.getMessage());
            }
        });

        btnDeposit.addActionListener(e -> {
            String amountStr = JOptionPane.showInputDialog(frame, "Enter deposit amount:");
            if (amountStr != null) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    if (amount <= 0) {
                        showErrorDialog("Invalid Amount", "Deposit amount must be greater than zero.");
                        return;
                    }
                    bank.deposit(accountNumber, amount);
                    showSuccessDialog("Deposit successful!", "Your deposit of $" + String.format("%.2f", amount) + " has been credited.");
                    loadTransactions(txtTransactions);
                    if (loadOverviewTransactionsRef[0] != null) loadOverviewTransactionsRef[0].run();
                } catch (NumberFormatException ex) {
                    showErrorDialog("Invalid Amount", "Please enter a valid number.");
                }
            }
        });

        btnWithdraw.addActionListener(e -> {
            String amountStr = JOptionPane.showInputDialog(frame, "Enter withdrawal amount:");
            if (amountStr != null) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    if (amount <= 0) {
                        showErrorDialog("Invalid Amount", "Withdrawal amount must be greater than zero.");
                        return;
                    }
                    bank.withdraw(accountNumber, amount);
                    showSuccessDialog("Withdrawal successful!", "You have withdrawn $" + String.format("%.2f", amount) + " from your account.");
                    loadTransactions(txtTransactions);
                    if (loadOverviewTransactionsRef[0] != null) loadOverviewTransactionsRef[0].run();
                } catch (NumberFormatException ex) {
                    showErrorDialog("Invalid Amount", "Please enter a valid number.");
                } catch (IllegalArgumentException ex) {
                    showErrorDialog("Error", ex.getMessage());
                }
            }
        });

        btnTransfer.addActionListener(e -> {
            String recipient = JOptionPane.showInputDialog(frame, "Enter recipient account number:");
            if (recipient != null && !recipient.trim().isEmpty()) {
                String amountStr = JOptionPane.showInputDialog(frame, "Enter amount to transfer:");
                if (amountStr != null) {
                    try {
                        double amount = Double.parseDouble(amountStr);
                        if (amount <= 0) {
                            showErrorDialog("Invalid Amount", "Transfer amount must be greater than zero.");
                            return;
                        }
                        bank.transfer(accountNumber, recipient, amount);
                        showSuccessDialog("Transfer successful!", "You have transferred $" + String.format("%.2f", amount) + " to account " + recipient + ".");
                        loadTransactions(txtTransactions);
                        if (loadOverviewTransactionsRef[0] != null) loadOverviewTransactionsRef[0].run();
                    } catch (NumberFormatException ex) {
                        showErrorDialog("Invalid Amount", "Please enter a valid number.");
                    } catch (IllegalArgumentException ex) {
                        showErrorDialog("Error", ex.getMessage());
                    }
                }
            }
        });

        btnRequestLoan.addActionListener(e -> {
            // Check if customer has any existing active (not fully repaid) loans
            try (Connection conn = DatabaseConnection.getConnection()) {
                // Check for approved loans that are NOT fully repaid
                String query = "SELECT lr.id, lr.loan_amount, COALESCE(SUM(r.repayment_amount), 0) as total_repaid " +
                               "FROM loan_requests lr " +
                               "LEFT JOIN repayments r ON lr.id = r.loan_id " +
                               "WHERE lr.account_number = ? AND lr.status = 'Approve' " +
                               "GROUP BY lr.id, lr.loan_amount " +
                               "HAVING lr.loan_amount > COALESCE(SUM(r.repayment_amount), 0)";
                
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, accountNumber);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    showErrorDialog("Loan Restriction", "You already have an active loan that is not fully repaid. Please repay your existing loan before applying for a new one.");
                    return;
                }
                
                // Check for pending loans to prevent duplicate applications
                query = "SELECT COUNT(*) as pending_loans FROM loan_requests WHERE account_number = ? AND status = 'Pending'";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, accountNumber);
                rs = stmt.executeQuery();
                
                if (rs.next() && rs.getInt("pending_loans") > 0) {
                    showErrorDialog("Pending Loan Application", "You already have a pending loan application. Please wait for admin approval.");
                    return;
                }
            } catch (SQLException ex) {
                showErrorDialog("Error", "Failed to check loan status: " + ex.getMessage());
                return;
            }
            
            // Customer can apply for a loan if no active unpaid or pending loans
            new RequestLoanPage(accountNumber, bank).show();
        });

        buttonPanel.add(btnShowBalance);
        buttonPanel.add(btnDeposit);
        buttonPanel.add(btnWithdraw);
        buttonPanel.add(btnTransfer);
        buttonPanel.add(btnRequestLoan);

        // Transaction panel
        JPanel transactionPanel = new JPanel(new BorderLayout());
        transactionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0, 123, 255), 2), "Latest 5 Transactions", 0, 0, new Font("Segoe UI", Font.BOLD, 14), new Color(0, 123, 255)));
        transactionPanel.setBackground(Color.WHITE);
        transactionPanel.setOpaque(true);

        JTextArea txnArea = new JTextArea();
        txnArea.setEditable(false);
        txnArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txnArea.setBackground(new Color(240, 248, 255));
        txnArea.setLineWrap(true);
        txnArea.setWrapStyleWord(true);

        JButton btnRefreshTxn = createRoundedButton("Refresh", new Color(0, 123, 255));
        btnRefreshTxn.setPreferredSize(new Dimension(100, 32));
        
        JButton btnDownloadTxn = createRoundedButton("Download History", new Color(0, 123, 255));
        btnDownloadTxn.setPreferredSize(new Dimension(140, 32));

        Runnable loadOverviewTransactions = () -> {
            try {
                String history = bank.getTransactionHistory(accountNumber);
                String[] lines = history.split("\n");
                StringBuilder first5 = new StringBuilder();
                int count = 0;
                for (String line : lines) {
                    if (count >= 5) break;
                    if (!line.trim().isEmpty()) {
                        first5.append(line).append("\n");
                        count++;
                    }
                }
                txnArea.setText(first5.toString().isEmpty() ? "No transactions yet." : first5.toString());
            } catch (Exception e) {
                txnArea.setText("Error loading transactions: " + e.getMessage());
            }
        };

        loadOverviewTransactionsRef[0] = loadOverviewTransactions;
        loadOverviewTransactions.run();

        btnRefreshTxn.addActionListener(e -> loadOverviewTransactions.run());
        
        btnDownloadTxn.addActionListener(e -> downloadTransactionHistory());

        JScrollPane txnScroll = new JScrollPane(txnArea);
        txnScroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel txnButtonPanel = new JPanel(new BorderLayout());
        txnButtonPanel.add(txnScroll, BorderLayout.CENTER);
        
        JPanel buttonPanelBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanelBottom.add(btnRefreshTxn);
        buttonPanelBottom.add(btnDownloadTxn);
        txnButtonPanel.add(buttonPanelBottom, BorderLayout.SOUTH);

        transactionPanel.add(txnButtonPanel, BorderLayout.CENTER);

        // Combine panels
        JPanel centerBottomPanel = new JPanel(new BorderLayout(10, 10));
        centerBottomPanel.setBackground(new Color(245, 245, 245));
        centerBottomPanel.add(buttonPanel, BorderLayout.NORTH);
        centerBottomPanel.add(transactionPanel, BorderLayout.CENTER);

        // Create top section with welcome and info
        JPanel topSection = new JPanel(new BorderLayout(0, 10));
        topSection.setBackground(new Color(245, 245, 245));
        topSection.add(lblTitle, BorderLayout.NORTH);
        topSection.add(infoPanel, BorderLayout.CENTER);

        mainPanel.add(topSection, BorderLayout.NORTH);
        mainPanel.add(centerBottomPanel, BorderLayout.CENTER);

        // Add scrollpane to right panel
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        rightPanel.add(scrollPane, BorderLayout.CENTER);
    }

    // Helper method to create rounded buttons for left panel
    private JButton createRoundedButton(String text, Color bgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw rounded rectangle background
                g2.setColor(getModel().isPressed() ? bgColor.darker() : bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Draw border
                g2.setColor(new Color(200, 200, 200));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                
                // Draw text
                super.paintComponent(g);
            }
            
            @Override
            public void paintBorder(Graphics g) {
                // Don't paint the default border
            }
        };
        
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        // Use emoji-compatible font with larger size
        Font emojiFont = new Font("Segoe UI Symbol", Font.BOLD, 12);
        btn.setFont(emojiFont);
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btn.setPreferredSize(new Dimension(120, 40));
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return btn;
    }

    // Helper method to create styled labels
    private JLabel createStyledLabel(String text, int fontSize, int style) {
        return createStyledLabel(text, fontSize, style, Color.BLACK);
    }
    
    private JLabel createStyledLabel(String text, int fontSize, int style, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", style, fontSize));
        lbl.setForeground(color);
        return lbl;
    }
    
    // Helper method to create styled buttons
    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createRaisedBevelBorder());
        return btn;
    }
    
    // Helper method to show success dialog
    private void showSuccessDialog(String title, String message) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(240, 255, 240));
        
        JLabel iconLabel = new JLabel("✓");
        iconLabel.setFont(new Font("Arial", Font.BOLD, 48));
        iconLabel.setForeground(new Color(34, 139, 34));
        
        JLabel msgLabel = new JLabel("<html><div style='width: 300px'>" + message + "</div></html>");
        msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(msgLabel, BorderLayout.CENTER);
        
        JOptionPane.showMessageDialog(frame, panel, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Helper method to show error dialog
    private void showErrorDialog(String title, String message) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(255, 240, 240));
        
        JLabel iconLabel = new JLabel("✕");
        iconLabel.setFont(new Font("Arial", Font.BOLD, 48));
        iconLabel.setForeground(new Color(220, 20, 60));
        
        JLabel msgLabel = new JLabel("<html><div style='width: 300px'>" + message + "</div></html>");
        msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(msgLabel, BorderLayout.CENTER);
        
        JOptionPane.showMessageDialog(frame, panel, title, JOptionPane.ERROR_MESSAGE);
    }


    public void show() {
        frame.setVisible(true);
    }

    // Method to download complete transaction history as CSV
    private void downloadTransactionHistory() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT date, description, amount FROM transactions WHERE account_number = ? ORDER BY date DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            // Build CSV content
            StringBuilder csvContent = new StringBuilder();
            csvContent.append("Date & Time,Description,Amount\n");
            
            boolean hasTransactions = false;
            while (rs.next()) {
                hasTransactions = true;
                java.sql.Timestamp timestamp = rs.getTimestamp("date");
                String dateTime = timestamp != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp.getTime())) : "N/A";
                String description = rs.getString("description");
                double amount = rs.getDouble("amount");
                
                // Escape quotes in description for CSV
                String escapedDescription = description.replaceAll("\"", "\"\"");
                csvContent.append("\"").append(dateTime).append("\",\"")
                          .append(escapedDescription).append("\",")
                          .append(amount).append("\n");
            }

            if (!hasTransactions) {
                showErrorDialog("No Transactions", "You have no transaction history to download.");
                return;
            }

            // Open file chooser to save CSV
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Transaction History");
            fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
            
            // Set default filename with timestamp
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String timestamp = dateFormat.format(new Date());
            fileChooser.setSelectedFile(new File("Transaction_History_" + accountNumber + "_" + timestamp + ".csv"));
            
            int userSelection = fileChooser.showSaveDialog(frame);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try (PrintWriter writer = new PrintWriter(new FileWriter(fileToSave))) {
                    writer.print(csvContent.toString());
                    showSuccessDialog("Download Complete", "Transaction history downloaded successfully to:\n" + fileToSave.getAbsolutePath());
                } catch (IOException ex) {
                    showErrorDialog("Save Error", "Error saving file: " + ex.getMessage());
                }
            }
        } catch (SQLException ex) {
            showErrorDialog("Error", "Failed to retrieve transactions: " + ex.getMessage());
        }
    }
}

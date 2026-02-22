
/**
 * Connect4UI.java
 * Swing UI for Connect4 vs AI.
 * - EASY: D&C + Greedy
 * - MODERATE: Dynamic Programming (Minimax + Memo depth 4)
 * - HARD: Backtracking (Iterative Deepening + PVS depth 6)
 */

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class Connect4UI extends JFrame {

    // Game components
    private Board board;
    private Connect4AI winChecker;

    // UI components
    private JPanel[][] cellPanels;
    private JLabel[] dropLabels;
    private JPanel boardPanel;
    private JPanel controlPanel;
    private JLabel difficultyLabel;
    private JLabel algorithmLabel;

    // Stats labels
    private JLabel playerWinsLabel;
    private JLabel aiWinsLabel;
    private JLabel drawsLabel;

    // Game state
    private char currentPlayer;
    private boolean gameOver;
    private GameDifficulty difficulty;

    // Score tracking
    private int playerWins = 0;
    private int aiWins = 0;
    private int draws = 0;

    // Difficulty Enum
    public enum GameDifficulty {
        EASY("Easy", "D&C + Greedy"),
        MODERATE("Moderate", "Dynamic Programming (Minimax + Memo)"),
        HARD("Hard", "Backtracking (Iterative Deepening + PVS)");

        public final String displayName;
        public final String algorithms;

        GameDifficulty(String displayName, String algorithms) {
            this.displayName = displayName;
            this.algorithms = algorithms;
        }
    }

    // Colors - Modern neumorphic palette
    private static final Color COLOR_RED = new Color(239, 68, 68);
    private static final Color COLOR_YELLOW = new Color(251, 146, 60);
    private static final Color COLOR_EMPTY = new Color(229, 231, 235);
    private static final Color COLOR_BG = new Color(241, 243, 245);
    private static final Color COLOR_CARD_BG = new Color(255, 255, 255);
    private static final Color COLOR_SHADOW = new Color(200, 200, 200);
    private static final Color COLOR_HIGHLIGHT = new Color(255, 255, 255);
    private static final Color COLOR_TEXT = new Color(30, 30, 30);
    private static final Color COLOR_TEXT_LIGHT = new Color(100, 100, 100);
    private static final Color COLOR_PINK_BG = new Color(254, 226, 226);
    private static final Color COLOR_ORANGE_BG = new Color(255, 237, 213);
    private static final Color COLOR_GRAY_BG = new Color(243, 244, 246);

    /**
     * Constructor
     */
    public Connect4UI() {
        // Initialize game components
        board = new Board();
        winChecker = new Connect4AI(board);

        // Print D&C algorithms info to console
        winChecker.printAlgorithmInfo();

        // Initialize game state
        currentPlayer = 'R'; // Red starts
        gameOver = false;
        difficulty = GameDifficulty.MODERATE;

        // Show difficulty and mode selection dialog
        showGameModeDialog();

        // Setup UI
        setupUI();

        setVisible(true);
    }

    /**
     * Show difficulty selection dialog
     */
    private void showGameModeDialog() {
        JPanel selectionPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        selectionPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        selectionPanel.setBackground(COLOR_BG);

        JLabel titleLabel = new JLabel("Connect 4 - Select Difficulty");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        selectionPanel.add(titleLabel);

        JLabel diffLabel = new JLabel("Choose AI strength:");
        diffLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        selectionPanel.add(diffLabel);

        JPanel diffButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        diffButtonPanel.setBackground(COLOR_BG);

        JRadioButton easyButton = new JRadioButton("Easy (D&C + Greedy)", true);
        JRadioButton modButton = new JRadioButton("Moderate (Dynamic Programming)");
        JRadioButton hardButton = new JRadioButton("Hard (Backtracking)");

        ButtonGroup diffGroup = new ButtonGroup();
        diffGroup.add(easyButton);
        diffGroup.add(modButton);
        diffGroup.add(hardButton);

        diffButtonPanel.add(easyButton);
        diffButtonPanel.add(modButton);
        diffButtonPanel.add(hardButton);
        selectionPanel.add(diffButtonPanel);

        int result = JOptionPane.showConfirmDialog(
                null,
                selectionPanel,
                "Game Setup",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.CANCEL_OPTION) {
            System.exit(0);
        }

        if (easyButton.isSelected()) {
            difficulty = GameDifficulty.EASY;
        } else if (modButton.isSelected()) {
            difficulty = GameDifficulty.MODERATE;
        } else {
            difficulty = GameDifficulty.HARD;
        }
    }

    /**
     * Setup user interface
     */
    private void setupUI() {
        setTitle("Connect 4 - Divide & Conquer Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(COLOR_BG);

        // Create main content panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(COLOR_BG);
        mainPanel.setBorder(new EmptyBorder(15, 20, 20, 20));

        // Create stats panel at the top
        JPanel statsPanel = createStatsPanel();
        mainPanel.add(statsPanel, BorderLayout.NORTH);

        // Create center panel for drop labels and board
        JPanel centerPanel = new JPanel(new BorderLayout(0, 5));
        centerPanel.setBackground(COLOR_BG);

        // Create drop labels panel
        JPanel dropPanel = createDropLabelsPanel();
        centerPanel.add(dropPanel, BorderLayout.NORTH);

        // Create board panel with modern neumorphic design
        boardPanel = createBoardPanel();
        centerPanel.add(boardPanel, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // Add menu bar
        createMenuBar();

        setSize(780, 820);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    /**
     * Create stats panel with score cards
     */
    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setBackground(COLOR_BG);
        statsPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Your Wins card (pink)
        playerWinsLabel = new JLabel("0");
        JPanel playerCard = createStatCard("Your Wins", playerWinsLabel, COLOR_PINK_BG, COLOR_RED);
        statsPanel.add(playerCard);

        // AI Wins card (orange)
        aiWinsLabel = new JLabel("0");
        JPanel aiCard = createStatCard("AI Wins", aiWinsLabel, COLOR_ORANGE_BG, COLOR_YELLOW);
        statsPanel.add(aiCard);

        // Draws card (gray)
        drawsLabel = new JLabel("0");
        JPanel drawCard = createStatCard("Draws", drawsLabel, COLOR_GRAY_BG, COLOR_TEXT);
        statsPanel.add(drawCard);

        return statsPanel;
    }

    /**
     * Create a stats card with title and value label
     */
    private JPanel createStatCard(String title, JLabel valueLabel, Color bgColor, Color valueColor) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(bgColor);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                g2d.setColor(new Color(bgColor.getRed() - 20, bgColor.getGreen() - 20, bgColor.getBlue() - 20, 50));
                g2d.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 15, 15));
                g2d.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(200, 100));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(COLOR_TEXT_LIGHT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Configure the value label
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(valueColor);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(valueLabel);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    /**
     * Create drop labels panel
     */
    private JPanel createDropLabelsPanel() {
        JPanel dropPanel = new JPanel(new GridLayout(1, board.getCols(), 8, 0));
        dropPanel.setBackground(COLOR_BG);
        dropPanel.setBorder(new EmptyBorder(5, 15, 5, 15));

        dropLabels = new JLabel[board.getCols()];

        for (int col = 0; col < board.getCols(); col++) {
            final int column = col;
            JLabel dropLabel = new JLabel("Drop", SwingConstants.CENTER);
            dropLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            dropLabel.setForeground(COLOR_TEXT_LIGHT);
            dropLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            dropLabel.setPreferredSize(new Dimension(90, 25));

            // Add click listener
            dropLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleColumnClick(column);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    dropLabel.setForeground(COLOR_TEXT);
                    dropLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    dropLabel.setForeground(COLOR_TEXT_LIGHT);
                    dropLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                }
            });

            dropLabels[col] = dropLabel;
            dropPanel.add(dropLabel);
        }

        return dropPanel;
    }

    /**
     * Create board panel with neumorphic cell design
     */
    private JPanel createBoardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(COLOR_BG);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
                g2d.dispose();
            }
        };
        panel.setLayout(new GridLayout(board.getRows(), board.getCols(), 8, 8));
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setOpaque(false);

        cellPanels = new JPanel[board.getRows()][board.getCols()];

        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                final int column = col;
                JPanel cell = createNeumorphicCell();

                // Add click listener to cells
                cell.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        handleColumnClick(column);
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (!gameOver) {
                            cell.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        }
                    }
                });

                cellPanels[row][col] = cell;
                panel.add(cell);
            }
        }

        return panel;
    }

    /**
     * Create a neumorphic style cell with solid circular disc
     */
    private JPanel createNeumorphicCell() {
        JPanel cell = new JPanel() {
            private Color discColor = null;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int cellSize = Math.min(getWidth(), getHeight()) - 8;
                int cellX = (getWidth() - cellSize) / 2;
                int cellY = (getHeight() - cellSize) / 2;

                // Draw cell shadow (subtle, bottom-right)
                g2d.setColor(new Color(200, 200, 200, 100));
                g2d.fill(new RoundRectangle2D.Double(cellX + 2, cellY + 2, cellSize, cellSize, 18, 18));

                // Draw white cell background (rounded rectangle)
                g2d.setColor(COLOR_CARD_BG);
                g2d.fill(new RoundRectangle2D.Double(cellX, cellY, cellSize, cellSize, 18, 18));

                // Draw subtle border
                g2d.setColor(new Color(220, 220, 220));
                g2d.setStroke(new BasicStroke(1));
                g2d.draw(new RoundRectangle2D.Double(cellX, cellY, cellSize - 1, cellSize - 1, 18, 18));

                // If there's a disc, draw it as a solid circle inside the cell
                if (discColor != null) {
                    int discPadding = 8;
                    int discSize = cellSize - (discPadding * 2);
                    int discX = cellX + discPadding;
                    int discY = cellY + discPadding;

                    // Draw solid circular disc
                    g2d.setColor(discColor);
                    g2d.fillOval(discX, discY, discSize, discSize);
                }

                g2d.dispose();
            }

            public void setDiscColor(Color color) {
                this.discColor = color;
                repaint();
            }
        };

        cell.setPreferredSize(new Dimension(90, 90));
        cell.setOpaque(false);

        return cell;
    }

    /**
     * Helper method to set disc color on a cell
     */
    private void setDiscColor(JPanel cell, Color color) {
        try {
            java.lang.reflect.Method setDiscColorMethod = cell.getClass().getMethod("setDiscColor", Color.class);
            setDiscColorMethod.invoke(cell, color);
        } catch (Exception e) {
            // Fallback
            cell.setBackground(color);
        }
    }

    /**
     * Create menu bar
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(COLOR_CARD_BG);
        menuBar.setBorder(new EmptyBorder(5, 10, 5, 10));

        JMenu gameMenu = new JMenu("Game");
        gameMenu.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JMenuItem newGameItem = new JMenuItem("New Game");
        newGameItem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        newGameItem.addActionListener(e -> resetGame());

        JMenuItem settingsItem = new JMenuItem("Change Difficulty");
        settingsItem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        settingsItem.addActionListener(e -> {
            showGameModeDialog();
        });

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        exitItem.addActionListener(e -> System.exit(0));

        gameMenu.add(newGameItem);
        gameMenu.add(settingsItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JMenuItem aboutItem = new JMenuItem("About Algorithms");
        aboutItem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        aboutItem.addActionListener(e -> showAlgorithmInfo());

        helpMenu.add(aboutItem);

        menuBar.add(gameMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    /**
     * Show algorithm information dialog
     */
    private void showAlgorithmInfo() {
        String info = "Connect 4 - Algorithm Implementation\n\n" +
                "EASY - D&C + Greedy:\n" +
                "Win/Block/Safety heuristics + D&C column split.\n\n" +
                "MODERATE - Dynamic Programming:\n" +
                "Minimax + memoization (depth 4).\n" +
                "Transposition table caches board states.\n\n" +
                "HARD - Backtracking:\n" +
                "Iterative Deepening + PVS (depth 6).\n" +
                "Try move, recurse, undo. No memoization.\n\n" +
                "Moderate and Hard use strong minimax search.";

        JTextArea textArea = new JTextArea(info);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        textArea.setBackground(COLOR_BG);
        textArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(this, scrollPane, "Algorithm Information", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Handle column button click
     */
    private void handleColumnClick(int col) {
        if (gameOver) {
            return;
        }

        if (!board.isValidMove(col)) {
            return;
        }

        // Make move
        makeMove(col, currentPlayer);

        // Check win/draw
        if (checkGameEnd()) {
            return;
        }

        // Switch player
        currentPlayer = (currentPlayer == 'R') ? 'Y' : 'R';

        // AI plays when it's Yellow's turn
        if (currentPlayer == 'Y') {
            disableDropLabels();

            SwingWorker<Integer, Void> aiWorker = new SwingWorker<Integer, Void>() {
                @Override
                protected Integer doInBackground() {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return getAIMoveByDifficulty('Y');
                }

                @Override
                protected void done() {
                    try {
                        int aiMove = get();
                        makeMove(aiMove, 'Y');

                        if (!checkGameEnd()) {
                            currentPlayer = 'R';
                        }

                        enableDropLabels();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            aiWorker.execute();
        }
    }

    /**
     * Get AI move based on difficulty level
     */
    private int getAIMoveByDifficulty(char player) {
        switch (difficulty) {
            case EASY:
                return winChecker.findBestMove(player);
            case MODERATE:
                return winChecker.findBestMoveModerate(player);
            case HARD:
                return winChecker.findBestMoveHard(player);
            default:
                return winChecker.findBestMove(player);
        }
    }

    /**
     * Make a move on the board
     */
    private void makeMove(int col, char player) {
        int row = board.insertDisc(col, player);

        if (row != -1) {
            // Update UI
            Color color = (player == 'R') ? COLOR_RED : COLOR_YELLOW;
            setDiscColor(cellPanels[row][col], color);
        }
    }

    /**
     * Check if game has ended
     */
    private boolean checkGameEnd() {
        // Check win
        if (winChecker.checkWin(currentPlayer)) {
            gameOver = true;
            String winner = (currentPlayer == 'R') ? "Red" : "Yellow";

            // Update scores
            if (currentPlayer == 'R') {
                playerWins++;
                playerWinsLabel.setText(String.valueOf(playerWins));
            } else {
                aiWins++;
                aiWinsLabel.setText(String.valueOf(aiWins));
            }

            disableDropLabels();

            JOptionPane.showMessageDialog(
                    this,
                    winner + " wins the game!\n\nDifficulty: " + difficulty.name() +
                            "\nAlgorithms: " + difficulty.algorithms,
                    "Game Over",
                    JOptionPane.INFORMATION_MESSAGE);
            return true;
        }

        // Check draw
        if (board.isBoardFull()) {
            gameOver = true;
            draws++;
            drawsLabel.setText(String.valueOf(draws));
            disableDropLabels();

            JOptionPane.showMessageDialog(
                    this,
                    "The game is a draw!\n\nDifficulty: " + difficulty.name(),
                    "Game Over - Draw",
                    JOptionPane.INFORMATION_MESSAGE);
            return true;
        }

        return false;
    }

    /**
     * Reset game
     */
    private void resetGame() {
        board.initializeBoard();
        currentPlayer = 'R';
        gameOver = false;

        // Reset UI
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                setDiscColor(cellPanels[row][col], null);
            }
        }

        enableDropLabels();
    }

    /**
     * Disable drop labels
     */
    private void disableDropLabels() {
        for (JLabel label : dropLabels) {
            label.setEnabled(false);
            label.setForeground(COLOR_SHADOW);
            label.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * Enable drop labels
     */
    private void enableDropLabels() {
        for (int col = 0; col < dropLabels.length; col++) {
            if (board.isValidMove(col)) {
                dropLabels[col].setEnabled(true);
                dropLabels[col].setForeground(COLOR_TEXT_LIGHT);
                dropLabels[col].setCursor(new Cursor(Cursor.HAND_CURSOR));
            } else {
                dropLabels[col].setEnabled(false);
                dropLabels[col].setForeground(COLOR_SHADOW);
                dropLabels[col].setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }
}

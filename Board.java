/**
 * Board.java
 * Manages the Connect4 game board state
 * Contains board operations: insert, undo, check valid moves
 */

public class Board {
    private static final int ROWS = 6;
    private static final int COLS = 7;
    
    private char[][] board;
    
    // Constructor
    public Board() {
        board = new char[ROWS][COLS];
        initializeBoard();
    }
    
    // Initialize empty board
    public void initializeBoard() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                board[i][j] = ' ';
            }
        }
    }
    
    // Get board array
    public char[][] getBoard() {
        return board;
    }
    
    // Get dimensions
    public int getRows() {
        return ROWS;
    }
    
    public int getCols() {
        return COLS;
    }
    
    // Check if column is valid for move
    public boolean isValidMove(int col) {
        if (col < 0 || col >= COLS) {
            return false;
        }
        return board[0][col] == ' ';
    }
    
    // Insert disc into column (returns row where disc landed, -1 if invalid)
    public int insertDisc(int col, char player) {
        if (!isValidMove(col)) {
            return -1;
        }
        
        // Drop disc to lowest available row
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][col] == ' ') {
                board[row][col] = player;
                return row;
            }
        }
        return -1;
    }
    
    // Remove disc from column (undo move)
    public void removeDisc(int col) {
        for (int row = 0; row < ROWS; row++) {
            if (board[row][col] != ' ') {
                board[row][col] = ' ';
                break;
            }
        }
    }
    
    // Check if board is full
    public boolean isBoardFull() {
        for (int col = 0; col < COLS; col++) {
            if (board[0][col] == ' ') {
                return false;
            }
        }
        return true;
    }
}

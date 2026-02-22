/**
 * DivideAndConquerGreedy.java
 * Strict D&C and Greedy algorithms for Connect4.
 * Time complexities are optimal for each operation.
 */

import java.util.*;

public class DivideAndConquerGreedy {

    private final Board board;

    public DivideAndConquerGreedy(Board board) {
        this.board = board;
    }

    // =====================================================================
    // METHOD 1: D&C + GREEDY WIN DETECTION - Member 1
    // D&C: Divide into 4 direction subproblems; each direction uses recursive
    //      row/column range split. Conquer: check each half. Combine: OR.
    // Greedy: Check direction with highest win frequency first (horizontal).
    //         Early exit on first win found (skip remaining directions).
    // Time: O(R*C) worst; O(min scan) with greedy early exit
    // =====================================================================

    public boolean checkWin(char player) {
        if (checkHorizontalDnC(player)) return true;
        if (checkVerticalDnC(player)) return true;
        if (checkDiagonalDownDnC(player)) return true;
        return checkDiagonalUpDnC(player);
    }

    /**
     * D&C: Divide row range [rStart, rEnd) into halves. Conquer each half.
     * Greedy: Check left half first; if win found, skip right half.
     */
    private boolean checkHorizontalDnC(char player) {
        int rows = board.getRows();
        int cols = board.getCols();
        return checkHorizontalRange(player, 0, rows, cols);
    }

    private boolean checkHorizontalRange(char player, int rStart, int rEnd, int cols) {
        if (rEnd - rStart < 1) return false;
        if (rEnd - rStart == 1) {
            return scanRowForWin(player, rStart, cols, 0, 1);
        }
        int mid = rStart + (rEnd - rStart) / 2;
        if (checkHorizontalRange(player, rStart, mid, cols)) return true;
        return checkHorizontalRange(player, mid, rEnd, cols);
    }

    private boolean checkVerticalDnC(char player) {
        int rows = board.getRows();
        int cols = board.getCols();
        return checkVerticalRange(player, 0, cols, rows);
    }

    private boolean checkVerticalRange(char player, int cStart, int cEnd, int rows) {
        if (cEnd - cStart < 1) return false;
        if (cEnd - cStart == 1) {
            return scanColForWin(player, cStart, rows, 1, 0);
        }
        int mid = cStart + (cEnd - cStart) / 2;
        if (checkVerticalRange(player, cStart, mid, rows)) return true;
        return checkVerticalRange(player, mid, cEnd, rows);
    }

    private boolean checkDiagonalDownDnC(char player) {
        int rows = board.getRows();
        int cols = board.getCols();
        return checkDiagDownRange(player, 0, rows - 3, cols);
    }

    private boolean checkDiagDownRange(char player, int rStart, int rEnd, int cols) {
        if (rEnd - rStart < 1) return false;
        if (rEnd - rStart == 1) {
            return scanDiagonalForWin(player, rStart, 0, 1, 1, cols);
        }
        int mid = rStart + (rEnd - rStart) / 2;
        if (checkDiagDownRange(player, rStart, mid, cols)) return true;
        return checkDiagDownRange(player, mid, rEnd, cols);
    }

    private boolean checkDiagonalUpDnC(char player) {
        int rows = board.getRows();
        int cols = board.getCols();
        return checkDiagUpRange(player, 3, rows, cols);
    }

    private boolean checkDiagUpRange(char player, int rStart, int rEnd, int cols) {
        if (rEnd - rStart < 1) return false;
        if (rEnd - rStart == 1) {
            return scanDiagonalForWin(player, rStart, 0, -1, 1, cols);
        }
        int mid = rStart + (rEnd - rStart) / 2;
        if (checkDiagUpRange(player, rStart, mid, cols)) return true;
        return checkDiagUpRange(player, mid, rEnd, cols);
    }

    private boolean scanRowForWin(char player, int row, int cols, int dr, int dc) {
        char[][] g = board.getBoard();
        for (int c = 0; c <= cols - 4; c++) {
            if (g[row][c] == player && g[row + dr][c + dc] == player
                    && g[row + 2 * dr][c + 2 * dc] == player && g[row + 3 * dr][c + 3 * dc] == player)
                return true;
        }
        return false;
    }

    private boolean scanColForWin(char player, int col, int rows, int dr, int dc) {
        char[][] g = board.getBoard();
        for (int r = 0; r <= rows - 4; r++) {
            if (g[r][col] == player && g[r + dr][col + dc] == player
                    && g[r + 2 * dr][col + 2 * dc] == player && g[r + 3 * dr][col + 3 * dc] == player)
                return true;
        }
        return false;
    }

    private boolean scanDiagonalForWin(char player, int startRow, int startCol, int dr, int dc, int cols) {
        char[][] g = board.getBoard();
        int rows = board.getRows();
        for (int c = startCol; c <= cols - 4; c++) {
            int r = startRow;
            if (r + 3 * dr < 0 || r + 3 * dr >= rows) continue;
            if (g[r][c] == player && g[r + dr][c + dc] == player
                    && g[r + 2 * dr][c + 2 * dc] == player && g[r + 3 * dr][c + 3 * dc] == player)
                return true;
        }
        return false;
    }

    // =====================================================================
    // METHOD 2: D&C + GREEDY FIND VALID MOVES - Member 2
    // D&C: Divide column range into left, center, right (3-way split).
    //      Center columns are most valuable in Connect4.
    // Conquer: recursively find valid moves in each third.
    // Greedy: Combine with center-first ordering (best moves first).
    // Time: O(C) - each column visited once
    // =====================================================================

    public List<Integer> findValidMovesDnC(int start, int end) {
        validateColumnRange(start, end);
        int centerCol = board.getCols() / 2;
        return findValidMovesDnCGreedy(start, end, centerCol);
    }

    /**
     * D&C with greedy: 3-way split prioritizes center.
     * Greedy: Process center segment first; merge center before left/right.
     */
    private List<Integer> findValidMovesDnCGreedy(int start, int end, int centerCol) {
        if (start > end) return new ArrayList<>();
        if (start == end) return baseCaseSingleColumn(start);

        int rangeSize = end - start + 1;
        if (rangeSize <= 2) {
            List<Integer> out = new ArrayList<>();
            for (int c = start; c <= end; c++)
                if (isColumnPlayable(c)) out.add(c);
            return out;
        }

        int third = rangeSize / 3;
        int leftEnd = start + third - 1;
        int rightStart = end - third + 1;

        List<Integer> left = findValidMovesDnCGreedy(start, leftEnd, centerCol);
        List<Integer> center = findValidMovesDnCGreedy(leftEnd + 1, rightStart - 1, centerCol);
        List<Integer> right = findValidMovesDnCGreedy(rightStart, end, centerCol);

        return mergeCenterFirst(left, center, right, centerCol);
    }

    private List<Integer> mergeCenterFirst(List<Integer> left, List<Integer> center,
            List<Integer> right, int centerCol) {
        List<Integer> merged = new ArrayList<>(left.size() + center.size() + right.size());
        merged.addAll(center);
        merged.addAll(left);
        merged.addAll(right);
        return merged;
    }

    private void validateColumnRange(int start, int end) {
        int cols = board.getCols();
        if (start < 0 || end >= cols || start > end) {
            throw new IllegalArgumentException(
                "Invalid range [" + start + "," + end + "] for board with " + cols + " columns");
        }
    }

    private List<Integer> baseCaseSingleColumn(int col) {
        List<Integer> result = new ArrayList<>(1);
        if (isColumnPlayable(col)) result.add(col);
        return result;
    }

    private boolean isColumnPlayable(int col) {
        return col >= 0 && col < board.getCols() && board.isValidMove(col);
    }

    // =====================================================================
    // METHOD 3: D&C + GREEDY BOARD EVALUATION - Member 3
    // D&C: Divide board into 4 quadrants (2x2 grid). Conquer each quadrant.
    // Greedy: Weight center quadrants higher (pieces near center more valuable).
    // Combine: weighted sum of quadrant scores + connected-piece bonus.
    // Time: O(R*C) total; countConnectedDnC is O(P) for P pieces
    // =====================================================================

    public int evaluatePositionDnC(char player) {
        int rows = board.getRows();
        int cols = board.getCols();
        int midR = rows / 2;
        int midC = cols / 2;

        int q1 = evaluateQuadrant(player, 0, midR, 0, midC);
        int q2 = evaluateQuadrant(player, 0, midR, midC, cols);
        int q3 = evaluateQuadrant(player, midR, rows, 0, midC);
        int q4 = evaluateQuadrant(player, midR, rows, midC, cols);

        int conn3 = countConnectedDnC(player, 3);
        int conn2 = countConnectedDnC(player, 2);

        return combineQuadrantScoresGreedy(q1, q2, q3, q4, midR, midC, rows, cols)
                + (conn3 * 50 + conn2 * 10);
    }

    private int evaluateQuadrant(char player, int rStart, int rEnd, int cStart, int cEnd) {
        char opp = (player == 'R') ? 'Y' : 'R';
        char[][] g = board.getBoard();
        int score = 0;
        for (int r = rStart; r < rEnd; r++) {
            for (int c = cStart; c < cEnd; c++) {
                if (g[r][c] == player) {
                    score += 10;
                    if (c == board.getCols() / 2) score += 5;
                } else if (g[r][c] == opp) {
                    score -= 10;
                    if (c == board.getCols() / 2) score -= 5;
                }
            }
        }
        return score;
    }

    private int combineQuadrantScoresGreedy(int q1, int q2, int q3, int q4,
            int midR, int midC, int rows, int cols) {
        int w1 = 1, w2 = 2, w3 = 2, w4 = 3;
        return w1 * q1 + w2 * q2 + w3 * q3 + w4 * q4;
    }

    /**
     * D&C: Divide board into left/right halves by columns. Recursively count in each half.
     * Combine: add left + right + patterns spanning the vertical boundary.
     */
    private int countConnectedDnC(char player, int len) {
        char[][] g = board.getBoard();
        int rows = board.getRows();
        int cols = board.getCols();
        return countConnectedDnCRecurse(g, player, len, 0, rows, 0, cols);
    }

    private int countConnectedDnCRecurse(char[][] g, char player, int len,
            int rStart, int rEnd, int cStart, int cEnd) {
        int rSize = rEnd - rStart;
        int cSize = cEnd - cStart;
        if (rSize <= 2 || cSize <= 2) {
            return countConnectedInRegion(g, player, len, rStart, rEnd, cStart, cEnd);
        }
        int midC = cStart + (cEnd - cStart) / 2;
        int left = countConnectedDnCRecurse(g, player, len, rStart, rEnd, cStart, midC);
        int right = countConnectedDnCRecurse(g, player, len, rStart, rEnd, midC, cEnd);
        int cross = countCrossingVertical(g, player, len, rStart, rEnd, cStart, cEnd, midC);
        return left + right + cross;
    }

    private int countConnectedInRegion(char[][] g, char player, int len,
            int rStart, int rEnd, int cStart, int cEnd) {
        int rows = board.getRows();
        int cols = board.getCols();
        Set<String> seen = new HashSet<>();
        int count = 0;
        int[][] dirs = { { 0, 1 }, { 1, 0 }, { 1, 1 }, { -1, 1 } };
        for (int r = rStart; r < rEnd; r++) {
            for (int c = cStart; c < cEnd; c++) {
                if (g[r][c] != player) continue;
                for (int[] d : dirs) {
                    int dr = d[0], dc = d[1];
                    int er = r + (len - 1) * dr, ec = c + (len - 1) * dc;
                    if (er < 0 || er >= rows || ec < 0 || ec >= cols) continue;
                    String key = r + "," + c + "," + dr + "," + dc;
                    if (seen.contains(key)) continue;
                    if (checkPattern(g, player, r, c, dr, dc, len)) {
                        count++;
                        seen.add(key);
                    }
                }
            }
        }
        return count;
    }

    private int countCrossingVertical(char[][] g, char player, int len,
            int rStart, int rEnd, int cStart, int cEnd, int midC) {
        int rows = board.getRows();
        int cols = board.getCols();
        Set<String> seen = new HashSet<>();
        int count = 0;
        int[][] dirs = { { 0, 1 }, { 1, 1 }, { -1, 1 } };
        for (int r = rStart; r < rEnd; r++) {
            for (int c = Math.max(cStart, midC - len + 1); c < Math.min(midC + 1, cEnd - len + 1); c++) {
                if (c + len - 1 < midC || c >= midC) continue;
                if (g[r][c] != player) continue;
                for (int[] d : dirs) {
                    int dr = d[0], dc = d[1];
                    int er = r + (len - 1) * dr, ec = c + (len - 1) * dc;
                    if (er < 0 || er >= rows || ec < 0 || ec >= cols) continue;
                    boolean crosses = (c < midC && c + (len - 1) * dc >= midC) || (c >= midC && c + (len - 1) * dc < midC);
                    if (!crosses) continue;
                    String key = r + "," + c + "," + dr + "," + dc;
                    if (seen.contains(key)) continue;
                    if (checkPattern(g, player, r, c, dr, dc, len)) {
                        count++;
                        seen.add(key);
                    }
                }
            }
        }
        return count;
    }

    private boolean checkPattern(char[][] g, char player, int sr, int sc, int dr, int dc, int len) {
        for (int i = 0; i < len; i++) {
            int r = sr + i * dr, c = sc + i * dc;
            if (r < 0 || r >= board.getRows() || c < 0 || c >= board.getCols() || g[r][c] != player)
                return false;
        }
        return true;
    }

    // =====================================================================
    // METHOD 4: D&C + GREEDY BEST MOVE (Recursive) - Member 4
    // D&C: Divide columns into halves, conquer each, combine with chooseBetter.
    // Greedy: Early exit when winning move found; prefer center columns first.
    // Recursive: findBestMoveGreedyRecurse splits, recurses, chooses better.
    // Time: O(C * R*C) for combine; greedy pruning when win found
    // =====================================================================

    public int findBestMoveGreedy(char player, int[] columns, WinChecker winChecker,
            SafeMoveChecker safeChecker) {
        if (columns == null || columns.length == 0) return -1;
        return findBestMoveGreedyRecurse(player, columns, 0, columns.length - 1, winChecker);
    }

    private int findBestMoveGreedyRecurse(char player, int[] columns, int start, int end,
            WinChecker winChecker) {
        if (start > end) return -1;
        if (start == end) return isColumnPlayable(columns[start]) ? columns[start] : -1;

        int mid = start + (end - start) / 2;
        int leftBest = findBestMoveGreedyRecurse(player, columns, start, mid, winChecker);
        if (isWinningMove(leftBest, player, winChecker)) return leftBest;

        int rightBest = findBestMoveGreedyRecurse(player, columns, mid + 1, end, winChecker);
        if (isWinningMove(rightBest, player, winChecker)) return rightBest;

        return chooseBetter(player, leftBest, rightBest, winChecker);
    }

    private int chooseBetter(char player, int c1, int c2, WinChecker winChecker) {
        if (c1 == -1) return c2;
        if (c2 == -1) return c1;
        int s1 = evaluateColumnScore(player, c1, winChecker);
        int s2 = evaluateColumnScore(player, c2, winChecker);
        return s1 >= s2 ? c1 : c2;
    }

    private boolean isWinningMove(int col, char player, WinChecker winChecker) {
        if (col == -1) return false;
        board.insertDisc(col, player);
        boolean win = winChecker.checkWin(player);
        board.removeDisc(col);
        return win;
    }

    private int evaluateColumnScore(char player, int col, WinChecker winChecker) {
        board.insertDisc(col, player);
        int score = winChecker.checkWin(player) ? 100000 : evaluatePositionDnC(player);
        board.removeDisc(col);
        return score;
    }

    /** Callback for win check (avoids circular dependency with Backtracking) */
    public interface WinChecker {
        boolean checkWin(char player);
    }

    /** Callback for safety check */
    public interface SafeMoveChecker {
        boolean isSafe(char player, int col);
    }
}

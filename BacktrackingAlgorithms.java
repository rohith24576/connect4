
/**
 * BacktrackingAlgorithms.java
 * Strong backtracking for Connect4: Iterative Deepening + Principal Variation Search.
 * Uses decision -> explore -> undo pattern with aggressive pruning.
 * 4 methods: findImmediateWin, isSafeMove, findBestMoveMinimaxBacktracking, findBestMoveThreatHeuristic
 */

import java.util.*;

public class BacktrackingAlgorithms {

    private final Board board;
    private final DivideAndConquerGreedy dnc;

    private static final int WIN_SCORE = 100000;
    private static final int MAX_DEPTH = 10;
    private final int[] killerMoves;
    private final int[][] historyHeuristic;

    public BacktrackingAlgorithms(Board board, DivideAndConquerGreedy dnc) {
        this.board = board;
        this.dnc = dnc;
        this.killerMoves = new int[MAX_DEPTH * 2];
        this.historyHeuristic = new int[7][2];
        Arrays.fill(killerMoves, -1);
    }

    // =====================================================================
    // METHOD 1: FIND IMMEDIATE WIN (Backtracking) - Member 1
    // Decision -> Explore -> Undo pattern for winning move search.
    // Center-first ordering: tries center columns first for faster win discovery.
    // Multiple-win resolution: if several columns win, returns center-preferred one.
    // Board-full and empty-board short-circuits. D&C recursion with greedy pruning.
    // Time: O(C * R*C) worst case
    // =====================================================================

    public int findImmediateWin(char player) {
        if (board.isBoardFull())
            return -1;
        List<Integer> validColumns = getValidColumns();
        if (validColumns.isEmpty())
            return -1;
        validColumns = orderColumnsByCenterPriority(validColumns);
        List<Integer> winningCols = findImmediateWinDnC(player, validColumns, 0, validColumns.size() - 1);
        return pickBestWinningColumn(winningCols);
    }

    /** Member 1: Center-first column ordering for win search. */
    private List<Integer> orderColumnsByCenterPriority(List<Integer> cols) {
        int center = board.getCols() / 2;
        List<Integer> ordered = new ArrayList<>(cols);
        ordered.sort((a, b) -> Integer.compare(Math.abs(a - center), Math.abs(b - center)));
        return ordered;
    }

    private List<Integer> findImmediateWinDnC(char player, List<Integer> columns, int left, int right) {
        List<Integer> wins = new ArrayList<>();
        if (left > right)
            return wins;
        if (left == right) {
            int col = columns.get(left);
            if (isColumnValid(col)) {
                simulatePlaceDisc(col, player);
                if (checkPlayerWins(player)) wins.add(col);
                undoPlaceDisc(col);
            }
            return wins;
        }
        int mid = left + (right - left) / 2;
        wins.addAll(findImmediateWinDnC(player, columns, left, mid));
        wins.addAll(findImmediateWinDnC(player, columns, mid + 1, right));
        return wins;
    }

    /** Member 1: Find blocking move when opponent has immediate win. */
    public int findImmediateBlock(char opponent) {
        return findImmediateWin(opponent);
    }

    /** Member 1: Collect all winning columns; return center-preferred if multiple. */
    private int pickBestWinningColumn(List<Integer> winningCols) {
        if (winningCols.isEmpty()) return -1;
        int center = board.getCols() / 2;
        int best = winningCols.get(0);
        for (int col : winningCols) {
            if (Math.abs(col - center) < Math.abs(best - center))
                best = col;
        }
        return best;
    }

    private List<Integer> getValidColumns() {
        List<Integer> cols = new ArrayList<>();
        for (int c = 0; c < board.getCols(); c++) {
            if (board.isValidMove(c))
                cols.add(c);
        }
        return cols;
    }

    private boolean isColumnValid(int col) {
        return col >= 0 && col < board.getCols() && board.isValidMove(col);
    }

    private void simulatePlaceDisc(int col, char player) {
        board.insertDisc(col, player);
    }

    private void undoPlaceDisc(int col) {
        board.removeDisc(col);
    }

    private boolean checkPlayerWins(char player) {
        return dnc.checkWin(player);
    }

    // =====================================================================
    // METHOD 2: SAFETY CHECK WITH 2-PLY AND 3-PLY LOOKAHEAD (Backtracking) - Member 2
    // Decision -> Explore -> Undo for move safety validation.
    // Checks: (1) opponent immediate win, (2) opponent double threat, (3) opponent fork setup,
    // (4) trapped-in-two (opponent forces win in 2), (5) our move creates fork (safe).
    // Time: O(C^2 * R*C) worst case
    // =====================================================================

    public boolean isSafeMove(char player, int col) {
        if (!isColumnValid(col))
            return false;
        char opponent = getOpponent(player);

        simulatePlaceDisc(col, player);
        if (createsOurFork(player, opponent))
            { undoPlaceDisc(col); return true; }
        boolean safe = exploreOpponentResponses(player, opponent);
        undoPlaceDisc(col);
        return safe;
    }

    /** Member 2: Does our move create a double threat? If so, move is safe (we win next). */
    private boolean createsOurFork(char player, char opponent) {
        int winCount = 0;
        List<Integer> moves = getValidColumns();
        for (int c : moves) {
            if (!isColumnValid(c)) continue;
            simulatePlaceDisc(c, player);
            if (checkPlayerWins(player)) winCount++;
            undoPlaceDisc(c);
            if (winCount >= 2) return true;
        }
        return false;
    }

    private boolean exploreOpponentResponses(char ourPlayer, char opponent) {
        List<Integer> opponentMoves = getValidColumns();
        for (int col : opponentMoves) {
            if (!isColumnValid(col))
                continue;
            simulatePlaceDisc(col, opponent);
            if (checkPlayerWins(opponent)) {
                undoPlaceDisc(col);
                return false;
            }
            if (hasDoubleThreat(opponent, ourPlayer)) {
                undoPlaceDisc(col);
                return false;
            }
            if (allowsOpponentForkNext(ourPlayer, opponent, col)) {
                undoPlaceDisc(col);
                return false;
            }
            if (isTrappedInTwo(ourPlayer, opponent, col)) {
                undoPlaceDisc(col);
                return false;
            }
            undoPlaceDisc(col);
        }
        return true;
    }

    /** Member 2: After opponent plays col, can they create fork on their next turn? */
    private boolean allowsOpponentForkNext(char ourPlayer, char opponent, int oppCol) {
        List<Integer> ourResponses = getValidColumns();
        for (int ourCol : ourResponses) {
            if (!isColumnValid(ourCol)) continue;
            simulatePlaceDisc(ourCol, ourPlayer);
            if (checkPlayerWins(ourPlayer)) { undoPlaceDisc(ourCol); continue; }
            if (hasDoubleThreat(opponent, ourPlayer)) {
                undoPlaceDisc(ourCol);
                return true;
            }
            undoPlaceDisc(ourCol);
        }
        return false;
    }

    /** Member 2: 3-ply - no matter how we respond, does opponent force win? */
    private boolean isTrappedInTwo(char ourPlayer, char opponent, int oppCol) {
        List<Integer> ourMoves = getValidColumns();
        for (int ourCol : ourMoves) {
            if (!isColumnValid(ourCol)) continue;
            simulatePlaceDisc(ourCol, ourPlayer);
            if (checkPlayerWins(ourPlayer)) { undoPlaceDisc(ourCol); return false; }
            boolean oppHasWinningMove = false;
            List<Integer> oppMoves2 = getValidColumns();
            for (int oppCol2 : oppMoves2) {
                if (!isColumnValid(oppCol2)) continue;
                simulatePlaceDisc(oppCol2, opponent);
                if (checkPlayerWins(opponent)) { oppHasWinningMove = true; undoPlaceDisc(oppCol2); break; }
                undoPlaceDisc(oppCol2);
            }
            undoPlaceDisc(ourCol);
            if (!oppHasWinningMove) return false;
        }
        return true;
    }

    private boolean hasDoubleThreat(char threatPlayer, char defender) {
        int winCount = 0;
        List<Integer> moves = getValidColumns();
        for (int col : moves) {
            if (!isColumnValid(col)) continue;
            simulatePlaceDisc(col, threatPlayer);
            if (checkPlayerWins(threatPlayer)) winCount++;
            undoPlaceDisc(col);
            if (winCount >= 2) return true;
        }
        return false;
    }

    private char getOpponent(char player) {
        return (player == 'R') ? 'Y' : 'R';
    }

    /** Small helper: center column index for move ordering. */
    private int getCenterColumn() {
        return board.getCols() / 2;
    }

    /** Small helper: distance from center (0 = center, 3 = edge). Lower is better. */
    private int centerDistance(int col) {
        return Math.abs(col - getCenterColumn());
    }

    // =====================================================================
    // METHOD 3: ITERATIVE DEEPENING + PRINCIPAL VARIATION SEARCH (Strong AI)
    // Member 3: Iterative deepening from depth 2 to MAX_DEPTH; PVS for pruning.
    // Decision: try each valid move. Explore: recurse with PVS (null-window search).
    // Undo: remove disc after each branch. Killer + history for move ordering.
    // Time: O(b^d) with PVS; iterative deepening multiplies by depth factor
    // =====================================================================

    public int findBestMoveMinimaxBacktracking(char player, int depth) {
        Arrays.fill(killerMoves, -1);
        for (int[] row : historyHeuristic) Arrays.fill(row, 0);

        char opp = getOpponent(player);

        int win = findImmediateWin(player);
        if (win != -1)
            return win;
        int block = findImmediateWin(opp);
        if (block != -1)
            return block;

        List<Integer> moves = dnc.findValidMovesDnC(0, board.getCols() - 1);
        if (moves.isEmpty())
            return -1;

        orderMovesByHeuristic(moves, player);
        int searchDepth = Math.min(Math.max(2, depth), MAX_DEPTH);

        int best = moves.get(0);
        int bestScore = Integer.MIN_VALUE;

        for (int d = 2; d <= searchDepth; d += 2) {
            int currentBest = moves.get(0);
            int currentScore = Integer.MIN_VALUE;
            for (int col : moves) {
                board.insertDisc(col, player);
                int score = -pvs(player, d - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
                board.removeDisc(col);
                recordKiller(col, 0);
                recordHistory(col, player, 0);
                if (score > currentScore || (score == currentScore && centerDistance(col) < centerDistance(currentBest))) {
                    currentScore = score;
                    currentBest = col;
                }
            }
            best = currentBest;
            bestScore = currentScore;
            if (bestScore >= WIN_SCORE - MAX_DEPTH)
                break;
        }
        return best;
    }

    private int pvs(char player, int depth, int alpha, int beta, boolean isMax) {
        char opp = getOpponent(player);

        if (checkPlayerWins(player))
            return WIN_SCORE + depth;
        if (checkPlayerWins(opp))
            return -WIN_SCORE - depth;
        if (board.isBoardFull() || depth <= 0)
            return dnc.evaluatePositionDnC(player);

        List<Integer> moves = dnc.findValidMovesDnC(0, board.getCols() - 1);
        orderMovesByHeuristic(moves, isMax ? player : opp);

        int best = Integer.MIN_VALUE;
        char mover = isMax ? player : opp;
        boolean first = true;

        for (int col : moves) {
            board.insertDisc(col, mover);
            int score;
            if (first) {
                score = -pvs(player, depth - 1, -beta, -alpha, !isMax);
                first = false;
            } else {
                score = -pvs(player, depth - 1, -alpha - 1, -alpha, !isMax);
                if (score > alpha && score < beta)
                    score = -pvs(player, depth - 1, -beta, -score, !isMax);
            }
            board.removeDisc(col);
            recordKiller(col, MAX_DEPTH - depth);
            recordHistory(col, mover, MAX_DEPTH - depth);
            best = Math.max(best, score);
            alpha = Math.max(alpha, score);
            if (beta <= alpha)
                break;
        }
        return best;
    }

    private void orderMovesByHeuristic(List<Integer> moves, char currentPlayer) {
        char opponent = getOpponent(currentPlayer);
        moves.sort((a, b) -> {
            int threatA = getThreatScore(a, currentPlayer);
            int threatB = getThreatScore(b, currentPlayer);
            if (threatA != threatB) return threatB - threatA;
            int blockA = getBlockScore(a, currentPlayer, opponent);
            int blockB = getBlockScore(b, currentPlayer, opponent);
            if (blockA != blockB) return blockB - blockA;
            int killerA = getKillerPriority(a);
            int killerB = getKillerPriority(b);
            if (killerA != killerB) return killerB - killerA;
            int histA = getHistoryScore(a);
            int histB = getHistoryScore(b);
            if (histA != histB) return histB - histA;
            return Integer.compare(centerDistance(a), centerDistance(b));
        });
    }

    private int getThreatScore(int col, char player) {
        if (!board.isValidMove(col)) return 0;
        int row = board.insertDisc(col, player);
        if (row < 0) { board.removeDisc(col); return 0; }
        int score = dnc.checkWin(player) ? 1000 : (countThreats(player, row, col) * 50);
        board.removeDisc(col);
        return score;
    }

    private int countThreats(char player, int row, int col) {
        char[][] g = board.getBoard();
        int[][] dirs = {{0, 1}, {1, 0}, {1, 1}, {-1, 1}};
        int count = 0;
        for (int[] d : dirs) {
            int fwd = countPiecesFrom(g, row + d[0], col + d[1], d[0], d[1], player);
            int bwd = countPiecesFrom(g, row - d[0], col - d[1], -d[0], -d[1], player);
            if (1 + fwd + bwd >= 3) count++;
        }
        return count;
    }

    private int getBlockScore(int col, char blocker, char opponent) {
        if (!board.isValidMove(col)) return 0;
        int row = board.insertDisc(col, blocker);
        if (row < 0) return 0;
        int score = scoreBlockAt(opponent, row, col);
        board.removeDisc(col);
        return score;
    }

    private int getKillerPriority(int col) {
        for (int d = 0; d < killerMoves.length; d++)
            if (killerMoves[d] == col) return 1000 - d;
        return 0;
    }

    private int getHistoryScore(int col) {
        return (col >= 0 && col < 7) ? historyHeuristic[col][0] + historyHeuristic[col][1] : 0;
    }

    private void recordKiller(int col, int depth) {
        if (depth >= 0 && depth < killerMoves.length)
            killerMoves[depth] = col;
    }

    private void recordHistory(int col, char player, int depth) {
        if (col >= 0 && col < 7)
            historyHeuristic[col][player == 'R' ? 0 : 1] += (1 << Math.min(depth, 10));
    }

    private int countPiecesFrom(char[][] g, int r, int c, int dr, int dc, char p) {
        int n = 0;
        for (int i = 0; i < 3; i++) {
            int nr = r + i * dr, nc = c + i * dc;
            if (nr < 0 || nr >= board.getRows() || nc < 0 || nc >= board.getCols() || g[nr][nc] != p)
                return n;
            n++;
        }
        return n;
    }

    // =====================================================================
    // METHOD 4: THREAT HEURISTIC WITH BACKTRACKING
    // Member 4: Backtracking - try each move (simulate), explore (score with
    // 1-ply opponent lookahead), undo. Recursive over move list.
    // Time: O(C * R) per move; 1-ply adds O(C) opponent responses
    // =====================================================================

    public int findBestMoveThreatHeuristic(char player) {
        char opp = getOpponent(player);

        int win = findImmediateWin(player);
        if (win != -1)
            return win;
        int block = findImmediateWin(opp);
        if (block != -1)
            return block;

        List<Integer> moves = dnc.findValidMovesDnC(0, board.getCols() - 1);
        if (moves.isEmpty())
            return -1;

        return findBestMoveThreatHeuristicBacktrack(moves, player, opp, 0, -1, Integer.MIN_VALUE);
    }

    /**
     * Backtracking: try move (simulate) -> explore (score with 1-ply lookahead) -> undo -> recurse.
     */
    private int findBestMoveThreatHeuristicBacktrack(List<Integer> moves, char player, char opp,
            int idx, int bestCol, int bestScore) {
        if (idx >= moves.size()) return bestCol;

        int col = moves.get(idx);
        simulatePlaceDisc(col, player);
        int score = scoreThreatHeuristicWithLookahead(col, player, opp);
        undoPlaceDisc(col);

        if (score > bestScore) {
            bestScore = score;
            bestCol = col;
        }
        return findBestMoveThreatHeuristicBacktrack(moves, player, opp, idx + 1, bestCol, bestScore);
    }

    /**
     * Score our move; 1-ply lookahead: consider opponent's best blocking response.
     */
    private int scoreThreatHeuristicWithLookahead(int col, char player, char opp) {
        int emptyRow = getDropRow(col);
        if (emptyRow < 0) return Integer.MIN_VALUE;
        int row = emptyRow + 1;

        int myThreat = scoreThreatAt(player, row, col);
        int blockVal = scoreBlockAt(opp, row, col);
        int doubleThreat = (countThreats(player, row, col) >= 2) ? 100 : 0;
        int centerBonus = 10 - centerDistance(col);
        int baseScore = myThreat * 2 + blockVal * 3 + doubleThreat * 5 + centerBonus;

        List<Integer> oppMoves = dnc.findValidMovesDnC(0, board.getCols() - 1);
        if (oppMoves.isEmpty()) return baseScore;

        int worst = Integer.MAX_VALUE;
        for (int oppCol : oppMoves) {
            simulatePlaceDisc(oppCol, opp);
            int ourScoreAfter = scorePositionAfterOpponent(col, player, opp);
            undoPlaceDisc(oppCol);
            worst = Math.min(worst, ourScoreAfter);
        }
        return worst == Integer.MAX_VALUE ? baseScore : worst;
    }

    private int scorePositionAfterOpponent(int ourCol, char player, char opp) {
        int emptyRow = getDropRow(ourCol);
        if (emptyRow < 0) return 0;
        int row = emptyRow + 1;
        return scoreThreatAt(player, row, ourCol) * 2 + scoreBlockAt(opp, row, ourCol) * 3
                + (10 - centerDistance(ourCol));
    }

    private int getDropRow(int col) {
        char[][] g = board.getBoard();
        for (int r = board.getRows() - 1; r >= 0; r--)
            if (g[r][col] == ' ')
                return r;
        return -1;
    }

    private int scoreThreatAt(char player, int row, int col) {
        char[][] g = board.getBoard();
        int[][] dirs = {{0, 1}, {1, 0}, {1, 1}, {-1, 1}};
        int total = 0;
        for (int[] d : dirs) {
            int fwd = countPiecesFrom(g, row + d[0], col + d[1], d[0], d[1], player);
            int bwd = countPiecesFrom(g, row - d[0], col - d[1], -d[0], -d[1], player);
            int lineLen = 1 + fwd + bwd;
            if (lineLen == 2)
                total += 20;
            else if (lineLen >= 3)
                total += 50;
        }
        return total;
    }

    private int scoreBlockAt(char opp, int row, int col) {
        char[][] g = board.getBoard();
        int rows = board.getRows(), cols = board.getCols();
        int[][] dirs = {{0, 1}, {1, 0}, {1, 1}, {-1, 1}};
        for (int[] d : dirs) {
            for (int offset = 0; offset < 4; offset++) {
                int sr = row - offset * d[0], sc = col - offset * d[1];
                int er = sr + 3 * d[0], ec = sc + 3 * d[1];
                if (sr < 0 || sc < 0 || sr >= rows || sc >= cols || er >= rows || ec >= cols || er < 0 || ec < 0)
                    continue;
                int oppCount = 0;
                for (int i = 0; i < 4; i++) {
                    int nr = sr + i * d[0], nc = sc + i * d[1];
                    if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && g[nr][nc] == opp)
                        oppCount++;
                }
                if (oppCount == 3)
                    return 100;
            }
        }
        return 0;
    }

}

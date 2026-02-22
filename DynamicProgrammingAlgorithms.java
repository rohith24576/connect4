/**
 * DynamicProgrammingAlgorithms.java
 * Eight methods, each representing one algorithm used in DP-style game search:
 * memoization, Zobrist hashing, transposition table, alpha-beta, move ordering, etc.
 * Each method is medium-length (not too long, not too short).
 */

import java.util.*;

public class DynamicProgrammingAlgorithms {

    private final Board board;
    private final DivideAndConquerGreedy dnc;

    private final Map<Long, TTEntry> transpositionTable;
    private final Map<Long, Integer> evaluationCache;
    private final Map<Long, Integer> winMoveCache;
    private final Map<Long, List<Integer>> moveOrderCache;
    private final long[][] zobristTable;
    private final int[] killerMoves;
    private final int[][] historyHeuristic;

    private static final int EXACT = 0, LOWER = 1, UPPER = 2;
    private static final int WIN_SCORE = 100000;
    private static final int MAX_CACHE_SIZE = 50000;

    private long cacheHits, cacheMisses;

    public DynamicProgrammingAlgorithms(Board board, DivideAndConquerGreedy dnc) {
        this.board = board;
        this.dnc = dnc;
        this.transpositionTable = new HashMap<>();
        this.evaluationCache = new HashMap<>();
        this.winMoveCache = new HashMap<>();
        this.moveOrderCache = new HashMap<>();
        this.zobristTable = initZobristTable();
        this.killerMoves = new int[32];
        this.historyHeuristic = new int[7][2];
    }

    // =====================================================================
    // METHOD 1: ROOT SEARCH WITH GREEDY WIN/BLOCK
    // DP algorithm: Early termination - avoid full search when win/block exists.
    // TIME COMPLEXITY: O(R*C^2) win/block + O(C * minimaxMemo) for root moves
    // =====================================================================

    public int findBestMoveHard(char player, int depth) {
        clearCache();
        char opp = (player == 'R') ? 'Y' : 'R';

        int win = tryImmediateWin(player);
        if (win != -1) return win;

        int block = tryImmediateWin(opp);
        if (block != -1) return block;

        int cols = board.getCols();
        int searchDepth = Math.max(1, depth);
        List<Integer> moves = dnc.findValidMovesDnC(0, cols - 1);
        if (moves.isEmpty()) return -1;

        orderMovesByHeuristic(moves, player);
        int best = moves.get(0);
        int maxScore = Integer.MIN_VALUE;
        int center = cols / 2;

        for (int col : moves) {
            board.insertDisc(col, player);
            int score = minimaxMemo(player, searchDepth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
            board.removeDisc(col);
            if (score > maxScore) {
                maxScore = score;
                best = col;
            } else if (score == maxScore) {
                int bestDist = Math.abs(best - center);
                int colDist = Math.abs(col - center);
                if (colDist < bestDist) best = col;
            }
        }
        return best;
    }

    // =====================================================================
    // METHOD 2: GREEDY EARLY EXIT - TRY IMMEDIATE WIN
    // DP algorithm: Memoization - cache winning column for board state; reuse when
    // same position reached from different game tree paths.
    // TIME COMPLEXITY: O(1) on cache hit; O(C log C) sort + O(R*C^2) worst on miss
    // =====================================================================

    /**
     * Stack-style recursion: try center column first, then recurse on left/right halves.
     * Memoization: cache (hash, player) -> winning column; avoids recomputing same position.
     */
    private int tryImmediateWin(char player) {
        long hash = computeZobristHash();
        long key = (hash << 1) | (player == 'R' ? 0 : 1);
        Integer cached = winMoveCache.get(key);
        if (cached != null) return cached;

        List<Integer> cols = new ArrayList<>();
        for (int c = 0; c < board.getCols(); c++) cols.add(c);
        cols.sort((a, b) -> Integer.compare(
            Math.abs(a - board.getCols() / 2),
            Math.abs(b - board.getCols() / 2)));
        int result = tryImmediateWinStack(player, cols, 0);
        winMoveCache.put(key, result);
        return result;
    }

    private int tryImmediateWinStack(char player, List<Integer> cols, int idx) {
        if (idx >= cols.size()) return -1;
        int col = cols.get(idx);
        if (col < 0 || col >= board.getCols() || !board.isValidMove(col))
            return tryImmediateWinStack(player, cols, idx + 1);
        board.insertDisc(col, player);
        boolean wins = dnc.checkWin(player);
        board.removeDisc(col);
        if (wins) return col;
        return tryImmediateWinStack(player, cols, idx + 1);
    }

    // =====================================================================
    // METHOD 3: ZOBRIST HASHING
    // DP algorithm: Fast state fingerprint for memoization key.
    // TIME COMPLEXITY: O(R * C)
    // =====================================================================

    private long computeZobristHash() {
        long hash = 0;
        char[][] g = board.getBoard();
        int rows = board.getRows();
        int cols = board.getCols();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                char cell = g[r][c];
                if (cell == ' ') continue;
                int idx = r * cols + c;
                int p = (cell == 'R') ? 1 : 2;
                hash ^= zobristTable[idx][p];
            }
        }
        return hash;
    }

    private long[][] initZobristTable() {
        Random r = new Random(42);
        long[][] table = new long[board.getRows() * board.getCols()][3];
        for (int i = 0; i < table.length; i++)
            for (int j = 0; j < 3; j++)
                table[i][j] = r.nextLong();
        return table;
    }

    // =====================================================================
    // METHOD 4: EVALUATION MEMOIZATION
    // DP algorithm: Cache heuristic scores; avoid recomputing same position.
    // TIME COMPLEXITY: O(1) on cache hit; O(R*C) on cache miss
    // =====================================================================

    public int evaluateCached(char player) {
        long hash = computeZobristHash();
        Integer cached = evaluationCache.get(hash);
        if (cached != null) {
            cacheHits++;
            return cached;
        }
        cacheMisses++;
        int score = dnc.evaluatePositionDnC(player);
        if (evaluationCache.size() >= MAX_CACHE_SIZE) {
            evictEvaluationCache();
        }
        evaluationCache.put(hash, score);
        return score;
    }

    /**
     * Full cache reset when full. Distinct from evictOldEntries (depth-based partial eviction).
     */
    private void evictEvaluationCache() {
        evaluationCache.clear();
    }

    // =====================================================================
    // METHOD 5: TRANSPOSITION TABLE LOOKUP
    // DP algorithm: Retrieve cached minimax result with bound types.
    // TIME COMPLEXITY: O(1)
    // =====================================================================

    private Integer lookupTransposition(long hash, int depth, int alpha, int beta) {
        TTEntry entry = transpositionTable.get(hash);
        if (entry == null) return null;
        if (entry.depth < depth) return null;
        cacheHits++;
        if (entry.flag == EXACT) return entry.score;
        if (entry.flag == LOWER) {
            if (entry.score >= beta) return entry.score;
            return null;
        }
        if (entry.flag == UPPER) {
            if (entry.score <= alpha) return entry.score;
            return null;
        }
        return null;
    }

    // =====================================================================
    // METHOD 6: TRANSPOSITION TABLE STORAGE + EVICTION
    // DP algorithm: Store results for overlapping subproblems; evict when full.
    // TIME COMPLEXITY: O(1) for store; O(k) for evictOldEntries
    // =====================================================================

    private void storeTransposition(long hash, int depth, int score, int flag) {
        int size = transpositionTable.size();
        if (size >= MAX_CACHE_SIZE) {
            evictOldEntries();
        }
        TTEntry e = new TTEntry(score, depth, flag, 0);
        transpositionTable.put(hash, e);
    }

    private void evictOldEntries() {
        int size = transpositionTable.size();
        if (size < MAX_CACHE_SIZE) return;
        int remove = Math.max(1, size / 4);
        List<Map.Entry<Long, TTEntry>> entries = new ArrayList<>(transpositionTable.entrySet());
        entries.sort((a, b) -> Integer.compare(a.getValue().depth, b.getValue().depth));
        for (int i = 0; i < remove && i < entries.size(); i++) {
            transpositionTable.remove(entries.get(i).getKey());
        }
    }

    // =====================================================================
    // METHOD 7: MOVE ORDERING BY HEURISTIC (KILLER + HISTORY + CENTER)
    // DP algorithm: Memoization - cache move ordering per board state; reuse when
    // same position reached from different move orders (overlapping subproblems).
    // TIME COMPLEXITY: O(1) on cache hit; O(C log C) on miss
    // =====================================================================

    private void orderMovesByHeuristic(List<Integer> moves, char currentPlayer) {
        long hash = computeZobristHash();
        List<Integer> cachedOrder = moveOrderCache.get(hash);
        if (cachedOrder != null) {
            moves.sort((a, b) -> {
                int ia = cachedOrder.indexOf(a);
                int ib = cachedOrder.indexOf(b);
                if (ia < 0) ia = Integer.MAX_VALUE;
                if (ib < 0) ib = Integer.MAX_VALUE;
                return Integer.compare(ia, ib);
            });
            return;
        }

        int cols = board.getCols();
        int center = cols / 2;
        char opponent = (currentPlayer == 'R') ? 'Y' : 'R';
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
            return Integer.compare(Math.abs(a - center), Math.abs(b - center));
        });
        moveOrderCache.put(hash, new ArrayList<>(moves));
    }

    private int getThreatScore(int col, char player) {
        if (!board.isValidMove(col)) return 0;
        board.insertDisc(col, player);
        int score = dnc.checkWin(player) ? 100 : (hasThreat(player) ? 50 : 0);
        board.removeDisc(col);
        return score;
    }

    private int getBlockScore(int col, char currentPlayer, char opponent) {
        if (!board.isValidMove(col)) return 0;
        int row = board.insertDisc(col, currentPlayer);
        if (row < 0) return 0;
        int score = blocksOpponent(row, col, opponent) ? 80 : 0;
        board.removeDisc(col);
        return score;
    }

    private boolean hasThreat(char player) {
        char[][] g = board.getBoard();
        int rows = board.getRows(), cols = board.getCols();
        int[][] dirs = {{0,1},{1,0},{1,1},{1,-1}};
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (g[r][c] != player) continue;
                for (int[] d : dirs) {
                    int count = countInDirection(r, c, d[0], d[1], player);
                    if (count >= 3) return true;
                }
            }
        }
        return false;
    }

    /**
     * Explicit 3-cell check: verify each of 3 consecutive cells in fwd/bwd direction.
     * Distinct from Backtracking's window-based scoreBlockAt.
     */
    private boolean blocksOpponent(int row, int col, char opponent) {
        char[][] g = board.getBoard();
        int rows = board.getRows(), cols = board.getCols();
        int[][] dirs = {{0,1},{1,0},{1,1},{1,-1}};
        for (int[] d : dirs) {
            if (hasThreeConsecutive(g, row + d[0], col + d[1], d[0], d[1], opponent, rows, cols))
                return true;
            if (hasThreeConsecutive(g, row - d[0], col - d[1], -d[0], -d[1], opponent, rows, cols))
                return true;
        }
        return false;
    }

    private boolean hasThreeConsecutive(char[][] g, int r, int c, int dr, int dc, char p, int rows, int cols) {
        for (int i = 0; i < 3; i++) {
            int nr = r + i * dr, nc = c + i * dc;
            if (nr < 0 || nr >= rows || nc < 0 || nc >= cols || g[nr][nc] != p) return false;
        }
        return true;
    }

    private int countInDirection(int r, int c, int dr, int dc, char player) {
        char[][] g = board.getBoard();
        int rows = board.getRows(), cols = board.getCols();
        int count = 0;
        for (int i = 0; i < 4; i++) {
            int nr = r + i * dr, nc = c + i * dc;
            if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) break;
            if (g[nr][nc] == player) count++;
            else break;
        }
        return count;
    }

    private int getKillerPriority(int col) {
        for (int d = 0; d < killerMoves.length; d++)
            if (killerMoves[d] == col) return 1000 - d;
        return 0;
    }

    private int getHistoryScore(int col) {
        return (col >= 0 && col < 7) ? historyHeuristic[col][0] + historyHeuristic[col][1] : 0;
    }

    // =====================================================================
    // METHOD 8: MINIMAX WITH ALPHA-BETA + MEMOIZATION
    // DP algorithm: Overlapping subproblems - same position reached by different
    // move orders; transposition table stores and reuses results.
    // TIME COMPLEXITY: O(1) on TT hit; O(b^d) worst without memo; O(unique_states) with TT
    // =====================================================================

    public int minimaxMemo(char player, int depth, int alpha, int beta, boolean isMax) {
        char opp = (player == 'R') ? 'Y' : 'R';
        long hash = computeZobristHash();

        Integer cached = lookupTransposition(hash, depth, alpha, beta);
        if (cached != null) return cached;
        cacheMisses++;

        if (dnc.checkWin(player)) return WIN_SCORE + depth;
        if (dnc.checkWin(opp)) return -WIN_SCORE - depth;
        if (board.isBoardFull() || depth <= 0) {
            int eval = evaluateCached(player);
            storeTransposition(hash, depth, eval, EXACT);
            return eval;
        }

        List<Integer> moves = dnc.findValidMovesDnC(0, board.getCols() - 1);
        char currentPlayer = isMax ? player : opp;
        orderMovesByHeuristic(moves, currentPlayer);

        int score;
        if (isMax) {
            int maxEval = Integer.MIN_VALUE;
            for (int col : moves) {
                board.insertDisc(col, player);
                int eval = minimaxMemo(player, depth - 1, alpha, beta, false);
                board.removeDisc(col);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            score = maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int col : moves) {
                board.insertDisc(col, opp);
                int eval = minimaxMemo(player, depth - 1, alpha, beta, true);
                board.removeDisc(col);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            score = minEval;
        }

        int flag = (score <= alpha) ? UPPER : (score >= beta) ? LOWER : EXACT;
        storeTransposition(hash, depth, score, flag);
        return score;
    }

    // ---------------------------------------------------------------------
    // Cache management (used by methods above)
    // ---------------------------------------------------------------------

    public void clearCache() {
        transpositionTable.clear();
        evaluationCache.clear();
        winMoveCache.clear();
        moveOrderCache.clear();
        Arrays.fill(killerMoves, -1);
        for (int[] row : historyHeuristic) Arrays.fill(row, 0);
        cacheHits = cacheMisses = 0;
    }

    private static class TTEntry {
        int score, depth, flag, bestMove;
        TTEntry(int score, int depth, int flag, int bestMove) {
            this.score = score;
            this.depth = depth;
            this.flag = flag;
            this.bestMove = bestMove;
        }
    }
}

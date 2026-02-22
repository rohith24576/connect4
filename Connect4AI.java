/**
 * Connect4AI.java
 * Coordinator for Connect4 algorithms. Delegates to paradigm-specific classes.
 */

import java.util.*;

public class Connect4AI {

    private final Board board;
    private final DivideAndConquerGreedy dnc;
    private final BacktrackingAlgorithms backtracking;
    private final DynamicProgrammingAlgorithms dp;

    public Connect4AI(Board board) {
        this.board = board;
        this.dnc = new DivideAndConquerGreedy(board);
        this.backtracking = new BacktrackingAlgorithms(board, dnc);
        this.dp = new DynamicProgrammingAlgorithms(board, dnc);
    }

    public boolean checkWin(char player) {
        return dnc.checkWin(player);
    }

    public int findBestMove(char player) {
        char opp = (player == 'R') ? 'Y' : 'R';

        int win = backtracking.findImmediateWin(player);
        if (win != -1) return win;
        int block = backtracking.findImmediateBlock(opp);
        if (block != -1) return block;

        List<Integer> valid = dnc.findValidMovesDnC(0, board.getCols() - 1);
        if (valid.isEmpty()) return -1;

        List<Integer> safe = new ArrayList<>();
        for (int col : valid)
            if (backtracking.isSafeMove(player, col)) safe.add(col);
        if (safe.isEmpty()) {
            return backtracking.findBestMoveThreatHeuristic(player);
        }

        int[] arr = new int[safe.size()];
        for (int i = 0; i < safe.size(); i++) arr[i] = safe.get(i);

        DivideAndConquerGreedy.WinChecker wc = p -> dnc.checkWin(p);
        DivideAndConquerGreedy.SafeMoveChecker sc = (p, c) -> backtracking.isSafeMove(p, c);
        return dnc.findBestMoveGreedy(player, arr, wc, sc);
    }

    public int findBestMoveModerate(char player) {
        return dp.findBestMoveHard(player, 4);
    }

    public int findBestMoveHard(char player) {
        return backtracking.findBestMoveMinimaxBacktracking(player, 6);
    }

    public void printAlgorithmInfo() {
        System.out.println("\n========================================");
        System.out.println("EASY: D&C + Greedy");
        System.out.println("MODERATE: DP (Minimax + Memo depth 4)");
        System.out.println("HARD: Backtracking (Iterative Deepening + PVS depth 6)");
        System.out.println("========================================\n");
    }
}

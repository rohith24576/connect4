================================================================================
                    CONNECT4 - TIME COMPLEXITY ANALYSIS
================================================================================
R = rows (6), C = columns (7), P = number of pieces on board, b = branching factor, d = search depth
================================================================================

--------------------------------------------------------------------------------
FILE: DivideAndConquerGreedy.java
--------------------------------------------------------------------------------

METHOD: checkWin(char player)
TIME COMPLEXITY: O(R * C) worst case; O(min scan) with early exit
WHY:
  - Divides win check into 4 directions: horizontal, vertical, diagonal down, diagonal up
  - Each direction uses D&C: splits row/column range recursively into halves
  - Base case: scan one row/column/diagonal = O(C) or O(R) per base case
  - Total base cases across recursion tree: O(R) for horizontal, O(C) for vertical
  - Greedy early exit: stops as soon as any direction finds a win; skips remaining directions
  - Worst case: no win found, all 4 directions fully scanned = O(R*C)

--------------------------------------------------------------------------------

METHOD: findValidMovesDnC(int start, int end)
TIME COMPLEXITY: O(C)
WHY:
  - D&C: 3-way split (left, center, right) of column range
  - Base case: single column validity check = O(1)
  - Each column visited exactly once in the recursion tree
  - Combine step: O(left + center + right) = O(total columns in range)
  - No sorting; linear merge. Total work proportional to number of columns.

--------------------------------------------------------------------------------

METHOD: evaluatePositionDnC(char player)
TIME COMPLEXITY: O(R * C)
WHY:
  - D&C: Divides board into 4 quadrants; evaluates each independently
  - Each quadrant scan: O((R/2) * (C/2)) = O(R*C/4) per quadrant
  - Four quadrants: 4 * O(R*C/4) = O(R*C)
  - countConnectedDnC: O(P) where P = pieces; P <= R*C
  - Combine: weighted sum = O(1)
  - Dominant term: O(R*C)

--------------------------------------------------------------------------------

METHOD: findBestMoveGreedy(char player, int[] columns, ...)
TIME COMPLEXITY: O(R * C^2) worst case; reduced with greedy pruning
WHY:
  - D&C: Splits columns into left/right halves recursively
  - Recurrence: T(n) = 2*T(n/2) + O(R*C) for chooseBetter (evaluate 2 moves)
  - Total combine steps across recursion tree: O(C) (1 + 2 + 4 + ... + C/2)
  - Each combine: evaluateColumnScore twice = O(R*C) each
  - Total: O(C) * O(R*C) = O(R*C^2)
  - Greedy: Early exit when winning move found; skips right subtree = less work

--- DivideAndConquerGreedy HELPER METHODS ---
  checkHorizontalDnC, checkVerticalDnC, checkDiagonalDownDnC, checkDiagonalUpDnC: O(R*C) each worst
  checkHorizontalRange, checkVerticalRange, checkDiagDownRange, checkDiagUpRange: O(R) or O(C) per base
  scanRowForWin, scanColForWin, scanDiagonalForWin: O(C), O(R), O(C) respectively
  findValidMovesDnCGreedy: O(C) - each column visited once
  mergeCenterFirst, validateColumnRange: O(C), O(1)
  baseCaseSingleColumn, isColumnPlayable: O(1)
  evaluateQuadrant: O((rEnd-rStart)*(cEnd-cStart))
  combineQuadrantScoresGreedy: O(1)
  countConnectedDnC: O(P) where P = pieces, P <= R*C
  checkPattern: O(len) = O(1) for len=2,3,4
  findBestMoveGreedyRecurse: O(R*C^2) - same as findBestMoveGreedy
  chooseBetter: O(R*C) - two evaluateColumnScore calls
  isWinningMove: O(R*C) - insert, checkWin, remove
  evaluateColumnScore: O(R*C) - checkWin or evaluatePositionDnC

--------------------------------------------------------------------------------
FILE: BacktrackingAlgorithms.java
--------------------------------------------------------------------------------

METHOD: findImmediateWin(char player)
TIME COMPLEXITY: O(R * C^2) worst case
WHY:
  - Board-full and empty short-circuits: O(1)
  - orderColumnsByCenterPriority: O(C log C) sort by distance from center
  - findImmediateWinDnC: visits all C columns (collects all winning columns)
  - Base case per column: insert O(1), checkWin O(R*C), remove O(1)
  - C base cases: O(C) * O(R*C) = O(R*C^2)
  - pickBestWinningColumn: O(k) where k = winning cols, at most C

--------------------------------------------------------------------------------

METHOD: isSafeMove(char player, int col)
TIME COMPLEXITY: O(R * C^4) worst case with 3-ply; O(R * C^3) with 2-ply only
WHY:
  - createsOurFork: O(C * R*C) - if our move creates double threat, return true (safe)
  - exploreOpponentResponses: for each of C opponent moves
  - Per opponent move: checkWin, hasDoubleThreat O(C*R*C), allowsOpponentForkNext O(C*C*R*C),
    isTrappedInTwo O(C*C*R*C)
  - isTrappedInTwo: 3-ply - for each our response, check if opponent can force win
  - Pruning: early exit on any unsafe condition found

--------------------------------------------------------------------------------

METHOD: findBestMoveMinimaxBacktracking(char player, int depth)
TIME COMPLEXITY: O(d/2 * b^(d/2)) with iterative deepening + PVS
WHY:
  - Iterative deepening: searches depth 2, 4, 6, 8, 10 (or specified depth)
  - Principal Variation Search (PVS): null-window search for non-PV moves; O(b^(d/2)) with good ordering
  - Killer moves + history heuristic: improves move ordering, reduces effective branching factor
  - Each depth iteration: full PVS search; total work dominated by max depth
  - No memoization (pure backtracking); early exit on proven win

--------------------------------------------------------------------------------

METHOD: findBestMoveThreatHeuristic(char player)
TIME COMPLEXITY: O(R * C^2) worst case
WHY:
  - Win/block check: findImmediateWin called twice = 2 * O(R*C^2) = O(R*C^2) dominant
  - findValidMovesDnC: O(C)
  - For each valid move: getDropRow O(R), scoreThreatAt O(1), scoreBlockAt O(1), countThreats O(1)
  - Loop: O(C) moves * O(R) = O(R*C)
  - Total: O(R*C^2) + O(R*C) = O(R*C^2)

--- BacktrackingAlgorithms HELPER METHODS ---
  findImmediateBlock: O(R*C^2) - calls findImmediateWin
  orderColumnsByCenterPriority: O(C log C) - sort
  findImmediateWinDnC: O(R*C^2) - C base cases, checkWin each
  pickBestWinningColumn: O(k) where k = winning cols, at most C
  getValidColumns: O(C)
  isColumnValid: O(1)
  simulatePlaceDisc, undoPlaceDisc: O(1)
  checkPlayerWins: O(R*C) - delegates to dnc.checkWin
  createsOurFork: O(C * R*C) - C moves, checkWin each
  exploreOpponentResponses: O(C) * (checkWin + hasDoubleThreat + allowsOpponentForkNext + isTrappedInTwo)
  hasDoubleThreat: O(C * R*C)
  allowsOpponentForkNext: O(C * C * R*C) - nested loops
  isTrappedInTwo: O(C * C * R*C) - 3-ply
  getOpponent, getCenterColumn, centerDistance: O(1)
  pvs: O(b^(d/2)) with alpha-beta
  orderMovesByHeuristic: O(C log C) - sort; getThreatScore/getBlockScore O(R*C) each
  getThreatScore: O(R*C) - insert, checkWin, countThreats
  countThreats: O(1) - 4 dirs, countPiecesFrom max 3 each
  getBlockScore: O(R*C) - insert, scoreBlockAt
  getKillerPriority, getHistoryScore: O(depth), O(1)
  recordKiller, recordHistory: O(1)
  countPiecesFrom: O(1) - max 3 iterations
  getDropRow: O(R)
  scoreThreatAt: O(1) - 4 dirs, countPiecesFrom
  scoreBlockAt: O(1) - 4 dirs * 4 offsets * 4 cells

--------------------------------------------------------------------------------
FILE: DynamicProgrammingAlgorithms.java (8 methods, each = one DP algorithm)
--------------------------------------------------------------------------------

METHOD 1: findBestMoveHard(char player, int depth)
DP algorithm: Early termination - avoid full search when win/block exists
TIME COMPLEXITY: O(R*C^2) win/block + O(C * minimaxMemo) for root moves
WHY:
  - tryImmediateWin: O(C) columns * O(R*C) checkWin = O(R*C^2) worst
  - findValidMovesDnC: O(C); orderMovesByHeuristic: O(C log C)
  - For each of C moves: insert, minimaxMemo, remove
  - With TT: minimaxMemo cost reduced to O(unique_states) in practice

--------------------------------------------------------------------------------

METHOD 2: tryImmediateWin(char player)
DP algorithm: Pruning - stop search when a winning move is found
TIME COMPLEXITY: O(C log C) sort + O(R*C^2) worst; O(k * R*C) with early exit
WHY:
  - Stack-style recursion (tryImmediateWinStack): center-first column order
  - Sort columns by distance from center: O(C log C)
  - Per column: insert O(1), checkWin O(R*C), remove O(1)
  - Recursive index-based; returns immediately on first winning move
  - Worst case: no win, all C columns = O(C * R*C) = O(R*C^2)

--------------------------------------------------------------------------------

METHOD 3: computeZobristHash()
DP algorithm: Fast state fingerprint for memoization key
TIME COMPLEXITY: O(R * C)
WHY:
  - Iterates every cell once: R*C cells
  - Per cell: index O(1), player code O(1), XOR O(1)
  - Total: O(R*C). Enables O(1) HashMap lookup for memoization

--------------------------------------------------------------------------------

METHOD 4: evaluateCached(char player)
DP algorithm: Memoization - cache heuristic scores; avoid recomputing same position
TIME COMPLEXITY: O(1) on cache hit; O(R*C) on cache miss
WHY:
  - computeZobristHash: O(R*C); HashMap lookup: O(1)
  - On hit: return stored score = O(1)
  - On miss: evaluatePositionDnC = O(R*C), then store = O(1)

--------------------------------------------------------------------------------

METHOD 5: lookupTransposition(long hash, int depth, int alpha, int beta)
DP algorithm: Retrieve cached minimax result with bound types (EXACT/LOWER/UPPER)
TIME COMPLEXITY: O(1)
WHY:
  - HashMap get by hash: O(1) average
  - Depth and bound checks: O(1)
  - Returns cached score or null on miss

--------------------------------------------------------------------------------

METHOD 6: storeTransposition(...) / evictOldEntries() / evictEvaluationCache()
DP algorithm: Store results for overlapping subproblems; evict when cache full
TIME COMPLEXITY: O(1) for store; O(k) for evictOldEntries; O(k) for evictEvaluationCache
WHY:
  - storeTransposition: HashMap put = O(1); may trigger eviction
  - evictOldEntries: depth-based, remove k = size/4 entries = O(k)
  - evictEvaluationCache: full clear when evaluation cache full = O(k), k = cache size
  - Bounded cache prevents unbounded memory growth

--------------------------------------------------------------------------------

METHOD 7: orderMovesByHeuristic(List<Integer> moves)
DP algorithm: Reuse move order from previous cutoffs for better alpha-beta pruning
TIME COMPLEXITY: O(C log C)
WHY:
  - Sort C moves by killer priority, history score, center distance
  - Killer lookup: O(depth); history: O(1) per move
  - Dominant: sort = O(C log C)

--------------------------------------------------------------------------------

METHOD 8: minimaxMemo(char player, int depth, int alpha, int beta, boolean isMax)
DP algorithm: Overlapping subproblems - same position from different move orders;
              transposition table stores and reuses results
TIME COMPLEXITY: O(1) on TT hit; O(b^d) worst without memo; O(unique_states) with TT
WHY:
  - lookupTransposition: O(1); on hit return immediately
  - On miss: recurse. Alpha-beta: O(b^(d/2)) with good move ordering
  - Leaf: evaluateCached = O(1) hit or O(R*C) miss
  - TT: positions reached by different paths = cache hit

--- DynamicProgrammingAlgorithms HELPER METHODS ---
  tryImmediateWinStack: O(R*C^2) worst - same as tryImmediateWin
  evictEvaluationCache: O(k) where k = cache size
  evictOldEntries: O(k) where k = size/4 entries removed
  getThreatScore: O(R*C) - insert, checkWin, hasThreat
  getBlockScore: O(R*C) - insert, blocksOpponent
  hasThreat: O(R*C) - scan board, countInDirection
  blocksOpponent: O(1) - 4 dirs, hasThreeConsecutive
  hasThreeConsecutive: O(1) - 3 cells
  countInDirection: O(1) - max 4 cells
  getKillerPriority: O(depth)
  getHistoryScore: O(1)
  clearCache: O(k) where k = cache size

--------------------------------------------------------------------------------
SUMMARY TABLE
--------------------------------------------------------------------------------
Method                              | File                    | Time Complexity
-------------------------------------|-------------------------|----------------------------------
checkWin                             | DivideAndConquerGreedy  | O(R*C)
findValidMovesDnC                    | DivideAndConquerGreedy  | O(C)
evaluatePositionDnC                  | DivideAndConquerGreedy  | O(R*C)
findBestMoveGreedy                   | DivideAndConquerGreedy  | O(R*C^2)
findImmediateWin                     | BacktrackingAlgorithms  | O(R*C^2) worst
findImmediateBlock                   | BacktrackingAlgorithms  | O(R*C^2)
isSafeMove                           | BacktrackingAlgorithms  | O(R*C^4) worst (3-ply)
findBestMoveMinimaxBacktracking      | BacktrackingAlgorithms  | O(d * b^(d/2)) ID+PVS
findBestMoveThreatHeuristic          | BacktrackingAlgorithms  | O(R*C^2)
findBestMoveHard                     | DynamicProgrammingAlgo  | O(C * minimaxMemo)
tryImmediateWin                      | DynamicProgrammingAlgo  | O(R*C^2) worst
computeZobristHash                   | DynamicProgrammingAlgo  | O(R*C)
evaluateCached                       | DynamicProgrammingAlgo  | O(1) hit; O(R*C) miss
lookupTransposition                  | DynamicProgrammingAlgo  | O(1)
storeTransposition/evict*            | DynamicProgrammingAlgo  | O(1) / O(k)
orderMovesByHeuristic                | DynamicProgrammingAlgo  | O(C log C)
minimaxMemo                          | DynamicProgrammingAlgo  | O(1) hit; O(b^d) miss

--------------------------------------------------------------------------------

# Connect 4: A Multi-Paradigm Algorithmic Assessment Framework

A sophisticated implementation of the classic Connect 4 game, engineered as a comprehensive demonstration of progressive algorithmic complexity. This project showcases the application of diverse computer science paradigms, ranging from recursive divide-and-conquer strategies to advanced state-space search optimizations.

## Technical Overview

The framework is designed to provide a tiered challenge through three distinct AI architectures, each representing a different computational approach to game theory and decision-making. The system integrates a modern Java Swing-based interface with a high-performance backend, ensuring both visual clarity and algorithmic rigor.

## System Architectures

### Tier 1: Recursive Heuristics (Efficiency Focus)

The entry-level AI utilizes a **Divide and Conquer** paradigm coupled with **Greedy** evaluation.

- **Win Detection**: Employs recursive spatial partitioning to identify four-in-a-row sequences in $O(R \times C)$ time.
- **Evaluation Engine**: Board states are decomposed into quadrants for parallelizable assessment, followed by a weighted aggregation of quadrant scores.
- **Decision Logic**: Prioritizes immediate state transitions that yield a terminal win or prevent an immediate loss.

### Tier 2: State-Space Memoization (Storage Focus)

The moderate-tier AI implements **Minimax Search** enhanced by **Dynamic Programming** techniques to manage the exponential growth of the game tree.

- **Transposition Management**: Utilizes **Zobrist Hashing** for $O(1)$ state fingerprinting and retrieval, significantly reducing redundant computations in overlapping subproblems.
- **Memoization Layer**: Stores evaluated depths and score bounds (Exact, Lower, Upper) to prune entire branches based on historical search data.
- **Resource Control**: Features a bounded cache eviction strategy to maintain performance within strict memory constraints.

### Tier 3: Search Space Optimization (Time Focus)

The elite-tier AI employs advanced **Backtracking** and competitive search enhancements for deep-ply strategic planning.

- **Iterative Deepening**: Dynamically adjusts search depth to maximize utility within computational time limits.
- **Principal Variation Search (PVS)**: Optimized Alpha-Beta pruning that utilizes null-window searches to confirm the superiority of the principal variation.
- **Heuristic Move Ordering**: Integrates Killer Move heuristics and History heuristics to prioritize high-value moves, maximizing the probability of early cutoffs.

## Implementation Details

The codebase is organized into modular components to facilitate architectural clarity:

- **`Main.java`**: Orchestrates the application lifecycle and UI initialization.
- **`Connect4UI.java`**: Implements a high-fidelity, Neumorphic user interface with asynchronous AI processing.
- **`Board.java`**: Manages the immutable game state and move validation protocols.
- **`Connect4AI.java`**: Serves as the strategy dispatcher for the various AI modules.
- **`DivideAndConquerGreedy.java`**: Contains logic for recursive partitioning and greedy heuristics.
- **`DynamicProgrammingAlgorithms.java`**: Handles hashing, transposition tables, and memoized search.
- **`BacktrackingAlgorithms.java`**: Implements global search optimizations and competitive game theory algorithms.

## Computational Complexity Summary

| Component                | Methodology                  | Complexity                       |
| :----------------------- | :--------------------------- | :------------------------------- |
| **State Fingerprinting** | Zobrist Hashing              | $O(R \times C)$                  |
| **Win Validation**       | Sequential Scan (Optimized)  | $O(R \times C)$                  |
| **Memoized Search**      | Transposition Table Lookup   | $O(1)$ (Average Case)            |
| **Strategic Search**     | PVS with Iterative Deepening | $O(b^{d/2})$ (Empirical Average) |
| **Move Security**        | Multi-Ply Safety Analysis    | $O(R \times C^4)$                |

For a granular analysis of all implemented methods, please consult the [Technical Complexity Specification](./timecomplexity.txt).

## Deployment and Execution

### Prerequisites

- Java Development Kit (JDK) 8.0 or higher.

### Compilation

From the project root directory, execute:

```bash
javac *.java
```

### Execution

Launch the framework using the following command:

```bash
java Main
```

---

_Developed as a repository for algorithmic research and demonstration._

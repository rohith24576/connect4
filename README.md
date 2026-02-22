# Connect 4: Advanced Algorithmic Strategy Game

A high-performance implementation of the classic Connect 4 game, serving as a robust platform for demonstrating progressive algorithmic intelligence. This project features a sophisticated AI system capable of strategic planning through multiple computational paradigms, from recursive heuristics to optimized state-space searches.

## Key Features

- **Multi-Tiered AI**: Challenge yourself against three distinct AI architectures, each representing a different level of strategic depth.
- **Modern UI**: A sleek, Java Swing-based interface with a Neumorphic design aesthetic, offering a premium user experience.
- **Real-Time Analysis**: Efficient backend processing ensures AI moves are calculated with high precision and minimal latency.
- **Cross-Platform**: Built with Java for seamless execution across different operating systems.

## AI Difficulty Levels

### Level 1: Divide and Conquer (Greedy)

The entry-level AI utilizes a **Divide and Conquer** paradigm coupled with **Greedy** evaluation.

- **Recursive Partitioning**: Board states are decomposed into quadrants for efficient assessment.
- **Immediate Utility**: Prioritizes moves that lead to an immediate win or prevent a loss.

### Level 2: Dynamic Programming (Memoization)

The moderate-tier AI implements **Minimax Search** enhanced by **Dynamic Programming** to manage the exponential growth of the game tree.

- **Transposition Tables**: Utilizes **Zobrist Hashing** for $O(1)$ state retrieval, eliminating redundant computations.
- **Memoized Evaluation**: Caches previously calculated scores to prune search branches effectively.

### Level 3: Backtracking (Strategic Search)

The elite-tier AI employs advanced **Backtracking** and competitive search enhancements for deep strategic planning.

- **Iterative Deepening**: Dynamically adjusts search depth within computational constraints.
- **Principal Variation Search (PVS)**: Optimized Alpha-Beta pruning for superior move selection.
- **Heuristic Move Ordering**: Uses history and killer-move heuristics to maximize search efficiency.

## Implementation Details

The codebase is organized into modular components for architectural clarity:

- **`Main.java`**: Application entry point and UI initialization.
- **`Connect4UI.java`**: Implements the modern user interface and asynchronous AI handling.
- **`Board.java`**: Core game logic and state management.
- **`Connect4AI.java`**: Strategy dispatcher for AI modules.
- **`DivideAndConquerGreedy.java`**: Logic for quadrant-based recursive heuristics.
- **`DynamicProgrammingAlgorithms.java`**: State hashing and transposition-based search.
- **`BacktrackingAlgorithms.java`**: Global search optimizations and minimax enhancements.

## Computational Complexity

| Component                | Methodology                  | Complexity                       |
| :----------------------- | :--------------------------- | :------------------------------- |
| **State Fingerprinting** | Zobrist Hashing              | $O(R \times C)$                  |
| **Win Validation**       | Sequential Scan (Optimized)  | $O(R \times C)$                  |
| **Memoized Search**      | Transposition Table Lookup   | $O(1)$ (Average Case)            |
| **Strategic Search**     | PVS with Iterative Deepening | $O(b^{d/2})$ (Empirical Average) |

For a detailed analysis, refer to the [Technical Complexity Specification](./timecomplexity.txt).

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 8.0 or higher.

### Compilation

From the project root directory, run:

```bash
javac *.java
```

### Execution

Launch the game with:

```bash
java Main
```

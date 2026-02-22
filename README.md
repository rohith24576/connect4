# Connect 4 - Algorithm Showcase

A premium, modern Connect 4 implementation in Java featuring a variety of computer science algorithms, from Divide and Conquer to advanced Backtracking with Iterative Deepening.

![Connect 4 Gameplay](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Swing UI](https://img.shields.io/badge/UI-Swing-blue?style=for-the-badge)
![Algorithms](https://img.shields.io/badge/Algorithms-D%26C%20%7C%20DP%20%7C%20Backtracking-brightgreen?style=for-the-badge)

## 🌟 Overview

This project is not just a game; it's an algorithmic deep-dive into game theory and search optimization. Built with a modern Neumorphic UI, it allows players to challenge an AI that utilizes different computational strategies based on the selected difficulty level.

## 🚀 Key Features

- **Modern Neumorphic Design**: A sleek, card-based UI with subtle shadows and a curated color palette.
- **Three Distinct AI Difficulties**:
  - 🟢 **Easy**: Uses **Divide & Conquer** and **Greedy** heuristics.
  - 🟡 **Moderate**: Leverages **Dynamic Programming** with Minimax and Memoization.
  - 🔴 **Hard**: Employs advanced **Backtracking** with Iterative Deepening and Principal Variation Search (PVS).
- **Real-time Score Tracking**: Keeps track of player wins, AI wins, and draws.
- **Detailed Complexity Analysis**: Every algorithm is documented with its Big O time complexity.

## 🧠 Algorithmic Implementation

### 1. Divide & Conquer + Greedy (Easy)

- **Win Detection**: Uses recursive range splitting to check for four-in-a-row in $O(R \times C)$ time.
- **Position Evaluation**: Divides the board into quadrants, evaluates them independently, and combines the results.
- **Greedy Selection**: Prioritizes immediate wins and blocks without deep searching.

### 2. Dynamic Programming (Moderate)

- **Minimax with Memoization**: Explores game states and caches them to avoid redundant calculations.
- **Transposition Tables**: Uses Zobrist Hashing ($O(R \times C)$) to uniquely identify board states for $O(1)$ lookup.
- **State Efficiency**: Implements bounded cache eviction to manage memory effectively.

### 3. Backtracking & Search Optimization (Hard)

- **Iterative Deepening**: Progressively searches deeper (Depth 2, 4, 6...) until the search limit is reached.
- **Principal Variation Search (PVS)**: A highly optimized version of Alpha-Beta pruning that uses null-window searches for non-PV moves.
- **Move Ordering**: Uses Heuristic scoring (Center priority, Threat/Block scores) and Killer Moves to maximize pruning efficiency.

## 📂 Project Structure

```bash
├── Main.java                 # Entry point of the application
├── Connect4UI.java           # Main Swing interface and UI logic
├── Board.java                # Core board state and move validation
├── Connect4AI.java           # Central controller for AI decision making
├── DivideAndConquerGreedy.java     # Logic for the Easy difficulty algorithms
├── DynamicProgrammingAlgorithms.java # Memoization and hashing implementations
├── BacktrackingAlgorithms.java       # Deep search and PVS implementations
└── timecomplexity.txt        # Comprehensive analysis of all method complexities
```

## 🛠️ Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher.

### Installation & Execution

1. Clone the repository:
   ```bash
   git clone https://github.com/Sreeram5678/connect4.git
   ```
2. Navigate to the project directory:
   ```bash
   cd connect4
   ```
3. Compile the project:
   ```bash
   javac *.java
   ```
4. Run the application:
   ```bash
   java Main
   ```

## 📊 Complexity Summary

| Method           | Algorithm File                 | Time Complexity                         |
| :--------------- | :----------------------------- | :-------------------------------------- |
| `checkWin`       | `DivideAndConquerGreedy`       | $O(R \times C)$                         |
| `evaluateCached` | `DynamicProgrammingAlgorithms` | $O(1)$ cache hit / $O(R \times C)$ miss |
| `isSafeMove`     | `BacktrackingAlgorithms`       | $O(R \times C^4)$ (3-ply check)         |
| `minimaxMemo`    | `DynamicProgrammingAlgorithms` | $O(\text{unique states})$ with TT       |
| `pvsSearch`      | `BacktrackingAlgorithms`       | $O(b^{d/2})$ with optimal pruning       |

_For a full breakdown, refer to [timecomplexity.txt](./timecomplexity.txt)._

---

Designed with ❤️ for Algorithmic Excellence.

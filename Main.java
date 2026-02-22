/**
 * Main.java
 * Entry point for Connect4 Game
 * Launches the UI
 */

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Launch the UI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new Connect4UI();
        });
    }
}

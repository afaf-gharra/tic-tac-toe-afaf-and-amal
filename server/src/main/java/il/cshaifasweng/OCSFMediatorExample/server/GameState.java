package il.cshaifasweng.OCSFMediatorExample.server;

import java.io.Serializable;

public class GameState implements Serializable {
    public char[][] board = new char[3][3];
    public char currentPlayer = 'X';
    public boolean gameOver = false;
    public String winner = null;

    public boolean makeMove(int row, int col, char player) {
        if (board[row][col] == '\0') {
            board[row][col] = player;
            checkWinner();
            currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
            return true;
        }
        return false;
    }

    private void checkWinner() {
        // Check rows, cols, diagonals...
        // Update `winner` and `gameOver` accordingly
    }
}

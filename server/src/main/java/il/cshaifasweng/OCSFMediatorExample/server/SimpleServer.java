package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimpleServer extends AbstractServer {
    private char currentPlayer = 'X';
    private char[][] grid = new char[3][3];
    private List<ConnectionToClient> clients = new ArrayList<>();

    public SimpleServer(int port) {
        super(port);
    }


    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
        String message = (String) msg;
        if (message.startsWith("MOVE")) {
            String[] parts = message.split(":");
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            char player = (char) client.getInfo("mark");

            if (player != currentPlayer) {
                try {
                    client.sendToClient("ERROR:Not your turn");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            if (grid[x][y] == '\0') {
                grid[x][y] = player;
                sendToAllClients("CELL:" + x + ":" + y + ":" + player);

                if (checkWin(player)) {
                    sendToAllClients("WIN:" + player);
                    resetGame();
                } else if (checkDraw()) {
                    sendToAllClients("DRAW");
                    resetGame();
                } else {
                    currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
                    sendToAllClients("TURN:" + currentPlayer);
                }
            } else {
                try {
                    client.sendToClient("ERROR:Cell already taken");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean checkWin(char player) {
        for (int i = 0; i < 3; i++) {
            if (grid[i][0] == player && grid[i][1] == player && grid[i][2] == player) return true;
            if (grid[0][i] == player && grid[1][i] == player && grid[2][i] == player) return true;
        }
        return (grid[0][0] == player && grid[1][1] == player && grid[2][2] == player) ||
                (grid[0][2] == player && grid[1][1] == player && grid[2][0] == player);
    }

    private boolean checkDraw() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (grid[i][j] == '\0') return false;
            }
        }
        return true;
    }

    private void resetGame() {
        grid = new char[3][3];
        currentPlayer = 'X';
        sendToAllClients("RESET");
        sendToAllClients("TURN:" + currentPlayer);
    }

    @Override
    protected synchronized void clientConnected(ConnectionToClient client) {
        if (clients.size() >= 2) {
            try {
                client.sendToClient("ERROR:Server is full");
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        clients.add(client);
        char mark = clients.size() == 1 ? 'X' : 'O';
        client.setInfo("mark", mark);
        try {
            client.sendToClient("WELCOME:" + mark);
            if (clients.size() == 2) {
                sendToAllClients("START");
                sendToAllClients("TURN:" + currentPlayer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected synchronized void clientDisconnected(ConnectionToClient client) {
        clients.remove(client);
    }

}
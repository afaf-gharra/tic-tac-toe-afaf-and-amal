package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.server.ocsf.AbstractServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameServer extends AbstractServer {
    private List<ConnectionToClient> players = new ArrayList<>();
    private GameState state = new GameState();

    public GameServer(int port) { super(port); }

    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client)  {
        System.out.println(client.getInetAddress() + " message: "+msg);
        if (msg instanceof int[] move && players.size() == 2) {
            int row = move[0]; int col = move[1];
            char playerSymbol = (client == players.get(0)) ? 'X' : 'O';
            if (state.currentPlayer == playerSymbol && state.makeMove(row, col, playerSymbol)) {
                sendToAllClients(GameMessage.MessageType.BOARD_UPDATE+" "+  state);
                if (state.gameOver) {
                    sendToAllClients(GameMessage.MessageType.GAME_OVER+" "+ state.winner);
                } else {
                    try {
                        players.get(state.currentPlayer == 'X' ? 0 : 1)
                                .sendToClient(GameMessage.MessageType.YOUR_TURN+" "+  null);
                        players.get(state.currentPlayer == 'O' ? 0 : 1)
                                .sendToClient(GameMessage.MessageType.WAIT+" "+  null);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                }
            } else {
                try {
                    client.sendToClient(GameMessage.MessageType.INVALID_MOVE+" "+ null);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    protected void clientConnected(ConnectionToClient client) {
        if (players.size() < 2) {
            super.clientConnected(client);
            System.out.println(client);
            System.out.println("Connected");

            players.add(client);
            char symbol = (players.size() == 1) ? 'X' : 'O';
            try {
                client.sendToClient(GameMessage.MessageType.ASSIGN_SYMBOL+" "+ symbol);
                if (players.size() == 2) {
                    Collections.shuffle(players);
                    players.get(0).sendToClient(GameMessage.MessageType.START_GAME+" "+ 'X');
                    players.get(1).sendToClient(GameMessage.MessageType.START_GAME+" "+  'O');
                    players.get(0).sendToClient(GameMessage.MessageType.YOUR_TURN+" "+ null);
                    players.get(1).sendToClient(GameMessage.MessageType.WAIT+" "+  null);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }
}
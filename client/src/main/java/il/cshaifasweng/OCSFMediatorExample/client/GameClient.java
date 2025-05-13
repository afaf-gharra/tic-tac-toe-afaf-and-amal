package il.cshaifasweng.OCSFMediatorExample.client;
import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;
import java.io.IOException;
import java.util.List;

public class GameClient extends AbstractClient {
    public enum MessageType { BOARD_UPDATE, YOUR_TURN, WAIT, GAME_OVER, START_GAME, INVALID_MOVE, ASSIGN_SYMBOL, MOVE }

    private char mySymbol;
    private GameState state;
    private static GameClient gameClient = null;
    public static boolean myTurn = false;
    private GameClient(String host, int port) {
        super(host, port);
    }

    @Override
    protected void handleMessageFromServer(Object msg) {
        System.out.println(msg);
        List<String> gameMessage = List.of(msg.toString().split(" "));
        MessageType key = MessageType.valueOf(gameMessage.get(0));
        switch (key) {
            case ASSIGN_SYMBOL -> mySymbol = (char) gameMessage.get(1).charAt(0);
            case BOARD_UPDATE -> {
//                GameState s = (GameState) gameMessage.get(1);
//                updateBoard(s.board);
            }
            case YOUR_TURN -> myTurn = true;
            case WAIT -> myTurn = false;
//            case MessageType.GAME_OVER -> showAlert("Game Over. Winner: " + gameMessage.get(1));
//            case MessageType.INVALID_MOVE -> showAlert("Invalid move!");
        }
    }

    static public GameClient getClient() {
        if (gameClient == null) {
            gameClient = new GameClient("localhost", 3000);
        }
        return gameClient;
    }

    public void sendMove(int row, int col) throws IOException, IOException {
        sendToServer(new int[]{row, col});
    }
}
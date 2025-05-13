package il.cshaifasweng.OCSFMediatorExample.server;

import java.io.Serializable;

public class GameMessage implements Serializable {

    public enum MessageType { BOARD_UPDATE, YOUR_TURN, WAIT, GAME_OVER, START_GAME, INVALID_MOVE, ASSIGN_SYMBOL }
    public MessageType type;
    public Object payload;
    private GameMessage(MessageType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }
}

package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.client.ocsf.AbstractClient;
import org.greenrobot.eventbus.EventBus;

public class SimpleClient extends AbstractClient {
    private EventBus eventBus = EventBus.getDefault();

    public SimpleClient(String host, int port) {
        super(host, port);
    }

    @Override
    protected void handleMessageFromServer(Object msg) {
        String message = (String) msg;
        eventBus.post(new ServerMessageEvent(message));
    }

    public static class ServerMessageEvent {
        private String message;

        public ServerMessageEvent(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
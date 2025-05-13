package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

public class GameGUI extends Application {
    private GameClient client;
    private Button[][] buttons = new Button[3][3];
    private char mySymbol;

    @Override
    public void start(Stage stage) throws Exception {
        EventBus.getDefault().register(this);
        client = GameClient.getClient();
        client.openConnection();

        GridPane grid = new GridPane();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int row = i, col = j;
                Button btn = new Button(" ");
                btn.setMinSize(100, 100);
                btn.setOnAction(e -> {
                    if (GameClient.myTurn && btn.getText().equals(" ")) {
                        try { client.sendMove(row, col); } catch (IOException ex) {}
                    }
                });
                buttons[i][j] = btn;
                grid.add(btn, j, i);
            }
        }



        stage.setScene(new Scene(grid));
        stage.setTitle("X/O Game");
        stage.show();
    }



    @Override
    public void stop() throws Exception {
        // TODO Auto-generated method stub
        EventBus.getDefault().unregister(this);
        client.sendToServer("remove client");
        client.closeConnection();
        super.stop();
    }

    @Subscribe
    public void onWarningEvent(WarningEvent event) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    String.format("Message: %s\nTimestamp: %s\n",
                            event.getWarning().getMessage(),
                            event.getWarning().getTime().toString())
            );
            alert.show();

        });

    }

    private void updateBoard(char[][] board) {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                buttons[i][j].setText(board[i][j] == '\0' ? " " : String.valueOf(board[i][j]));
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

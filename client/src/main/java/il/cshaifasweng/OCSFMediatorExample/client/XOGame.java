package il.cshaifasweng.OCSFMediatorExample.client;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.Objects;

public class XOGame extends Application {
    private XOClient client;
    private char myMark;
    private char currentTurn;
    private Stage primaryStage;
    private Label statusLabel; // Add class-level reference
    private GridPane gridPane; // Add class-level reference

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        EventBus.getDefault().register(this);

        gridPane = new GridPane();
        Button[][] buttons = new Button[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Button btn = new Button();
                btn.setPrefSize(100, 100);
                int x = i;
                int y = j;
                btn.setOnAction(e -> {
                    try {
                        handleButtonClick(x, y);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
                buttons[i][j] = btn;
                gridPane.add(btn, j, i);
            }
        }

        statusLabel = new Label("Waiting to connect...");
        VBox root = new VBox(10, gridPane, statusLabel);
        Scene scene = new Scene(root, 300, 350);
        primaryStage.setScene(scene);

        client = new XOClient("localhost", 3000);
        try {
            client.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        primaryStage.setTitle("Tic Tac Toe");
        primaryStage.show();
    }

    private void handleButtonClick(int x, int y) throws IOException {
        if (currentTurn == myMark) {
            client.sendToServer("MOVE:" + x + ":" + y);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServerMessage(XOClient.ServerMessageEvent event) {
        String message = event.getMessage();
        Platform.runLater(() -> {
            if (message.startsWith("WELCOME")) {
                myMark = message.split(":")[1].charAt(0);
                updateStatus("Connected as " + myMark);
            } else if (message.startsWith("CELL")) {
                String[] parts = message.split(":");
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                char mark = parts[3].charAt(0);
                Platform.runLater(() -> updateButton(x, y, mark));
            } else if (message.startsWith("TURN")) {
                currentTurn = message.split(":")[1].charAt(0);
                updateStatus("Current turn: " + currentTurn);
                setButtonsDisabled(myMark != currentTurn);
            } else if (message.startsWith("WIN")) {
                String winner = message.split(":")[1];
                showVictoryAlert(winner);
                updateStatus("Player " + message.split(":")[1] + " wins!");
                setButtonsDisabled(true);
            } else if (message.startsWith("DRAW")) {
                showDrawAlert();
                updateStatus("Game is a draw!");
                setButtonsDisabled(true);
            } else if (message.startsWith("RESET")) {
                Platform.runLater(this::resetBoard);
            } else if (message.startsWith("ERROR")) {
                updateStatus("Error: " + message.split(":")[1]);
            } else if (message.startsWith("START")) {
                updateStatus("Game started!");
            }
        });
    }

    private void showVictoryAlert(String winner) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.NONE);
            DialogPane dialogPane = alert.getDialogPane();

            ButtonType playAgainButton = new ButtonType("Play Again", ButtonBar.ButtonData.OK_DONE);
            alert.getButtonTypes().setAll(playAgainButton);

            VBox content = new VBox(20);
            content.setAlignment(Pos.CENTER);

            Label title = new Label(winner.equals(String.valueOf(myMark)) ?
                    "🎉 You Win! 🎉" : "😭 You Lose...");
            title.setStyle("-fx-font-size: 24px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

            Label subTitle = new Label(winner.equals(String.valueOf(myMark)) ?
                    "Congratulations! You're the champion!" : "Better luck next time!");
            subTitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");

            content.getChildren().addAll(title, subTitle);
            dialogPane.setContent(content);

            // Add animation
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(0),
                            new KeyValue(title.scaleXProperty(), 1),
                            new KeyValue(title.scaleYProperty(), 1)
                    ),
                    new KeyFrame(Duration.seconds(0.5),
                            new KeyValue(title.scaleXProperty(), 1.2),
                            new KeyValue(title.scaleYProperty(), 1.2)
                    ),
                    new KeyFrame(Duration.seconds(1),
                            new KeyValue(title.scaleXProperty(), 1),
                            new KeyValue(title.scaleYProperty(), 1)
                    )
            );
            timeline.setCycleCount(2);
            timeline.play();

            alert.showAndWait().ifPresent(response -> resetBoard());
        });
    }

    private void showDrawAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.NONE);
            DialogPane dialogPane = alert.getDialogPane();

            ButtonType playAgainButton = new ButtonType("Try Again", ButtonBar.ButtonData.OK_DONE);
            alert.getButtonTypes().setAll(playAgainButton);

            VBox content = new VBox(20);
            content.setAlignment(Pos.CENTER);

            Label title = new Label("🤝 It's a Draw! 🤝");
            title.setStyle("-fx-font-size: 24px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

            Label subTitle = new Label("No winners this time...");
            subTitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");

            content.getChildren().addAll(title, subTitle);
            dialogPane.setContent(content);

            // Add pulse animation
            ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(0.6), title);
            scaleTransition.setFromX(1);
            scaleTransition.setFromY(1);
            scaleTransition.setToX(1.1);
            scaleTransition.setToY(1.1);
            scaleTransition.setCycleCount(ScaleTransition.INDEFINITE);
            scaleTransition.setAutoReverse(true);
            scaleTransition.play();

            alert.showAndWait().ifPresent(response -> resetBoard());
        });
    }


    private void updateButton(int x, int y, char mark) {
        Platform.runLater(() -> {
            int index = x * 3 + y;
            Button btn = (Button) gridPane.getChildren().get(index);
            btn.setText(String.valueOf(mark));
            btn.setDisable(true);
        });
    }

    private void updateStatus(String text) {
        Platform.runLater(() -> statusLabel.setText(text));
    }

    private void resetBoard() {
        Platform.runLater(() -> {
            for (Node node : gridPane.getChildren()) {
                Button btn = (Button) node;
                btn.setText("");
                btn.setDisable(false);
            }
        });
    }

    private void setButtonsDisabled(boolean disabled) {
        for (Node node : gridPane.getChildren()) {
            node.setDisable(disabled);
        }
    }

    @Override
    public void stop() {
        try {
            client.closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        EventBus.getDefault().unregister(this);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
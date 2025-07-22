package GameSettings;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.multiplayer.MultiplayerService;
import com.almasb.fxgl.time.TimerAction;
import component.GameFactory;
import component.GameLogic;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.*;
import static com.almasb.fxgl.dsl.FXGL.*;

public class ClientGameApp extends GameApplication {

    // Game constants
    private static final int SCREEN_WIDTH = 1000;
    private static final int SCREEN_HEIGHT = 600;
    private static final double GAME_DURATION_SECONDS = 180.0;
    private static final int MAX_HIGH_SCORES_TO_SHOW = 10;
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 55555;

    public GameLogic gameLogic;

    private TimerAction gameTimerAction;
    private double timeLeft = GAME_DURATION_SECONDS;
    private final List<ScoreEntry> highScores = new ArrayList<>();

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(SCREEN_WIDTH);
        settings.setHeight(SCREEN_HEIGHT);
        settings.setTitle("Player - Sonic Adventure FXGL");
        settings.addEngineService(MultiplayerService.class);
    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new GameFactory());
        getGameScene().setBackgroundColor(Color.DARKBLUE);
        // Music music = getAssetLoader().loadMusic("OST.mp3");
        // getAudioPlayer().loopMusic(music);

        gameLogic = new GameLogic();
        gameLogic.init();

        showMainMenu();
    }

    /**
     * Creates and displays the main menu.
     */
    private void showMainMenu() {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #333333;");

        Text title = new Text("Main Menu");
        title.setStyle("-fx-font-size: 24px; -fx-fill: white;");

        Button btnPlay = createStyledButton("Play", "#0000FF", "#0000CC");
        Button btnHelp = createStyledButton("Help", "#FFFF00", "#CCCC00", Color.BLACK);
        Button btnAbout = createStyledButton("About", "#FF0000", "#CC0000");
        Button btnScores = createStyledButton("Scores", "#800080", "#6A0DAD");

        root.getChildren().addAll(title, btnPlay, btnHelp, btnAbout, btnScores);

        Stage menuStage = new Stage();
        btnPlay.setOnAction(e -> {
            menuStage.close();
            showCharacterSelectionMenu();
        });
        btnHelp.setOnAction(e -> showHelp());
        btnAbout.setOnAction(e -> showAbout());
        btnScores.setOnAction(e -> showScores());

        Scene scene = new Scene(root, 350, 350);
        menuStage.setScene(scene);
        menuStage.setTitle("Game Main Menu");
        menuStage.show();
    }
    
    /**
     * Utility method to create styled buttons for menus to reduce code duplication.
     */
    private Button createStyledButton(String text, String baseColor, String hoverColor, Color textColor) {
        Button button = new Button(text);
        String baseStyle = String.format("-fx-font-size: 18px; -fx-padding: 10px 20px; -fx-background-color: %s; -fx-text-fill: %s; -fx-border-radius: 5px; -fx-background-radius: 5px;", baseColor, textColor == Color.WHITE ? "white" : "black");
        String hoverStyle = String.format("-fx-font-size: 18px; -fx-padding: 10px 20px; -fx-background-color: %s; -fx-text-fill: %s; -fx-border-radius: 5px; -fx-background-radius: 5px;", hoverColor, textColor == Color.WHITE ? "white" : "black");

        button.setStyle(baseStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
        return button;
    }

    private Button createStyledButton(String text, String baseColor, String hoverColor) {
        return createStyledButton(text, baseColor, hoverColor, Color.WHITE);
    }


    /**
     * Shows the character selection menu.
     */
    private void showCharacterSelectionMenu() {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #333333;");

        Text title = new Text("Select Your Character");
        title.setStyle("-fx-font-size: 24px; -fx-fill: white;");

        Button btnSonic = createStyledButton("Sonic", "#0000FF", "#0000CC");
        Button btnTails = createStyledButton("Tails", "#FFFF00", "#CCCC00", Color.BLACK);
        Button btnKnuckles = createStyledButton("Knuckles", "#FF0000", "#CC0000");

        Stage stage = new Stage();
        btnSonic.setOnAction(e -> {
            stage.close();
            startNetworkAndGame();
        });
        btnTails.setOnAction(e -> {
            stage.close();
            startNetworkAndGame();
        });
        btnKnuckles.setOnAction(e -> {
            stage.close();
            startNetworkAndGame();
        });
        
        root.getChildren().addAll(title, btnSonic, btnTails, btnKnuckles);
        Scene scene = new Scene(root, 350, 250);
        stage.setScene(scene);
        stage.setTitle("Select Character");
        stage.show();
    }

    /**
     * Shows a help dialog with game instructions.
     */
    private void showHelp() {
        String helpText =
            "Game Instructions - Sonic Adventure FXGL\n" +
            "==================================\n" +
            "\n" +
            "Welcome to Sonic Adventure FXGL!\n" +
            "\n" +
            "How to Play:\n" +
            "- Use A and D to move left and right.\n" +
            "- Use W to jump.\n" +
            "- Collect rings to boost your score and for protection.\n" +
            "- Collect trash (paper, rubber, general) to clean the environment.\n" +
            "- Jump on enemy robots to defeat them.\n" +
            "- Defeat Eggman to win!\n" +
            "\n" +
            "Scoring:\n" +
            "- Your final score is the sum of collected trash and rings.\n" +
            "- A 'Game Over' results in a score of 0.\n" +
            "\n" +
            "Rules:\n" +
            "- Getting hit by an enemy makes you lose rings. No rings means you lose a life.\n" +
            "- Losing all lives is Game Over.\n" +
            "- Falling below the screen (Y > 1000) is Game Over.\n";
        getDialogService().showMessageBox(helpText);
    }

    /**
     * Shows an about dialog with credits and version info.
     */
    private void showAbout() {
        String aboutText = 
            "About Sonic Adventure FXGL\n\n" +
            "Programming Language: Java\n" +
            "External Libraries:\n" +
            "- FXGL (Game Framework)\n" +
            "- JavaFX (UI)\n\n" +
            "Developers:\n" +
            "- Millan, Villalba, Acosta, Rodriguez\n\n" +
            "Version: 1.0.2\n";
        getDialogService().showMessageBox(aboutText);
    }

    /**
     * Shows the high scores dialog.
     */
    private void showScores() {
        highScores.sort(Collections.reverseOrder());
        StringBuilder sb = new StringBuilder("--- High Scores ---\n\n");
        if (highScores.isEmpty()) {
            sb.append("No scores registered yet.");
        } else {
            for (int i = 0; i < Math.min(highScores.size(), MAX_HIGH_SCORES_TO_SHOW); i++) {
                ScoreEntry entry = highScores.get(i);
                sb.append(String.format("%d. %s: %d\n", i + 1, entry.getPlayerName(), entry.getScore()));
            }
        }
        getDialogService().showMessageBox(sb.toString());
    }

    /**
     * Initializes network connection and starts the game level.
     */
    private void startNetworkAndGame() {
        getGameWorld().removeEntities(getGameWorld().getEntities());
        spawn("background");
        setLevelFromMap("mapazo.tmx");
        spawn("eggman", 500, 420); // Initial spawn, might be replaced by server message
        
        var client = getNetService().newTCPClient(SERVER_IP, SERVER_PORT);
        client.setOnConnected(conn -> {
            conn.send(new Bundle("Hola"));
            getExecutor().startAsyncFX(() -> { onClient(); return null; });
            System.out.println("Successfully connected to server.");
        });
        client.connectAsync();

        startTimer();
    }

    /**
     * Handles client-side network logic after connecting to the server.
     */
    private void onClient() {
        // TODO: Implement client-side logic such as receiving updates from server,
        // spawning player entity, handling remote players, etc.
        System.out.println("Client-side network logic started.");
    }

    /**
     * Starts or restarts the game timer.
     */
   private void startTimer() {
    timeLeft = GAME_DURATION_SECONDS;
    gameLogic.cambiarTextoTiempo("Time: " + (int) timeLeft);

    if (gameTimerAction != null) {
        gameTimerAction.expire();
    }
    
    gameTimerAction = getGameTimer().runAtInterval(() -> {
        timeLeft--;
        gameLogic.cambiarTextoTiempo("Time: " + (int) timeLeft); // Actualiza la UI

        if (timeLeft <= 0) {
            timeLeft = 0;
            gameTimerAction.expire(); // ¡Importante! Detener el temporizador.
            showGameOver(); // Finalizar el juego.
        }
    }, Duration.seconds(1));
}

private void showGameOver() {
    // Nos aseguramos que solo se muestre una vez.
    if (gameTimerAction.isExpired()) {
        getDialogService().showMessageBox("Game Over!", () -> {
            promptForNameAndSaveScore(0);
        });
    }
}

/**
 * Prompts the user for their name and saves the score.
 */
private void promptForNameAndSaveScore(int score) {
    getDialogService().showInputBox("Enter your name:", name -> {
        if (name != null && !name.trim().isEmpty()) {
            highScores.add(new ScoreEntry(name.trim(), score));
        }
        showScores();
    });
}

}
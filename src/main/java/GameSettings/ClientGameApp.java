package GameSettings;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.audio.Music;
import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.input.virtual.VirtualButton;
import com.almasb.fxgl.multiplayer.MultiplayerService;
import com.almasb.fxgl.net.Connection;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.time.TimerAction;
import component.GameFactory;
import component.GameLogic;
import component.Personajes.KnucklesComponent;
import component.Personajes.PlayerComponent; 
import component.Personajes.SonicComponent;
import component.Personajes.TailsComponent;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.Point2D;

import static com.almasb.fxgl.dsl.FXGL.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class ClientGameApp extends GameApplication {

    private final int anchoPantalla = 1000;
    private final int altoPantalla = 600;
    private Connection<Bundle> conexion;
    private Map<String, Player> personajeRemotos = new HashMap<>();
    private Player player = null; // Inicializado como null, solo se asigna tras el spawn
    private String idPendiente = null; // Variable temporal para el ID del jugador
    private String personajePendiente = null;
    private int contadorAnillos = 0;
    private int contadorBasura = 0;
    private int contadorPapel = 0;
    private int contadorCaucho = 0;
    public GameLogic gameLogic;

    private TimerAction gameTimerAction;
    private double timeLeft = 180.0;

    private List<ScoreEntry> highScores = new ArrayList<>();

    private long clientUpdateCount = 0;

    private boolean pendingReset = false;
    private boolean isPlayerReady = false; // Nueva bandera para controlar la disponibilidad del jugador
    private final Map<String, Point2D> remoteTargetPositions = new HashMap<>();
    private String serverIP = null; // Nueva variable para la IP del servidor

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setWidth(anchoPantalla);
        gameSettings.setHeight(altoPantalla);
        gameSettings.setTitle("Jugador Sonic");
        gameSettings.addEngineService(MultiplayerService.class);
    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new GameFactory());
        getGameScene().setBackgroundColor(Color.DARKBLUE);
        Music music = getAssetLoader().loadMusic("OST2.mp3");
        getAudioPlayer().loopMusic(music);

        Platform.runLater(() -> {
            gameLogic = new GameLogic();
            gameLogic.init();
            showMainMenu();
        });
    }

    /**
     * Muestra el menú principal del juego con opciones para Jugar, Ayuda, Acerca De y Puntuaciones.
     */
    private void showMainMenu() {
        Stage stage = new Stage();

        // Cargar la imagen de fondo desde los recursos
        Image fondoImg = new Image(getClass().getResourceAsStream("/assets/textures/background/fondo_menu.png"));
        ImageView fondoView = new ImageView(fondoImg);
        fondoView.setFitWidth(350); // Ajusta según el tamaño del menú
        fondoView.setFitHeight(350);

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        Text title = new Text("Menú Principal");
        title.setFont(javafx.scene.text.Font.font("Impact", 36));
        title.setFill(Color.WHITE);
        title.setStyle("-fx-effect: dropshadow(gaussian, black, 4, 0.7, 2, 2);");

        Button btnPlay = new Button("Jugar");
        Button btnHelp = new Button("Ayuda");
        Button btnAbout = new Button("Acerca De");
        Button btnScores = new Button("Puntuaciones");

        // Estilos para los botones
        String baseButtonStyle = "-fx-font-family: 'Impact'; -fx-font-size: 22px; -fx-padding: 10px 20px; -fx-text-fill: white; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 1, 1);";

        String playButtonStyle = baseButtonStyle + "-fx-background-color: #0000FF;"; // Azul
        String playButtonHoverStyle = "-fx-background-color: #0000CC;";
        btnPlay.setTextFill(Color.WHITE);

        String helpButtonStyle = baseButtonStyle + "-fx-background-color: #f75f07ff;"; // Naranja
        String helpButtonHoverStyle = "-fx-background-color: #f75f07ff;";
        btnHelp.setTextFill(Color.BLACK);

        String aboutButtonStyle = baseButtonStyle + "-fx-background-color: #FF0000;"; // Rojo
        String aboutButtonHoverStyle = "-fx-background-color: #CC0000;";

        String scoresButtonStyle = baseButtonStyle + "-fx-background-color: #800080;"; // Púrpura
        String scoresButtonHoverStyle = "-fx-background-color: #6A0DAD;";
        btnScores.setTextFill(Color.WHITE);

        btnPlay.setStyle(playButtonStyle);
        btnHelp.setStyle(helpButtonStyle);
        btnAbout.setStyle(aboutButtonStyle);
        btnScores.setStyle(scoresButtonStyle);

        btnPlay.setOnMouseEntered(e -> btnPlay.setStyle(playButtonHoverStyle));
        btnPlay.setOnMouseExited(e -> btnPlay.setStyle(playButtonStyle));

        btnHelp.setOnMouseEntered(e -> btnHelp.setStyle(helpButtonHoverStyle));
        btnHelp.setOnMouseExited(e -> { btnHelp.setStyle(helpButtonStyle); btnHelp.setTextFill(Color.BLACK); });
        
        btnAbout.setOnMouseEntered(e -> btnAbout.setStyle(aboutButtonHoverStyle));
        btnAbout.setOnMouseExited(e -> btnAbout.setStyle(aboutButtonStyle));

        btnScores.setOnMouseEntered(e -> btnScores.setStyle(scoresButtonHoverStyle));
        btnScores.setOnMouseExited(e -> btnScores.setStyle(scoresButtonStyle));

        btnPlay.setOnAction(e -> {
            stage.close();
            showServerIPDialog(); // Pedir IP antes de selección de personaje
        });

        btnHelp.setOnAction(e -> {
            showHelp();
        });

        btnAbout.setOnAction(e -> {
            showAbout();
        });

        btnScores.setOnAction(e -> {
            showScores();
        });

        root.getChildren().addAll(title, btnPlay, btnHelp, btnAbout, btnScores);
        // Eliminar o comentar la línea del color de fondo plano
        // root.setStyle("-fx-background-color: #0c0c0cff;");

        // Usar StackPane para poner la imagen de fondo detrás del VBox
        StackPane stack = new StackPane();
        stack.getChildren().addAll(fondoView, root);

        Scene scene = new Scene(stack, 350, 350);
        stage.setScene(scene);
        stage.setTitle("Menú Principal de Juego");
        stage.show();
    }

    /**
     * Muestra un diálogo para introducir la IP del servidor.
     */
    private void showServerIPDialog() {
        Platform.runLater(() -> {
            Stage dialog = new Stage();
            VBox root = new VBox(20);
            root.setAlignment(Pos.CENTER);
            root.setStyle("-fx-background-color: #001f3f; -fx-padding: 30px;");
            Text title = new Text("Introduce la IP del servidor");
            title.setFont(javafx.scene.text.Font.font("Impact", 28));
            title.setFill(Color.WHITE);
            title.setStyle("-fx-effect: dropshadow(gaussian, black, 4, 0.7, 2, 2);");
            javafx.scene.control.TextField ipField = new javafx.scene.control.TextField();
            ipField.setPromptText("Ejemplo: 123.123.123.123");
            ipField.setMaxWidth(220);
            ipField.setStyle("-fx-font-size: 18px; -fx-background-radius: 8px; -fx-background-color: #003366; -fx-text-fill: white; -fx-prompt-text-fill: #b0c4de; -fx-border-color: #0055aa; -fx-border-radius: 8px; -fx-padding: 8px;");
            Button btnOK = new Button("Conectar");
            btnOK.setStyle("-fx-font-family: 'Impact'; -fx-font-size: 20px; -fx-background-color: #0000FF; -fx-text-fill: white; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 1, 1);");
            btnOK.setOnMouseEntered(ev -> btnOK.setStyle("-fx-font-family: 'Impact'; -fx-font-size: 20px; -fx-background-color: #003366; -fx-text-fill: white; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 1, 1);"));
            btnOK.setOnMouseExited(ev -> btnOK.setStyle("-fx-font-family: 'Impact'; -fx-font-size: 20px; -fx-background-color: #0000FF; -fx-text-fill: white; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 1, 1);"));
            btnOK.setOnAction(ev -> {
                String ip = ipField.getText();
                if (ip == null || ip.trim().isEmpty()) {
                    getDialogService().showMessageBox("Por favor, introduce una IP válida.");
                    return;
                }
                serverIP = ip.trim();
                dialog.close();
                showCharacterSelectionMenu();
            });
            root.getChildren().addAll(title, ipField, btnOK);
            Scene scene = new Scene(root, 400, 220);
            dialog.setScene(scene);
            dialog.setTitle("Conexión al servidor");
            dialog.show();
        });
    }

    /**
     * Muestra el menú de selección de personaje.
     */
    private void showCharacterSelectionMenu() {
        Platform.runLater(() -> { // Asegura que se ejecuta en el hilo FX
            Stage stage = new Stage();

            // Cargar la imagen de fondo desde los recursos
            Image fondoImg = new Image(getClass().getResourceAsStream("/assets/textures/background/fondo_menu.png"));
            ImageView fondoView = new ImageView(fondoImg);
            fondoView.setFitWidth(350); // Ajusta según el tamaño del menú
            fondoView.setFitHeight(250);

            VBox root = new VBox(15);
            root.setAlignment(Pos.CENTER);
            Text title = new Text("Selecciona tu personaje");
            title.setFont(javafx.scene.text.Font.font("Impact", 32));
            title.setFill(Color.WHITE);
            title.setStyle("-fx-effect: dropshadow(gaussian, black, 4, 0.7, 2, 2);");

            Button btnSonic = new Button("Sonic");
            Button btnTails = new Button("Tails");
            Button btnKnuckles = new Button("Knuckles");

            String baseCharButtonStyle = "-fx-font-family: 'Impact'; -fx-font-size: 20px; -fx-padding: 10px 20px; -fx-text-fill: white; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 1, 1);";

            String sonicButtonStyle = baseCharButtonStyle + "-fx-background-color: #0000FF;";
            String sonicButtonHoverStyle = "-fx-background-color: #0000CC;";
            btnSonic.setTextFill(Color.WHITE);

            String tailsButtonStyle = baseCharButtonStyle + "-fx-background-color: #f75f07ff;";
            String tailsButtonHoverStyle = "-fx-background-color: #f75f07ff;";
            btnTails.setTextFill(Color.BLACK);

            String knucklesButtonStyle = baseCharButtonStyle + "-fx-background-color: #FF0000;";
            String knucklesButtonHoverStyle = "-fx-background-color: #CC0000;";

            btnSonic.setStyle(sonicButtonStyle);
            btnTails.setStyle(tailsButtonStyle);
            btnKnuckles.setStyle(knucklesButtonStyle);

            btnSonic.setOnMouseEntered(e -> btnSonic.setStyle(sonicButtonHoverStyle));
            btnSonic.setOnMouseExited(e -> btnSonic.setStyle(sonicButtonStyle));
            
            btnTails.setOnMouseEntered(e -> btnTails.setStyle(tailsButtonHoverStyle));
            btnTails.setOnMouseExited(e -> { btnTails.setStyle(tailsButtonStyle); btnTails.setTextFill(Color.BLACK); });
            
            btnKnuckles.setOnMouseEntered(e -> btnKnuckles.setStyle(knucklesButtonHoverStyle));
            btnKnuckles.setOnMouseExited(e -> btnKnuckles.setStyle(knucklesButtonStyle));

            btnSonic.setOnAction(e -> {
                personajePendiente = "sonic";
                stage.close();
                startNetworkAndGame();
            });
            btnTails.setOnAction(e -> {
                personajePendiente = "tails";
                stage.close();
                startNetworkAndGame();
            });
            btnKnuckles.setOnAction(e -> {
                personajePendiente = "knuckles";
                stage.close();
                startNetworkAndGame();
            });

            root.getChildren().addAll(title, btnSonic, btnTails, btnKnuckles);
            // Eliminar o comentar la línea del color de fondo plano
            // root.setStyle("-fx-background-color: #080808ff;");

            // Usar StackPane para poner la imagen de fondo detrás del VBox
            StackPane stack = new StackPane();
            stack.getChildren().addAll(fondoView, root);

            Scene scene = new Scene(stack, 350, 250);
            stage.setScene(scene);
            stage.setTitle("Selecciona personaje");
            stage.show();
        });
    }

    /**
     * Muestra un diálogo de ayuda con las instrucciones y reglas del juego.
     */
    private void showHelp() {
        String helpText = "¡Bienvenido a Sonic Adventure FXGL!\n\n" +
                          "   Cómo jugar:\n" +
                          " - Usa las teclas A y D para mover a tu personaje a la izquierda y derecha.\n" +
                          " - Usa la tecla W para saltar.\n" +
                          " - Recoge anillos para aumentar tu puntuación y protegerte de los enemigos.\n" +
                          " - Recoge basura (papel, caucho, basura general) para limpiar el entorno.\n" +
                          " - Salta sobre los robots enemigos para eliminarlos.\n" +
                          " - Ten cuidado con Eggman, ¡es el jefe final!\n\n" +
                          "   Puntuaciones:\n" +
                          " - Tu puntuación se calcula sumando la cantidad de basura recolectada (papel, caucho, basura) \n" +
                          "   más los anillos que tengas al final de la partida.\n" +
                          " - Si la partida termina en 'Game Over', tu puntuación registrada será 0.\n" +
                          " - Puedes ver las puntuaciones más altas desde el menú principal.\n\n" +
                          "   Reglas:\n" +
                          " - Pierdes anillos al ser golpeado por un enemigo. Si no tienes anillos, pierdes una vida.\n" +
                          " - Si pierdes todas tus vidas, es Game Over.\n" +
                          " - Si caes del mapa, es Game Over.\n" + 
                          " - Elimina a Eggman para ganar el juego.";

        Stage dialog = new Stage();
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setSpacing(20);
        root.setStyle("-fx-background-color: #001f3f; -fx-padding: 30px;");
        Text text = new Text(helpText);
        text.setFill(Color.WHITE);
        text.setFont(javafx.scene.text.Font.font("Impact", 16));
        text.setStyle("-fx-effect: dropshadow(gaussian, black, 2, 0.7, 1, 1);");
        text.setWrappingWidth(500);
        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setStyle("-fx-font-family: 'Impact'; -fx-font-size: 18px; -fx-background-color: #003366; -fx-text-fill: white; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        btnCerrar.setOnAction(e -> dialog.close());
        root.getChildren().addAll(text, btnCerrar);
        Scene scene = new Scene(root, 550, 500);
        dialog.setScene(scene);
        dialog.setTitle("Ayuda");
        dialog.show();
    }

    /**
     * Muestra un diálogo con información sobre el juego, desarrolladores y versión.
     */
    private void showAbout() {
        String aboutText = "Acerca de Sonic Adventure FXGL\n\n" +
                           "Lenguaje de Programación: JAVA\n" +
                           "Librerías Externas Utilizadas:\n" +
                           "- FXGL (Framework de Juegos)\n" +
                           "- JavaFX (Para la interfaz de usuario)\n" +
                           "- Otras librerías internas de FXGL para audio, física, red, etc.\n\n" +
                           "Desarrolladores:\n" +
                           "- Millan, Villalba, Acosta, Rodriguez \n" +
                           "Versión Actual: 1.0.1";

        Stage dialog = new Stage();
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setSpacing(20);
        root.setStyle("-fx-background-color: #001f3f; -fx-padding: 30px;");
        Text text = new Text(aboutText);
        text.setFill(Color.WHITE);
        text.setFont(javafx.scene.text.Font.font("Impact", 18));
        text.setStyle("-fx-effect: dropshadow(gaussian, black, 2, 0.7, 1, 1);");
        text.setWrappingWidth(500);
        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setStyle("-fx-font-family: 'Impact'; -fx-font-size: 18px; -fx-background-color: #003366; -fx-text-fill: white; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        btnCerrar.setOnAction(e -> dialog.close());
        root.getChildren().addAll(text, btnCerrar);
        Scene scene = new Scene(root, 550, 400);
        dialog.setScene(scene);
        dialog.setTitle("Acerca de");
        dialog.show();
    }

    /**
     * Muestra las puntuaciones más altas registradas.
     */
    private void showScores() {
        // Ordenar las puntuaciones en orden descendente
        Collections.sort(highScores, Collections.reverseOrder());

        StringBuilder sb = new StringBuilder("--- Puntuaciones Altas ---\n\n");
        if (highScores.isEmpty()) {
            sb.append("Aún no hay puntuaciones registradas.");
        } else {
            // Mostrar solo las 10 mejores puntuaciones (o menos si hay menos de 10)
            for (int i = 0; i < Math.min(highScores.size(), 10); i++) {
                ScoreEntry entry = highScores.get(i);
                sb.append(String.format("%d. %s: %d\n", i + 1, entry.getPlayerName(), entry.getScore()));
            }
        }
        Stage dialog = new Stage();
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setSpacing(20);
        root.setStyle("-fx-background-color: #001f3f; -fx-padding: 30px;");
        Text text = new Text(sb.toString());
        text.setFill(Color.WHITE);
        text.setFont(javafx.scene.text.Font.font("Impact", 18));
        text.setStyle("-fx-effect: dropshadow(gaussian, black, 2, 0.7, 1, 1);");
        text.setWrappingWidth(500);
        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setStyle("-fx-font-family: 'Impact'; -fx-font-size: 18px; -fx-background-color: #003366; -fx-text-fill: white; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        btnCerrar.setOnAction(e -> dialog.close());
        root.getChildren().addAll(text, btnCerrar);
        Scene scene = new Scene(root, 550, 400);
        dialog.setScene(scene);
        dialog.setTitle("Puntuaciones");
        dialog.show();
    }

    private void startNetworkAndGame() {
        // Limpiar todas las entidades del mundo del juego para un nuevo inicio
        List<Entity> allEntities = new ArrayList<>(getGameWorld().getEntities());
        for (Entity entity : allEntities) {
            entity.removeFromWorld();
        }

        spawn("fondo");
        setLevelFromMap("mapazo.tmx");

        if (serverIP == null) {
            getDialogService().showMessageBox("Error: No se ha especificado la IP del servidor.");
            return;
        }
        var client = getNetService().newTCPClient(serverIP, 50000);
        client.setOnConnected(conn -> {
            conexion = conn;
            Bundle hola = new Bundle("Hola");
            conn.send(hola);
            Platform.runLater(() -> onClient()); // Asegura que se ejecuta en el hilo FX
        });
        // Eliminados setOnDisconnected y setOnConnectionFailed por incompatibilidad con FXGL
        client.connectAsync();

        // Inicializar y empezar el temporizador del juego
        timeLeft = 180.0; // Reiniciar el tiempo
        gameLogic.cambiarTextoTiempo("Tiempo: " + (int)timeLeft); // Mostrar el tiempo inicial

        // Detener cualquier temporizador anterior antes de iniciar uno nuevo
        if (gameTimerAction != null) {
            gameTimerAction.expire(); 
        }
        // Usamos runAtInterval para el contador principal de tiempo, asumiendo que es estable
        gameTimerAction = getGameTimer().runAtInterval(() -> {
            timeLeft -= 1;
            if (timeLeft <= 0) {
                timeLeft = 0;
                gameTimerAction.expire(); // Detener el temporizador
                showGameOver(); // Llamar a Game Over
            }
            gameLogic.cambiarTextoTiempo("Tiempo: " + (int)timeLeft);
        }, Duration.seconds(1)); // Actualizar cada segundo
    }

    private void onClient() {
        conexion.addMessageHandlerFX((conexion, bundle) -> {
            switch (bundle.getName()) {
                case "TuID": {
                    idPendiente = bundle.get("id");
                    Bundle solicitar = new Bundle("SolicitarCrearPersonaje");
                    solicitar.put("id", idPendiente);
                    solicitar.put("tipo", personajePendiente);
                    solicitar.put("x", 50); // Initial X position
                    solicitar.put("y", 150); // Initial Y position
                    conexion.send(solicitar);

                    // Send initial position to synchronize server
                    Bundle sync = new Bundle("SyncPos");
                    sync.put("id", idPendiente);
                    sync.put("x", 50);
                    sync.put("y", 150);
                    conexion.send(sync);
                    break;
                }

                case "ServidorLleno": {
                    getDialogService().showMessageBox("El servidor está lleno. Máximo 3 jugadores.", () -> {
                        Platform.runLater(this::performResetAndReturnToMenu);
                    });
                    break;
                }

                case "AnilloRecogido": {
                    String ringId = bundle.get("ringId");

                    getGameWorld().getEntitiesByType(GameFactory.EntityType.RING).stream()
                        .filter(r -> ringId.equals(r.getProperties().getString("id")))
                        .findFirst()
                        .ifPresent(Entity::removeFromWorld);

                    // Update counter if the player is oneself
                    String playerId = bundle.get("playerId");
                    if (player != null && playerId.equals(player.getId())) {
                        contadorAnillos++;
                        gameLogic.cambiarTextoAnillos("anillos: " + contadorAnillos);
                    }
                    break;
                }

                                case "Interactuar": { // <- NUEVO CASO
                    String id = bundle.get("id");
                    if (!player.getId().equals(id)) { // No animar al jugador que envió el mensaje
                        Player remotePlayer = personajeRemotos.get(id);
                        if (remotePlayer != null) {
                            remotePlayer.interactuar(); // Ejecuta la animación de interacción en el jugador remoto
                        }
                    }
                    break;
                }

                case "EggmanEliminado": {
                    showGameWon();
                    break;
                }

                case "RobotEliminado": {
                    String robotId = bundle.get("robotId");

                    getGameWorld().getEntitiesByType(GameFactory.EntityType.ROBOT_ENEMIGO).stream()
                        .filter(r -> robotId.equals(r.getProperties().getString("id")))
                        .findFirst()
                        .ifPresent(Entity::removeFromWorld);
                    break;
                }

                case "BasuraRecogida": {
                    String trashId = bundle.get("trashId");
                    List<Entity> basuras = new ArrayList<>();
                    basuras.addAll(getGameWorld().getEntitiesByType(GameFactory.EntityType.BASURA));
                    basuras.addAll(getGameWorld().getEntitiesByType(GameFactory.EntityType.PAPEL));
                    basuras.addAll(getGameWorld().getEntitiesByType(GameFactory.EntityType.CAUCHO));

                    basuras.stream()
                        .filter(r -> trashId.equals(r.getProperties().getString("id")))
                        .findFirst()
                        .ifPresent(entity -> {
                            String tipoBasura = entity.getProperties().getString("tipo");
                            entity.removeFromWorld();

                            if (player != null && bundle.get("playerId").equals(player.getId())) {
                                switch (tipoBasura) {
                                    case "papel":
                                        contadorPapel++;
                                        gameLogic.cambiarTextoPapel("Papel: " + contadorPapel);
                                        break;
                                    case "caucho":
                                        contadorCaucho++;
                                        gameLogic.cambiarTextoCaucho("Caucho: " + contadorCaucho);
                                        break;
                                    case "basura":
                                        contadorBasura++;
                                        gameLogic.cambiarTextoBasura("Basura: " + contadorBasura);
                                        break;
                                }
                            }
                        });
                    break;
                }

                case "CrearRobotEnemigo": {
                    double x = ((Number) bundle.get("x")).doubleValue();
                    double y = ((Number) bundle.get("y")).doubleValue();
                    String id = bundle.get("id");
                    Entity robot = spawn("robotEnemigo", x, y);
                    robot.getProperties().setValue("id", id); // Save id to identify it later
                    break;
                }

                case "CrearEggman": {
                    double x = ((Number) bundle.get("x")).doubleValue();
                    double y = ((Number) bundle.get("y")).doubleValue();
                    String id = bundle.get("id");
                    Entity eggman = spawn("eggman", x, y);
                    eggman.getProperties().setValue("id", id); // Save id to identify it later

                    break;
                }

                case "crearRing": {
                    double x = ((Number) bundle.get("x")).doubleValue();
                    double y = ((Number) bundle.get("y")).doubleValue();
                    String id = bundle.get("id");
                    Entity ring = spawn("ring", x, y);
                    ring.getProperties().setValue("id", id); // Save id to identify it later
                    break;
                }

                case "crearbasura": {
                    double x = ((Number) bundle.get("x")).doubleValue();
                    double y = ((Number) bundle.get("y")).doubleValue();
                    String id = bundle.get("id");
                    String tipo = bundle.get("tipo");  

                    Entity basura = spawn(tipo, x, y);
                    basura.getProperties().setValue("id", id);
                    basura.getProperties().setValue("tipo", tipo);

                    break;
                }

                case "Crear Personaje": {
                    String id = bundle.get("id");
                    String tipo = bundle.get("tipo");
                    double x = ((Number)bundle.get("x")).doubleValue();
                    double y = ((Number)bundle.get("y")).doubleValue();

                    if (id.equals(idPendiente)) {
                        if (!isPlayerReady) { // Solo si el jugador local no ha sido configurado
                            Entity entidad = spawn(tipo, x, y); // La entidad se crea y sus componentes se añaden
                            player = (Player) entidad;
                            if (idPendiente != null) {
                                player.setId(idPendiente);
                            }
                            // Buscar PlayerComponent por instanceof
                            PlayerComponent pComp = null;
                            for (var comp : player.getComponents()) {
                                if (comp instanceof PlayerComponent) {
                                    pComp = (PlayerComponent) comp;
                                    break;
                                }
                            }
                            if (pComp != null) {
                                player.setPlayerComponent(pComp);
                            } else {
                                System.err.println("ERROR: PlayerComponent no encontrado después de spawnear la entidad para el jugador local. Esto es crítico.");
                            }
                            getGameScene().getViewport().bindToEntity(player, anchoPantalla/2.0, altoPantalla/1.5);
                            getGameScene().getViewport().setLazy(true);
                            isPlayerReady = true; // El jugador local está listo
                        } else {
                            player.setX(x);
                            player.setY(y);
                        }
                    } else {
                        Player remotePlayer = personajeRemotos.get(id);
                        if (remotePlayer == null) {
                            Entity entidad = spawn(tipo, x, y);
                            remotePlayer = (Player) entidad;
                            personajeRemotos.put(id, remotePlayer);
                            // Buscar y asignar PlayerComponent para el jugador remoto
                            PlayerComponent pComp = null;
                            for (var comp : remotePlayer.getComponents()) {
                                if (comp instanceof PlayerComponent) {
                                    pComp = (PlayerComponent) comp;
                                    break;
                                }
                            }
                            if (pComp != null) {
                                remotePlayer.setPlayerComponent(pComp);
                            } else {
                                System.err.println("ERROR: PlayerComponent no encontrado después de spawnear la entidad para el jugador remoto. Esto es crítico.");
                            }
                        } else {
                            remotePlayer.setX(x);
                            remotePlayer.setY(y);
                        }
                    }
                    break;
                }

                case "SyncPos": {
                    String syncId = bundle.get("id");
                    if (player != null && syncId.equals(player.getId())) {
                        return; // Ignorar la posición propia
                    }

                    Player remotePlayer = personajeRemotos.get(syncId);
                    if (remotePlayer != null) {
                        Number xNum = bundle.get("x");
                        Number yNum = bundle.get("y");
                        // Actualizar la posición objetivo para interpolación suave
                        remoteTargetPositions.put(syncId, new Point2D(xNum.doubleValue(), yNum.doubleValue()));
                    }
                    break;
                }
                case "SyncRobotPos": {
                    var robots = getGameWorld().getEntitiesByType(GameFactory.EntityType.ROBOT_ENEMIGO);
                    if (!robots.isEmpty()) {
                        Entity robot = robots.get(0);
                        double x = ((Number) bundle.get("x")).doubleValue();
                        double y = ((Number) bundle.get("y")).doubleValue();
                        robot.setPosition(x, y);
                    }
                    break;
                }

                case "SyncEggmanPos": {
                    var eggmans = getGameWorld().getEntitiesByType(GameFactory.EntityType.EGGMAN);
                    if (!eggmans.isEmpty()) {
                        Entity eggman = eggmans.get(0);
                        double x = ((Number) bundle.get("x")).doubleValue();
                        double y = ((Number) bundle.get("y")).doubleValue();
                        eggman.setPosition(x, y);
                    }
                    break;
                }

                case "Mover a la izquierda":
                case "Mover a la derecha":
                case "Saltar":
                case "Detente": {
                    String moveId = bundle.get("id");
                    if (player != null && moveId.equals(player.getId())) {
                        return; // Ignore your own messages
                    }
                    Player remotePlayer = personajeRemotos.get(moveId);
                    if (remotePlayer == null) {
                        return;
                    }
                    switch (bundle.getName()) {
                        case "Mover a la izquierda":
                            remotePlayer.moverIzquierda();
                            break;
                        case "Mover a la derecha":
                            remotePlayer.moverDerecha();
                            break;
                        case "Saltar":
                            remotePlayer.saltar();
                            break;
                        case "Detente":
                            remotePlayer.detener();
                            break;
                    }
                    break;
                }

            }
        });
    }
            
    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("Mover a la izquierda") {
            @Override
            protected void onAction() {
                if (!isPlayerReady || player == null) {
                    return;
                }
                Bundle bundle = new Bundle("Mover a la izquierda");
                bundle.put("id", player.getId());
                conexion.send(bundle);
                player.moverIzquierda();
            }
            @Override
            protected void onActionEnd() {
                if (!isPlayerReady || player == null) {
                    return;
                }
                Bundle bundle = new Bundle("Detente");
                bundle.put("id", player.getId());
                conexion.send(bundle);
                player.detener();
            }
        }, KeyCode.A, VirtualButton.LEFT);

        getInput().addAction(new UserAction("Mover a la derecha") {
            @Override
            protected void onAction() {
                if (!isPlayerReady || player == null) {
                    return;
                }
                Bundle bundle = new Bundle("Mover a la derecha");
                bundle.put("id", player.getId());
                conexion.send(bundle);
                player.moverDerecha();
            }
            @Override
            protected void onActionEnd() {
                if (!isPlayerReady || player == null) {
                    return;
                }
                Bundle bundle = new Bundle("Detente");
                bundle.put("id", player.getId());
                conexion.send(bundle);
                player.detener();
            }
        }, KeyCode.D, VirtualButton.RIGHT);

        getInput().addAction(new UserAction("Saltar") {
            @Override
            protected void onActionBegin() {
                if (!isPlayerReady || player == null) {
                    return;
                }
                Bundle bundle = new Bundle("Saltar");
                bundle.put("id", player.getId());
                conexion.send(bundle);
                player.saltar();
            }
        }, KeyCode.W, VirtualButton.A);

        getInput().addAction(new UserAction("Interactuar") {
            @Override
            protected void onActionBegin() {
                if (!isPlayerReady || player == null) {
                    return;
                }

                // Llama al método de interacción del jugador para activar la animación localmente.
                player.interactuar();

                // Envía un mensaje al servidor para que otros clientes vean la animación.
                Bundle bundle = new Bundle("Interactuar");
                bundle.put("id", player.getId());
                conexion.send(bundle);

                // Lógica específica del personaje
                String playerType = player.getTipo();

                if (playerType.equals("knuckles")) {
                    // Solo puede golpear si no está en el aire.
                    if (!player.getComponent(PhysicsComponent.class).isMovingY()) {
                        // Define el área de golpe en frente de Knuckles.
                        double punchWidth = 45;
                        double punchHeight = 40;
                        double punchX = player.getScaleX() > 0 
                            ? player.getRightX() 
                            : player.getX() - punchWidth;
                        double punchY = player.getY();

                        // Busca enemigos en el área de golpe. (Rectangle2D.Double es una implementación de Rectangle2D)
                        getGameWorld().getEntitiesInRange(new javafx.geometry.Rectangle2D(punchX, punchY, punchWidth, punchHeight)).stream()
                            .filter(e -> e.isType(GameFactory.EntityType.ROBOT_ENEMIGO))
                            .findFirst() // Golpea solo al primer enemigo encontrado.
                            .ifPresent(enemy -> {
                                String robotId = enemy.getProperties().getString("id");
                                Bundle damageBundle = new Bundle("DamageRobot");
                                damageBundle.put("robotId", robotId);
                                conexion.send(damageBundle); // Envía el mensaje de daño al servidor.
                            });
                    }
                } else if (playerType.equals("sonic")) {
                    SonicComponent sonicComp = player.getComponent(SonicComponent.class);
                    // Intenta transformar a Sonic. El método devuelve true si la transformación es exitosa.
                    if (sonicComp != null && sonicComp.transform()) {
                        gameLogic.activarInvencibilidad(6000, player); // Activa 6 segundos de invencibilidad.
                        
                        // Programa la reversión al estado normal después de 6 segundos.
                        getGameTimer().runOnceAfter(sonicComp::revert, Duration.seconds(6));
                    }
                }
            }
        }, KeyCode.E);

        getInput().addAction(new UserAction("Desactivar filtro") {
            @Override
            protected void onActionBegin() {
                if (gameLogic != null) {
                    gameLogic.filtroColor(0);
                }
            }
        }, KeyCode.L);
    }

   @Override
    protected void initPhysics() {

        onCollisionBegin(GameFactory.EntityType.PLAYER, GameFactory.EntityType.RING, (jugador, ring) -> {
            play("recoger.wav");
            String ringId = ring.getProperties().getString("id");
            Bundle recoger = new Bundle("RecogerAnillo");
            recoger.put("ringId", ringId);
            recoger.put("playerId", player.getId());
            conexion.send(recoger);
        });

        onCollisionBegin(GameFactory.EntityType.PLAYER, GameFactory.EntityType.BASURA, (jugador, basura) -> {
            recogerBasura((Player)jugador, basura);
        });

        onCollisionBegin(GameFactory.EntityType.PLAYER, GameFactory.EntityType.PAPEL, (jugador, papel) -> {
            if (player.hasComponent(TailsComponent.class)|| player.hasComponent(SonicComponent.class)) {
                recogerBasura((Player)jugador, papel);
            }
        });

        onCollisionBegin(GameFactory.EntityType.PLAYER, GameFactory.EntityType.CAUCHO, (jugador, caucho) -> {
            if (jugador.hasComponent(KnucklesComponent.class)|| player.hasComponent(SonicComponent.class)) {
               recogerBasura((Player)jugador, caucho);
            }
        });

       onCollisionBegin(GameFactory.EntityType.PLAYER, GameFactory.EntityType.ROBOT_ENEMIGO, (entidad, robot) -> {
           double alturaPlayer = entidad.getHeight();
           double bottomPlayer = entidad.getY() + alturaPlayer;
           double topRobot = robot.getY();

           boolean golpeDesdeArriba = bottomPlayer <= topRobot + 10;

           if (golpeDesdeArriba) {
               String robotId = robot.getProperties().getString("id");
               Bundle eliminar = new Bundle("EliminarRobot");
               eliminar.put("robotId", robotId);
               eliminar.put("playerId", player.getId());
               conexion.send(eliminar);
               entidad.getComponent(PhysicsComponent.class).setVelocityY(-300);
           } else {
               perderVidas();
           }
       });

        onCollisionBegin(GameFactory.EntityType.PLAYER, GameFactory.EntityType.EGGMAN, (entidad, eggman) -> {
            double alturaPlayer = entidad.getHeight();
            double bottomPlayer = entidad.getY() + alturaPlayer;
            double topEggman = eggman.getY();

            boolean golpeDesdeArriba = bottomPlayer <= topEggman + 10;

            if (golpeDesdeArriba) {
                String eggmanId = eggman.getProperties().getString("id");
                Bundle dañoEggman = new Bundle("DañoEggman");
                dañoEggman.put("eggmanId", eggmanId);
                dañoEggman.put("playerId", player.getId());
                conexion.send(dañoEggman);

                player.getComponent(PhysicsComponent.class).setVelocityY(-300);
            } else {
                perderVidas();
            }
        });
    }

    private void recogerBasura(Player entidad, Entity basuraEntidad) {
        String trashId = basuraEntidad.getProperties().getString("id");

        String tipo = entidad.getTipo();

        Bundle recoger = new Bundle("RecogerBasura");
        recoger.put("trashId", trashId);
        recoger.put("playerId", player.getId());
        recoger.put("tipo", tipo);
        conexion.send(recoger);
    }

    private void perderVidas() {
        if (player.isInvencible()){
            return;
        }
        
        if (contadorAnillos > 0) {
            play("perder_anillos.wav");
            contadorAnillos = 0;
            gameLogic.cambiarTextoAnillos("Anillos: " + contadorAnillos);
            gameLogic.activarInvencibilidad(3000, player); // Esto usará el TimerAction interno de GameLogic
            return; 
        }

        play("muerte.wav");
        player.restarVida();
        gameLogic.cambiarTextoVidas("Vidas: " + player.getVidas());
        if (player.estaMuerto()) {
            showGameOver();
        } else {
            gameLogic.activarInvencibilidad(3000, player); // Esto usará el TimerAction interno de GameLogic
        }
    }

    /**
     * Muestra el mensaje de Game Over y solicita el nombre del jugador para la puntuación.
     */
    private void showGameOver() {
        if (gameTimerAction != null) {
            gameTimerAction.expire();
            gameTimerAction = null; 
        }
        getDialogService().showMessageBox("¡Game Over!", () -> {
            promptForNameAndSaveScore(0);
        });
    }

    /**
     * Muestra el mensaje de Juego Ganado y solicita el nombre del jugador para la puntuación.
     */
    private void showGameWon() {
        if (gameTimerAction != null) {
            gameTimerAction.expire();
            gameTimerAction = null; 
        }
        getDialogService().showMessageBox("¡Ganaste el Juego, Felicidades!", () -> {
            int finalScore = contadorAnillos + contadorBasura + contadorPapel + contadorCaucho;
            promptForNameAndSaveScore(finalScore);
        });
    }

    /**
     * Solicita al usuario un nombre y guarda la puntuación.
     * Si el nombre es vacío o nulo, vuelve a solicitarlo.
     * @param score La puntuación a guardar.
     */
    private void promptForNameAndSaveScore(int score) {
        getDialogService().showInputBox("Ingresa tu nombre para el registro de puntuación:", name -> {
            String playerName = name;
            if (playerName == null || playerName.trim().isEmpty()) {
                getDialogService().showMessageBox("El nombre no puede estar vacío. Por favor, intenta de nuevo.", () -> {
                    promptForNameAndSaveScore(score);
                });
                return; 
            }
            highScores.add(new ScoreEntry(playerName.trim(), score));
            
            // Establecer bandera para reiniciar en el próximo onUpdate
            pendingReset = true;
        });
    }

    /**
     * Reinicia el estado del juego y vuelve al menú principal.
     * Ahora se activa mediante una bandera y se ejecuta en onUpdate.
     */
    private void performResetAndReturnToMenu() {
        // Limpiar todas las entidades del mundo del juego
        List<Entity> allEntities = new ArrayList<>(getGameWorld().getEntities());
        for (Entity entity : allEntities) {
            entity.removeFromWorld();
        }

        // Reiniciar contadores
        contadorAnillos = 0;
        contadorBasura = 0;
        contadorPapel = 0;
        contadorCaucho = 0;
        // Reiniciar el objeto player a una nueva instancia
        player = null;
        isPlayerReady = false; // El jugador ya no está listo hasta que se vuelva a crear

        // Detener el temporizador del juego si está activo
        if (gameTimerAction != null) {
            gameTimerAction.expire();
            gameTimerAction = null;
        }
        timeLeft = 180.0; // Reiniciar el tiempo para la próxima partida

        // Reiniciar la lógica del juego (UI, etc.)
        if (gameLogic != null) {
            gameLogic.reset();
        }

        // Volver al menú principal
        showMainMenu();

        // Resetear la bandera
        pendingReset = false;
    }

    @Override
    protected void onUpdate(double tpf) {
        // Si hay un reinicio pendiente, ejecutarlo en el hilo FX
        if (pendingReset) {
            Platform.runLater(this::performResetAndReturnToMenu);
            return; // Salir para evitar más lógica hasta el reinicio
        }

        // Incrementa el contador de actualizaciones del cliente
        clientUpdateCount++;

        // Sincronizar posición del jugador con el servidor más frecuentemente
        if (conexion != null && player != null && isPlayerReady) { // Solo sincronizar si el jugador está listo
            if (clientUpdateCount % 3 == 0) { // Sincronizar cada 3 ticks (aprox. 20 veces/seg a 60 FPS)
                Bundle bundle = new Bundle("SyncPos");
                bundle.put("id", player.getId());
                bundle.put("x", player.getX());
                bundle.put("y", player.getY());
                conexion.send(bundle);
            }

            // GAME OVER CONDITION BY HEIGHT
            if (player.getY() > 1000) { // Si el jugador cae por debajo de Y=1000 (fuera de pantalla)
                showGameOver();
            }
        }

        // Interpolación de personajes remotos
        for (Entity e : getGameWorld().getEntitiesByType(GameFactory.EntityType.PLAYER)) {
            PlayerComponent pc = e.getComponentOptional(PlayerComponent.class).orElse(null);
            if (pc != null && player != null) {
                Player ePlayer = (Player) e;
                if (!ePlayer.getId().equals(player.getId())) {
                    String id = ePlayer.getId();
                    Point2D target = remoteTargetPositions.get(id);
                    if (target != null) {
                        double lerp = 0.15; // Factor de suavizado (ajustable)
                        double newX = e.getX() + (target.getX() - e.getX()) * lerp;
                        double newY = e.getY() + (target.getY() - e.getY()) * lerp;
                        e.setPosition(newX, newY);
                    }
                }
            }
        }

        // Actualizar la lógica del juego, incluyendo la invencibilidad
        if (gameLogic != null) {
            gameLogic.onUpdate(tpf);
        }

        // Eliminar el mensaje de debug de entidades PLAYER
        // List<Entity> players = getGameWorld().getEntitiesByType(GameFactory.EntityType.PLAYER);
        // System.out.print("[DEBUG] Entidades PLAYER en este cliente: " + players.size() + " | IDs: ");
        // for (Entity ent : players) {
        //     String pid = (ent instanceof Player) ? ((Player)ent).getId() : "?";
        //     System.out.print(pid + ", ");
        // }
        // System.out.println();
    }
}

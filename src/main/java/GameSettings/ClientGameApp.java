package GameSettings;

import com.almasb.fxgl.audio.Music;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.input.virtual.VirtualButton;
import com.almasb.fxgl.multiplayer.MultiplayerService;
import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.app.GameApplication;
import static com.almasb.fxgl.dsl.FXGL.*;
import java.util.ArrayList;
import java.util.Collections; // Importar para Collections.sort
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import component.GameLogic;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.net.Connection;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import component.GameFactory;
import component.Personajes.KnucklesComponent;
import component.Personajes.SonicComponent;
import component.Personajes.TailsComponent;
import javafx.scene.text.Text;
import javafx.scene.Scene;
import javafx.util.Duration; // Importar Duration

public class ClientGameApp extends GameApplication {

    private final int anchoPantalla = 1000;
    private final int altoPantalla = 600;
    private Connection<Bundle> conexion;
    private Map<String, Player> personajeRemotos = new HashMap<>();
    private Player player = new Player();
    private String personajePendiente = null;
    private int contadorAnillos = 0;
    private int contadorBasura = 0;
    private int contadorPapel = 0;
    private int contadorCaucho = 0;
    private boolean flag_Interactuar = false;
    private Entity stand_by;
    public GameLogic gameLogic;

    // Lista para almacenar las puntuaciones altas
    private List<ScoreEntry> highScores = new ArrayList<>();

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
        showMainMenu(); // Ahora se muestra el menú principal al inicio
        getGameScene().setBackgroundColor(Color.DARKBLUE);
        Music music = getAssetLoader().loadMusic("OST.mp3");
        //getAudioPlayer().loopMusic(music);
        gameLogic = new GameLogic();
        gameLogic.init();

        // Spawn de Eggman en coordenadas visibles (ajustadas para demostración)
        // Puedes cambiar estas coordenadas si tu mapa es diferente
        spawn("eggman", 500, 420); 
        System.out.println("Eggman ha sido spawnado en X=500, Y=420.");
    }

    /**
     * Muestra el menú principal del juego con opciones para Jugar, Ayuda, Acerca De y Puntuaciones.
     */
    private void showMainMenu() {
        getExecutor().startAsyncFX(() -> {
            Stage stage = new Stage();
            VBox root = new VBox(15);
            root.setAlignment(Pos.CENTER);
            Text title = new Text("Menú Principal");
            title.setStyle("-fx-font-size: 24px; -fx-fill: white;"); // Estilo para el título

            Button btnPlay = new Button("Jugar");
            Button btnHelp = new Button("Ayuda");
            Button btnAbout = new Button("Acerca De");
            Button btnScores = new Button("Puntuaciones"); // Nuevo botón de puntuaciones

            // Estilos para los botones
            String baseButtonStyle = "-fx-font-size: 18px; -fx-padding: 10px 20px; -fx-text-fill: white; -fx-border-radius: 5px; -fx-background-radius: 5px;";

            // Estilos específicos para cada botón
            String playButtonStyle = baseButtonStyle + "-fx-background-color: #0000FF;"; // Azul
            String playButtonHoverStyle = "-fx-background-color: #0000CC;"; // Azul más oscuro

            String helpButtonStyle = baseButtonStyle + "-fx-background-color: #FFFF00;"; // Amarillo
            String helpButtonHoverStyle = "-fx-background-color: #CCCC00;"; // Amarillo más oscuro
            btnHelp.setTextFill(Color.BLACK); // Texto negro para legibilidad en amarillo

            String aboutButtonStyle = baseButtonStyle + "-fx-background-color: #FF0000;"; // Rojo
            String aboutButtonHoverStyle = "-fx-background-color: #CC0000;"; // Rojo más oscuro

            String scoresButtonStyle = baseButtonStyle + "-fx-background-color: #800080;"; // Púrpura (ejemplo)
            String scoresButtonHoverStyle = "-fx-background-color: #6A0DAD;"; // Púrpura más oscuro

            btnPlay.setStyle(playButtonStyle);
            btnHelp.setStyle(helpButtonStyle);
            btnAbout.setStyle(aboutButtonStyle);
            btnScores.setStyle(scoresButtonStyle); // Aplicar estilo al nuevo botón

            btnPlay.setOnMouseEntered(e -> btnPlay.setStyle(playButtonHoverStyle));
            btnPlay.setOnMouseExited(e -> btnPlay.setStyle(playButtonStyle));

            btnHelp.setOnMouseEntered(e -> btnHelp.setStyle(helpButtonHoverStyle));
            btnHelp.setOnMouseExited(e -> { btnHelp.setStyle(helpButtonStyle); btnHelp.setTextFill(Color.BLACK); });
            
            btnAbout.setOnMouseEntered(e -> btnAbout.setStyle(aboutButtonHoverStyle));
            btnAbout.setOnMouseExited(e -> btnAbout.setStyle(aboutButtonStyle));

            btnScores.setOnMouseEntered(e -> btnScores.setStyle(scoresButtonHoverStyle)); // Hover para puntuaciones
            btnScores.setOnMouseExited(e -> btnScores.setStyle(scoresButtonStyle)); // Salir hover para puntuaciones


            btnPlay.setOnAction(e -> {
                stage.close(); // Cierra el menú principal
                showCharacterSelectionMenu(); // Abre la selección de personaje
            });

            btnHelp.setOnAction(e -> {
                showHelp(); // Muestra el diálogo de ayuda (el menú principal permanece abierto)
            });

            btnAbout.setOnAction(e -> {
                showAbout(); // Muestra el diálogo "Acerca De" (el menú principal permanece abierto)
            });

            btnScores.setOnAction(e -> {
                showScores(); // Muestra el diálogo de puntuaciones
            });

            root.getChildren().addAll(title, btnPlay, btnHelp, btnAbout, btnScores); // Añadir el nuevo botón
            root.setStyle("-fx-background-color: #333333;"); // Fondo oscuro para el VBox
            Scene scene = new Scene(root, 350, 350); // Ajustar tamaño de la ventana del menú
            stage.setScene(scene);
            stage.setTitle("Menú Principal de Juego");
            stage.show();
        });
    }

    /**
     * Muestra el menú de selección de personaje.
     */
    private void showCharacterSelectionMenu() {
        getExecutor().startAsyncFX(() -> {
            Stage stage = new Stage();
            VBox root = new VBox(15);
            root.setAlignment(Pos.CENTER);
            Text title = new Text("Selecciona tu personaje");
            title.setStyle("-fx-font-size: 24px; -fx-fill: white;");

            Button btnSonic = new Button("Sonic");
            Button btnTails = new Button("Tails");
            Button btnKnuckles = new Button("Knuckles");

            // Estilos para los botones de personaje
            String baseCharButtonStyle = "-fx-font-size: 18px; -fx-padding: 10px 20px; -fx-text-fill: white; -fx-border-radius: 5px; -fx-background-radius: 5px;";

            // Estilos específicos para cada botón de personaje
            String sonicButtonStyle = baseCharButtonStyle + "-fx-background-color: #0000FF;"; // Azul
            String sonicButtonHoverStyle = "-fx-background-color: #0000CC;"; // Azul más oscuro

            String tailsButtonStyle = baseCharButtonStyle + "-fx-background-color: #FFFF00;"; // Amarillo
            String tailsButtonHoverStyle = "-fx-background-color: #CCCC00;"; // Amarillo más oscuro
            btnTails.setTextFill(Color.BLACK); // Texto negro para legibilidad en amarillo

            String knucklesButtonStyle = baseCharButtonStyle + "-fx-background-color: #FF0000;"; // Rojo
            String knucklesButtonHoverStyle = "-fx-background-color: #CC0000;"; // Rojo más oscuro

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
                stage.close(); // Cierra la selección de personaje
                startNetworkAndGame();
            });
            btnTails.setOnAction(e -> {
                personajePendiente = "tails";
                stage.close(); // Cierra la selección de personaje
                startNetworkAndGame();
            });
            btnKnuckles.setOnAction(e -> {
                personajePendiente = "knuckles";
                stage.close(); // Cierra la selección de personaje
                startNetworkAndGame();
            });

            root.getChildren().addAll(title, btnSonic, btnTails, btnKnuckles);
            root.setStyle("-fx-background-color: #333333;");
            Scene scene = new Scene(root, 350, 250); // Ajustar tamaño de la ventana
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
                          "Cómo jugar:\n" +
                          "- Usa las teclas A y D para mover a tu personaje a la izquierda y derecha.\n" +
                          "- Usa la tecla W para saltar.\n" +
                          "- Recoge anillos para aumentar tu puntuación y protegerte de los enemigos.\n" +
                          "- Recoge basura (papel, caucho, basura general) para limpiar el entorno.\n" +
                          "- Salta sobre los robots enemigos para eliminarlos.\n" +
                          "- Ten cuidado con Eggman, ¡es el jefe final!\n\n" +
                          "Puntuaciones:\n" +
                          "- Tu puntuación se calcula sumando la cantidad de basura recolectada (papel, caucho, basura) más los anillos que tengas al final de la partida.\n" +
                          "- Si la partida termina en 'Game Over', tu puntuación registrada será 0.\n" +
                          "- Puedes ver las puntuaciones más altas desde el menú principal.\n\n" +
                          "Reglas:\n" +
                          "- Pierdes anillos al ser golpeado por un enemigo. Si no tienes anillos, pierdes una vida.\n" +
                          "- Si pierdes todas tus vidas, es Game Over.\n" +
                          "- Si caes por debajo de la altura Y=1000, es Game Over.\n" + 
                          "- Elimina a Eggman para ganar el juego.";

        getDialogService().showMessageBox(helpText, () -> {
            // Callback cuando el diálogo se cierra. El menú principal permanece abierto.
        });
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
                           "- Millan\n" +
                           "- Villalba\n" +
                           "- Acosta\n" +
                           "- Rodriguez\n\n" +
                           "Versión Actual: 1.0.1"; // Versión actualizada

        getDialogService().showMessageBox(aboutText, () -> {
            // Callback cuando el diálogo se cierra. El menú principal permanece abierto.
        });
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
        getDialogService().showMessageBox(sb.toString(), () -> {
            // Callback cuando el diálogo se cierra. El menú principal permanece abierto.
        });
    }

    private void startNetworkAndGame() {
        spawn("fondo");
        setLevelFromMap("mapazo.tmx");
        var client = getNetService().newTCPClient("localhost", 55555);
        client.setOnConnected(conn -> {
            conexion = conn;
            Bundle hola = new Bundle("Hola");
            conn.send(hola);
            getExecutor().startAsyncFX(() -> onClient());
            System.out.println("Cliente conectado");
        });
        client.connectAsync();
    }

    private void onClient() {
        conexion.addMessageHandlerFX((conexion, bundle) -> {
            switch (bundle.getName()) {
                case "TuID": {
                    player.setId(bundle.get("id"));
                    Bundle solicitar = new Bundle("SolicitarCrearPersonaje");
                    solicitar.put("id", player.getId());
                    solicitar.put("tipo", personajePendiente);
                    solicitar.put("x", 50); // Initial X position
                    solicitar.put("y", 150); // Initial Y position
                    conexion.send(solicitar);

                    // Send initial position to synchronize server
                    Bundle sync = new Bundle("SyncPos");
                    sync.put("id", player.getId());
                    sync.put("x", 50);
                    sync.put("y", 150);
                    conexion.send(sync);
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
                    if (playerId.equals(player.getId())) {
                        contadorAnillos++;
                        gameLogic.cambiarTextoAnillos("anillos: " + contadorAnillos);
                    }
                    break;
                }

                case "EggmanEliminado": {
                    String eggmanId = bundle.get("eggmanId");

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

                            if (bundle.get("playerId").equals(player.getId())) {
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
                    System.out.println("Creating character: " + bundle.get("tipo"));
                    String id = bundle.get("id");
                    String tipo = bundle.get("tipo");
                    double x = ((Number)bundle.get("x")).doubleValue();
                    double y = ((Number)bundle.get("y")).doubleValue();

                    if (id.equals(player.getId())) {
                        if (player.getTipo().equals("")) {
                            Entity entidad = spawn(tipo, x, y);
                            player = (Player) entidad;
                            player.setConexion(conexion);
                            // half of the screen in x, and a little further down in y
                            getGameScene().getViewport().bindToEntity(player, anchoPantalla/2.0, altoPantalla/1.5);
                            getGameScene().getViewport().setLazy(true);
                        } else {
                            player.setX(x);
                            player.setY(y);
                        }
                    } else {
                        Player remotePlayer = personajeRemotos.get(id);
                        if (remotePlayer == null) {
                            Entity entidad = spawn(tipo, x, y);
                            remotePlayer = (Player) entidad;;
                            personajeRemotos.put(id, remotePlayer);
                        } else {
                            remotePlayer.setX(x);
                            remotePlayer.setY(y);
                        }
                    }
                    break;
                }

                case "SyncPos": {
                    String syncId = bundle.get("id");
                    if (syncId.equals(player.getId())) {
                        return; // Ignore yourself
                    }

                    Player remotePlayer = personajeRemotos.get(syncId);
                    if (remotePlayer != null) {
                        Number xNum = bundle.get("x");
                        Number yNum = bundle.get("y");
                        remotePlayer.setX(xNum.doubleValue());
                        remotePlayer.setY(yNum.doubleValue());
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

                case "EstadoBasuraGlobal": {
                    int total = bundle.get("total");
                    int restante = bundle.get("restante");

                    gameLogic.agregarBarra((float) restante / total);
                    gameLogic.filtroColor((float) restante / total); // Change tone
                    gameLogic.cambiarTextoBasuraGlobal("Basura restante: " + restante + "/" + total);
                    break;
                }

                case "Mover a la izquierda":
                case "Mover a la derecha":
                case "Saltar":
                case "Detente": {
                    String moveId = bundle.get("id");
                    if (moveId.equals(player.getId())) {
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
                if (player == null) return;
                Bundle bundle = new Bundle("Mover a la izquierda");
                bundle.put("id", player.getId());
                conexion.send(bundle);
                player.moverIzquierda();
                Bundle sync = new Bundle("SyncPos");
                sync.put("id", player.getId());
                sync.put("x", player.getX());
                sync.put("y", player.getY());
                conexion.send(sync);
            }
            @Override
            protected void onActionEnd() {
                if (player == null) return;
                Bundle bundle = new Bundle("Detente");
                bundle.put("id", player.getId());
                conexion.send(bundle);
                player.detener();
                Bundle sync = new Bundle("SyncPos");
                sync.put("id", player.getId());
                sync.put("x", player.getX());
                sync.put("y", player.getY());
                conexion.send(sync);
            }
        }, KeyCode.A, VirtualButton.LEFT);

        getInput().addAction(new UserAction("Mover a la derecha") {
            @Override
            protected void onAction() {
                if (player == null) return;
                Bundle bundle = new Bundle("Mover a la derecha");
                bundle.put("id", player.getId());
                conexion.send(bundle);
                player.moverDerecha();
                Bundle sync = new Bundle("SyncPos");
                sync.put("id", player.getId());
                sync.put("x", player.getX());
                sync.put("y", player.getY());
                conexion.send(sync);
            }
            @Override
            protected void onActionEnd() {
                if (player == null) return;
                Bundle bundle = new Bundle("Detente");
                bundle.put("id", player.getId());
                conexion.send(bundle);
                player.detener();
                Bundle sync = new Bundle("SyncPos");
                sync.put("id", player.getId());
                sync.put("x", player.getX());
                sync.put("y", player.getY());
                conexion.send(sync);
            }
        }, KeyCode.D, VirtualButton.RIGHT);

        getInput().addAction(new UserAction("Saltar") {
            @Override
            protected void onActionBegin() {
                if (player == null) return;
                Bundle bundle = new Bundle("Saltar");
                bundle.put("id", player.getId());
                conexion.send(bundle);
                player.saltar();
                GameLogic.SyncPos(player);
            }
        }, KeyCode.W, VirtualButton.A);

        getInput().addAction(new UserAction("Interactuar") {
            @Override
            protected void onActionBegin() {
                if (player == null) return;
                if (flag_Interactuar) { // Only interact if the flag has been activated
                    // Send message to server to interact with the environment
                    Bundle bundle = new Bundle("Interactuar");
                    bundle.put("id", player.getId());
                    bundle.put("tipo", player.getTipo());
                    conexion.send(bundle);
                    player.interactuar();
                    recogerBasura(player, stand_by); // Call the recogerBasura method with the stand_by entity
                    // }
                }
            }
        }, KeyCode.E);

        getInput().addAction(new UserAction("Transformar") {
            @Override
            protected void onActionBegin() {
                if (player == null) return;
                player.transformarSuperSonic();
            }
        }, KeyCode.P);

        getInput().addAction(new UserAction("Desactivar filtro") {
            @Override
            protected void onActionBegin() {
                gameLogic.filtroColor(0);
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
            if (player.hasComponent(SonicComponent.class) || player.hasComponent(TailsComponent.class) || player.hasComponent(KnucklesComponent.class)) {
                recogerBasura((Player)jugador, basura);
            }
        });

        onCollisionBegin(GameFactory.EntityType.PLAYER, GameFactory.EntityType.PAPEL, (jugador, papel) -> {
            if (player.hasComponent(TailsComponent.class)) {
                recogerBasura((Player)jugador, papel);
            }
        });

        onCollisionBegin(GameFactory.EntityType.KNUCKLES, GameFactory.EntityType.CAUCHO, (jugador, caucho) -> {
            if (player.hasComponent(KnucklesComponent.class)) {
                flag_Interactuar = true;
                stand_by = caucho; // Save the rubber entity to interact
            }
        });

        onCollisionEnd(GameFactory.EntityType.PLAYER, GameFactory.EntityType.CAUCHO, (jugador, caucho) -> {
            flag_Interactuar = false;
        });

       onCollisionBegin(GameFactory.EntityType.PLAYER, GameFactory.EntityType.ROBOT_ENEMIGO, (entidad, robot) -> {
           double alturaPlayer = entidad.getHeight();
           double alturaRobot = robot.getHeight();

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
            double alturaEggman = eggman.getHeight();

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
        long ahora = System.currentTimeMillis();

        if (player.isInvencible()){
            return;
        }
        
        if (contadorAnillos > 0) {
            play("perder_anillos.wav");
            contadorAnillos = 0;
            gameLogic.cambiarTextoAnillos("Anillos: " + contadorAnillos);
            GameLogic.activarInvencibilidad(3000, player);
            return; 
        }

        play("muerte.wav");
        player.restarVida();
        gameLogic.cambiarTextoVidas("Vidas: " + player.getVidas());
        if (player.estaMuerto()) {
            showGameOver();
        } else {
            GameLogic.activarInvencibilidad(3000, player);
        }
    }

    /**
     * Muestra el mensaje de Game Over y solicita el nombre del jugador para la puntuación.
     */
    private void showGameOver() {
        getDialogService().showMessageBox("¡Game Over!", () -> {
            promptForNameAndSaveScore(0); // Puntuación es 0 en Game Over
        });
    }

    /**
     * Muestra el mensaje de Juego Ganado y solicita el nombre del jugador para la puntuación.
     */
    private void showGameWon() {
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
                    promptForNameAndSaveScore(score); // Volver a solicitar el nombre
                });
                return; 
            }
            highScores.add(new ScoreEntry(playerName.trim(), score));
            // Aquí podrías añadir lógica para guardar highScores a un archivo si fuera necesario
            
            // Reiniciar el juego y volver al menú principal
            resetGameAndReturnToMenu();
        });
    }

    /**
     * Reinicia el estado del juego y vuelve al menú principal.
     */
    private void resetGameAndReturnToMenu() {
        // Ejecutar la limpieza del mundo y el reinicio de forma asíncrona
        // Esto evita ConcurrentModificationException al diferir la limpieza del mundo
        // hasta que la iteración actual del bucle del juego haya terminado.
        FXGL.runOnce(() -> {
            // Limpiar todas las entidades del mundo del juego
            getGameWorld().removeEntities(getGameWorld().getEntities());
            System.out.println("DEBUG: Game world cleared.");

            // Reiniciar contadores
            contadorAnillos = 0;
            contadorBasura = 0;
            contadorPapel = 0;
            contadorCaucho = 0;
            flag_Interactuar = false;
            stand_by = null;
            System.out.println("DEBUG: Contadores y banderas reiniciados.");

            // Reiniciar el objeto player a una nueva instancia
            // Esto asegura que el jugador tenga vidas y estado iniciales
            player = new Player(); // Asumiendo que el constructor de Player lo inicializa correctamente
            System.out.println("DEBUG: Objeto Player reinicializado.");

            // Reiniciar la lógica del juego (UI, etc.)
            if (gameLogic != null) {
                gameLogic.reset(); // Este método ya fue implementado en GameLogic.java
                System.out.println("DEBUG: UI de GameLogic reiniciada.");
            }

            // Volver al menú principal
            showMainMenu();
            System.out.println("DEBUG: Regresando al menú principal.");

        }, Duration.ZERO); // Ejecutar tan pronto como sea posible en el siguiente tick del juego
    }


    @Override
    protected void onUpdate(double tpf) {
        // Existing position synchronization logic
        if (conexion != null && player != null) {
            if (System.currentTimeMillis() % 100 < 16) {
                Bundle bundle = new Bundle("SyncPos");
                bundle.put("id", player.getId());
                bundle.put("x", player.getX());
                bundle.put("y", player.getY());
                conexion.send(bundle);
            }

            // GAME OVER CONDITION BY HEIGHT
            if (player.getY() > 1000) { // If the player falls below Y=1000 (off-screen)
                showGameOver();
            }
        }
    }
}

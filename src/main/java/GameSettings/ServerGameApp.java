package GameSettings;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.multiplayer.MultiplayerService;
import com.almasb.fxgl.net.Connection;
import component.Enemigos.EggmanComponent;
import component.GameFactory;

import java.io.Serializable;
import java.util.*;

import static GameSettings.Entities.posicionesBasura;
import static GameSettings.Entities.posicionesRings;
import static GameSettings.Entities.posicionesRobots;
import static com.almasb.fxgl.dsl.FXGL.*;

public class ServerGameApp extends GameApplication implements Serializable {
    private static final int SERVER_PORT = 55555;

    // Usar un mapa para personajes existentes mejora el rendimiento de búsqueda de O(n) a O(1)
    private final Map<String, Bundle> personajesExistentes = new HashMap<>();
    private final Map<String, Entity> anillos = new HashMap<>();
    private final Map<String, Entity> basuras = new HashMap<>();
    private final Map<String, Entity> robots = new HashMap<>();
    private final Map<String, Entity> eggmanBoss = new HashMap<>();
    private final Set<Integer> eventosDisparados = new HashSet<>();

    private com.almasb.fxgl.net.Server<Bundle> server;
    private long updateCount = 0; // Contador para las actualizaciones del juego

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setWidth(800);
        gameSettings.setHeight(500);
        gameSettings.setTitle("Server");
        gameSettings.addEngineService(MultiplayerService.class);
    }

    @Override
    protected void initGame() {
        server = getNetService().newTCPServer(SERVER_PORT);
        server.setOnConnected(conn -> {
            String nuevoId = UUID.randomUUID().toString();
            Bundle tuId = new Bundle("TuID");
            tuId.put("id", nuevoId);
            conn.send(tuId);
            getExecutor().startAsyncFX(() -> onServer(conn));
        });
        System.out.println("Servidor creado"); 
        server.startAsync();
        Jugar();
    }

    /**
     * Inicializa el mundo del juego con entidades como anillos, robots y basura.
     */
    private void Jugar() {
        getGameWorld().addEntityFactory(new GameFactory());
        setLevelFromMap("mapazo.tmx");

        for (int[] pos : posicionesRings) {
            String ringId = UUID.randomUUID().toString();
            Entity ring = spawn("ring", pos[0], pos[1]);
            ring.getProperties().setValue("id", ringId);
            anillos.put(ringId, ring);
        }

        for (int[] pos : posicionesRobots) {
            String robotId = UUID.randomUUID().toString();
            Entity robot = spawn("robotEnemigo", pos[0], pos[1]);
            robot.getProperties().setValue("id", robotId);
            robots.put(robotId, robot);
        }

        String[] tiposDeBasura = { "basura", "papel", "caucho" };
        for (int[] pos : posicionesBasura) {
            String tipo = tiposDeBasura[(int) (Math.random() * tiposDeBasura.length)];
            String id = UUID.randomUUID().toString();
            Entity basuraEntidad = spawn(tipo, pos[0], pos[1]);
            basuraEntidad.getProperties().setValue("id", id);
            basuraEntidad.getProperties().setValue("tipo", tipo);
            basuras.put(id, basuraEntidad);
        }
    }

    /**
     * El manejador principal de mensajes del servidor. Delega la lógica a métodos especializados.
     */
    public void onServer(Connection<Bundle> connection) {
        connection.addMessageHandlerFX((conn, bundle) -> {
            switch (bundle.getName()) {
                case "Hola":
                    handleNewClient(conn);
                    break;
                case "SolicitarCrearPersonaje":
                    handleCharacterCreation(bundle);
                    break;
                case "Mover a la izquierda":
                case "Mover a la derecha":
                case "Saltar":
                case "Detente":
                case "SyncPos":
                    handlePlayerMovement(conn, bundle);
                    break;
                case "RecogerAnillo":
                    handleRingPickup(bundle);
                    break;
                case "RecogerBasura":
                    handleTrashPickup(bundle);
                    break;
                case "EliminarRobot":
                    handleRobotDefeated(bundle);
                    break;
                case "DañoEggman":
                    handleEggmanDamage(bundle);
                    break;
                case "Interactuar":
                    // Lógica de interacción si es necesaria en el servidor
                    break;
            }
        });
    }

    // --- Métodos de Manejo de Lógica ---

    /**
     * Envía el estado actual del juego (personajes, ítems) a un cliente recién conectado.
     */
    private void handleNewClient(Connection<Bundle> conn) {
        System.out.println("jugador se conecto"); 
        
        // Enviar personajes existentes
        personajesExistentes.values().forEach(personaje -> {
            Bundle copia = new Bundle("Crear Personaje");
            copia.put("id", personaje.get("id")); 
            copia.put("tipo", personaje.get("tipo")); 
            copia.put("x", personaje.get("x")); 
            copia.put("y", personaje.get("y")); 
            conn.send(copia);
        });

        // Enviar entidades existentes (anillos, basura, robots)
        sendEntitiesToClient(conn, "crearRing", anillos);
        sendEntitiesToClient(conn, "crearbasura", basuras);
        sendEntitiesToClient(conn, "CrearRobotEnemigo", robots);
    }
    
    /**
     * Ayudante genérico para enviar un conjunto de entidades a un cliente.
     */
    private void sendEntitiesToClient(Connection<Bundle> conn, String messageName, Map<String, Entity> entityMap) {
        for (Map.Entry<String, Entity> entry : entityMap.entrySet()) {
            Bundle crearEntidad = new Bundle(messageName);
            crearEntidad.put("id", entry.getKey());
            crearEntidad.put("x", entry.getValue().getX());
            crearEntidad.put("y", entry.getValue().getY());
            if (entry.getValue().getProperties().exists("tipo")) { // Solo si la propiedad "tipo" existe
                crearEntidad.put("tipo", entry.getValue().getProperties().getString("tipo"));
            }
            conn.send(crearEntidad);
        }
    }

    /**
     * Crea un nuevo personaje o reenvía uno existente si el ID ya está registrado.
     */
    private void handleCharacterCreation(Bundle bundle) {
        String id = bundle.get("id"); 
        if (!personajesExistentes.containsKey(id)) {
            Bundle personaje = new Bundle("Crear Personaje");
            personaje.put("id", id);
            personaje.put("tipo", bundle.get("tipo"));
            personaje.put("x", 50.0);
            personaje.put("y", 150.0);
            personajesExistentes.put(id, personaje);
            server.broadcast(personaje);
        } else {
            server.broadcast(personajesExistentes.get(id));
        }
    }

    /**
     * Retransmite los mensajes de movimiento y sincronización de posición a otros clientes.
     */
    private void handlePlayerMovement(Connection<Bundle> conn, Bundle bundle) {
        if (bundle.getName().equals("SyncPos")) {
            String id = bundle.get("id");
            Bundle personaje = personajesExistentes.get(id);
            if (personaje != null) {
                personaje.put("x", bundle.get("x")); 
                personaje.put("y", bundle.get("y")); 
            }
        }
        // Retransmite a todos los clientes excepto al remitente.
        for (Connection<Bundle> c : server.getConnections()) {
            if (c != conn) {
                c.send(bundle);
            }
        }
    }

    /**
     * Procesa la recolección de un anillo, lo elimina del mundo y notifica a los clientes.
     */
    private void handleRingPickup(Bundle bundle) {
        String ringId = bundle.get("ringId");
        Entity ring = anillos.remove(ringId);
        if (ring != null) {
            ring.removeFromWorld();
            Bundle anilloRecogido = new Bundle("AnilloRecogido");
            anilloRecogido.put("playerId", bundle.get("playerId"));
            anilloRecogido.put("ringId", ringId);
            server.broadcast(anilloRecogido);
        }
    }

    /**
     * Procesa la recolección de basura, verifica la lógica de recolección y notifica a los clientes.
     */
    private void handleTrashPickup(Bundle bundle) {
        String trashId = bundle.get("trashId");
        Entity trash = basuras.get(trashId);
        if (trash != null) {
            String tipoJugador = bundle.get("tipo");
            String tipoBasura = trash.getProperties().getString("tipo");

            boolean puedeRecoger = tipoBasura.equals("basura") ||
                                   (tipoBasura.equals("caucho") && tipoJugador.equals("knuckles")) ||
                                   (tipoBasura.equals("papel") && tipoJugador.equals("tails"));
            if (puedeRecoger) {
                basuras.remove(trashId).removeFromWorld(); 
                
                Bundle basuraRecogida = new Bundle("BasuraRecogida");
                basuraRecogida.put("playerId", bundle.get("playerId"));
                basuraRecogida.put("trashId", trashId); 
                server.broadcast(basuraRecogida);

                verificarEventoBasura(); 
            }
        }
    }
    
    /**
     * Procesa la derrota de un robot, lo elimina del mundo y notifica a los clientes.
     */
    private void handleRobotDefeated(Bundle bundle) {
        String robotId = bundle.get("robotId"); 
        Entity robot = robots.remove(robotId);
        if (robot != null) {
            robot.removeFromWorld(); 
            Bundle robotEliminado = new Bundle("RobotEliminado");
            robotEliminado.put("playerId", bundle.get("playerId")); 
            robotEliminado.put("robotId", robotId); 
            server.broadcast(robotEliminado);
        }
    }

    /**
     * Procesa el daño infligido a Eggman y notifica si es derrotado.
     */
    private void handleEggmanDamage(Bundle bundle) {
        String eggmanId = bundle.get("eggmanId"); 
        Entity eggman = eggmanBoss.get(eggmanId);
        if (eggman != null) {
            EggmanComponent egg = eggman.getComponent(EggmanComponent.class);
            egg.restarVida(); 
            if (egg.estaMuerto()) {
                Bundle eggmanEliminado = new Bundle("EggmanEliminado");
                eggmanEliminado.put("eggmanId", eggmanId); 
                server.broadcast(eggmanEliminado);
            }
        }
    }

    /**
     * Dispara eventos en el juego basados en la cantidad de basura restante.
     */
    private void verificarEventoBasura() {
        int cantidadRestante = basuras.size(); 
        if (cantidadRestante <= 6 && !eventosDisparados.contains(6)) {
            eventosDisparados.add(6);
            String eggmanId = UUID.randomUUID().toString();
            Entity eggman = spawn("eggman", 1330, 340);
            eggman.getProperties().setValue("id", eggmanId);
            eggmanBoss.put(eggmanId, eggman);
            Bundle crearEggman = new Bundle("CrearEggman");
            crearEggman.put("id", eggmanId); 
            crearEggman.put("x", eggman.getX()); 
            crearEggman.put("y", eggman.getY()); 
            server.broadcast(crearEggman);
        }
    }

    @Override
    protected void onUpdate(double tpf) {
        // Incrementa el contador de actualizaciones en cada frame
        updateCount++;

        // Sincroniza la posición de las entidades no controladas por jugadores a un ritmo fijo.
        // Se sincroniza cada 6 ticks (aprox. 10 veces por segundo a 60 FPS).
        if (updateCount % 6 == 0) { 
            // Sincroniza robots
            for (Map.Entry<String, Entity> entry : robots.entrySet()) {
                Bundle pos = new Bundle("SyncRobotPos");
                pos.put("id", entry.getKey()); 
                pos.put("x", entry.getValue().getX()); 
                pos.put("y", entry.getValue().getY()); 
                server.broadcast(pos);
            }
            // Sincroniza Eggman
            for (Map.Entry<String, Entity> entry : eggmanBoss.entrySet()) {
                Bundle pos = new Bundle("SyncEggmanPos");
                pos.put("id", entry.getKey());
                pos.put("x", entry.getValue().getX());
                pos.put("y", entry.getValue().getY()); 
                server.broadcast(pos);
            }
        }
    }
}

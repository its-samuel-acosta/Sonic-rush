package GameSettings;

import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.net.Connection;
import component.Personajes.PlayerComponent;


// Clase Player que extiende de Entity, maneja el estado y delega acciones al PlayerComponent
// Centralizando logica del estado
public class Player extends Entity {

    private int vidas = 3;
    private String id = "";
    private Connection<Bundle> conexion;
    private boolean invencible = false;
    private PlayerComponent playerComponent; // Referencia al PlayerComponent

    // Constructor vacío, para ser usado cuando la entidad Player es creada por el FXGL
    public Player() {
        // El playerComponent se establecera externamente despues de que la entidad sea spawneada
    }


    // Realiza una accion en el PlayerComponent si esta presente
    private void performAction(java.util.function.Consumer<PlayerComponent> action) {
        if (playerComponent != null) {
            action.accept(playerComponent);
        } else {
            // Este error indica que playerComponent no fue seteado correctamente
            System.err.println("ERROR: playerComponent es nulo cuando se intenta la acción para " + getTipo() + " (en Player.java). La acción no se puede realizar.");
        }
    }

    
    public void moverIzquierda() { performAction(PlayerComponent::moverIzquierda); }
    public void moverDerecha() { performAction(PlayerComponent::moverDerecha); }
    public void saltar() { performAction(PlayerComponent::saltar); }
    public void detener() { performAction(PlayerComponent::detener); }
    public void interactuar() { performAction(PlayerComponent::interactuar); }
    
    public String getTipo() {
        // Si playerComponent es nulo aquí, significa que no se ha seteado correctamente.
        return playerComponent != null ? playerComponent.getTipo() : "Desconocido";
    }
 
    // Getters y Setters
    public void setInvencibilidad(boolean flag) { this.invencible = flag; }
    public boolean isInvencible() { return invencible; }
    public Connection<Bundle> getConexion() { return conexion; }
    public void setConexion(Connection<Bundle> conexion) { this.conexion = conexion; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getVidas() { return vidas; }
    public void restarVida() { vidas--; }
    public boolean estaMuerto() { return vidas <= 0; }

    // Setter para playerComponent, para ser llamado desde ClientGameApp
    public void setPlayerComponent(PlayerComponent component) {
        this.playerComponent = component;
        System.out.println("DEBUG: PlayerComponent seteado en Player.java para tipo: " + (component != null ? component.getTipo() : "null"));
    }
}

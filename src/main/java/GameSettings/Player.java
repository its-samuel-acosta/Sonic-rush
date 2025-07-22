package GameSettings;

import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.net.Connection;
import component.Personajes.PlayerComponent;
import component.Personajes.SonicComponent;

/**
 * La clase Player maneja el estado (vidas, ID) y delega acciones al PlayerComponent.
 * Esta estructura centraliza la lógica de estado y evita duplicación.
 */
public class Player extends Entity {

    private int vidas = 3;
    private String id = "";
    private Connection<Bundle> conexion;
    private boolean invencible = false;

    private void performAction(java.util.function.Consumer<PlayerComponent> action) {
        getComponentOptional(PlayerComponent.class).ifPresent(action);
    }

    public void moverIzquierda() { performAction(PlayerComponent::moverIzquierda); }
    public void moverDerecha() { performAction(PlayerComponent::moverDerecha); }
    public void saltar() { performAction(PlayerComponent::saltar); }
    public void detener() { performAction(PlayerComponent::detener); }
    public void interactuar() { performAction(PlayerComponent::interactuar); }
    
    public String getTipo() {
        return getComponentOptional(PlayerComponent.class).map(PlayerComponent::getTipo).orElse("");
    }

    public void transformarSuperSonic() {
        if (!isInvencible()) {
            this.getComponentOptional(SonicComponent.class).ifPresent(SonicComponent::transformarSuperSonic);
        } else {
            this.getComponentOptional(SonicComponent.class).ifPresent(SonicComponent::destransformar);
        }
        setInvencibilidad(!isInvencible());
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
}
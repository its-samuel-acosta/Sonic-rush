package component.Personajes;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.component.Required;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;

/**
 * Clase abstracta base para los componentes de personaje del jugador.
 * Maneja la lógica común de movimiento, animaciones y saltos.
 */
@Required(PhysicsComponent.class) // Asegura que la entidad tenga un PhysicsComponent
public abstract class PlayerComponent extends Component {

    protected PhysicsComponent physics;
    protected AnimatedTexture texture;
    protected AnimationChannel parado, caminando, saltando;
    
    protected int saltosPermitidos = 2;
    protected int velocidad_lateral_base;
    protected int velocidad_vertical_base;
    static int MAX_SALTOS = 2; // Valor por defecto, puede ser sobrescrito por subclases

    public abstract String getTipo();

    @Override
    public void onAdded() {
        // Obtener el PhysicsComponent de la entidad
        this.physics = entity.getComponent(PhysicsComponent.class);
        if (this.physics == null) {
            System.err.println("ERROR: PhysicsComponent es nulo en " + getTipo() + " en onAdded!");
        } else {
            System.out.println("DEBUG: PhysicsComponent inicializado para " + getTipo() + " en onAdded.");
        }

        entity.getTransformComponent().setScaleOrigin(new Point2D(16, 21));
        entity.getViewComponent().addChild(texture);
        physics.onGroundProperty().addListener((obs, old, tocandoPiso) -> {
            if (tocandoPiso) {
                saltosPermitidos = MAX_SALTOS;
                System.out.println("DEBUG: " + getTipo() + " está en el suelo. Saltos restablecidos a " + MAX_SALTOS);
            }
        });
    }

    @Override
    public void onUpdate(double tpf) {
        if (physics == null) {
            System.err.println("ERROR: PhysicsComponent es nulo en onUpdate para " + getTipo() + ". No se puede actualizar la animación o velocidad.");
            return;
        }
        // Depuración de velocidad actual en cada frame
        // System.out.println("DEBUG: " + getTipo() + " Velocidad actual - X: " + physics.getVelocityX() + ", Y: " + physics.getVelocityY());

        // Lógica de animación basada en el movimiento
        if (physics.isMovingX() && !physics.isMovingY()) {
            if (texture.getAnimationChannel() != caminando) {
                texture.loopAnimationChannel(caminando);
            }
        } else if (physics.isMovingY()) {
            if (texture.getAnimationChannel() != saltando) {
                texture.loopAnimationChannel(saltando);
            }
        } else {
            if (texture.getAnimationChannel() != parado) {
                texture.loopAnimationChannel(parado);
            }
        }
    }

    public void moverIzquierda() {
        if (physics == null) {
            System.err.println("ERROR: PhysicsComponent es nulo al intentar mover a la izquierda para " + getTipo() + ". No se puede aplicar velocidad.");
            return;
        }
        System.out.println("DEBUG: Intentando establecer velocidad izquierda para " + getTipo());
        getEntity().setScaleX(-1); // Voltear el sprite
        physics.setVelocityX(-velocidad_lateral_base);
        System.out.println("DEBUG: " + getTipo() + " moviéndose a la izquierda. VelocidadX establecida a: " + physics.getVelocityX());
    }

    public void moverDerecha() {
        if (physics == null) {
            System.err.println("ERROR: PhysicsComponent es nulo al intentar mover a la derecha para " + getTipo() + ". No se puede aplicar velocidad.");
            return;
        }
        System.out.println("DEBUG: Intentando establecer velocidad derecha para " + getTipo());
        getEntity().setScaleX(1); // Orientar el sprite a la derecha
        physics.setVelocityX(velocidad_lateral_base);
        System.out.println("DEBUG: " + getTipo() + " moviéndose a la derecha. VelocidadX establecida a: " + physics.getVelocityX());
    }

    public void detener() {
        if (physics == null) {
            System.err.println("ERROR: PhysicsComponent es nulo al intentar detener para " + getTipo() + ". No se puede aplicar velocidad.");
            return;
        }
        System.out.println("DEBUG: Intentando detener para " + getTipo());
        physics.setVelocityX(0);
        System.out.println("DEBUG: " + getTipo() + " detenido. VelocidadX establecida a: " + physics.getVelocityX());
    }

    public void saltar() {
        if (physics == null) {
            System.err.println("ERROR: PhysicsComponent es nulo al intentar saltar para " + getTipo() + ". No se puede aplicar velocidad.");
            return;
        }
        if (saltosPermitidos > 0) {
            System.out.println("DEBUG: Intentando saltar para " + getTipo());
            physics.setVelocityY(-velocidad_vertical_base);
            saltosPermitidos--;
            System.out.println("DEBUG: " + getTipo() + " saltó. VelocidadY establecida a: " + physics.getVelocityY() + ", Saltos restantes: " + saltosPermitidos);
        } else {
            System.out.println("DEBUG: " + getTipo() + " no le quedan saltos.");
        }
    }
    
    // La interacción por defecto no hace nada, se sobreescribe en Knuckles
    public void interactuar() {}
}

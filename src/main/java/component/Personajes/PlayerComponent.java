package component.Personajes;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.component.Required;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;

//Clase abstracta base para los componentes de personaje del jugador.
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
        } 

        entity.getTransformComponent().setScaleOrigin(new Point2D(16, 21));
        entity.getViewComponent().addChild(texture);
        physics.onGroundProperty().addListener((obs, old, tocandoPiso) -> {
            if (tocandoPiso) {
                saltosPermitidos = MAX_SALTOS;
            }
        });
    }
    
    @Override
    public void onUpdate(double tpf) {
        if (physics == null) {
            System.err.println("ERROR: PhysicsComponent es nulo en onUpdate para " + getTipo() + ". No se puede actualizar la animación o velocidad.");
            return;
        }

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
        getEntity().setScaleX(-1); // Voltear el sprite
        physics.setVelocityX(-velocidad_lateral_base);
    }

    public void moverDerecha() {
        if (physics == null) {
            System.err.println("ERROR: PhysicsComponent es nulo al intentar mover a la derecha para " + getTipo() + ". No se puede aplicar velocidad.");
            return;
        }
        getEntity().setScaleX(1); // Orientar el sprite a la derecha
        physics.setVelocityX(velocidad_lateral_base);
    }

    public void detener() {
        if (physics == null) {
            System.err.println("ERROR: PhysicsComponent es nulo al intentar detener para " + getTipo() + ". No se puede aplicar velocidad.");
            return;
        }
        physics.setVelocityX(0);
    }

    public void saltar() {
        if (physics == null) {
            System.err.println("ERROR: PhysicsComponent es nulo al intentar saltar para " + getTipo() + ". No se puede aplicar velocidad.");
            return;
        }
        if (saltosPermitidos > 0) {
            physics.setVelocityY(-velocidad_vertical_base);
            saltosPermitidos--;
        } else {
        }
    }
    
    // La interacción por defecto
    public void interactuar() {}
}

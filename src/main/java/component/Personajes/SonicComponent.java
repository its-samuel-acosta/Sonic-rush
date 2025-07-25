package component.Personajes;

import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.util.Duration;
import static com.almasb.fxgl.dsl.FXGL.image;
import static com.almasb.fxgl.dsl.FXGL.play;

public class SonicComponent extends PlayerComponent {

    // Canales de animación estáticos para evitar recargar imágenes
    public static final AnimationChannel NORMAL_IDLE = new AnimationChannel(image("Personajes/sonic.png"), 21, 32, 42, Duration.seconds(4.5), 0, 20);
    public static final AnimationChannel NORMAL_CAMINA = new AnimationChannel(image("Personajes/sonic_camina.png"), 6, 44, 42, Duration.seconds(0.66), 0, 5);
    public static final AnimationChannel NORMAL_SALTO = new AnimationChannel(image("Personajes/sonic_camina.png"), 7, 44, 42, Duration.seconds(0.66), 6, 6);
    public static final AnimationChannel SUPER_IDLE = new AnimationChannel(image("Personajes/super_idle.png"), 21, 32, 42, Duration.seconds(4.5), 0, 20);
    public static final AnimationChannel SUPER_CAMINA = new AnimationChannel(image("Personajes/super_sonic.png"), 8, 44, 60, Duration.seconds(0.66), 0, 7);
    public static final AnimationChannel SUPER_VUELO = new AnimationChannel(image("Personajes/super_sonic.png"), 8, 44, 60, Duration.seconds(0.66), 7, 7);

    private boolean isSuper = false;
    public boolean canTransform = true;

    public SonicComponent() {
        MAX_SALTOS = 2; // Sonic puede saltar 2 veces
        velocidad_lateral_base = 400; 
        velocidad_vertical_base = 370;
        parado = NORMAL_IDLE; 
        caminando = NORMAL_CAMINA; 
        saltando = NORMAL_SALTO; 
        
        texture = new AnimatedTexture(parado); 
        texture.loop();
    }

    @Override
    public String getTipo() {
        return "sonic";
    }

    @Override
    public void onUpdate(double tpf) {
        // La lógica de animación se basa en el estado (normal o super)
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

    @Override
    public void saltar() {
        super.saltar();
        if (!isSuper) { // El sonido de salto solo en estado normal
            play("salto.wav"); 
        }
    }

    /**
     * Activa la transformación a Super Sonic.
     * @return true si la transformación ocurrió, false si no.
     */
    public boolean transform() {
        if (!canTransform) {
            return false;
        }
        canTransform = false;
        isSuper = true;

        // Cambiar a animaciones de Super Sonic
        parado = SUPER_IDLE;
        caminando = SUPER_CAMINA;
        saltando = SUPER_VUELO;
        
        // Ajustar el punto de anclaje para el sprite más alto
        entity.getTransformComponent().setScaleOrigin(new javafx.geometry.Point2D(16, 30)); // Ajusta según el centro del sprite de 60px

        // Aplicar la nueva animación de inmediato
        texture.loopAnimationChannel(parado);
        return true;
    }

    /**
     * Revierte a Sonic a su estado normal.
     */
    public void revert() {
        isSuper = false;

        // Revertir a las animaciones normales
        parado = NORMAL_IDLE;
        caminando = NORMAL_CAMINA;
        saltando = NORMAL_SALTO;
        
        // Restaurar el punto de anclaje original
        entity.getTransformComponent().setScaleOrigin(new javafx.geometry.Point2D(16, 21)); // Centro original del sprite normal

        // Aplicar la animación normal de inmediato
        texture.loopAnimationChannel(parado);
    }

    // El método interactuar ahora está vacío porque la lógica se dispara desde ClientGameApp
    @Override
    public void interactuar() {
        // La lógica de transformación se inicia en ClientGameApp para acceder a GameLogic y Timers.
    }
}
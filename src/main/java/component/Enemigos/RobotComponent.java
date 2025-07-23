package component.Enemigos;

import static com.almasb.fxgl.dsl.FXGL.image;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Componente para el enemigo Robot. Hereda la lógica de patrullaje de EnemyComponent.
 */
public class RobotComponent extends EnemyComponent {

    // Define las características específicas del robot
    private static final double VELOCIDAD = 50;
    private static final double DISTANCIA_PATRULLAJE = 500;
    private static final int VIDAS = 1;

    public RobotComponent() {
        super(VELOCIDAD, DISTANCIA_PATRULLAJE, VIDAS);
    }

    @Override
    public void onAdded() {
        super.onAdded(); // Llama a la lógica base de EnemyComponent
        Image image = image("Enemigos/enemigo.png");
        ImageView view = new ImageView(image);
        view.setFitWidth(100);
        view.setFitHeight(100);
        entity.getViewComponent().addChild(view);
    }
}
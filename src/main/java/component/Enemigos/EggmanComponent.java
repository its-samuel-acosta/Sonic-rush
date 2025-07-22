package component.Enemigos;

import static com.almasb.fxgl.dsl.FXGL.image;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Componente para el jefe Eggman. Hereda la lógica de patrullaje de EnemyComponent.
 */
public class EggmanComponent extends EnemyComponent {

    // Define las características específicas de Eggman
    private static final double VELOCIDAD = 75;
    private static final double DISTANCIA_PATRULLAJE = 300;
    private static final int VIDAS = 10;

    public EggmanComponent() {
        super(VELOCIDAD, DISTANCIA_PATRULLAJE, VIDAS);
    }

    @Override
    public void onAdded() {
        super.onAdded(); // Llama a la lógica base de EnemyComponent
        Image image = image("Enemigos/eggman.png");
        ImageView view = new ImageView(image);
        view.setFitWidth(100);
        view.setFitHeight(100);
        entity.getViewComponent().addChild(view);
    }
}
package component.Enemigos;

import static com.almasb.fxgl.dsl.FXGL.image;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


//Clase para el robot enemigo, hereda losica de EnemyComponent
public class RobotComponent extends EnemyComponent {

    // Define las características específicas del robot
    private static final double VELOCIDAD = 15;
    private static final double DISTANCIA_PATRULLAJE = 80;
    private static final int VIDAS = 1;

    public RobotComponent() {
        super(VELOCIDAD, DISTANCIA_PATRULLAJE, VIDAS);
    }

    //Configura la apariencia del robot enemigo en el juego
    @Override
    public void onAdded() {
        super.onAdded(); // Llama a la lógica base de EnemyComponent
        Image image = image("Enemigos/enemigo2.png");
        ImageView view = new ImageView(image);
        view.setFitWidth(48);
        view.setFitHeight(48);
        entity.getViewComponent().addChild(view);
    }
}
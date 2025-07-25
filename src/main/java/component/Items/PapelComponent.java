package component.Items;

import static com.almasb.fxgl.dsl.FXGL.*;

import com.almasb.fxgl.entity.component.Component;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PapelComponent extends Component {

    //Configura la apariencia del papel en el juego
    @Override
    public void onAdded() {
        Image image = image("Items/papel.png");
        ImageView view = new ImageView(image);
        view.setFitWidth(32);   // Aumentado para mejor visibilidad
        view.setFitHeight(32);
        entity.getViewComponent().addChild(view);
    }
}
package component.Items;

import static com.almasb.fxgl.dsl.FXGL.*;

import com.almasb.fxgl.entity.component.Component;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class TrashComponent extends Component {

    // Configura la apariencia de la Basura en el juego
    @Override
    public void onAdded() {
        Image image = image("Items/basura.png");
        ImageView view = new ImageView(image);
        view.setFitWidth(32); 
        view.setFitHeight(32);
        entity.getViewComponent().addChild(view);
    }
}
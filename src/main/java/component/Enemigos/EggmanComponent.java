// Source code is decompiled from a .class file using FernFlower decompiler.
package component.Enemigos;

import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// Clase que representa a eggman en el juego
// hereda propiedades de Enemycomponent
public class EggmanComponent extends EnemyComponent {
   public EggmanComponent() {
      super(75.0, 600.0, 10); //establece sus caracteristicas
   }

   //configura la apariencia de eggman en el juego
   public void onAdded() {
      super.onAdded();
      Image image = FXGL.image("Enemigos/eggman2.png");
      ImageView view = new ImageView(image);
      view.setFitWidth(100.0);
      view.setFitHeight(100.0);
      this.entity.getViewComponent().addChild(view);
   }
}

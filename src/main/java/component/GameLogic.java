package component;

import GameSettings.Player;
import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.net.Connection;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.io.Serializable;

import static com.almasb.fxgl.dsl.FXGL.*;

// CLase que maneja los estados del juego, tesxto en pantalla y mecanicas globales
public class GameLogic extends Component implements Serializable {

    //Varaibles para Textos
    private Text textoCaucho;
    private Text textoAnillos;
    private Text textoVidas;
    private Text textoBasura;
    private Text textoPapel;
    private Text textoTiempo;

    private ProgressBar currentProgressBar; 
    private ColorAdjust currentColorAdjust; 

    // Variables para la invencibilidad 
    private double invincibilityRemainingTime = 0.0;    // Tiempo restante de invencibilidad
    private Player invinciblePlayer = null;             // Referencia al jugador invencible
    private double blinkTimerAccumulator = 0.0;         // Acumulador para el parpadeo

    public GameLogic() {
        //Fuente personalizada para los textox dentro del juego
        Font miFuente = getAssetLoader().loadFont("PublicPixel.ttf").newFont(24);

        //Textos en Pantalla
        textoCaucho = new Text("Caucho: 0");
        textoCaucho.setStyle("-fx-font-size: 24px; -fx-fill: red;");
        textoCaucho.setFont(miFuente);
        addUINode(textoCaucho, 20, 110);

        textoAnillos = new Text("Anillos: 0");
        textoAnillos.setStyle("-fx-font-size: 24px; -fx-fill: yellow;");
        textoAnillos.setFont(miFuente);
        addUINode(textoAnillos, 20, 20);

        textoVidas = new Text("Vidas: 3");
        textoVidas.setStyle("-fx-font-size: 24px; -fx-fill: green;");
        textoVidas.setFont(miFuente);
        addUINode(textoVidas, 700, 50);

        textoBasura = new Text("Basura: 0");
        textoBasura.setStyle("-fx-font-size: 24px; -fx-fill: blue;");
        textoBasura.setFont(miFuente);
        addUINode(textoBasura, 20, 50);

        textoPapel = new Text("Papel: 0");
        textoPapel.setStyle("-fx-font-size: 24px; -fx-fill: white;");
        textoPapel.setFont(miFuente);
        addUINode(textoPapel, 20, 80);

        textoTiempo = new Text("Tiempo: 180");
        textoTiempo.setStyle("-fx-font-size: 24px; -fx-fill: orange;");
        textoTiempo.setFont(miFuente);
        addUINode(textoTiempo, 700, 80);

        this.currentProgressBar = null;
        this.currentColorAdjust = null;
    }

    @Override
    public void onUpdate(double tpf) {
        // Lógica de invencibilidad 
        if (invincibilityRemainingTime > 0) {
            invincibilityRemainingTime -= tpf * 1000; // Restar milisegundos

            // Parpadeo por invencibilidad
            blinkTimerAccumulator += tpf * 1000;
            if (blinkTimerAccumulator >= 200) { // Cada 200 ms
                if (invinciblePlayer != null) {
                    invinciblePlayer.getViewComponent().setVisible(!invinciblePlayer.getViewComponent().isVisible());
                }
                blinkTimerAccumulator = 0;
            }

            if (invincibilityRemainingTime <= 0) {
                if (invinciblePlayer != null) {
                    invinciblePlayer.setInvencibilidad(false);
                    invinciblePlayer.getViewComponent().setVisible(true); // Restaurar visibilidad
                }
                invinciblePlayer = null; // Limpiar 
                invincibilityRemainingTime = 0;
                blinkTimerAccumulator = 0; // Resetear el acumulador de parpadeo
            }
        }
    }

    public static void enviarMensaje(String titulo, Connection<Bundle> conexion) {
        Bundle bundle = new Bundle(titulo);
        conexion.send(bundle);
    }

    public static void imprimir(String titulo) {
        System.out.println(titulo);
    }

    //Agrega una barra de progreso
    //Descartado
    public void agregarBarra(float num) {
        if (currentProgressBar != null) {
            getGameScene().getRoot().getChildren().remove(currentProgressBar);
        }

        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(num);
        if (num > 0.75) {
            progressBar.setStyle("-fx-accent: red; -fx-background-color: transparent;");
        } else if (num > 0.5) {
            progressBar.setStyle("-fx-accent: orange; -fx-background-color: transparent;");
        } else {
            progressBar.setStyle("-fx-accent: green; -fx-background-color: transparent;");
        }
        progressBar.setPrefWidth(200);
        progressBar.setPrefHeight(10);
        progressBar.setLayoutX(20);
        progressBar.setLayoutY(30);
        getGameScene().getRoot().getChildren().add(progressBar);
        this.currentProgressBar = progressBar;
    }

    //Aplica efectos de color en la pantalla
    //Descartado
    public void filtroColor(float num) {
        if (currentColorAdjust == null) {
            currentColorAdjust = new ColorAdjust();
            getGameScene().getRoot().setEffect(currentColorAdjust);
        }
        currentColorAdjust.setHue(num * 0.15);
    }

    public static void agregarTexto(String mensaje, String color, int size, int x, int y) {
        Text texto = new Text(mensaje);
        texto.setStyle("-fx-font-size: " + size + "px; -fx-fill: " + color + "; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 1, 1);");
        texto.setFont(Font.font("Impact", size));
        addUINode(texto, x, y);
    }

    public void cambiarTextoAnillos(String mensaje) {
        textoAnillos.setText(mensaje);
    }

    public void cambiarTextoCaucho(String mensaje) {
        textoCaucho.setText(mensaje);
    }

    public void cambiarTextoVidas(String mensaje) {
        textoVidas.setText(mensaje);
    }

    public void cambiarTextoBasura(String mensaje) {
        textoBasura.setText(mensaje);
    }

    public void cambiarTextoPapel(String mensaje) {
        textoPapel.setText(mensaje);
    }

    public void cambiarTextoTiempo(String mensaje) {
        textoTiempo.setText(mensaje);
    }

    public void init() {
        reset(); 
    }

    //REinicia el estado de los textos en juego
    public void reset() {
        textoCaucho.setText("Caucho: 0");
        textoAnillos.setText("Anillos: 0");
        textoVidas.setText("Vidas: 3");
        textoBasura.setText("Basura: 0");
        textoPapel.setText("Papel: 0");
        textoTiempo.setText("Tiempo: 180");

        if (currentProgressBar != null) {
            getGameScene().getRoot().getChildren().remove(currentProgressBar);
            currentProgressBar = null;
        }

        if (currentColorAdjust != null) {
            getGameScene().getRoot().setEffect(null); 
            currentColorAdjust = null;
        }

        // Resetear variables de invencibilidad
        invincibilityRemainingTime = 0.0;
        invinciblePlayer = null;
        blinkTimerAccumulator = 0.0;
    }

    //Activa la invencibilidad y aplica el efecto de parpadeo
    public void activarInvencibilidad(int milisegundos, Player player) {
        player.setInvencibilidad(true);
        this.invincibilityRemainingTime = milisegundos;
        this.invinciblePlayer = player;
        this.blinkTimerAccumulator = 0.0; // Reiniciar el acumulador de parpadeo
    }
}
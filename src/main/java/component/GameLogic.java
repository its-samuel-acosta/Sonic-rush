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

public class GameLogic extends Component implements Serializable {

    private Text textoCaucho;
    private Text textoAnillos;
    private Text textoVidas;
    private Text textoBasura;
    private Text textoPapel;
    private Text textoTiempo;

    private ProgressBar currentProgressBar; 
    private ColorAdjust currentColorAdjust; 

    // Variables para la invencibilidad manual
    private double invincibilityRemainingTime = 0.0; // Tiempo restante de invencibilidad
    private Player invinciblePlayer = null; // Referencia al jugador invencible
    private double blinkTimerAccumulator = 0.0; // Acumulador para el parpadeo

    public GameLogic() {
        textoCaucho = new Text("Caucho: 0");
        textoCaucho.setStyle("-fx-font-size: 28px; -fx-fill: red; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 1, 1);");
        textoCaucho.setFont(Font.font("Impact", 28));
        addUINode(textoCaucho, 20, 110);

        textoAnillos = new Text("Anillos: 0");
        textoAnillos.setStyle("-fx-font-size: 28px; -fx-fill: yellow; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 1, 1);");
        textoAnillos.setFont(Font.font("Impact", 28));
        addUINode(textoAnillos, 20, 20);

        textoVidas = new Text("Vidas: 3");
        textoVidas.setStyle("-fx-font-size: 28px; -fx-fill: green; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 1, 1);");
        textoVidas.setFont(Font.font("Impact", 28));
        addUINode(textoVidas, 700, 50);

        textoBasura = new Text("Basura: 0");
        textoBasura.setStyle("-fx-font-size: 28px; -fx-fill: blue; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 1, 1);");
        textoBasura.setFont(Font.font("Impact", 28));
        addUINode(textoBasura, 20, 50);

        textoPapel = new Text("Papel: 0");
        textoPapel.setStyle("-fx-font-size: 28px; -fx-fill: white; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 1, 1);");
        textoPapel.setFont(Font.font("Impact", 28));
        addUINode(textoPapel, 20, 80);

        textoTiempo = new Text("Tiempo: 180");
        textoTiempo.setStyle("-fx-font-size: 28px; -fx-fill: orange; -fx-effect: dropshadow(gaussian, black, 2, 0.7, 1, 1);");
        textoTiempo.setFont(Font.font("Impact", 28));
        addUINode(textoTiempo, 700, 80);

        this.currentProgressBar = null;
        this.currentColorAdjust = null;
    }

    @Override
    public void onUpdate(double tpf) {
        // Lógica de invencibilidad manual
        if (invincibilityRemainingTime > 0) {
            invincibilityRemainingTime -= tpf * 1000; // Restar milisegundos

            // Lógica de parpadeo manual
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
                    invinciblePlayer.getViewComponent().setVisible(true); // Asegurarse de que sea visible al final
                }
                invinciblePlayer = null; // Limpiar la referencia
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

    /**
     * Agrega o actualiza una barra de progreso en la interfaz de usuario.
     * Gestiona la instancia de la barra de progreso para evitar duplicados.
     * @param num El valor de progreso (entre 0.0 y 1.0).
     */
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

    /**
     * Aplica o actualiza un filtro de color en la escena del juego.
     * Gestiona la instancia del filtro para evitar duplicados.
     * @param num El valor para el tono del color (hue).
     */
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

    /**
     * Restablece todos los elementos de UI gestionados por GameLogic a su estado inicial.
     */
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

    /**
     * Activa la invencibilidad del jugador por un tiempo determinado,
     * incluyendo un efecto de parpadeo.
     * @param milisegundos Duración de la invencibilidad en milisegundos.
     * @param player El objeto Player al que se le aplica la invencibilidad.
     */
    public void activarInvencibilidad(int milisegundos, Player player) {
        player.setInvencibilidad(true);
        this.invincibilityRemainingTime = milisegundos;
        this.invinciblePlayer = player;
        this.blinkTimerAccumulator = 0.0; // Reiniciar el acumulador de parpadeo
    }
}
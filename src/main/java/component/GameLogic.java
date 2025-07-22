package component;

import GameSettings.Player;
import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.net.Connection;
import com.almasb.fxgl.time.TimerAction;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

import static com.almasb.fxgl.dsl.FXGL.*;
import static com.almasb.fxgl.dsl.FXGL.getGameTimer;

public class GameLogic extends Component implements Serializable {

    private Text textoCaucho;
    private Text textoAnillos;
    private Text textoBasuraGlobal; // Ahora es una variable de instancia
    private Text textoVidas;
    private Text textoBasura;
    private Text textoPapel;

    private ProgressBar currentProgressBar; // Para gestionar la barra de progreso
    private ColorAdjust currentColorAdjust; // Para gestionar el efecto de ajuste de color

    public GameLogic() {
        // Inicialización de todos los objetos Text
        textoCaucho = new Text("Caucho: 0");
        textoCaucho.setStyle("-fx-font-size: 24px; -fx-fill: red;");
        textoCaucho.setFont(Font.font("Impact", 24));
        addUINode(textoCaucho, 20, 110);

        textoAnillos = new Text("Anillos: 0");
        textoAnillos.setStyle("-fx-font-size: 24px; -fx-fill: yellow;");
        textoAnillos.setFont(Font.font("Impact", 24));
        addUINode(textoAnillos, 20, 20);

        textoVidas = new Text("Vidas: 3");
        textoVidas.setStyle("-fx-font-size: 24px; -fx-fill: green;");
        textoVidas.setFont(Font.font("Impact", 24));
        addUINode(textoVidas, 700, 50);

        textoBasura = new Text("Basura: 0");
        textoBasura.setStyle("-fx-font-size: 24px; -fx-fill: blue;");
        textoBasura.setFont(Font.font("Impact", 24));
        addUINode(textoBasura, 20, 50);

        textoPapel = new Text("Papel: 0");
        textoPapel.setStyle("-fx-font-size: 24px; -fx-fill: white;");
        textoPapel.setFont(Font.font("Impact", 24));
        addUINode(textoPapel, 20, 80);

        textoBasuraGlobal = new Text("Basura restante: 0"); // Inicializar aquí
        textoBasuraGlobal.setStyle("-fx-font-size: 24px; -fx-fill: white;");
        textoBasuraGlobal.setFont(Font.font("Impact", 24));
        addUINode(textoBasuraGlobal, 700, 20); // Añadirlo al UI

        // Inicializar referencias a null para la barra de progreso y el filtro de color
        this.currentProgressBar = null;
        this.currentColorAdjust = null;
    }

    public static void enviarMensaje(String titulo, Connection<Bundle> conexion) {
        Bundle bundle = new Bundle(titulo);
        conexion.send(bundle);
    }

    public static void SyncPos(@Nullable Player player) {
        Bundle bundle = new Bundle("syncPos");
        bundle.put("x", player.getPosition().getX());
        bundle.put("y", player.getPosition().getY());
        bundle.put("tipo", player.getTipo());
        player.getConexion().send(bundle);
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
        // Eliminar la barra de progreso existente si la hay
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
        this.currentProgressBar = progressBar; // Almacenar la referencia
    }

    /**
     * Aplica o actualiza un filtro de color en la escena del juego.
     * Gestiona la instancia del filtro para evitar duplicados.
     * @param num El valor para el tono del color (hue).
     */
    public void filtroColor(float num) {
        if (currentColorAdjust == null) {
            currentColorAdjust = new ColorAdjust();
            getGameScene().getRoot().setEffect(currentColorAdjust); // Aplicar solo una vez
        }
        currentColorAdjust.setHue(num * 0.15); // Cambia el tono
    }

    // Este método ya no es estático y no debería ser llamado directamente para agregar textos que se gestionan como instancias.
    // Se mantiene para compatibilidad si hay otros usos, pero se recomienda usar las variables de instancia de Text.
    public static void agregarTexto(String mensaje, String color, int size, int x, int y) {
        Text texto = new Text(mensaje);
        texto.setStyle("-fx-font-size: " + size + "px; -fx-fill: " + color + ";");
        texto.setFont(Font.font("Impact", size));
        addUINode(texto, x, y);
    }

    // El método crearTextoGlobal() ya no es necesario como método estático que añade al UI,
    // ya que textoBasuraGlobal se inicializa en el constructor.
    // Si se usaba externamente para crear otros textos globales, su lógica debería ser revisada.
    @SuppressWarnings("unused")
    private static Text crearTextoGlobal() {
        // Este método ya no es necesario ya que textoBasuraGlobal se inicializa en el constructor.
        // Se mantiene como un placeholder si la lógica original lo requería para algo más.
        return new Text(""); // Retorna un texto vacío o null si no es usado.
    }

    public void cambiarTextoBasuraGlobal(String mensaje) {
        textoBasuraGlobal.setText(mensaje);
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

    /**
     * Inicializa la lógica del juego, restableciendo los textos de UI.
     * Se llama al inicio del juego.
     */
    public void init() {
        reset(); // Llama a reset para asegurar que todos los elementos de UI estén en su estado inicial
    }

    /**
     * Restablece todos los elementos de UI gestionados por GameLogic a su estado inicial.
     * Este método se llama cuando el juego regresa al menú principal.
     */
    public void reset() {
        textoCaucho.setText("Caucho: 0");
        textoAnillos.setText("Anillos: 0");
        textoVidas.setText("Vidas: 3");
        textoBasura.setText("Basura: 0");
        textoPapel.setText("Papel: 0");
        textoBasuraGlobal.setText("Basura restante: 0");

        // Eliminar y restablecer la barra de progreso
        if (currentProgressBar != null) {
            getGameScene().getRoot().getChildren().remove(currentProgressBar);
            currentProgressBar = null;
        }

        // Restablecer el filtro de color
        if (currentColorAdjust != null) {
            getGameScene().getRoot().setEffect(null); // Eliminar el efecto del nodo raíz
            currentColorAdjust = null;
        }
    }

    public static void activarInvencibilidad(int milisegundos, Player player) {
        player.setInvencibilidad(true);

        TimerAction blinkAction = getGameTimer().runAtInterval(() -> {
            player.getViewComponent().setVisible(!player.getViewComponent().isVisible());
        }, Duration.millis(200));

        getGameTimer().runOnceAfter(() -> {
            player.setInvencibilidad(false);
            player.getViewComponent().setVisible(true);
            blinkAction.expire();
        }, Duration.millis(milisegundos));
    }
}

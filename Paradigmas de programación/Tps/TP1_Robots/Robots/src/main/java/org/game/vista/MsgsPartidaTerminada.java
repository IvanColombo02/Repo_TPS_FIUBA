package org.game.vista;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import org.game.controlador.Controlador;
import org.game.controlador.Inicializador;
import org.game.sonido.ReproductorSonido;

public class MsgsPartidaTerminada {
    private static final int ESPACIADO_VBOX = 15;
    private static final int ANCHO_VENTANA = 300;
    private static final int ALTO_VENTANA = 150;

    private static final String MSG_SALIR = "Salir del juego";
    private static final String MSG_SIGUINTE = "Pasar al siguiente nivel";
    private static final String MSG_REINICIAR = "Reiniciar nivel";
    private static final String TITULO_FIN = "Fin del juego";
    private static final String TITULO_FELICITACIONES = "¡Felicitaciones!";
    private static final String MSG_PARTIDA_GANADA = "¡Ganaste!";
    private static final String MSG_VOLVER_MENU = "Volver al menú";
    private static final String ESTILO_BTN_VOLVER = "-fx-background-color: blue; -fx-text-fill: white;";
    private static final String ESTILO_BTN_SALIR = "-fx-background-color: red; -fx-text-fill: white;";
    private static final String ESTILO_BTN_SIGUIENTE = "-fx-background-color: green; -fx-text-fill: white;";


    // devuelve un mensage de partida perdida
    public void mostrarPartidaPerdida(Stage ventanaPrincipal, String texto, Controlador controlador) {
        Stage ventana = new Stage();
        ReproductorSonido.detenerBackSound();
        ReproductorSonido.reproducirLossSound();
        mostrarVentana(ventanaPrincipal, controlador, ventana, TITULO_FIN, texto, false);
    }

    // devuelve un mensage de partida ganada
    public void mostrarPartidaGanada(Stage ventanaPrincipal, Controlador controlador) {
        Stage ventana = new Stage();
        ReproductorSonido.detenerBackSound();
        ReproductorSonido.reproducirWinSound();
        mostrarVentana(ventanaPrincipal, controlador, ventana, TITULO_FELICITACIONES, MSG_PARTIDA_GANADA, true);
    }

    private void mostrarVentana(Stage ventanaPrincipal, Controlador controlador, Stage ventana, String titulo, String mensajeVentana, boolean ganada) {
        ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.setTitle(titulo);

        Label mensaje = new Label(mensajeVentana);
        Button volver = new Button(MSG_VOLVER_MENU);
        volver.setStyle(ESTILO_BTN_VOLVER);

        Button btn;
        VBox layout = new VBox(ESPACIADO_VBOX);
        volver.setOnAction(e -> volver(ventanaPrincipal, ventana));
        ventana.setOnCloseRequest(e -> salir(ventana));

        if (!ganada) {
            Button btnReinicio = new Button(MSG_REINICIAR);
            btnReinicio.setStyle(ESTILO_BTN_SIGUIENTE);
            btnReinicio.setOnAction(e -> reiniciar(ventana, controlador));

            btn = new Button(MSG_SALIR);
            btn.setStyle(ESTILO_BTN_SALIR);
            btn.setOnAction(e -> salir(ventana));
            layout.getChildren().addAll(mensaje, volver, btnReinicio, btn);
        } else {
            btn = new Button(MSG_SIGUINTE);
            btn.setStyle(ESTILO_BTN_SIGUIENTE);
            btn.setOnAction(e -> siguiente(ventana, controlador));
            layout.getChildren().addAll(mensaje, volver, btn);
        }

        layout.setAlignment(Pos.CENTER);

        Scene escena = new Scene(layout, ANCHO_VENTANA, ALTO_VENTANA);
        ventana.setScene(escena);
        ventana.showAndWait();
    }

    private void siguiente(Stage ventana, Controlador controlador) {
        ventana.close();
        controlador.siguienteNivel();
    }

    private void reiniciar(Stage ventana, Controlador controlador) {
        ventana.close();
        controlador.reiniciarNivel();
    }

    private void volver(Stage ventanaPrincipal, Stage ventana) {
        ventana.close();
        new Inicializador(ventanaPrincipal).comenzar();
    }

    private void salir(Stage ventana) {
        ventana.close();
        Inicializador.salir();
    }
}
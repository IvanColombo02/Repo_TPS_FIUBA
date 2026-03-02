package org.game.controlador;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.stage.Stage;
import org.game.modelo.Jugador;
import org.game.modelo.Tablero;
import org.game.sonido.ReproductorSonido;
import org.game.vista.Menu;
import org.game.vista.Vista;

public class Inicializador {

    private final Stage stage;

    public Inicializador(Stage stage) {
        this.stage = stage;
    }

    // Inicia el menu
    public void comenzar() {
        Menu menu = new Menu(stage, this);
        menu.mostrarMenu();
    }

    // setea control, modelos, vista
    // cuando se llame al listener, este ejecuta esta funcion para empezar el nuevo nivel
    // uso: cuando inicia el nivel o cuando se gana y pasa al siguiente

    public void nuevoNivel(int filas, int columnas) {
        nuevoNivel(filas, columnas, 1, (filas + columnas) / 8, 1);
    }

    public void nuevoNivel(int filas, int columnas, int nivel, int cantidadRobots, int cantTelSeg) {

        ReproductorSonido.reproducirBackSound();
        Jugador jugador = new Jugador();
        jugador.setTeletransportacionesSegurasRestantes(cantTelSeg);

        // modelos
        Tablero tablero = new Tablero(filas, columnas);
        tablero.setJugador(jugador, filas / 2, columnas / 2);

        // control
        Controlador controlador = new Controlador(tablero, jugador, cantidadRobots, nivel, stage, this);

        // vista
        Vista vista = new Vista(tablero, stage, controlador, nivel);

        //le paso la referencia de vista a controlador tambien
        controlador.setVista(vista);

        // genero los robots para el tablero, lo muestro e inicializo las capturas de teclas en control
        vista.generarRobots(filas, columnas, cantidadRobots);
        vista.mostrarTablero();

        // inicializo los eventos en el controlador
        controlador.inicializarEventos();
    }

    // para salir del juego al cerrar una ventana
    public static void salir() {
        ReproductorSonido.detenerBackSound();
        Platform.exit();
    }
}

package org.game.vista;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.game.controlador.Controlador;
import org.game.modelo.*;

import java.util.Objects;

public class Vista {
    private static final String RUTA_GIF_INCENDIO = "/Images/Explosion/explosion.gif";

    private final Tablero tablero;
    private final VistaTablero vistaTablero;
    private final Stage stage;
    private final Controlador controlador;
    private final Image fuego;
    private final int nroNivel;
    private final GestorVisualRobots vistaRobots;

    public Vista(Tablero tablero, Stage stage, Controlador controlador, int nroNivel) {
        this.tablero = tablero;
        this.controlador = controlador;
        this.stage = stage;
        this.nroNivel = nroNivel;
        this.vistaTablero = new VistaTablero(stage, controlador);
        this.fuego = new Image(Objects.requireNonNull(getClass().getResourceAsStream(RUTA_GIF_INCENDIO)));
        this.vistaRobots = new GestorVisualRobots(this.tablero, this, controlador);
    }

    // metodo para mostrar tablero
    public void mostrarTablero() {
        vistaTablero.actualizarNivel(nroNivel);
        vistaTablero.mostrarTablero(tablero);
        generarDiamantes(tablero.getCantFilas(), tablero.getCantColumnas());
    }

    // Mueve el personaje al siguiente lugar
    public void moverPersonaje(Personaje personaje, int posFilaAnterior, int posColAnterior, int posFilaNueva, int posColNueva) {
        Button posAnterior = vistaTablero.getBoton(posFilaAnterior, posColAnterior);
        Button posNueva = vistaTablero.getBoton(posFilaNueva, posColNueva);
        posAnterior.setGraphic(null);
        vistaTablero.setImagenBoton(posNueva, personaje.getImagen());
    }

    // Mueve los robots hacia el jugador
    public void moverRobots() {
        vistaRobots.accionMoverRobots();
    }

    // Pide al gestor visual de robots que genere los robots
    public void generarRobots(int filas, int columnas, int cantidad) {
        vistaRobots.generarRobots(filas, columnas, cantidad);
    }

    // Pide al gestor visual de diamantes que genere los diamantes
    public void generarDiamantes(int filas, int columnas) {
        GestorVisualDiamantes gestorDiamantes = new GestorVisualDiamantes(filas, columnas, tablero, vistaTablero);
        gestorDiamantes.generarDiamantes();
    }

    /*
     *
     * SECCION GETTERS, SETTERS
     *
     */

    public void setImagenFuego(int fil, int col) {
        vistaTablero.setImagenBoton(vistaTablero.getBoton(fil, col), this.fuego);
    }

    public Label getLabelDiamantes() {
        return vistaTablero.getLabelDiamantes();
    }

    public Stage getStage() {
        return this.stage;
    }

    public Controlador getControlador() {
        return this.controlador;
    }
}

package org.game.controlador;

import javafx.stage.Stage;
import org.game.modelo.Jugador;
import org.game.modelo.Tablero;
import org.game.vista.Vista;
import org.game.vista.NotificadorVistaJugador;

import java.util.Random;

public class AccionesJugador {
    private final Tablero tablero;
    private final Jugador jugador;
    private final Controlador controlador;
    private final NotificadorVistaJugador notificadorVista;

    public AccionesJugador(Tablero tablero, Jugador jugador, Vista vista, Stage stage, Controlador controlador) {
        this.tablero = tablero;
        this.jugador = jugador;
        this.controlador = controlador;
        this.notificadorVista = new NotificadorVistaJugador(vista, stage, controlador);
    }

    // le dice a modelo y vista lo que tienen que hacer
    public boolean accionMoverJugador(int posFilaNueva, int posColNueva) {
        int posFilaAnterior = tablero.getPosFilJugador();
        int posColAnterior = tablero.getPosColJugador();

        tablero.moverJugador(posFilaNueva, posColNueva);
        notificadorVista.moverPersonaje(jugador, posFilaAnterior, posColAnterior, posFilaNueva, posColNueva);

        int cantidadDiamantes = tablero.getCantidadDiamantes();
        notificadorVista.actualizarLabelDiamantes(cantidadDiamantes);

        if (cantidadDiamantes == 0) {
            notificadorVista.mostrarPartidaGanada();
            return false;
        }
        return true;
    }

    // Teletransporta al jugador a una celda random
    public void accionTeletransportacion() {
        int filas = tablero.getCantFilas();
        int columnas = tablero.getCantColumnas();
        Random rand = new Random();
        while (true) {
            int nuevaFila = rand.nextInt(filas);
            int nuevaCol = rand.nextInt(columnas);
            if (tablero.esPosicionValida(nuevaFila, nuevaCol)) {
                intentarMoverJugador(nuevaFila, nuevaCol);
                break;
            }
        }
    }

    // chequea si se puede realizar la teletransportacion segura, en ese caso, lo hace
    public void accionTeletransportacionSegura(int nuevaFila, int nuevaCol) {
        if (sePuedeTeletransportar(nuevaFila, nuevaCol)) {
            controlador.subTeletransportacionesSeguras();
            accionMoverJugador(nuevaFila, nuevaCol);
        }
    }

    // Intenta mover al jugador a una posicion y maneja los casos donde cae en una celda incendiada
    // o en donde haya un robot
    public boolean intentarMoverJugador(int filaNueva, int colNueva) {
        int posFilaAnterior = jugador.getPosFilaJugador();
        int posColAnterior = jugador.getPosColJugador();
        if (tablero.esCeldaIncendiada(filaNueva, colNueva)) {
            tablero.moverJugador(filaNueva, colNueva);
            notificadorVista.moverPersonaje(jugador, posFilaAnterior, posColAnterior, filaNueva, colNueva);
            notificadorVista.mostrarPartidaPerdidaPorIncendio();
            return false;
        }
        if (tablero.getCelda(filaNueva, colNueva).hayRobot()) {
            tablero.moverJugador(filaNueva, colNueva);
            notificadorVista.moverPersonaje(jugador, posFilaAnterior, posColAnterior, filaNueva, colNueva);
            notificadorVista.mostrarPartidaPerdidaPorRobot();
            return false;
        }
        return accionMoverJugador(filaNueva, colNueva);
    }

    // chequea si se puede teletransportar a esa posicion
    private boolean sePuedeTeletransportar(int fila, int col) {
        return tablero.esPosicionValida(fila, col) && !tablero.esCeldaOcupada(fila, col);
    }
}

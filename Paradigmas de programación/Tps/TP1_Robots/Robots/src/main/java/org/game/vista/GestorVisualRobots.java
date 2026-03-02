package org.game.vista;

import org.game.controlador.Controlador;
import org.game.modelo.*;
import org.game.sonido.ReproductorSonido;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.min;

public class GestorVisualRobots {
    private static final String MSG_PERDISTE = "Perdiste :(";

    private final Tablero tablero;
    private final List<Robot> robots;
    private final Vista vista;
    private final Controlador controlador;
    private final MsgsPartidaTerminada muestreoMsgs;

    public GestorVisualRobots(Tablero tablero, Vista vista, Controlador controlador) {
        this.tablero = tablero;
        this.robots = new ArrayList<>();
        this.vista = vista;
        this.controlador = controlador;
        this.muestreoMsgs = new MsgsPartidaTerminada();
    }

    // genera los robots en el tablero
    public void generarRobots(int filas, int columnas, int cantidad) {
        for (int i = 0; i < min(cantidad, filas * columnas - 6); i++) {
            Robot robot;
            if (i < cantidad / 4) {
                robot = new Robot2X();
            } else {
                robot = new Robot1X();
            }
            this.robots.add(robot);

            int jugadorFila = tablero.getPosFilJugador();
            int jugadorCol = tablero.getPosColJugador();

            boolean flag = false;
            while (!flag) {
                int fil = ThreadLocalRandom.current().nextInt(0, filas);
                int col = ThreadLocalRandom.current().nextInt(0, columnas);
                Celda celda = this.tablero.getCelda(fil, col);

                boolean esPosJugador = (fil == jugadorFila && col == jugadorCol);

                if (!celda.estaOcupada() && !esPosJugador) {
                    flag = true;
                    tablero.setRobot(robot, fil, col);
                }
            }
        }
    }

    // Mueve los robots hacia el jugador
    public void accionMoverRobots() {
        for (int i = 0; i < robots.size(); i++) {
            Robot robot = robots.get(i);

            if (tablero.robotFueEliminado(robot)) {
                continue;
            }

            int vFil = tablero.getPosFilRobot(robot);
            int vCol = tablero.getPosColRobot(robot);

            boolean estado = robot.moverHaciaJugador(tablero);
            int nFil = tablero.getPosFilRobot(robot);
            int nCol = tablero.getPosColRobot(robot);

            vista.moverPersonaje(robot, vFil, vCol, nFil, nCol);
            if (estado) {
                mostrarMsgPartidaPerdida();
                return;
            }
            int jugadorFila = tablero.getPosFilJugador();
            int jugadorCol = tablero.getPosColJugador();

            if (nFil == jugadorFila && nCol == jugadorCol) {
                mostrarMsgPartidaPerdida();
                return;
            }

            accionIncendiarCelda(robot, i, nFil, nCol);
        }
    }

    // Se encarga de incendiar la celda y eliminar todos los robots en esta celda
    public void accionIncendiarCelda(Robot robot, int indiceOrigen, int filOrigen, int colOrigen) {
        Celda celda = tablero.getCelda(filOrigen, colOrigen);
        if (celda.estaIncendiada()) {
            setCeldaIncendiada(filOrigen, colOrigen);
            if (tablero.eliminarRobot(robot)) {
                mostrarMsgPartidaGanada();
            }
            return;
        }
        for (int j = 0; j < robots.size(); j++) {
            if (j == indiceOrigen) continue;

            Robot otro = robots.get(j);
            if (tablero.robotFueEliminado(otro)) continue;

            int filOtro = tablero.getPosFilRobot(otro);
            int colOtro = tablero.getPosColRobot(otro);

            if (filOrigen == filOtro && colOrigen == colOtro) {
                ReproductorSonido.reproducirColisionSound();
                setCeldaIncendiada(filOrigen, colOrigen);
                tablero.eliminarRobot(robot);
                tablero.eliminarRobot(otro);
                celda.incendiar();

                if (tablero.todosRobotsEliminados()) {
                    mostrarMsgPartidaGanada();
                }
                return;
            }
        }
    }

    private void mostrarMsgPartidaPerdida() {
        muestreoMsgs.mostrarPartidaPerdida(vista.getStage(), MSG_PERDISTE, controlador);
    }

    private void mostrarMsgPartidaGanada() {
        muestreoMsgs.mostrarPartidaGanada(vista.getStage(), vista.getControlador());
    }

    public void setCeldaIncendiada(int fil, int col) {
        vista.setImagenFuego(fil, col);
    }
}

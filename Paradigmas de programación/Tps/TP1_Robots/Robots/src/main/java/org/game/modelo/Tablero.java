package org.game.modelo;

import org.game.sonido.ReproductorSonido;

import java.util.*;

public class Tablero {
    private final Celda[][] matriz;
    private Jugador jugador;
    private final Map<Personaje, Celda> celdasRobots;
    private final Set<Personaje> robotsEliminados;
    private int cantDiamantes;


    public Tablero(int n, int m) {
        this.matriz = new Celda[n][m];
        for (int fil = 0; fil < matriz.length; fil++) {
            for (int col = 0; col < matriz[0].length; col++) {
                matriz[fil][col] = new Celda(fil, col);
            }
        }
        this.celdasRobots = new HashMap<>();
        this.robotsEliminados = new HashSet<>();
    }

    // Actualiza la posicion del jugador ademas de tambien actualizar
    // la cantidad de diamantes y reproducir dicho sonido
    public void moverJugador(int nuevaPosFila, int nuevaPosCol) {
        Celda posJugadorProx = matriz[nuevaPosFila][nuevaPosCol];
        jugador.mover(posJugadorProx);
        if (posJugadorProx.hayDiamante()) {
            posJugadorProx.eliminarDiamante();
            cantDiamantes--;
            ReproductorSonido.reproducirDiamondSound();
        }
    }

    // Actualiza la posicion del robot y la guarda
    public void moverRobot(Robot robot, int posFilaNuevaRobot, int posColNuevaRobot) {
        Celda celdaActualRobot = getCelda(getPosFilRobot(robot), getPosColRobot(robot));
        Celda proxCeldaRobot = getCelda(posFilaNuevaRobot, posColNuevaRobot);
        celdaActualRobot.eliminarRobot();
        proxCeldaRobot.setRobot(robot);
        this.celdasRobots.put(robot, matriz[posFilaNuevaRobot][posColNuevaRobot]);
    }

    // elimina el robot de la celda
    public boolean eliminarRobot(Robot robot) {
        if (robotFueEliminado(robot)) return false;
        Celda celda = celdasRobots.get(robot);
        if (celda != null) {
            celda.eliminarRobot();
            celdasRobots.remove(robot);
        }
        robotsEliminados.add(robot);
        return todosRobotsEliminados();
    }

    /*
     *
     * SECCION CHEQUEO Y VALIDACIONES
     *
     */

    public boolean todosRobotsEliminados() {
        return celdasRobots.isEmpty();
    }

    // Para que no se vaya de los limites del tablero
    public boolean esPosicionValida(int posFila, int posColumna) {
        return posFila >= 0 && posColumna >= 0 && posFila < matriz.length && posColumna < matriz[0].length;
    }

    public boolean robotFueEliminado(Robot robot) {
        return this.robotsEliminados.contains(robot);
    }

    public boolean hayDiamante(int posFilaNueva, int posColNueva) {
        Celda celda = getCelda(posFilaNueva, posColNueva);
        return celda.hayDiamante();
    }

    public boolean esCeldaOcupada(int fila, int col) {
        return getCelda(fila, col).estaOcupada();
    }

    public boolean esCeldaIncendiada(int fila, int col) {
        return getCelda(fila, col).estaIncendiada();
    }

    /*
     *
     * SECCION SETEO Y GETTERS
     *
     */

    // Setea jugador inicialmente.
    public void setJugador(Jugador jugador, int fil, int col) {
        Celda celda = this.matriz[fil][col];
        this.jugador = jugador;
        this.jugador.setCeldaJugador(celda);
        celda.setJugador(jugador);
    }

    // Setea robots inicialmente.
    public void setRobot(Robot robot, int fil, int col) {
        Celda celda = this.matriz[fil][col];
        celda.setRobot(robot);
        this.celdasRobots.put(robot, celda);
    }

    // Coloca los diamantes en la celda correspodiente
    public void setDiamante(int fila, int col) {
        Celda celda = matriz[fila][col];
        celda.setDiamante();
        this.cantDiamantes++;
    }

    public int getCantidadDiamantes() {
        return cantDiamantes;
    }

    public Celda getCelda(int fil, int col) {
        return this.matriz[fil][col];
    }

    public int getPosFilJugador() {
        return this.jugador.getPosFilaJugador();
    }

    public int getPosColJugador() {
        return this.jugador.getPosColJugador();
    }

    public int getPosFilRobot(Robot robot) {
        return this.celdasRobots.get(robot).getFil();
    }

    public int getPosColRobot(Robot robot) {
        return this.celdasRobots.get(robot).getCol();
    }

    public int getCantFilas() {
        return this.matriz.length;
    }

    public int getCantColumnas() {
        return this.matriz[0].length;
    }

    public Celda[][] getMatriz() {
        return matriz;
    }
}

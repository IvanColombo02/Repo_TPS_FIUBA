package org.game.modelo;

public class Celda {
    private boolean ocupada;
    private boolean incendiada;
    private boolean hayDiamante;
    private final int fil;
    private final int col;
    private Jugador jugador;
    private Robot robot;


    public Celda(int fil, int col) {
        this.fil = fil;
        this.col = col;
        this.ocupada = false;
        this.incendiada = false;
    }

    /*
     *
     * SECCION SETEO Y GETTERS
     *
     */

    public void eliminarDiamante() {
        this.hayDiamante = false;
    }

    public void eliminarJugador() {
        this.jugador = null;
        this.ocupada = false;
    }

    public void eliminarRobot() {
        this.robot = null;
        this.ocupada = false;
    }

    public void setDiamante() {
        this.hayDiamante = true;
        this.ocupada = true;
    }

    public void setJugador(Jugador jugador) {
        this.jugador = jugador;
        this.ocupada = true;
    }

    public void setRobot(Robot robot) {
        this.robot = robot;
        this.ocupada = true;
    }

    public void incendiar() {
        this.incendiada = true;
        this.ocupada = true;
    }

    public Personaje getPersonaje() {
        if (this.jugador != null) {
            return this.jugador;
        }
        return this.robot;
    }

    public boolean hayJugador() {
        return this.jugador != null;
    }

    public boolean hayRobot() {
        return this.robot != null;
    }

    public int getFil() {
        return this.fil;
    }

    public int getCol() {
        return this.col;
    }

    /*
     *
     * SECCION CHEQUEO Y VALIDACIONES
     *
     */

    public boolean estaIncendiada() {
        return this.incendiada;
    }

    public boolean estaOcupada() {
        return this.ocupada;
    }

    public boolean hayDiamante() {
        return hayDiamante;
    }

}

package org.game.modelo;

public class Jugador extends Personaje {
    private static final String RUTA_IMG = "/Images/Player/player.gif";

    private int teletransportacionesSegurasRestantes;
    private Celda celdaJugador;

    public Jugador() {
        super(RUTA_IMG);
    }

    public void mover(Celda celda) {
        celdaJugador.eliminarJugador();
        this.celdaJugador = celda;
        celdaJugador.setJugador(this);
    }

    public void setTeletransportacionesSegurasRestantes(int cantTelSeg) {
        this.teletransportacionesSegurasRestantes = cantTelSeg;
    }

    public void subTeletransportacionesSegurasRestantes() {
        this.teletransportacionesSegurasRestantes--;
    }

    public int getTeletransportacionesSegurasRestantes() {
        return teletransportacionesSegurasRestantes;
    }

    public int getPosFilaJugador() {
        return celdaJugador.getFil();
    }

    public int getPosColJugador() {
        return celdaJugador.getCol();
    }

    public void setCeldaJugador(Celda celda) {
        this.celdaJugador = celda;
    }
}

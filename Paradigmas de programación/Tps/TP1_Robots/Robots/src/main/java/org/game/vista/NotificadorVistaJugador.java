package org.game.vista;

import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.game.modelo.Jugador;
import org.game.controlador.Controlador;

public class NotificadorVistaJugador {

    private static final String MSG_CANT_DIAMANTES_RESTANTES = "Diamantes restantes: %d";
    private static final String MSG_CAIDA_EN_CELDA_INCENDIADA = "Caiste una celda incendiada :(";
    private static final String MSG_CAIDA_EN_CELDA_CON_ROBOT = "Caiste en la celda de un robot :(";

    private final Vista vista;
    private final Stage stage;
    private final Controlador controlador;
    private final MsgsPartidaTerminada muestreoMsgs;

    public NotificadorVistaJugador(Vista vista, Stage stage, Controlador controlador) {
        this.vista = vista;
        this.stage = stage;
        this.controlador = controlador;
        this.muestreoMsgs = new MsgsPartidaTerminada();
    }

    public void moverPersonaje(Jugador jugador, int filaAnt, int colAnt, int filaNueva, int colNueva) {
        vista.moverPersonaje(jugador, filaAnt, colAnt, filaNueva, colNueva);
    }

    public void actualizarLabelDiamantes(int cantidad) {
        Label label = vista.getLabelDiamantes();
        label.setText(String.format(MSG_CANT_DIAMANTES_RESTANTES, cantidad));
    }

    public void mostrarPartidaGanada() {
        muestreoMsgs.mostrarPartidaGanada(stage, controlador);
    }

    public void mostrarPartidaPerdidaPorRobot() {
        muestreoMsgs.mostrarPartidaPerdida(stage, MSG_CAIDA_EN_CELDA_CON_ROBOT, controlador);
    }

    public void mostrarPartidaPerdidaPorIncendio() {
        muestreoMsgs.mostrarPartidaPerdida(stage, MSG_CAIDA_EN_CELDA_INCENDIADA, controlador);
    }
}

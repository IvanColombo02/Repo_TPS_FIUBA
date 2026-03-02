package org.game.controlador;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.game.modelo.Jugador;
import org.game.modelo.Tablero;
import org.game.sonido.ReproductorSonido;
import org.game.vista.Vista;

public class Controlador {

    private final Tablero tablero;
    private Vista vista;
    private final Jugador jugador;
    private final Stage stage;
    private final Inicializador inicializador;
    private final int nivel;
    private final int cantidadRobots;
    private AccionesJugador accionJugador;

    public Controlador(Tablero tablero, Jugador jugador, int cantidadRobots, int nivel, Stage stage, Inicializador inicializador) {
        this.tablero = tablero;
        this.jugador = jugador;
        this.stage = stage;
        this.inicializador = inicializador;
        this.nivel = nivel;
        this.cantidadRobots = cantidadRobots;
    }

    // inicializa la recepcion de teclado para movimiento de jugador
    public void inicializarEventos() {
        stage.getScene().setOnKeyPressed(this::manejarMovimiento);
    }


    // en caso de haber ganado, esta funcion genera un nuevo nivel pasandole el jugador para guardar sus logros
    public void siguienteNivel() {
        pasajeNivel(1, 1, jugador.getTeletransportacionesSegurasRestantes() + 1);
    }

    // en caso de reiniciar nivel, se pasan los mismos parametros que tiene en el momento
    public void reiniciarNivel() {
        pasajeNivel(0, 0, jugador.getTeletransportacionesSegurasRestantes());
    }

    public void pasajeNivel(int incrementNivel, int incrementoRobots, int cantTelSeg) {
        inicializador.nuevoNivel(
                tablero.getCantFilas(),
                tablero.getCantColumnas(),
                nivel + incrementNivel,
                this.cantidadRobots + incrementoRobots,
                cantTelSeg
        );
    }

    public void realizarTeletransportacion() {
        accionJugador.accionTeletransportacion();
        ReproductorSonido.reproducirTeleportSound();
    }

    public void realizarTeletransportacionSegura(int nuevaFila, int nuevaCol) {
        accionJugador.accionTeletransportacionSegura(nuevaFila, nuevaCol);
        ReproductorSonido.reproducirTeleportSound();
    }

    // lee los eventos de teclado para poder actualizar el movimiento del jugador
    private void manejarMovimiento(KeyEvent evento) {
        KeyCode tecla = evento.getCode();
        int posFilaNueva = modificarFila(tecla);
        int posColNueva = modificarCol(tecla);

        int posFilaJugador = tablero.getPosFilJugador();
        int posColJugador = tablero.getPosColJugador();

        if (!esPosicionValida(posFilaNueva, posColNueva) ||
                (posFilaJugador == posFilaNueva && posColJugador == posColNueva)) {
            return;
        }

        if (posicionCambia(posFilaNueva, posColNueva) || tablero.hayDiamante(posFilaNueva, posColNueva)) {
            boolean movimientoExitoso = accionJugador.intentarMoverJugador(posFilaNueva, posColNueva);
            if (!movimientoExitoso) return;
            ReproductorSonido.reproducirMovSound();
            vista.moverRobots();
        }
    }

    /*
     *
     * SECCION DE CHEQUEO Y VALIDACIONES
     *
     */

    private boolean esPosicionValida(int posFilaNueva, int posColNueva) {
        // chequea si no se va fuera de los limites del tablero
        return tablero.esPosicionValida(posFilaNueva, posColNueva);
    }

    // chequea si la celda es accesible
    private boolean posicionCambia(int posFilaNueva, int posColNueva) {
        int posFilaJugador = tablero.getPosFilJugador();
        int posColJugador = tablero.getPosColJugador();
        return posFilaNueva != posFilaJugador || posColNueva != posColJugador;
    }

    /*
     *
     * SECCION SETEO Y GETTERS
     *
     */

    public void setVista(Vista vista) {
        this.vista = vista;
        this.accionJugador = new AccionesJugador(tablero, jugador, vista, stage, this);
    }

    public int getCantTltransSeguras() {
        return this.jugador.getTeletransportacionesSegurasRestantes();
    }

    public void subTeletransportacionesSeguras() {
        this.jugador.subTeletransportacionesSegurasRestantes();
    }

    /*
     *
     * SECCION MAPEO DE TECLAS
     *
     */

    // modifica segun la tecla para la fila
    private int modificarFila(KeyCode tecla) {
        int posFilaNueva = tablero.getPosFilJugador();
        if (tecla == KeyCode.NUMPAD8 || tecla == KeyCode.W) posFilaNueva--;
        if (tecla == KeyCode.NUMPAD2 || tecla == KeyCode.S) posFilaNueva++;
        if (tecla == KeyCode.NUMPAD7 || tecla == KeyCode.Q) posFilaNueva--;
        if (tecla == KeyCode.NUMPAD9 || tecla == KeyCode.E) posFilaNueva--;
        if (tecla == KeyCode.NUMPAD1 || tecla == KeyCode.Z) posFilaNueva++;
        if (tecla == KeyCode.NUMPAD3 || tecla == KeyCode.X) posFilaNueva++;
        return posFilaNueva;
    }

    // modifica segun la tecla para la columna
    private int modificarCol(KeyCode tecla) {
        int posColNueva = tablero.getPosColJugador();
        if (tecla == KeyCode.NUMPAD4 || tecla == KeyCode.A) posColNueva--;
        if (tecla == KeyCode.NUMPAD6 || tecla == KeyCode.D) posColNueva++;
        if (tecla == KeyCode.NUMPAD7 || tecla == KeyCode.Q) posColNueva--;
        if (tecla == KeyCode.NUMPAD9 || tecla == KeyCode.E) posColNueva++;
        if (tecla == KeyCode.NUMPAD3 || tecla == KeyCode.X) posColNueva++;
        if (tecla == KeyCode.NUMPAD1 || tecla == KeyCode.Z) posColNueva--;
        return posColNueva;
    }

}

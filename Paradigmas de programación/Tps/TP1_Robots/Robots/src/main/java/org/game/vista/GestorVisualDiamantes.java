package org.game.vista;

import javafx.scene.image.Image;
import org.game.modelo.Celda;
import org.game.modelo.Tablero;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class GestorVisualDiamantes {
    private static final String RUTA_GIF_DIAMANTE = "/Images/Diamond/diamond.gif";
    private static final int CANT_DIAMANTES_INICIALES = 4;
    private static final int MIN_DISTANCIA_DIAMANTES = 9;

    private final int filas;
    private final int columnas;
    private final Tablero tablero;
    private final VistaTablero vistaTablero;
    private final Image diamante;

    public GestorVisualDiamantes(int filas, int columnas, Tablero tablero, VistaTablero vistaTablero) {
        this.filas = filas;
        this.columnas = columnas;
        this.tablero = tablero;
        this.vistaTablero = vistaTablero;
        this.diamante = new Image(Objects.requireNonNull(getClass().getResourceAsStream(RUTA_GIF_DIAMANTE)));
    }

    // Genera los diamantes en el tablero
    public void generarDiamantes() {
        ArrayList<int[]> posicionesDiamantes = new ArrayList<>();
        while (posicionesDiamantes.size() < CANT_DIAMANTES_INICIALES) {

            int fila = ThreadLocalRandom.current().nextInt(0, filas);
            int columna = ThreadLocalRandom.current().nextInt(0, columnas);
            Celda celda = this.tablero.getCelda(fila, columna);

            if (celda.estaOcupada()) continue;

            boolean demasiadoCerca = false;
            for (int[] pos : posicionesDiamantes) {
                int distancia2 = calcularDistanciaCuadrada(fila, columna, pos);
                if (distancia2 < MIN_DISTANCIA_DIAMANTES) {
                    demasiadoCerca = true;
                    break;
                }
            }

            if (!demasiadoCerca) {
                posicionesDiamantes.add(new int[]{fila, columna});
                tablero.setDiamante(fila, columna);
                vistaTablero.setImagenBoton(vistaTablero.getBoton(fila, columna), diamante);
            }
        }
    }

    // Para evitar que los diamantes esten dentro de un cuadrado de menos de 3 celdas
    private int calcularDistanciaCuadrada(int fila, int columna, int[] posicion) {
        int df = fila - posicion[0];
        int dc = columna - posicion[1];
        return df * df + dc * dc;
    }
}

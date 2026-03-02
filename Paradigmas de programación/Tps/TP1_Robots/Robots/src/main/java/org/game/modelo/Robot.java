package org.game.modelo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public abstract class Robot extends Personaje {
    private final int cantidadDeMovimientos;

    protected Robot(String imagen, int cantidadDeMovimientos) {
        super(imagen);
        this.cantidadDeMovimientos = cantidadDeMovimientos;
    }

    // Devuelve true si el robot "come" al jugador.
    public boolean moverHaciaJugador(Tablero tablero) {
        for (int i = 0; i < this.cantidadDeMovimientos; i++) {
            if (moverUnPaso(tablero)) {
                return true;
            }
        }
        return false;
    }

    // Metodo que mueve un solo paso en dirección al jugador
    private boolean moverUnPaso(Tablero tablero) {
        Point nueva = hallarNuevaPosicion(tablero);
        Celda nuevaCelda = tablero.getCelda(nueva.x, nueva.y);

        if (nuevaCelda.hayJugador()) {
            tablero.moverRobot(this, nueva.x, nueva.y);
            return true;
        }

        tablero.moverRobot(this, nueva.x, nueva.y);
        return false;
    }

    // Metodo para hallar una posicion cuando se encuentre en el caso
    // de encontrarse un diamante o simplemente para moverse hacia el jugador
    private Point hallarNuevaPosicion(Tablero tablero) {
        int fil = tablero.getPosFilRobot(this);
        int col = tablero.getPosColRobot(this);

        Point[] prioridad = getPoints(tablero, fil, col); // Dependiendo el caso, devuelve el mejor movimiento

        for (Point destino : prioridad) {
            int nuevaFil = destino.x;
            int nuevaCol = destino.y;

            if (!tablero.esPosicionValida(nuevaFil, nuevaCol)) continue;

            Celda celdaObjetivo = tablero.getCelda(nuevaFil, nuevaCol);

            if (celdaObjetivo.hayJugador() || !celdaObjetivo.hayDiamante()) {
                return new Point(nuevaFil, nuevaCol);
            }
        }

        return new Point(fil, col); // No pudo moverse
    }

    // Obtiene los puntos posibles para cada caso
    private Point[] getPoints(Tablero tablero, int fil, int col) {
        int jugadorFil = tablero.getPosFilJugador();
        int jugadorCol = tablero.getPosColJugador();

        int difFila = Integer.compare(jugadorFil, fil);
        int difCol = Integer.compare(jugadorCol, col);

        Point directo = new Point(fil + difFila, col + difCol);
        List<Point> direcciones = new ArrayList<>();
        direcciones.add(directo);

        // Alternativas dependiendo de la dirección
        if (difFila == 0 && difCol != 0) {
            direcciones.add(new Point(fil + 1, col + difCol));
            direcciones.add(new Point(fil - 1, col + difCol));
        } else if (difCol == 0 && difFila != 0) {
            direcciones.add(new Point(fil + difFila, col + 1));
            direcciones.add(new Point(fil + difFila, col - 1));
        }

        // Agrega más opciones en caso de que las anteriores no funcionen
        // Esto evita que los robots se queden quietos cuando el jugador
        // esta en la misma fila o columna que el robot y de por medio hay
        // un diamante
        direcciones.add(new Point(fil + difFila, col));
        direcciones.add(new Point(fil, col + difCol));
        direcciones.add(new Point(fil - 1, col));
        direcciones.add(new Point(fil + 1, col));
        direcciones.add(new Point(fil, col - 1));
        direcciones.add(new Point(fil, col + 1));

        return direcciones.toArray(new Point[0]);
    }

}

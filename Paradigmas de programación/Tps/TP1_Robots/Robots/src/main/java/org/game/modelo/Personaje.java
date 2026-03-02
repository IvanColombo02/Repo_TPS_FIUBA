package org.game.modelo;

import javafx.scene.image.Image;

import java.util.Objects;

public abstract class Personaje {
    private final Image imagen;

    protected Personaje(String imagen) {
        this.imagen = new Image(Objects.requireNonNull(Personaje.class.getResourceAsStream(imagen)));
    }

    public Image getImagen() {
        return imagen;
    }
}

package org.game;

import javafx.application.Application;
import javafx.stage.Stage;
import org.game.controlador.Inicializador;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        new Inicializador(stage).comenzar();
    }

    public static void main(String[] args) {
        launch();
    }

}
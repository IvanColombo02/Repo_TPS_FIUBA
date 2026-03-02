package org.game.vista;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.game.controlador.Inicializador;

import java.util.Objects;

public class Menu {
    private static final int MIN_DIMENSION = 8;
    private static final int MAX_DIMENSION = 80;
    private static final int TAM_VBOX_TIT_SUBTIT = 10;
    private static final int TAM_VBOX_MENU = 15;
    private static final int TAM_PADDING = 20;
    private static final int ALTO_VENTANA_MENU = 600;
    private static final int ANCHO_VENTANA_MENU = 600;
    private static final int NUM_FONT_SUBTITULO = 20;
    private static final int WIDTH_LOGO = 175;
    private static final int WIDTH_TITULO = 250;
    private static final String DIMENSION_PREDEFINIDA = "15";
    private static final String MSG_SUBTITULO = "por NullPointer Bros";
    private static final String FONT_SUBTITULO = "Segoe UI";
    private static final String MSG_NRO_FILAS = "Nº de filas:";
    private static final String MSG_NRO_COLUMNAS = "Nº de columnas:";
    private static final String MSG_BTN_SALIR = "SALIR";
    private static final String MSG_BTN_COMENZAR = "COMENZAR";
    private static final String MSG_TITULO = "Menú";
    private static final String MSG_TITULO_ALERTA = "Alerta!";
    private static final String RUTA_IMG_LOGO = "/Images/Background/eye.gif";
    private static final String RUTA_IMG_TITULO = "/Images/Background/robots.png";
    private static final String ESTILO_BTN = "-fx-background-color: red; -fx-text-fill: white; -fx-font-weight: bold;";
    private static final String ESTILO_LABEL_ENTRADA = "-fx-background-color: #222; -fx-text-fill: white;";
    private static final String MSG_ALERTA_NUM_INVALIDO = "Por favor, ingresá números válidos.";
    private static final String MSG_FUERA_DE_RANGO = "Recordá que el nro. de columnas y filas debe estar comprendido entre 8 y 80.";


    private final Inicializador inicializador;
    private final Stage stage;


    public Menu(Stage stage, Inicializador inicializador) {
        this.stage = stage;
        this.inicializador = inicializador;
        mostrarMenu();
    }

    public void mostrarMenu() {
        // Título principal
        ImageView logoView = crearImageMenu(RUTA_IMG_LOGO, WIDTH_LOGO);
        ImageView tituloView = crearImageMenu(RUTA_IMG_TITULO, WIDTH_TITULO);

        // Subtítulo
        Label labelSubtitulo = new Label(MSG_SUBTITULO);
        labelSubtitulo.setFont(Font.font(FONT_SUBTITULO, FontWeight.NORMAL, NUM_FONT_SUBTITULO));
        labelSubtitulo.setTextFill(Color.WHITE);

        // Labels e Inputs
        Label labelFil = crearLabelDatos(MSG_NRO_FILAS);
        Label labelCol = crearLabelDatos(MSG_NRO_COLUMNAS);

        TextField entradaFil = crearEntrada();
        TextField entradaCol = crearEntrada();

        // Botón iniciar
        Button iniciar = getButton(entradaFil, entradaCol);

        // Botón salir
        Button salir = new Button(MSG_BTN_SALIR);
        salir.setStyle(ESTILO_BTN);
        salir.setOnAction(e -> Inicializador.salir());

        // VBox para título y subtítulo
        VBox tituloBox = new VBox(TAM_VBOX_TIT_SUBTIT, logoView, tituloView, labelSubtitulo);
        tituloBox.setAlignment(Pos.CENTER);

        // VBox principal del menú
        VBox menu = new VBox(TAM_VBOX_MENU, tituloBox, labelFil, entradaFil, labelCol, entradaCol, iniciar, salir);
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(TAM_PADDING));
        menu.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        Scene menuScene = new Scene(menu, ALTO_VENTANA_MENU, ANCHO_VENTANA_MENU);
        stage.setScene(menuScene);
        stage.setTitle(MSG_TITULO);
        stage.show();
    }

    private Button getButton(TextField entradaFil, TextField entradaCol) {
        Button iniciar = new Button(MSG_BTN_COMENZAR);
        iniciar.setStyle(ESTILO_BTN);
        iniciar.setOnAction(e -> {
            String sfilas = entradaFil.getText();
            String scolumnas = entradaCol.getText();

            try {
                int filas = Integer.parseInt(sfilas);
                int columnas = Integer.parseInt(scolumnas);

                if (filas < MIN_DIMENSION || filas > MAX_DIMENSION || columnas < MIN_DIMENSION || columnas > MAX_DIMENSION) {
                    mostrarAlerta(MSG_FUERA_DE_RANGO);
                    return;
                }

                inicializador.nuevoNivel(filas, columnas);
            } catch (NumberFormatException ex) { // Por si se ingresa algo que no sean números.
                mostrarAlerta(MSG_ALERTA_NUM_INVALIDO);
            }
        });
        return iniciar;
    }

    private ImageView crearImageMenu(String rutaImagen, int width) {
        Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream(rutaImagen)));
        ImageView imgView = new ImageView(img);
        imgView.setFitWidth(width);
        imgView.setPreserveRatio(true);
        return imgView;
    }

    private Label crearLabelDatos(String msgLabel) {
        Label label = new Label(msgLabel);
        label.setTextFill(Color.WHITE);
        return label;
    }

    private TextField crearEntrada() {
        TextField entrada = new TextField(DIMENSION_PREDEFINIDA);
        entrada.setStyle(ESTILO_LABEL_ENTRADA);
        return entrada;
    }

    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(MSG_TITULO_ALERTA);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}

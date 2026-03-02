package org.game.vista;

import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.game.controlador.Controlador;
import org.game.modelo.Celda;
import org.game.modelo.Tablero;

public class VistaTablero {
    private static final String TEXTO_CANT_TELETRANSPORT_SEG = "Teletransportarse con seguridad (Quedan: %d)";
    private static final String TEXTO_CANT_DIAMANTES = "Diamantes restantes: 4";
    private static final String ESTILO_COLOR_FONDO = "-fx-background-color: #1F1F1F;";
    private static final String TITULO_INTERFAZ = "Robots by NullPointer Bros";
    private static final int TABLERO_WIDHT = 800;
    private static final int TABLERO_HEIGHT = 800;
    private static final int VENTANA_WIDTH = 300;
    private static final int VENTANA_HEIGHT = 300;
    private static final String TEXT_BTN_TELETRANSPORTE = "Teletransportarse";
    private static final String TEXT_NIVEL = "Nivel";
    private static final String TEXT_NUM_NIVEL = "0";
    private static final String ESTILO_COLOR_NRO_NIVEL = "-fx-font-size: 24px; -fx-text-fill: #FFFFFF; -fx-font-weight: bold;";
    private static final double TAM_VBOX_DIAMANT_NIVEL = 5;
    private static final double PADDING_NIVEL = 10;
    private static final double TAM_HBOX_CONTENEDOR_BTN = 20;
    private static final double PADDING_CONTENEDOR = 10;
    private static final double HEIGHT_BTN_TELETRANSPORT_AZAR = 50;
    private static final double HEIGHT_BTN_TELETRANSPORT_SEG = 50;
    private static final String ESTILO_HOVER_BORDES_BTN = "-fx-border-color: #005C9D; -fx-border-width: 3px; -fx-effect: dropshadow(gaussian, #005C9D, 5, 0, 0, 2);";
    private static final String ESTILO_BOTON_BASE = "-fx-background-color: linear-gradient(#8DBE3C, #5A9B35); " +
            "-fx-border-color: #2D6A2F; " +
            "-fx-border-width: 3px; " +
            "-fx-border-radius: 8px; " +
            "-fx-background-radius: 8px; " +
            "-fx-font-family: 'Press Start 2P', sans-serif; " +
            "-fx-font-size: " + "16px" + "; " +
            "-fx-padding: 8 12 8 12; " +
            "-fx-text-fill: #FFFFFF; " +
            "-fx-effect: dropshadow(gaussian, #000000, 5, 0, 0, 2);";
    private static final String FUENTE_TEXT_NIV_DIAM = "Arial";

    private final double TAM_CELDA = 40;
    private Button[][] botones;
    private final Stage stage;
    private final Controlador controlador;
    private boolean modoSeleccionCelda = false;
    private Button refCantTeletransportacionSegura;
    private Label nivelLabel;
    private Label labelDiamantes;

    public VistaTablero(Stage stage, Controlador controlador) {
        this.stage = stage;
        this.controlador = controlador;
    }

    /*
     *
     * SECCION SETEO DE TABLERO Y AJUSTES
     *
     */

    // genera un tablero con un button por celda y sus modificaciones
    public void mostrarTablero(Tablero tablero) {
        Celda[][] matriz = tablero.getMatriz();
        int filas = matriz.length;
        int columnas = matriz[0].length;
        botones = new Button[filas][columnas];

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);

        for (int fil = 0; fil < filas; fil++) {
            for (int col = 0; col < columnas; col++) {
                botones[fil][col] = crearBotonCelda(fil, col, matriz[fil][col]);
                grid.add(botones[fil][col], col, fil);
            }
        }

        StackPane tableroEscalable = ajustarInterfaz(columnas, filas, grid);
        HBox panelBotones = crearPanelBotonesAccion();

        Region colorFondo = new Region();
        colorFondo.setStyle(ESTILO_COLOR_FONDO);

        StackPane rootStack = new StackPane();
        rootStack.getChildren().addAll(colorFondo);

        BorderPane contenido = new BorderPane();
        contenido.setCenter(tableroEscalable);
        contenido.setBottom(panelBotones);
        BorderPane.setAlignment(tableroEscalable, Pos.CENTER);
        BorderPane.setAlignment(panelBotones, Pos.CENTER);

        rootStack.getChildren().add(contenido);

        Scene scene = new Scene(rootStack, TABLERO_WIDHT, TABLERO_HEIGHT);
        colorFondo.prefWidthProperty().bind(scene.widthProperty());
        colorFondo.prefHeightProperty().bind(scene.heightProperty());

        scene.widthProperty().addListener((obs, o, n) -> ajustarBotones(panelBotones, scene));
        scene.heightProperty().addListener((obs, o, n) -> ajustarBotones(panelBotones, scene));

        stage.setScene(scene);
        stage.setTitle(TITULO_INTERFAZ);
        stage.setResizable(true);
        stage.show();
        stage.setMinWidth(VENTANA_WIDTH);
        stage.setMinHeight(VENTANA_HEIGHT);
    }

    // Ajusta la interfaz para que sea dinamico junto con el tablero a la hora de maximixar o minimizar la interfaz
    private StackPane ajustarInterfaz(int columnas, int filas, GridPane grid) {
        StackPane container = new StackPane();
        container.setAlignment(Pos.CENTER);
        container.getChildren().add(grid);
        container.setMinSize(0, 0);
        container.widthProperty().addListener((obs, o, n) -> scaleGrid(container, grid, columnas, filas));
        container.heightProperty().addListener((obs, o, n) -> scaleGrid(container, grid, columnas, filas));
        return container;
    }

    // Para mantener a escala el tablero
    private void scaleGrid(StackPane container, GridPane grid, int columnas, int filas) {
        double cellWidth = TAM_CELDA * columnas;
        double cellHeight = TAM_CELDA * filas;
        double scaleX = container.getWidth() / cellWidth;
        double scaleY = container.getHeight() / cellHeight;
        double scale = Math.min(scaleX, scaleY);
        grid.setScaleX(scale);
        grid.setScaleY(scale);
    }

    /*
     *
     * SECCION SETEO DE IMAGEN, ESTILO DE BUTTON Y AJUSTES
     *
     */

    // Setea la imagen en la celda
    public void setImagenBoton(Button boton, Image img) {
        ImageView view = new ImageView(img);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        view.setCache(true);
        view.setFitWidth(TAM_CELDA);
        view.setFitHeight(TAM_CELDA);
        boton.setGraphic(view);
    }

    // Devuelve el btn pedido
    public Button getBoton(int fila, int columna) {
        return botones[fila][columna];
    }

    // Activa el modo de seleccion de celda cuando tiene teletransportaciones seguras
    private void activarModoSeleccionCelda() {
        this.modoSeleccionCelda = true;
        for (Button[] fila : botones) {
            for (Button btn : fila) {
                btn.setCursor(Cursor.HAND);
            }
        }
    }


    // Desactiva el modo seleccion de celda con teletransportacion segura
    private void desactivarModoSeleccionCelda() {
        this.modoSeleccionCelda = false;
        for (Button[] fila : botones) {
            for (Button btn : fila) {
                btn.setCursor(Cursor.DEFAULT);
            }
        }
    }

    // Botones de teletransportación
    private HBox crearPanelBotonesAccion() {
        Button btnAzar = crearBotonAccion(TEXT_BTN_TELETRANSPORTE, controlador::realizarTeletransportacion);
        Button btnSegura = crearBotonAccion(
                String.format(TEXTO_CANT_TELETRANSPORT_SEG, controlador.getCantTltransSeguras()),
                () -> {
                    if (controlador.getCantTltransSeguras() > 0) {
                        activarModoSeleccionCelda();
                    }
                }
        );
        this.refCantTeletransportacionSegura = btnSegura; // Para poder cambiar el número

        // Label de diamantes restantes
        this.labelDiamantes = new Label(TEXTO_CANT_DIAMANTES);
        setFuenteBordeada(this.labelDiamantes);

        // Crear el Label de nivel
        Label labelNivel = new Label(TEXT_NIVEL);
        setFuenteBordeada(labelNivel);

        // Crear el contador de nivel
        nivelLabel = new Label(TEXT_NUM_NIVEL);
        nivelLabel.setStyle(ESTILO_COLOR_NRO_NIVEL);

        // Organizar los elementos en un VBox (nivel encima de los botones)
        VBox panelNivel = new VBox(TAM_VBOX_DIAMANT_NIVEL, labelDiamantes, labelNivel, nivelLabel);
        panelNivel.setAlignment(Pos.CENTER);
        panelNivel.setPadding(new Insets(PADDING_NIVEL));

        // Crear el HBox para los botones
        HBox contenedor = new HBox(TAM_HBOX_CONTENEDOR_BTN, btnAzar, panelNivel, btnSegura);
        contenedor.setAlignment(Pos.CENTER);
        contenedor.setPadding(new Insets(PADDING_CONTENEDOR));
        contenedor.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnAzar, Priority.ALWAYS);
        HBox.setHgrow(btnSegura, Priority.ALWAYS);
        btnAzar.setMaxWidth(Double.MAX_VALUE);
        btnSegura.setMaxWidth(Double.MAX_VALUE);
        btnAzar.setPrefHeight(HEIGHT_BTN_TELETRANSPORT_AZAR);
        btnSegura.setPrefHeight(HEIGHT_BTN_TELETRANSPORT_SEG);
        return contenedor;
    }

    // Setea la fuente bordeada para nivel y cantidad de diamantes
    public void setFuenteBordeada(Label label) {
        DropShadow ds = new DropShadow();
        ds.setOffsetX(0);
        ds.setOffsetY(0);
        ds.setColor(Color.BLACK);
        ds.setRadius(2);

        label.setTextFill(Color.WHITE);
        label.setFont(Font.font(FUENTE_TEXT_NIV_DIAM, FontWeight.BOLD, 16));
        label.setEffect(ds);
    }

    // Metodo para actualizar el nivel en la vista
    public void actualizarNivel(int nuevoNivel) {
        Platform.runLater(() -> {
            nivelLabel.setText(String.valueOf(nuevoNivel));
        });
    }


    // Crea los btn de cada celda con un action activo para recibir instrucciones durante ejecucion
    private Button crearBotonAccion(String texto, Runnable accion) {
        Button btn = new Button(texto);
        btn.setOnAction(e -> accion.run());
        estiloBoton(btn);
        return btn;
    }

    //  Setea el estilo de los botones de teletransportacion
    private void estiloBoton(Button btn) {
        String buttonStyle = getEstiloBotonBase();
        btn.setStyle(buttonStyle);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(150), btn);
        scaleTransition.setByX(0.1);
        scaleTransition.setByY(0.1);
        scaleTransition.setCycleCount(2);
        scaleTransition.setAutoReverse(true);
        btn.addEventHandler(ActionEvent.ACTION, e -> scaleTransition.play());

        // Estilo de hover con bordes más suaves y color más clásico
        btn.setOnMouseEntered(e -> btn.setStyle(buttonStyle + ESTILO_HOVER_BORDES_BTN));
        btn.setOnMouseExited(e -> btn.setStyle(buttonStyle));
    }

    // Devuelve el estilo de los botones de teletransportacion
    private String getEstiloBotonBase() {
        return ESTILO_BOTON_BASE;
    }

    // ajusta la dimension de los botones al minimizar o maximizar el tablero
    private void ajustarBotones(HBox contenedorBotones, Scene scene) {
        // Seccion predefinida para ajustar la dimension
        double minDimension = Math.min(scene.getWidth(), scene.getHeight());
        double padding = Math.max(5, minDimension * 0.015);
        contenedorBotones.setPadding(new Insets(padding));
        contenedorBotones.setSpacing(padding);
        double buttonHeight = Math.max(24, minDimension / 14);
        double buttonWidth = Math.max(80, minDimension / 3.5);

        contenedorBotones.getChildren().stream()
                .filter(node -> Button.class.isAssignableFrom(node.getClass()))
                .map(node -> (Button) node)
                .forEach(btn -> {
                    btn.setPrefWidth(buttonWidth);
                    btn.setPrefHeight(buttonHeight);
                });
    }

    // Crea cada celda del tablero
    private Button crearBotonCelda(int fila, int columna, Celda celda) {
        Button boton = new Button();
        boton.setMaxSize(TAM_CELDA, TAM_CELDA);
        boton.setMinSize(TAM_CELDA, TAM_CELDA);
        boton.setFocusTraversable(false);
        boton.setPadding(Insets.EMPTY);
        GridPane.setFillWidth(boton, true);
        GridPane.setFillHeight(boton, true);

        Color color = (fila + columna) % 2 == 0 ? Color.LIGHTGRAY : Color.DARKGRAY;
        boton.setBackground(new Background(new BackgroundFill(color, null, null)));

        if (celda.estaOcupada()) {
            setImagenBoton(boton, celda.getPersonaje().getImagen());
        }

        // para cuando se activa la seleccion de celda
        boton.setOnAction(e -> {
            if (modoSeleccionCelda) {
                controlador.realizarTeletransportacionSegura(fila, columna);
                desactivarModoSeleccionCelda();
                actualizarBotonTeletransportacionSegura();
            }
        });
        return boton;
    }

    // actualiza la cantidad de teletransportaciones seguras mientras se juega
    private void actualizarBotonTeletransportacionSegura() {
        this.refCantTeletransportacionSegura.setText(String.format(TEXTO_CANT_TELETRANSPORT_SEG, controlador.getCantTltransSeguras()));
    }

    public Label getLabelDiamantes() {
        return this.labelDiamantes;
    }
}

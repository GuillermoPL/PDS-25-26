package es.um.pds.tableros.infrastructure.javafx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Insets;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.ports.input.board.BoardService;
import es.um.pds.tableros.domain.ports.input.board.commands.CambiarBloqueoBoardCommand;
import es.um.pds.tableros.domain.ports.input.card.CardService;
import es.um.pds.tableros.domain.ports.input.card.commands.MoverCardCommand;
import es.um.pds.tableros.infrastructure.rest.dto.BoardDTO;
import es.um.pds.tableros.infrastructure.rest.dto.CardDTO;
import es.um.pds.tableros.infrastructure.mappers.BoardMapper;
import es.um.pds.tableros.infrastructure.mappers.CardMapper;
import es.um.pds.tableros.infrastructure.javafx.SceneManager;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BoardViewController {

    // ── Constantes de "sin filtro" ─────────────────────────────────────────────
    private static final String SIN_FILTRO_NOMBRE = "Todas";
    private static final String SIN_FILTRO_COLOR  = "Todos";

    // ── Dependencias inyectadas por Spring ────────────────────────────────────
    private final BoardService       boardService;
    private final CardService        cardService;
    private final BoardMapper        boardMapper;
    private final CardMapper         cardMapper;
    private final ApplicationContext springContext;
    private final SceneManager       sceneManager;

    // ── Nodos FXML ─────────────────────────────────────────────────────────────
    @FXML private Label              lblTituloTablero;
    @FXML private Button             btnBloqueo;
    @FXML private HBox               hboxColumnas;
    @FXML private ComboBox<String>   cbFiltroEtiquetas; // filtra por nombre
    @FXML private ComboBox<String>   cbFiltroColor;     // filtra por color hex

    // ── Estado ────────────────────────────────────────────────────────────────
    private String  boardIdActual;
    private boolean estadoBloqueoActual;
    private String  filtroNombreActual = SIN_FILTRO_NOMBRE;
    private String  filtroColorActual  = SIN_FILTRO_COLOR;

    // ── Constructor ───────────────────────────────────────────────────────────
    public BoardViewController(BoardService boardService, CardService cardService,
                               BoardMapper boardMapper, CardMapper cardMapper,
                               ApplicationContext springContext, SceneManager sceneManager) { // <--- NUEVO: Inyectamos
        this.boardService  = boardService;
        this.cardService   = cardService;
        this.boardMapper   = boardMapper;
        this.cardMapper    = cardMapper;
        this.springContext = springContext;
        this.sceneManager  = sceneManager;
    }

    // ── Punto de entrada desde SceneManager ───────────────────────────────────
    public void inicializarTablero(String boardId) {
        this.boardIdActual = boardId;
        configurarCeldaColor(); // configura la cellFactory UNA sola vez
        renderizarTodo();
    }

    // ── Renderizado principal ─────────────────────────────────────────────────
    private void renderizarTodo() {
        hboxColumnas.getChildren().clear();

        BoardDTO tablero = boardService.obtenerTableroPorId(new BoardId(boardIdActual))
                .map(boardMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Tablero no encontrado"));

        List<CardDTO> todasLasTarjetas = cardService
                .obtenerTarjetasPorTablero(new BoardId(boardIdActual))
                .stream().map(cardMapper::toDTO).toList();

        lblTituloTablero.setText(tablero.getTitulo());
        this.estadoBloqueoActual = tablero.isLocked();
        btnBloqueo.setText(this.estadoBloqueoActual ? "Desbloquear 🔓" : "Bloquear 🔒");

        actualizarComboNombre(todasLasTarjetas);
        actualizarComboColor(todasLasTarjetas);

        List<CardDTO> tarjetasFiltradas = todasLasTarjetas.stream()
                .filter(this::pasaFiltroNombre)
                .filter(this::pasaFiltroColor)
                .toList();

        for (BoardDTO.ListaDTO listaInfo : tablero.getListas()) {
            List<CardDTO> tarjetasDeEstaLista = tarjetasFiltradas.stream()
                    .filter(c -> listaInfo.getId().equals(c.getListIdActual()))
                    .toList();

            hboxColumnas.getChildren().add(
                crearColumnaVisual(listaInfo.getId(), listaInfo.getNombre(), tarjetasDeEstaLista));
        }
    }

    // ── Actualización de ComboBoxes ───────────────────────────────────────────
    private void actualizarComboNombre(List<CardDTO> tarjetas) {
        List<String> nombres = tarjetas.stream()
                .filter(c -> c.getEtiquetas() != null)
                .flatMap(c -> c.getEtiquetas().stream())
                .map(CardDTO.EtiquetaDTO::getNombre)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        nombres.add(0, SIN_FILTRO_NOMBRE);
        cbFiltroEtiquetas.setOnAction(null);
        cbFiltroEtiquetas.getItems().setAll(nombres);
        
        if (!nombres.contains(filtroNombreActual)) filtroNombreActual = SIN_FILTRO_NOMBRE;
        cbFiltroEtiquetas.setValue(filtroNombreActual);
        
        cbFiltroEtiquetas.setOnAction(e -> {
            String v = cbFiltroEtiquetas.getValue();
            if (v != null) { filtroNombreActual = v; renderizarTodo(); }
        });
    }

    private void actualizarComboColor(List<CardDTO> tarjetas) {
        List<String> colores = tarjetas.stream()
                .filter(c -> c.getEtiquetas() != null)
                .flatMap(c -> c.getEtiquetas().stream())
                .map(CardDTO.EtiquetaDTO::getColor)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        colores.add(0, SIN_FILTRO_COLOR);
        cbFiltroColor.setOnAction(null);
        cbFiltroColor.getItems().setAll(colores);
        
        if (!colores.contains(filtroColorActual)) filtroColorActual = SIN_FILTRO_COLOR;
        cbFiltroColor.setValue(filtroColorActual);
        
        cbFiltroColor.setOnAction(e -> {
            String v = cbFiltroColor.getValue();
            if (v != null) { filtroColorActual = v; renderizarTodo(); }
        });
    }

    private void configurarCeldaColor() {
        cbFiltroColor.setCellFactory(lv -> new ListCell<>() {
            private final Rectangle rect = new Rectangle(16, 16);
            private final Label     lbl  = new Label();
            private final HBox      box  = new HBox(8, rect, lbl);
            { 
                box.setStyle("-fx-alignment: CENTER_LEFT;"); 
                lbl.setStyle("-fx-text-fill: #333333;");
            }

            @Override
            protected void updateItem(String colorHex, boolean empty) {
                super.updateItem(colorHex, empty);
                if (empty || colorHex == null) {
                    setGraphic(null);
                    setText(null);
                } else if (SIN_FILTRO_COLOR.equals(colorHex)) {
                    rect.setFill(Color.TRANSPARENT);
                    rect.setStroke(Color.LIGHTGRAY);
                    lbl.setText("Todos los colores");
                    setGraphic(box);
                    setText(null);
                } else {
                    try {
                        rect.setFill(Color.web(colorHex));
                        rect.setStroke(Color.TRANSPARENT);
                    } catch (Exception ex) {
                        rect.setFill(Color.GRAY);
                    }
                    lbl.setText(colorHex);
                    setGraphic(box);
                    setText(null);
                }
            }
        });

        cbFiltroColor.setButtonCell(new ListCell<>() {
            private final Rectangle rect = new Rectangle(14, 14);
            private final Label     lbl  = new Label();
            private final HBox      box  = new HBox(6, rect, lbl);
            { 
                box.setStyle("-fx-alignment: CENTER_LEFT;"); 
                lbl.setStyle("-fx-text-fill: #333333;");
            }

            @Override
            protected void updateItem(String colorHex, boolean empty) {
                super.updateItem(colorHex, empty);
                if (empty || colorHex == null) {
                    setGraphic(null);
                    setText(null);
                } else if (SIN_FILTRO_COLOR.equals(colorHex)) {
                    rect.setFill(Color.TRANSPARENT);
                    rect.setStroke(Color.LIGHTGRAY);
                    lbl.setText("Todos");
                    setGraphic(box);
                    setText(null);
                } else {
                    try {
                        rect.setFill(Color.web(colorHex));
                        rect.setStroke(Color.TRANSPARENT);
                    } catch (Exception ex) {
                        rect.setFill(Color.GRAY);
                    }
                    lbl.setText(colorHex);
                    setGraphic(box);
                    setText(null);
                }
            }
        });
    }

    // ── Predicados de filtro ──────────────────────────────────────────────────
    private boolean pasaFiltroNombre(CardDTO c) {
        if (SIN_FILTRO_NOMBRE.equals(filtroNombreActual)) return true;
        return c.getEtiquetas() != null &&
               c.getEtiquetas().stream().anyMatch(e -> filtroNombreActual.equals(e.getNombre()));
    }

    private boolean pasaFiltroColor(CardDTO c) {
        if (SIN_FILTRO_COLOR.equals(filtroColorActual)) return true;
        return c.getEtiquetas() != null &&
               c.getEtiquetas().stream().anyMatch(e -> filtroColorActual.equals(e.getColor()));
    }

    // ── Construcción de columnas ───────────────────────────────────────────────
    private VBox crearColumnaVisual(String listId, String nombreLista, List<CardDTO> tarjetas) {
        VBox columna = new VBox(8);
        columna.setPrefWidth(250);
        columna.setStyle("-fx-background-color: #ebecf0; -fx-background-radius: 5; -fx-padding: 10;");

        Label lblTitulo = new Label(nombreLista + " (" + tarjetas.size() + ")");
        lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 0 0 6 0; -fx-text-fill: #333333;");
        columna.getChildren().add(lblTitulo);

        VBox contenedorTarjetas = new VBox(8);
        contenedorTarjetas.setPadding(new Insets(4, 0, 4, 0));
        tarjetas.forEach(t -> contenedorTarjetas.getChildren().add(crearTarjetaVisual(t)));
        columna.getChildren().add(contenedorTarjetas);

        Button btnAnadirTarjeta = new Button("+ Añadir tarjeta");
        btnAnadirTarjeta.setMaxWidth(Double.MAX_VALUE);
        btnAnadirTarjeta.setStyle("-fx-background-color: rgba(0,0,0,0.08); -fx-cursor: hand;");
        btnAnadirTarjeta.setDisable(this.estadoBloqueoActual);
        btnAnadirTarjeta.setOnAction(e -> abrirDialogoNuevaTarjeta(listId));
        columna.getChildren().add(btnAnadirTarjeta);

        columna.setOnDragOver(event -> {
            if (event.getGestureSource() != columna && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        columna.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean exito = false;
            if (db.hasString()) {
                String cardIdVolando = db.getString();
                try {
                    MoverCardCommand cmd = new MoverCardCommand(cardIdVolando, boardIdActual, listId);
                    cardService.moverTarjeta(cmd);
                    exito = true;
                    renderizarTodo();
                } catch (IllegalArgumentException | IllegalStateException e) {
                    mostrarAlertaRegla(e.getMessage());
                }
            }
            event.setDropCompleted(exito);
            event.consume();
        });

        return columna;
    }

    private VBox crearTarjetaVisual(CardDTO tarjeta) {
        VBox tarjetaVisual = new VBox(4);
        tarjetaVisual.setStyle(
            "-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 4; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 2, 0, 0, 1); -fx-cursor: hand;");

        String iconoTipo = "CHECKLIST".equals(tarjeta.getTipo()) ? "☑ " : "✔ ";
        Label lblTitulo = new Label(iconoTipo + tarjeta.getTitulo());
        lblTitulo.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        lblTitulo.setWrapText(true);
        tarjetaVisual.getChildren().add(lblTitulo);

        if ("CHECKLIST".equals(tarjeta.getTipo()) && tarjeta.getChecklistItems() != null && !tarjeta.getChecklistItems().isEmpty()) {
            VBox contenedorChecklist = new VBox(3);
            contenedorChecklist.setPadding(new Insets(4, 0, 4, 12)); 
            
            for (String paso : tarjeta.getChecklistItems()) {
                CheckBox chkPaso = new CheckBox(paso);
                chkPaso.setStyle("-fx-font-size: 11px; -fx-text-fill: #555555;");
                chkPaso.setDisable(true); 
                chkPaso.setOpacity(0.85); 
                contenedorChecklist.getChildren().add(chkPaso);
            }
            tarjetaVisual.getChildren().add(contenedorChecklist);
        }

        if (tarjeta.getEtiquetas() != null && !tarjeta.getEtiquetas().isEmpty()) {
            HBox chips = new HBox(4);
            tarjeta.getEtiquetas().forEach(et -> {
                Label chip = new Label(et.getNombre());
                chip.setStyle(
                    "-fx-background-color: " + et.getColor() + "; " +
                    "-fx-text-fill: white; -fx-padding: 2 6 2 6; " +
                    "-fx-background-radius: 8; -fx-font-size: 10px;");
                chips.getChildren().add(chip);
            });
            tarjetaVisual.getChildren().add(chips);
        }

        if (tarjeta.isCompletada()) {
            Label badge = new Label("✓ Completada");
            badge.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 10px; -fx-font-weight: bold;");
            tarjetaVisual.getChildren().add(badge);
        }

        tarjetaVisual.setOnDragDetected(event -> {
            Dragboard db = tarjetaVisual.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(tarjeta.getId());
            db.setContent(content);
            event.consume();
        });

        return tarjetaVisual;
    }

    // ── Acciones FXML ──────────────────────────────────────────────────────────
    
    @FXML
    public void handleVolver() {
        this.sceneManager.navigateToDashboard();
    }
    
    @FXML
    public void handleAlternarBloqueo() {
        try {
            CambiarBloqueoBoardCommand cmd =
                new CambiarBloqueoBoardCommand(boardIdActual, !this.estadoBloqueoActual);
            boardService.cambiarEstadoBloqueo(cmd);
            renderizarTodo();
        } catch (Exception e) {
            mostrarAlertaError("Error al cambiar bloqueo", e.getMessage());
        }
    }

    @FXML
    public void handleAnadirListaVentana() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AnadirLista.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();
            AnadirListaController ctrl = loader.getController();
            ctrl.setBoardId(this.boardIdActual);
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(obtenerVentanaPrincipal());
            dialog.setTitle("Añadir Lista");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
            renderizarTodo();
        } catch (IOException e) {
            mostrarAlertaError("Error al abrir diálogo", e.getMessage());
        }
    }

    @FXML
    public void handleVerHistorial() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Historial.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();
            HistorialController ctrl = loader.getController();
            ctrl.setBoardId(this.boardIdActual);
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(obtenerVentanaPrincipal());
            dialog.setTitle("Historial del Tablero");
            dialog.setScene(new Scene(root));
            dialog.show();
        } catch (IOException e) {
            mostrarAlertaError("Error al abrir historial", e.getMessage());
        }
    }

    @FXML
    public void handleLimpiarFiltros() {
        filtroNombreActual = SIN_FILTRO_NOMBRE;
        filtroColorActual  = SIN_FILTRO_COLOR;
        renderizarTodo();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void abrirDialogoNuevaTarjeta(String listId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NuevaTarjeta.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();
            NuevaTarjetaController ctrl = loader.getController();
            ctrl.setBoardId(this.boardIdActual);
            ctrl.setListId(listId);
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(obtenerVentanaPrincipal());
            dialog.setTitle("Nueva Tarjeta");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
            renderizarTodo();
        } catch (IOException e) {
            mostrarAlertaError("Error al abrir diálogo", e.getMessage());
        }
    }

    private Window obtenerVentanaPrincipal() {
        return hboxColumnas.getScene().getWindow();
    }

    private void mostrarAlertaRegla(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Movimiento denegado");
        alert.setHeaderText("Regla de negocio incumplida");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAlertaError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
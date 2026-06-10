package es.um.pds.tableros.infrastructure.javafx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.*;
import javafx.scene.input.*;
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

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Controlador de la vista principal del tablero Kanban ({@code BoardView.fxml}).
 *
 * <p>Gestiona el ciclo completo de la pantalla: renderizado de columnas,
 * drag & drop de tarjetas entre listas, bloqueo/desbloqueo del tablero,
 * y apertura de los diálogos modales de añadir lista, crear tarjeta e historial.
 *
 * <p>Sigue el mismo patrón que {@link DashboardController}: Spring inyecta
 * todas las dependencias por constructor, y el {@link SceneManager} lo instancia
 * a través de {@code loader.setControllerFactory(springContext::getBean)}.
 */
@Component
public class BoardViewController {

    // ── Dependencias inyectadas por Spring ────────────────────────────────────
    private final BoardService       boardService;
    private final CardService        cardService;
    private final BoardMapper        boardMapper;
    private final CardMapper         cardMapper;
    private final ApplicationContext springContext; // para abrir sub-diálogos con Spring

    // ── Nodos FXML ─────────────────────────────────────────────────────────────
    @FXML private Label  lblTituloTablero;
    @FXML private Button btnBloqueo;
    @FXML private HBox   hboxColumnas;

    // ── Estado ────────────────────────────────────────────────────────────────
    private String  boardIdActual;
    private boolean estadoBloqueoActual;

    // ── Constructor ───────────────────────────────────────────────────────────

    public BoardViewController(BoardService boardService, CardService cardService,
                               BoardMapper boardMapper, CardMapper cardMapper,
                               ApplicationContext springContext) {
        this.boardService  = boardService;
        this.cardService   = cardService;
        this.boardMapper   = boardMapper;
        this.cardMapper    = cardMapper;
        this.springContext = springContext;
    }

    // ── Punto de entrada desde SceneManager ───────────────────────────────────

    /** Llamado por {@link SceneManager#navigateToBoard(String)} tras cargar el FXML. */
    public void inicializarTablero(String boardId) {
        this.boardIdActual = boardId;
        renderizarTodo();
    }

    // ── Renderizado principal ─────────────────────────────────────────────────

    /**
     * Limpia el HBox y reconstruye todas las columnas leyendo el estado actual
     * del dominio. Se llama al iniciar y tras cualquier operación que modifique
     * el tablero (mover tarjeta, añadir lista, bloquear, etc.).
     */
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

        // BoardDTO expone getNombresListas(): List<String> con los nombres de columna.
        // Usamos el nombre como clave tanto para identificar la lista como para filtrar tarjetas.
        for (String nombreLista : tablero.getNombresListas()) {

            List<CardDTO> tarjetasDeEstaLista = todasLasTarjetas.stream()
                    .filter(c -> nombreLista.equals(c.getListIdActual()))
                    .toList();

            VBox columna = crearColumnaVisual(nombreLista, nombreLista, tarjetasDeEstaLista);
            hboxColumnas.getChildren().add(columna);
        }
    }

    // ── Construcción de columnas ───────────────────────────────────────────────

    /**
     * Crea el nodo visual de una columna (lista de tareas).
     *
     * <p>Además del título y las tarjetas, añade:
     * <ul>
     *   <li>Un botón "+ Añadir tarjeta" (desactivado si el tablero está bloqueado).</li>
     *   <li>Los event handlers de drag & drop para aceptar tarjetas arrastradas.</li>
     * </ul>
     */
    private VBox crearColumnaVisual(String listId, String nombreLista, List<CardDTO> tarjetas) {
        VBox columna = new VBox(8);
        columna.setPrefWidth(250);
        columna.setStyle("-fx-background-color: #ebecf0; -fx-background-radius: 5; -fx-padding: 10;");

        // Cabecera con nombre y contador
        Label lblTitulo = new Label(nombreLista + " (" + tarjetas.size() + ")");
        lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 0 0 6 0;");
        columna.getChildren().add(lblTitulo);

        // Contenedor interno de tarjetas (es el destino real del drop)
        VBox contenedorTarjetas = new VBox(8);
        contenedorTarjetas.setPadding(new Insets(4, 0, 4, 0));
        tarjetas.forEach(t -> contenedorTarjetas.getChildren().add(crearTarjetaVisual(t)));
        columna.getChildren().add(contenedorTarjetas);

        // Botón añadir tarjeta: desactivado cuando el tablero está bloqueado
        Button btnAnadirTarjeta = new Button("+ Añadir tarjeta");
        btnAnadirTarjeta.setMaxWidth(Double.MAX_VALUE);
        btnAnadirTarjeta.setStyle("-fx-background-color: rgba(0,0,0,0.08); -fx-cursor: hand;");
        btnAnadirTarjeta.setDisable(this.estadoBloqueoActual);
        btnAnadirTarjeta.setOnAction(e -> abrirDialogoNuevaTarjeta(listId));
        columna.getChildren().add(btnAnadirTarjeta);

        // ── Drag & Drop: columna como DESTINO ────────────────────────────────
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

    /**
     * Crea el nodo visual de una tarjeta individual.
     * Muestra título, tipo y etiquetas; e implementa el inicio del drag & drop.
     */
    private VBox crearTarjetaVisual(CardDTO tarjeta) {
        VBox tarjetaVisual = new VBox(4);
        tarjetaVisual.setStyle(
            "-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 4; " +
            "-fx-effect: dropshadow(tiny, rgba(0,0,0,0.1), 2, 0, 0, 1); -fx-cursor: hand;");

        // Tipo (icono) + título
        String iconoTipo = "CHECKLIST".equals(tarjeta.getTipo()) ? "☑ " : "✔ ";
        Label lblTitulo = new Label(iconoTipo + tarjeta.getTitulo());
        lblTitulo.setStyle("-fx-font-size: 12px;");
        lblTitulo.setWrapText(true);
        tarjetaVisual.getChildren().add(lblTitulo);

        // Etiquetas de color (chips)
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

        // Badge "Completada"
        if (tarjeta.isCompletada()) {
            Label badge = new Label("✓ Completada");
            badge.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 10px; -fx-font-weight: bold;");
            tarjetaVisual.getChildren().add(badge);
        }

        // ── Drag & Drop: tarjeta como ORIGEN ─────────────────────────────────
        tarjetaVisual.setOnDragDetected(event -> {
            Dragboard db = tarjetaVisual.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(tarjeta.getId()); // ID de la tarjeta como payload
            db.setContent(content);
            event.consume();
        });

        return tarjetaVisual;
    }

    // ── Acciones FXML ──────────────────────────────────────────────────────────

    /** Alterna el estado bloqueado/desbloqueado del tablero y recarga la vista. */
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

    /**
     * Abre el diálogo modal de añadir lista.
     * Completa el método que el compañero dejó vacío en la versión original.
     */
    @FXML
    public void handleAnadirListaVentana() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AnadirLista.fxml"));
            loader.setControllerFactory(springContext::getBean); // Spring crea AnadirListaController

            Parent root = loader.load();

            // Pasamos el boardId al controlador del diálogo
            AnadirListaController ctrl = loader.getController();
            ctrl.setBoardId(this.boardIdActual);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(obtenerVentanaPrincipal());
            dialog.setTitle("Añadir Lista");
            dialog.setScene(new Scene(root));
            dialog.showAndWait(); // bloqueante: volvemos cuando el diálogo se cierra

            // Refrescamos para reflejar la nueva lista (si se creó)
            renderizarTodo();

        } catch (IOException e) {
            mostrarAlertaError("Error al abrir diálogo", e.getMessage());
        }
    }

    /** Abre el diálogo modal de historial de acciones del tablero. */
    @FXML
    public void handleVerHistorial() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Historial.fxml"));
            loader.setControllerFactory(springContext::getBean);

            Parent root = loader.load();

            HistorialController ctrl = loader.getController();
            ctrl.setBoardId(this.boardIdActual); // carga y muestra los eventos

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

    // ── Apertura del diálogo de nueva tarjeta (desde botón de columna) ─────────

    /**
     * Abre el diálogo modal de creación de tarjeta para la lista indicada.
     * Se llama desde el botón "+ Añadir tarjeta" de cada columna.
     */
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

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Obtiene la ventana principal para usarla como owner de los diálogos modales. */
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
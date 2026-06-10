package es.um.pds.tableros.infrastructure.javafx.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.geometry.Insets;

import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.ports.input.board.BoardService;
import es.um.pds.tableros.domain.ports.input.board.commands.CambiarBloqueoBoardCommand;
import es.um.pds.tableros.domain.ports.input.card.CardService;
import es.um.pds.tableros.domain.ports.input.card.commands.MoverCardCommand;
import es.um.pds.tableros.infrastructure.rest.dto.BoardDTO;
import es.um.pds.tableros.infrastructure.rest.dto.CardDTO;
import es.um.pds.tableros.infrastructure.mappers.BoardMapper;
import es.um.pds.tableros.infrastructure.mappers.CardMapper;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class BoardViewController {

    private final BoardService boardService;
    private final CardService cardService;
    private final BoardMapper boardMapper;
    private final CardMapper cardMapper;

    @FXML private Label lblTituloTablero;
    @FXML private Button btnBloqueo;
    @FXML private HBox hboxColumnas; // El contenedor del FXML

    private String boardIdActual;
    private boolean estadoBloqueoActual;

    public BoardViewController(BoardService boardService, CardService cardService, 
                               BoardMapper boardMapper, CardMapper cardMapper) {
        this.boardService = boardService;
        this.cardService = cardService;
        this.boardMapper = boardMapper;
        this.cardMapper = cardMapper;
    }

    /**
     * Este método lo llamarás al cambiar de pantalla para cargar el tablero seleccionado.
     */
    public void inicializarTablero(String boardId) {
        this.boardIdActual = boardId;
        renderizarTodo();
    }

    /**
     * Dibuja y refresca toda la interfaz leyendo del backend.
     */
    private void renderizarTodo() {
        // Limpiamos el lienzo horizontal antes de pintar
        hboxColumnas.getChildren().clear();

        // 1. Recuperamos los datos del dominio a través de los servicios (usando DTOs)
        BoardDTO tablero = boardService.obtenerTableroPorId(new BoardId(boardIdActual))
                .map(boardMapper::toDTO).orElseThrow(() -> new IllegalArgumentException("Tablero no encontrado"));
        
        List<CardDTO> tarjetas = cardService.obtenerTarjetasPorTablero(new BoardId(boardIdActual)).stream()
                .map(cardMapper::toDTO).toList();

        // 2. Actualizamos la cabecera del FXML
        lblTituloTablero.setText(tablero.getTitulo());
        this.estadoBloqueoActual = tablero.isLocked();
        btnBloqueo.setText(this.estadoBloqueoActual ? "Desbloquear 🔒" : "Bloquear 🔓");

        // 3. Construimos e inyectamos dinámicamente las columnas en el HBox
        // Asumimos que de momento vuestras columnas se identifican por su nombre
        for (String nombreLista : tablero.getNombresListas()) {
            
            // Filtramos las tarjetas que pertenecen a esta lista
            List<CardDTO> tarjetasDeEstaLista = tarjetas.stream()
                    .filter(c -> c.getListIdActual().equals(nombreLista))
                    .toList();
            
            // Invocamos al método creador de columnas y lo añadimos a la vista
            VBox columnaVisual = crearColumnaVisual(nombreLista, nombreLista, tarjetasDeEstaLista);
            hboxColumnas.getChildren().add(columnaVisual);
        }
    }

    /**
     * MÉTODO 1: Fabrica el nodo visual de una columna y le añade la escucha del Drag & Drop (Destino).
     */
    private VBox crearColumnaVisual(String nombreLista, String listId, List<CardDTO> tarjetas) {
        VBox columna = new VBox();
        columna.setPrefWidth(250);
        columna.setStyle("-fx-background-color: #ebecf0; -fx-background-radius: 5; -fx-padding: 10;");
        
        Label lblTitulo = new Label(nombreLista);
        lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 0 0 10 0;");
        columna.getChildren().add(lblTitulo);

        // Contenedor vertical interno para las tarjetas
        VBox contenedorTarjetas = new VBox(8);
        contenedorTarjetas.setPadding(new Insets(5, 0, 5, 0));
        
        // Rellenamos la columna con sus tarjetas
        tarjetas.forEach(t -> contenedorTarjetas.getChildren().add(crearTarjetaVisual(t)));
        columna.getChildren().add(contenedorTarjetas);

        // --- EVENTOS DRAG AND DROP (COLUMNA COMO DESTINO) ---
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
                    // Enviamos la orden de mover al backend
                    MoverCardCommand cmd = new MoverCardCommand(cardIdVolando, this.boardIdActual, listId);
                    cardService.moverTarjeta(cmd);
                    
                    exito = true;
                    renderizarTodo(); // Repintamos el nuevo estado con éxito
                    
                } catch (IllegalArgumentException | IllegalStateException e) {
                    // Si el dominio salta por bloqueo o lista llena, se pinta una alerta en JavaFX
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Movimiento Denegado");
                    alert.setHeaderText("Regla de negocio incumplida");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            }
            event.setDropCompleted(exito);
            event.consume();
        });

        return columna;
    }

    /**
     * MÉTODO 2: Fabrica el nodo visual de una tarjeta individual y le añade el arrastre (Origen).
     */
    private VBox crearTarjetaVisual(CardDTO tarjeta) {
        VBox tarjetaVisual = new VBox();
        tarjetaVisual.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 4; "
                + "-fx-effect: dropshadow(tiny, rgba(0,0,0,0.1), 2, 0, 0, 1);");
        
        Label tituloCard = new Label(tarjeta.getTitulo());
        tituloCard.setStyle("-fx-font-size: 12px;");
        tarjetaVisual.getChildren().add(tituloCard);

        // --- EVENTOS DRAG AND DROP (TARJETA COMO ORIGEN) ---
        tarjetaVisual.setOnDragDetected(event -> {
            Dragboard db = tarjetaVisual.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            
            // Empaquetamos el ID de la tarjeta en el envío del arrastre
            content.putString(tarjeta.getId());
            db.setContent(content);
            
            event.consume();
        });

        return tarjetaVisual;
    }

    /**
     * Acción del botón de la cabecera para bloquear/desbloquear.
     */
    @FXML
    public void handleAlternarBloqueo() {
        try {
            boolean nuevoEstado = !this.estadoBloqueoActual;
            CambiarBloqueoBoardCommand cmd = new CambiarBloqueoBoardCommand(this.boardIdActual, nuevoEstado);
            boardService.cambiarEstadoBloqueo(cmd);
            renderizarTodo();
        } catch (Exception e) {
            System.err.println("Error al cambiar estado de bloqueo: " + e.getMessage());
        }
    }

    @FXML
    public void handleAnadirListaVentana() {
        // Aquí meteríais un TextInputDialog flotante para capturar el nombre de la nueva columna
    }
}
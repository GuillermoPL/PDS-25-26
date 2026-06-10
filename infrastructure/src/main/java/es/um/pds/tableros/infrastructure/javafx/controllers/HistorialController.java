package es.um.pds.tableros.infrastructure.javafx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.ports.input.board.BoardService;

import java.util.List;

/**
 * Controlador del diálogo de historial ({@code Historial.fxml}).
 *
 * <p>Recupera el tablero mediante {@link BoardService#obtenerTableroPorId} y
 * lee el historial directamente del objeto de dominio {@link Board#getHistorial()},
 * sin necesitar un método adicional en el puerto de entrada.
 *
 * <p>El {@code boardId} se pasa mediante {@link #setBoardId(String)} antes de
 * llamar a {@code stage.showAndWait()}, siguiendo el mismo patrón que usan
 * los otros diálogos del proyecto.
 */
@Component
public class HistorialController {

    // ── Puerto de entrada inyectado por Spring ─────────────────────────────────
    private final BoardService boardService;

    // ── Nodo FXML ─────────────────────────────────────────────────────────────
    @FXML private ListView<String> listHistorial;

    // ── Contexto ──────────────────────────────────────────────────────────────
    private String boardId;

    // ── Constructor ───────────────────────────────────────────────────────────

    public HistorialController(BoardService boardService) {
        this.boardService = boardService;
    }

    // ── API para el controlador padre ──────────────────────────────────────────

    /**
     * Establece el tablero cuyo historial se va a mostrar y carga los datos.
     * Debe llamarse justo antes de {@code stage.showAndWait()}.
     */
    public void setBoardId(String boardId) {
        this.boardId = boardId;
        cargarHistorial();
    }

    // ── Carga de datos ─────────────────────────────────────────────────────────

    private void cargarHistorial() {
        listHistorial.getItems().clear();

        Board tablero = boardService.obtenerTableroPorId(new BoardId(boardId))
                .orElse(null);

        if (tablero == null) {
            listHistorial.getItems().add("— Tablero no encontrado —");
            return;
        }

        // Board#getHistorial() devuelve List<String> con los eventos registrados.
        // Si tu clase Board usa otro nombre (getHistory, getTraza...), cámbialo aquí.
        List<String> eventos = tablero.getHistorial();

        if (eventos == null || eventos.isEmpty()) {
            listHistorial.getItems().add("— Sin acciones registradas aún —");
        } else {
            // Orden más reciente primero
            for (int i = eventos.size() - 1; i >= 0; i--) {
                listHistorial.getItems().add(eventos.get(i));
            }
        }
    }

    // ── Acciones FXML ──────────────────────────────────────────────────────────

    @FXML
    public void handleCerrar() {
        ((Stage) listHistorial.getScene().getWindow()).close();
    }
}
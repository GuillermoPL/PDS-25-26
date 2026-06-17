package es.um.pds.tableros.infrastructure.javafx.controllers;

import java.util.List;

import org.springframework.stereotype.Component;

import es.um.pds.tableros.domain.ports.input.board.BoardService;
import es.um.pds.tableros.infrastructure.mappers.BoardMapper;
import es.um.pds.tableros.infrastructure.rest.dto.BoardDTO;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

/**
 * @brief Controlador visual para el cuadro de diálogo del historial de auditoría del tablero.
 * Recupera de forma segura el historial cronológico de eventos de un tablero a través del 
 * uso aislado de DTOs y revierte el orden de la colección para inyectarla en el ListView, 
 * mostrando los acontecimientos más recientes en la parte superior.
 */
@Component
public class HistorialController {

    private final BoardService boardService;
    private final BoardMapper boardMapper;

    /** Componente de lista FXML para inyectar las cadenas de texto del historial. */
    @FXML private ListView<String> listHistorial;

    private String boardId;

    /**
     * @brief Constructor con inyección del caso de uso y el componente mapper aislante.
     * @param boardService Puerto de entrada para consultas operacionales de tablero.
     * @param boardMapper Mapeador de aislamiento arquitectónico.
     */
    public HistorialController(BoardService boardService, BoardMapper boardMapper) {
        this.boardService = boardService;
        this.boardMapper = boardMapper;
    }

    /**
     * @brief Almacena el ID del tablero y gatilla la lectura del historial de auditoría.
     * @param boardId Identificador único del tablero Kanban.
     */
    public void setBoardId(String boardId) {
        this.boardId = boardId;
        cargarHistorial();
    }

    /**
     * @brief Consulta el puerto de entrada, mapea a DTO e inyecta los eventos en orden inverso.
     * Si no se localiza el tablero o carece de trazas registradas, inserta placeholders informativos planos 
     * en el ListView para guiar al usuario.
     */
    private void cargarHistorial() {
        listHistorial.getItems().clear();

        // AÑADIDO: Mapeamos a DTO para no tocar la entidad Board
        BoardDTO tablero = boardService.obtenerTableroPorId(boardId)
                .map(boardMapper::toDTO)
                .orElse(null);

        if (tablero == null) {
            listHistorial.getItems().add("— Tablero no encontrado —");
            return;
        }

        // Leemos del DTO
        List<String> eventos = tablero.getHistorial();

        if (eventos == null || eventos.isEmpty()) {
            listHistorial.getItems().add("— Sin acciones registradas aún —");
        } else {
            // Recorremos a la inversa para mostrar lo más reciente arriba
            for (int i = eventos.size() - 1; i >= 0; i--) {
                listHistorial.getItems().add(eventos.get(i));
            }
        }
    }

    /**
     * @brief Cierra el diálogo modal del historial.
     */
    @FXML
    public void handleCerrar() {
        ((Stage) listHistorial.getScene().getWindow()).close();
    }
}
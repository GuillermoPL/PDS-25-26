package es.um.pds.tableros.infrastructure.javafx.controllers;

import java.util.List;

import org.springframework.stereotype.Component;

import es.um.pds.tableros.domain.ports.input.board.BoardService;
import es.um.pds.tableros.infrastructure.mappers.BoardMapper;
import es.um.pds.tableros.infrastructure.rest.dto.BoardDTO;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

@Component
public class HistorialController {

    private final BoardService boardService;
    private final BoardMapper boardMapper;

    @FXML private ListView<String> listHistorial;

    private String boardId;

    // Modificado para inyectar el mapper
    public HistorialController(BoardService boardService, BoardMapper boardMapper) {
        this.boardService = boardService;
        this.boardMapper = boardMapper;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
        cargarHistorial();
    }

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
            for (int i = eventos.size() - 1; i >= 0; i--) {
                listHistorial.getItems().add(eventos.get(i));
            }
        }
    }

    @FXML
    public void handleCerrar() {
        ((Stage) listHistorial.getScene().getWindow()).close();
    }
}
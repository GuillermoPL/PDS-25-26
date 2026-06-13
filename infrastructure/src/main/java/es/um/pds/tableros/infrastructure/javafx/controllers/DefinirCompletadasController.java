package es.um.pds.tableros.infrastructure.javafx.controllers;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import es.um.pds.tableros.domain.ports.input.board.BoardService;
import es.um.pds.tableros.domain.ports.input.board.commands.DefinirListCompletadasCommand;
import es.um.pds.tableros.infrastructure.mappers.BoardMapper;
import es.um.pds.tableros.infrastructure.rest.dto.BoardDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;


@Component
@Scope("prototype")
public class DefinirCompletadasController {

    private final BoardService boardService;
    private final BoardMapper boardMapper;

    @FXML private ComboBox<BoardDTO.ListaDTO> cbListas;

    private String boardId;

    public DefinirCompletadasController(BoardService boardService, BoardMapper boardMapper) {
        this.boardService = boardService;
        this.boardMapper = boardMapper;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
        cargarListas();
    }

    private void cargarListas() {
        BoardDTO tablero = boardService.obtenerTableroPorId(boardId)
                .map(boardMapper::toDTO)
                .orElse(null);

        if (tablero != null && tablero.getListas() != null) {
            cbListas.getItems().setAll(tablero.getListas());
            
            // Configuramos cómo se ven los objetos ListaDTO en el ComboBox
            cbListas.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(BoardDTO.ListaDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNombre());
                }
            });
            cbListas.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(BoardDTO.ListaDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNombre());
                }
            });

            // Si ya hay una lista seleccionada previamente, la marcamos
            if (tablero.getListCompletadasId() != null) {
                cbListas.getItems().stream()
                        .filter(l -> l.getId().equals(tablero.getListCompletadasId()))
                        .findFirst()
                        .ifPresent(cbListas::setValue);
            }
        }
    }

    @FXML
    public void handleGuardar() {
        BoardDTO.ListaDTO seleccion = cbListas.getValue();
        if (seleccion != null) {
            try {
                DefinirListCompletadasCommand cmd = new DefinirListCompletadasCommand(boardId, seleccion.getId());
                boardService.definirListaCompletadas(cmd);
                cerrarVentana();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        } else {
            cerrarVentana();
        }
    }

    @FXML
    public void handleCancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        ((Stage) cbListas.getScene().getWindow()).close();
    }
}
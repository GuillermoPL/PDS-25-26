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

/**
 * @brief Controlador del diálogo de ajustes para definir la lista de tareas completadas.
 * Despliega un ComboBox para que el usuario seleccione cuál de las columnas existentes actuará
 * como sumidero Done, pre-seleccionando la opción si ya existía una previa en la persistencia H2.
 * @note Configurado con Scope prototipo ("prototype") para asegurar que Spring instancie una nueva 
 * estructura limpia libre de estados persistidos residuales de aperturas previas.
 */
@Component
@Scope("prototype")
public class DefinirCompletadasController {

    private final BoardService boardService;
    private final BoardMapper boardMapper;

    /** Selector desplegable FXML parametrizado con los sub-DTOs de listas del tablero. */
    @FXML private ComboBox<BoardDTO.ListaDTO> cbListas;

    private String boardId;

    /**
     * @brief Constructor con inyección del puerto de entrada y el mapper traductor.
     * @param boardService Caso de uso para la manipulación operacional de tableros.
     * @param boardMapper Mapeador para aislar tipos de dominio.
     */
    public DefinirCompletadasController(BoardService boardService, BoardMapper boardMapper) {
        this.boardService = boardService;
        this.boardMapper = boardMapper;
    }

    /**
     * @brief Asigna el identificador del tablero e inicia de forma inmediata la lectura de sus columnas.
     * @param boardId Identificador String del tablero Kanban.
     */
    public void setBoardId(String boardId) {
        this.boardId = boardId;
        cargarListas();
    }

    /**
     * @brief Consulta el puerto de entrada, mapea los datos a DTO y puebla los elementos del ComboBox.
     * Sobrescribe las factorías cellFactory y buttonCell del componente gráfico para renderizar de 
     * forma legible el atributo string 'nombre' de los objetos ListaDTO mapeados.
     */
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

    /**
     * @brief Procesa el guardado del ajuste seleccionado en la interfaz.
     * Extrae el DTO del ComboBox, arma el comando DefinirListCompletadasCommand y lo despacha 
     * al puerto del dominio para forzar la consistencia transaccional.
     */
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

    /**
     * @brief Aborta la configuración cerrando la vista sin persistir cambios.
     */
    @FXML
    public void handleCancelar() {
        cerrarVentana();
    }

    /**
     * @brief Recupera el Stage primario de la escena del ComboBox y cierra la ventana.
     */
    private void cerrarVentana() {
        ((Stage) cbListas.getScene().getWindow()).close();
    }
}
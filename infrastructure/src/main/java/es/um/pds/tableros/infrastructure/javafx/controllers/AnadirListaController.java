package es.um.pds.tableros.infrastructure.javafx.controllers;

import org.springframework.stereotype.Component;

import es.um.pds.tableros.domain.ports.input.board.BoardService;
import es.um.pds.tableros.domain.ports.input.board.commands.AnadirListCommand;
import es.um.pds.tableros.infrastructure.javafx.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * @brief Controlador del diálogo "Añadir Lista" (AnadirLista.fxml).
 * Antes de mostrar el diálogo, BoardViewController le inyecta el boardId del tablero activo 
 * mediante setBoardId(String). Cuando el usuario confirma, el controlador llama al puerto de entrada
 * del dominio y cierra la ventana; el BoardViewController refresca la vista al detectar que el Stage modal ya se cerró.
 * Es un \@Component de Spring para que el SceneManager pueda instanciarlo con loader.setControllerFactory(springContext::getBean).
 */
@Component
public class AnadirListaController {

    private final BoardService boardService;

    /** Campo de texto FXML para introducir el nombre de la nueva columna. */
    @FXML private TextField txtNombreLista;
    
    /** Campo de texto FXML opcional para definir el límite WIP máximo de tarjetas de la columna. */
    @FXML private TextField txtLimiteTarjetas;

    private String boardId;

    private final SceneManager sceneManager;
    
    /**
     * @brief Constructor con inyección automática de dependencias de Spring.
     * @param boardService Puerto de entrada para el control operacional de tableros.
     * @param sceneManager Administrador de sesión y rutas gráficas.
     */
    public AnadirListaController(BoardService boardService, SceneManager sceneManager) {
        this.boardService = boardService;
        this.sceneManager = sceneManager;
    }

    /**
     * @brief Establece el tablero al que pertenecerá la nueva lista.
     * Debe llamarse antes de realizar el showAndWait() del escenario modal.
     * @param boardId Identificador único del tablero Kanban activo.
     */
    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    /**
     * @brief Maneja la acción FXML de pulsación del botón Crear.
     * Extrae y valida el texto del campo, parsea el límite WIP si está presente, construye 
     * el comando AnadirListCommand adjuntando el email en sesión y delega en la capa de aplicación 
     * cerrando la ventana en caso de éxito.
     */
    @FXML
    public void handleCrear() {
        String nombre = txtNombreLista.getText().trim();
        String limiteStr = txtLimiteTarjetas != null ? txtLimiteTarjetas.getText().trim() : "";

        if (nombre.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Campo vacío");
            alert.setHeaderText(null);
            alert.setContentText("El nombre de la lista no puede estar vacío.");
            alert.showAndWait();
            return;
        }

        Integer limiteWip = null;
        if (!limiteStr.isBlank()) {
            try {
                limiteWip = Integer.parseInt(limiteStr);
                if (limiteWip <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Formato inválido");
                alert.setHeaderText(null);
                alert.setContentText("El límite de tarjetas debe ser un número entero mayor que 0.");
                alert.showAndWait();
                return;
            }
        }

        try {
            String emailUsuario = sceneManager.getCurrentUserEmail(); 
            AnadirListCommand cmd = new AnadirListCommand(boardId, nombre, limiteWip, emailUsuario); 
            boardService.anadirListaATablero(cmd);
            cerrarVentana();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error al crear la lista");
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * @brief Maneja la acción FXML de cancelación abortando la operación.
     */
    @FXML
    public void handleCancelar() {
        cerrarVentana();
    }

    /**
     * @brief Cierra el escenario Stage modal actual recuperando la escena del nodo de texto.
     */
    private void cerrarVentana() {
        ((Stage) txtNombreLista.getScene().getWindow()).close();
    }
}
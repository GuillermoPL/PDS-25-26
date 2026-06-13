package es.um.pds.tableros.infrastructure.javafx.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import es.um.pds.tableros.domain.ports.input.board.BoardService;
import es.um.pds.tableros.domain.ports.input.board.commands.AnadirListCommand;
import es.um.pds.tableros.infrastructure.javafx.SceneManager;

/**
 * Controlador del diálogo "Añadir Lista" ({@code AnadirLista.fxml}).
 *
 * <p>Antes de mostrar el diálogo, {@link BoardViewController} le inyecta
 * el {@code boardId} del tablero activo mediante {@link #setBoardId(String)}.
 * Cuando el usuario confirma, el controlador llama al puerto de entrada
 * del dominio y cierra la ventana; el BoardViewController refresca la vista
 * al detectar que el Stage modal ya se cerró.
 *
 * <p>Es un {@code @Component} de Spring para que el SceneManager pueda
 * instanciarlo con {@code loader.setControllerFactory(springContext::getBean)},
 * igual que los demás controladores del proyecto.
 */
@Component
public class AnadirListaController {

    // ── Puerto de entrada inyectado por Spring ─────────────────────────────────
    private final BoardService boardService;

    // ── Nodos FXML ─────────────────────────────────────────────────────────────
    @FXML private TextField txtNombreLista;

    // ── Contexto: se asigna antes de abrir el diálogo ─────────────────────────
    private String boardId;

    private final SceneManager sceneManager;
    
    // ── Constructor ───────────────────────────────────────────────────────────

    public AnadirListaController(BoardService boardService, SceneManager sceneManager) {
        this.boardService = boardService;
        this.sceneManager = sceneManager;
    }

    // ── API para el controlador padre ──────────────────────────────────────────

    /**
     * Establece el tablero al que pertenecerá la nueva lista.
     * Debe llamarse antes de {@code stage.showAndWait()}.
     */
    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    // ── Acciones FXML ──────────────────────────────────────────────────────────

    @FXML
    public void handleCrear() {
        String nombre = txtNombreLista.getText().trim();

        if (nombre.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Campo vacío");
            alert.setHeaderText(null);
            alert.setContentText("El nombre de la lista no puede estar vacío.");
            alert.showAndWait();
            return;
        }

        try {
        	String emailUsuario = sceneManager.getCurrentUserEmail(); // AÑADIDO
            AnadirListCommand cmd = new AnadirListCommand(boardId, nombre, null, emailUsuario); // AÑADIDO el 4º parámetro
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

    @FXML
    public void handleCancelar() {
        cerrarVentana();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void cerrarVentana() {
        ((Stage) txtNombreLista.getScene().getWindow()).close();
    }
}
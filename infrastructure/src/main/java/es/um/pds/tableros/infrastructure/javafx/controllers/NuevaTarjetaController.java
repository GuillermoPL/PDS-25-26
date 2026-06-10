package es.um.pds.tableros.infrastructure.javafx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import es.um.pds.tableros.domain.card.CardType;
import es.um.pds.tableros.domain.ports.input.card.CardService;
import es.um.pds.tableros.domain.ports.input.card.commands.CrearCardCommand;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador del diálogo "Nueva Tarjeta" ({@code NuevaTarjeta.fxml}).
 *
 * <p>Recibe el {@code boardId} y el {@code listId} del contexto del que lo abre
 * (normalmente {@link BoardViewController}) antes de mostrarse como ventana modal.
 * Al confirmar, invoca el puerto de entrada {@link CardService#crearTarjeta} y
 * cierra el diálogo; el padre refresca el tablero al volver de {@code showAndWait()}.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NuevaTarjetaController implements Initializable {

    // ── Puerto de entrada inyectado por Spring ─────────────────────────────────
    private final CardService cardService;

    // ── Nodos FXML ─────────────────────────────────────────────────────────────
    @FXML private TextField  txtTitulo;
    @FXML private ChoiceBox<CardType> cbTipo;
    @FXML private TextField txtEtiqueta;
    
    // ── Contexto asignado por el padre antes de showAndWait() ─────────────────
    private String boardId;
    private String listId;

    // ── Constructor ───────────────────────────────────────────────────────────

    public NuevaTarjetaController(CardService cardService) {
        this.cardService = cardService;
    }

    // ── Inicialización ─────────────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Poblamos el ChoiceBox con los valores del enum del dominio
        cbTipo.getItems().addAll(CardType.values());
        cbTipo.setValue(CardType.TASK); // selección por defecto
    }

    // ── API para el controlador padre ──────────────────────────────────────────

    /** Debe llamarse antes de {@code stage.showAndWait()}. */
    public void setBoardId(String boardId) { this.boardId = boardId; }
    public void setListId(String listId)   { this.listId  = listId;  }

    // ── Acciones FXML ──────────────────────────────────────────────────────────

    @FXML
    public void handleCrear() {
        String titulo = txtTitulo.getText().trim();

        if (titulo.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Campo obligatorio");
            alert.setHeaderText(null);
            alert.setContentText("El título de la tarjeta no puede estar vacío.");
            alert.showAndWait();
            txtTitulo.requestFocus();
            return;
        }
        String textoEtiqueta = txtEtiqueta.getText().trim();
        String nombreEtiqueta = textoEtiqueta.isEmpty() ? null : textoEtiqueta;
        try {
        	CrearCardCommand cmd = new CrearCardCommand(
                    boardId,
                    listId,
                    titulo,
                    cbTipo.getValue().name(),
                    nombreEtiqueta 
                );
            cardService.crearNuevaTarjeta(cmd);
            cerrarVentana();

        } catch (IllegalStateException e) {
            // El dominio rechaza la creación (tablero bloqueado, lista llena, etc.)
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Acción no permitida");
            alert.setHeaderText("Regla de negocio incumplida");
            alert.setContentText(e.getMessage());
            alert.showAndWait();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error al crear la tarjeta");
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
        ((Stage) txtTitulo.getScene().getWindow()).close();
    }
}
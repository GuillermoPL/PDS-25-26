package es.um.pds.tableros.infrastructure.javafx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import es.um.pds.tableros.domain.card.CardType;
import es.um.pds.tableros.domain.card.Etiqueta;
import es.um.pds.tableros.domain.ports.input.card.CardService;
import es.um.pds.tableros.domain.ports.input.card.commands.CrearCardCommand;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador del diálogo "Nueva Tarjeta" ({@code NuevaTarjeta.fxml}).
 *
 * <p>Construye un objeto de dominio {@link Etiqueta} directamente aquí, en la
 * infraestructura, y lo pasa al comando. Así el módulo {@code domain} no
 * depende de ningún DTO de infraestructura (regla de la arquitectura hexagonal).
 *
 * <p>El color se convierte de {@link Color} (JavaFX) a hex CSS (#rrggbb) antes
 * de crear la etiqueta, que es el formato que espera el record del dominio.
 */
@Component
public class NuevaTarjetaController implements Initializable {

    // ── Puerto de entrada inyectado por Spring ─────────────────────────────────
    private final CardService cardService;

    // ── Nodos FXML ─────────────────────────────────────────────────────────────
    @FXML private TextField          txtTitulo;
    @FXML private ChoiceBox<CardType> cbTipo;
    @FXML private TextField          txtEtiqueta;
    @FXML private ColorPicker        cpColor;

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
        cbTipo.getItems().addAll(CardType.values());
        cbTipo.setValue(CardType.TASK);

        // Color por defecto: azul Trello para que no quede vacío
        cpColor.setValue(Color.web("#0079bf"));
    }

    // ── API para el controlador padre ──────────────────────────────────────────

    public void setBoardId(String boardId) { this.boardId = boardId; }
    public void setListId(String listId)   { this.listId  = listId;  }

    // ── Acciones FXML ──────────────────────────────────────────────────────────

    @FXML
    public void handleCrear() {
        String titulo = txtTitulo.getText().trim();

        if (titulo.isBlank()) {
            mostrarAviso("Campo obligatorio", "El título de la tarjeta no puede estar vacío.");
            txtTitulo.requestFocus();
            return;
        }

        try {
            // Construimos la Etiqueta del dominio aquí, en infraestructura.
            // Solo si el usuario escribió un nombre; si no, pasamos null.
            Etiqueta etiqueta = construirEtiqueta();

            CrearCardCommand cmd = new CrearCardCommand(
                boardId,
                listId,
                titulo,
                cbTipo.getValue().name(), // "TASK" o "CHECKLIST"
                etiqueta
            );
            cardService.crearNuevaTarjeta(cmd);
            cerrarVentana();

        } catch (IllegalStateException e) {
            // El dominio rechaza la operación (tablero bloqueado, lista llena…)
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

    /**
     * Construye un objeto {@link Etiqueta} del dominio a partir de los campos
     * del formulario, o devuelve {@code null} si el nombre está vacío.
     *
     * <p>Convierte el {@link Color} de JavaFX a formato hex CSS (#rrggbb)
     * que es el formato que acepta el record {@link Etiqueta}.
     */
    private Etiqueta construirEtiqueta() {
        String nombre = txtEtiqueta.getText().trim();
        if (nombre.isBlank()) {
            return null; // etiqueta opcional
        }
        String colorHex = colorToHex(cpColor.getValue());
        return new Etiqueta(nombre, colorHex);
    }

    /**
     * Convierte un {@link Color} de JavaFX al string hex CSS {@code #rrggbb}.
     * Ejemplo: Color.RED → "#ff0000"
     */
    private String colorToHex(Color color) {
        return String.format("#%02x%02x%02x",
            (int) (color.getRed()   * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue()  * 255));
    }

    private void mostrarAviso(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void cerrarVentana() {
        ((Stage) txtTitulo.getScene().getWindow()).close();
    }
}
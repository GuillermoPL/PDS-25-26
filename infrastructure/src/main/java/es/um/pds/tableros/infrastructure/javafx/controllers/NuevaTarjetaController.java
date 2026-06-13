package es.um.pds.tableros.infrastructure.javafx.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import es.um.pds.tableros.domain.card.CardType;
import es.um.pds.tableros.domain.card.Etiqueta;
import es.um.pds.tableros.domain.ports.input.card.CardService;
import es.um.pds.tableros.domain.ports.input.card.commands.CrearCardCommand;
import es.um.pds.tableros.infrastructure.javafx.SceneManager;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador del diálogo "Nueva Tarjeta" ({@code NuevaTarjeta.fxml}).
 */
@Component
@Scope("prototype") // Obliga a Spring a crear una instancia nueva cada vez que se abre la ventana
public class NuevaTarjetaController implements Initializable {

    private final CardService cardService;

    @FXML private TextField          txtTitulo;
    @FXML private ChoiceBox<CardType> cbTipo;
    @FXML private TextField          txtEtiqueta;
    @FXML private ColorPicker        cpColor;
    
    @FXML private VBox               sectionChecklist;
    @FXML private TextField          txtNuevoItem;
    @FXML private ListView<String>   listViewItems;

    private String boardId;
    private String listId;
    
    private final ObservableList<String> checklistItemsTemporales = FXCollections.observableArrayList();

    private final SceneManager sceneManager;
    
    public NuevaTarjetaController(CardService cardService, SceneManager sceneManager) {
        this.cardService = cardService;
        this.sceneManager = sceneManager;
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbTipo.getItems().addAll(CardType.values());
        cbTipo.setValue(CardType.TASK);

        cpColor.setValue(Color.web("#0079bf"));
        
        listViewItems.setItems(checklistItemsTemporales);
        
        sectionChecklist.setVisible(false);
        sectionChecklist.setManaged(false);
        
        cbTipo.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isChecklist = (newVal == CardType.CHECKLIST);
            sectionChecklist.setVisible(isChecklist);
            sectionChecklist.setManaged(isChecklist);
        });
    }

    public void setBoardId(String boardId) { this.boardId = boardId; }
    public void setListId(String listId)   { this.listId  = listId;  }
    
    @FXML
    public void handleAnadirPaso() {
        String textoItem = txtNuevoItem.getText().trim();
        if (!textoItem.isBlank()) {
            checklistItemsTemporales.add(textoItem);
            txtNuevoItem.clear();
            txtNuevoItem.requestFocus(); 
        }
    }

    @FXML
    public void handleCrear() {
        String titulo = txtTitulo.getText().trim();

        if (titulo.isBlank()) {
            mostrarAviso("Campo obligatorio", "El título de la tarjeta no puede estar vacío.");
            txtTitulo.requestFocus();
            return;
        }

        try {
            // Solo cogemos los ítems si la tarjeta es realmente de tipo CHECKLIST
            List<String> itemsFinales = new ArrayList<>();
            if (cbTipo.getValue() == CardType.CHECKLIST) {
                itemsFinales.addAll(checklistItemsTemporales);
            }

            String emailUsuario = sceneManager.getCurrentUserEmail();
            
            // Y luego de itemsFinales, pásale el email (Nota: Ya quitamos la Etiqueta de dominio, ahora pasamos los 2 strings extraídos del helper)
            String nombreEt = null;
            String colorEt = null;
            Etiqueta etiquetaDominio = construirEtiqueta();
            if (etiquetaDominio != null) {
                nombreEt = etiquetaDominio.nombre();
                colorEt = etiquetaDominio.color();
            }

            CrearCardCommand cmd = new CrearCardCommand(
                boardId,
                listId,
                titulo,
                cbTipo.getValue().name(),
                nombreEt,        // String
                colorEt,         // String
                itemsFinales,
                emailUsuario     // AÑADIDO
            );
            
            cardService.crearNuevaTarjeta(cmd);
            cerrarVentana();

        } catch (IllegalStateException | IllegalArgumentException e) {
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

    private Etiqueta construirEtiqueta() {
        String nombre = txtEtiqueta.getText().trim();
        if (nombre.isBlank()) {
            return null; 
        }
        String colorHex = colorToHex(cpColor.getValue());
        return new Etiqueta(nombre, colorHex);
    }

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
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
import es.um.pds.tableros.domain.ports.input.card.CardService;
import es.um.pds.tableros.domain.ports.input.card.commands.CrearCardCommand;
import es.um.pds.tableros.infrastructure.javafx.SceneManager;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Component
@Scope("prototype") 
public class NuevaTarjetaController implements Initializable {

    private final CardService cardService;
    private final SceneManager sceneManager;

    @FXML private TextField          txtTitulo;
    @FXML private ChoiceBox<String>  cbTipo; // AHORA ES UN STRING
    @FXML private TextField          txtEtiqueta;
    @FXML private ColorPicker        cpColor;
    
    @FXML private VBox               sectionChecklist;
    @FXML private TextField          txtNuevoItem;
    @FXML private ListView<String>   listViewItems;

    private String boardId;
    private String listId;
    
    private final ObservableList<String> checklistItemsTemporales = FXCollections.observableArrayList();
    
    public NuevaTarjetaController(CardService cardService, SceneManager sceneManager) {
        this.cardService = cardService;
        this.sceneManager = sceneManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Rellenamos el ChoiceBox con Strings puros
        cbTipo.getItems().addAll("TASK", "CHECKLIST");
        cbTipo.setValue("TASK");

        cpColor.setValue(Color.web("#0079bf"));
        listViewItems.setItems(checklistItemsTemporales);
        
        sectionChecklist.setVisible(false);
        sectionChecklist.setManaged(false);
        
        cbTipo.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isChecklist = "CHECKLIST".equals(newVal);
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
            List<String> itemsFinales = new ArrayList<>();
            if ("CHECKLIST".equals(cbTipo.getValue())) {
                itemsFinales.addAll(checklistItemsTemporales);
            }

            String emailUsuario = sceneManager.getCurrentUserEmail();
            
            // Procesamos la etiqueta como simples Strings (adiós al objeto Etiqueta)
            String nombreEt = txtEtiqueta.getText().trim();
            if (nombreEt.isBlank()) {
                nombreEt = null;
            }
            String colorEt = nombreEt != null ? colorToHex(cpColor.getValue()) : null;

            CrearCardCommand cmd = new CrearCardCommand(
                boardId,
                listId,
                titulo,
                cbTipo.getValue(), // Ya es un String
                nombreEt,        
                colorEt,         
                itemsFinales,
                emailUsuario     
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
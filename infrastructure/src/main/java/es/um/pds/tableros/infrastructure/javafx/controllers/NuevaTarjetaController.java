package es.um.pds.tableros.infrastructure.javafx.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import es.um.pds.tableros.domain.ports.input.card.CardService;
import es.um.pds.tableros.domain.ports.input.card.commands.CrearCardCommand;
import es.um.pds.tableros.infrastructure.javafx.SceneManager;
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

/**
 * @brief Controlador visual encargado de gestionar el formulario de creación de nuevas tarjetas.
 * Soporta la instanciación reactiva adaptativa de la interfaz: oculta o muestra el contenedor de checklists 
 * dinámicamente si el ChoiceBox detecta un cambio de tipo (TASK a CHECKLIST). Transforma colores nativos 
 * de interfaz (Color) a códigos String hexadecimales planos compatibles con los comandos del dominio.
 * @note Configurado con Scope prototipo ("prototype") para garantizar ciclos de vida independientes y limpios 
 * en cada instanciación de tarjetas sobre las columnas.
 */
@Component
@Scope("prototype") 
public class NuevaTarjetaController implements Initializable {

    private final CardService cardService;
    private final SceneManager sceneManager;

    @FXML private TextField          txtTitulo;
    @FXML private ChoiceBox<String>  cbTipo; 
    @FXML private TextField          txtEtiqueta;
    @FXML private ColorPicker        cpColor;
    
    /** Contenedor vertical FXML que agrupa el formulario de ítems para el tipo CHECKLIST. */
    @FXML private VBox               sectionChecklist;
    @FXML private TextField          txtNuevoItem;
    @FXML private ListView<String>   listViewItems;

    private String boardId;
    private String listId;
    
    /** Lista observable de JavaFX encargada de sincronizar reactivamente los ítems agregados a la vista del checklist. */
    private final ObservableList<String> checklistItemsTemporales = FXCollections.observableArrayList();
    
    /**
     * @brief Constructor con inyección del caso de uso y gestor de rutas/sesiones.
     * @param cardService Puerto de entrada operacional para el ciclo de vida de tarjetas.
     * @param sceneManager Gestor técnico de control de contexto de usuario.
     */
    public NuevaTarjetaController(CardService cardService, SceneManager sceneManager) {
        this.cardService = cardService;
        this.sceneManager = sceneManager;
    }

    /**
     * @brief Inicializador del ciclo de vida de JavaFX (Interfaz Initializable).
     * Setea las tipologías textuales primitivas permitidas, asigna la lista observable al ListView, 
     * oculta por defecto el panel de checklists e instala un escuchador reactivo (Listener) sobre la 
     * propiedad de tipo para conmutar la visibilidad y gestión de layouts de la sección checklist.
     */
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

    /** @param boardId Identificador único del tablero. */
    public void setBoardId(String boardId) { this.boardId = boardId; }
    /** @param listId Identificador de la columna destino original. */
    public void setListId(String listId)   { this.listId  = listId;  }
    
    /**
     * @brief Gestiona la adición interactiva de sub-items de comprobación al listado temporal.
     * Inserta la cadena de texto en la ObservableList y devuelve el foco al campo de texto.
     */
    @FXML
    public void handleAnadirPaso() {
        String textoItem = txtNuevoItem.getText().trim();
        if (!textoItem.isBlank()) {
            checklistItemsTemporales.add(textoItem);
            txtNuevoItem.clear();
            txtNuevoItem.requestFocus(); 
        }
    }

    /**
     * @brief Procesa la acción FXML de confirmación y creación de la tarjeta.
     * Valida la presencia de campos mandatorios, extrae las colecciones temporales del checklist si procede, 
     * traduce el objeto cromático de interfaz a una cadena hexadecimal web estructurada (#rrggbb) y emite 
     * el comando inmutable CrearCardCommand atrapando excepciones de negocio o bloqueos WIP del dominio.
     */
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

    /**
     * @brief Cancela la operativa cerrando el Stage modal.
     */
    @FXML
    public void handleCancelar() {
        cerrarVentana();
    }

    /**
     * @brief Helper algorítmico que traduce un color RGB nativo de interfaz a formato String Hexadecimal.
     * @param color Objeto Color de JavaFX.
     * @return Cadena representativa plana de 7 caracteres (ej: "#ff0000").
     */
    private String colorToHex(Color color) {
        return String.format("#%02x%02x%02x",
            (int) (color.getRed()   * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue()  * 255));
    }

    /**
     * @brief Despliega una alerta modal flotante de advertencia (Warning Alert) de campos inválidos.
     * @param titulo Título de cabecera.
     * @param mensaje Detalle del aviso textual.
     */
    private void mostrarAviso(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * @brief Cierra de forma ordenada el escenario recuperando el Stage desde el nodo de título.
     */
    private void cerrarVentana() {
        ((Stage) txtTitulo.getScene().getWindow()).close();
    }
}
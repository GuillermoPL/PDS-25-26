package es.um.pds.tableros.infrastructure.javafx.controllers;

import java.util.List;

import org.springframework.stereotype.Component;

import es.um.pds.tableros.domain.ports.input.board.BoardService;
import es.um.pds.tableros.domain.ports.input.board.commands.CrearBoardCommand;
import es.um.pds.tableros.infrastructure.javafx.SceneManager;
import es.um.pds.tableros.infrastructure.mappers.BoardMapper;
import es.um.pds.tableros.infrastructure.plantillas.PlantillaService;
import es.um.pds.tableros.infrastructure.rest.dto.BoardDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

/**
 * @brief Controlador visual (JavaFX Controller) para la pantalla principal del Dashboard.
 * Gestiona el panel principal donde los usuarios visualizan sus tableros asociados,
 * tanto propios como compartidos. Permite la creación de tableros en blanco, la inicialización
 * a partir de archivos de plantilla YAML y la navegación interactiva hacia un tablero concreto.
 */
@Component
public class DashboardController {

    private final BoardService boardService;
    private final BoardMapper boardMapper;
    private final SceneManager sceneManager;
    private final PlantillaService plantillaService;
    
    /** Campo de texto FXML que almacena de forma inalterable el email del usuario en sesión. */
    @FXML private TextField txtEmail;
    
    /** Componente de lista FXML encargado de renderizar los títulos y roles de los tableros del usuario. */
    @FXML private ListView<String> listTableros;
    
    /** Colección local que actúa como caché en memoria de los tableros rehidratados de la consulta actual. */
    private List<BoardDTO> tablerosCargados;

    /**
     * @brief Constructor con inyección automatizada de componentes de aplicación e infraestructura.
     * @param boardService Puerto de entrada para interactuar con la lógica relacional de tableros.
     * @param boardMapper Mapeador auxiliar para transformar objetos de dominio a DTOs de vista.
     * @param sceneManager Administrador transversal encargado del enrutamiento gráfico de las escenas.
     * @param plantillaService Servicio técnico capaz de deserializar y construir estructuras desde YAML.
     */
    public DashboardController(
            BoardService boardService,
            BoardMapper boardMapper,
            SceneManager sceneManager,
            PlantillaService plantillaService) {

        this.boardService = boardService;
        this.boardMapper = boardMapper;
        this.sceneManager = sceneManager;
        this.plantillaService = plantillaService;
    }

    /**
     * @brief Inicializador automático del ciclo de vida del nodo de JavaFX.
     * Recupera de forma segura el email del usuario autenticado en la ventana previa, 
     * bloquea el cuadro de búsqueda para asegurar la integridad de acceso y lanza la 
     * carga inmediata de los datos asociados en la lista visual.
     */
    @FXML
    public void initialize() {
        String emailGuardado = sceneManager.getCurrentUserEmail();
        txtEmail.setText(emailGuardado); 
        txtEmail.setDisable(true); // Bloqueamos el campo para evitar su manipulación maliciosa en la interfaz
        
        handleCargarTableros(); 
    }

    /**
     * @brief Gestiona la acción de creación de un nuevo tablero Kanban vacío.
     * Despliega un cuadro flotante interactivo de captura de texto para adquirir el título deseado,
     * construye de forma determinista el comando inmutable y lo despacha al backend persistiendo los cambios.
     */
    @FXML
    public void handleCrearTablero() {
        String email = txtEmail.getText();

        if (email == null || email.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Falta Información");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, introduce tu email para poder asociar el nuevo tablero a tu cuenta.");
            alert.showAndWait();
            return;
        }
        sceneManager.setCurrentUserEmail(email);

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuevo Tablero");
        dialog.setHeaderText("Crear un nuevo tablero Kanban");
        dialog.setContentText("Introduce el título del tablero:");

        dialog.showAndWait().ifPresent(titulo -> {
            if (!titulo.isBlank()) {
                try {
                    CrearBoardCommand cmd = new CrearBoardCommand(titulo, email);
                    boardService.crearNuevoTablero(cmd);

                    // Refrescamos la lista automáticamente tras la creación exitosa
                    handleCargarTableros();

                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error al crear");
                    alert.setHeaderText("No se pudo crear el tablero");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            }
        });
    }
    
    /**
     * @brief Orquesta la recarga y renderizado de los tableros del usuario actual.
     * Consulta el puerto de entrada, mapea las colecciones del dominio a tipos de transferencia planos
     * y evalúa de manera adaptativa si el usuario ostenta el rango de propietario o invitado 
     * en cada entrada para formatear e inyectar el texto representativo final en el ListView.
     */
    @FXML
    public void handleCargarTableros() {
        String email = txtEmail.getText();
        
        try {
            if (email == null || email.isBlank()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Campo vacío");
                alert.setHeaderText(null);
                alert.setContentText("Por favor, introduce un correo electrónico para buscar.");
                alert.showAndWait();
                return;
            }
            sceneManager.setCurrentUserEmail(email);
            
            tablerosCargados = boardService.obtenerTablerosPorUsuario(email).stream()
                    .map(boardMapper::toDTO)
                    .toList();

            listTableros.getItems().clear();

            if (tablerosCargados.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Sin resultados");
                alert.setHeaderText(null);
                alert.setContentText("No se ha encontrado ningún tablero para este correo.");
                alert.showAndWait();
            } else {
                tablerosCargados.forEach(t -> {
                    String etiquetaRol;
                    
                    if (email.equalsIgnoreCase(t.getEmail())) {
                        etiquetaRol = "Propietario";
                    } 
                    else if (t.getPermisos() != null && t.getPermisos().containsKey(email)) {
                        etiquetaRol = t.getPermisos().get(email);
                    } 
                    else {
                        etiquetaRol = "Acceso desconocido";
                    }
                    
                    listTableros.getItems().add(t.getTitulo() + " — [" + etiquetaRol + "]");
                });
            }

        } catch (IllegalArgumentException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Correo inválido");
            alert.setHeaderText(null);
            alert.setContentText("Correo inválido.");
            alert.showAndWait();
            return;
        }
    }

    /**
     * @brief Maneja la selección de un tablero físico dentro del ListView.
     * Extrae el DTO indexado en la caché en concordancia con la selección y le encomienda 
     * al administrador de la ventana la navegación y trasbordo técnico de la vista detallada.
     */
    @FXML
    public void handleSeleccionarTablero() {
        int index = listTableros.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            BoardDTO tableroSeleccionado = tablerosCargados.get(index);
            this.sceneManager.navigateToBoard(tableroSeleccionado.getId());
        }
    }

    /**
     * @brief Despliega un menú interactivo para instanciar estructuras de tableros preconfiguradas.
     * Ofrece un catálogo flotante de archivos YAML (básica, ágil), parsea el fichero seleccionado 
     * construyendo el grafo transaccional de columnas y tareas por defecto, y refresca la interfaz.
     */
    @FXML
    public void handleCrearDesdePlantilla() {
        String email = txtEmail.getText();

        if (email == null || email.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Introduce primero tu email.");
            alert.showAndWait();
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                "basica.yaml",
                List.of(
                        "basica.yaml",
                        "plantilla-agil.yaml"
                )
        );

        dialog.setTitle("Crear desde plantilla");
        dialog.setHeaderText("Selecciona una plantilla");

        dialog.showAndWait().ifPresent(nombrePlantilla -> {
            plantillaService.crearTableroDesdePlantilla(
                    nombrePlantilla,
                    email
            );
            handleCargarTableros();
        });
    }

    /**
     * @brief Gestiona el cierre controlado de la sesión de usuario activa.
     * Purga los datos almacenados en memoria intermedia y revierte el escenario gráfico 
     * a la vista de login original.
     */
    @FXML
    public void handleCerrarSesion() {
        sceneManager.navigateToLogin();
    }
}
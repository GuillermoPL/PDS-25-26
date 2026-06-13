package es.um.pds.tableros.infrastructure.javafx.controllers;

import java.util.List;

import org.springframework.stereotype.Component;

import es.um.pds.tableros.domain.ports.input.board.BoardService;
import es.um.pds.tableros.domain.ports.input.board.commands.CrearBoardCommand;
import es.um.pds.tableros.infrastructure.javafx.SceneManager; // Importamos el manager
import es.um.pds.tableros.infrastructure.mappers.BoardMapper;
import es.um.pds.tableros.infrastructure.plantillas.PlantillaService;
import es.um.pds.tableros.infrastructure.rest.dto.BoardDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

@Component
public class DashboardController {

    private final BoardService boardService;
    private final BoardMapper boardMapper;
    private final SceneManager sceneManager; // Añadimos la dependencia
    private final PlantillaService plantillaService;
    
    @FXML private TextField txtEmail;
    @FXML private ListView<String> listTableros;
    private List<BoardDTO> tablerosCargados;

    // Spring se encarga de inyectar las tres piezas de forma automática
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
    @FXML
    public void initialize() {
        String emailGuardado = sceneManager.getCurrentUserEmail();
        // Ponemos el texto en el campo (puedes ponerlo deshabilitado para que no lo cambien)
        txtEmail.setText(emailGuardado); 
        txtEmail.setDisable(true); // Bloqueamos el campo para que no busquen los tableros de otro sin permiso
        
        // Cargamos los tableros de ese usuario automáticamente
        handleCargarTableros(); 
    }
    @FXML
    public void handleCrearTablero() {
        String email = txtEmail.getText();

        // 1. Validamos que el usuario haya introducido un email antes de dejarle crear un tablero
        if (email == null || email.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Falta Información");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, introduce tu email para poder asociar el nuevo tablero a tu cuenta.");
            alert.showAndWait();
            return;
        }
        sceneManager.setCurrentUserEmail(email);
        // 2. Abrimos un cuadro de diálogo flotante para pedir el título del tablero
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuevo Tablero");
        dialog.setHeaderText("Crear un nuevo tablero Kanban");
        dialog.setContentText("Introduce el título del tablero:");

        // 3. Si el usuario pulsa 'Aceptar', capturamos el título y llamamos al backend
        dialog.showAndWait().ifPresent(titulo -> {
            if (!titulo.isBlank()) {
                try {
                    // Construimos el comando inmutable y llamamos a tu servicio de aplicación
                    CrearBoardCommand cmd = new CrearBoardCommand(titulo, email);
                    boardService.crearNuevoTablero(cmd);

                    // 4. Refrescamos la lista automáticamente para que aparezca el tablero recién creado
                    handleCargarTableros();

                } catch (Exception e) {
                    // Capturamos cualquier error de validación (por ejemplo, si el email es inválido para el dominio)
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error al crear");
                    alert.setHeaderText("No se pudo crear el tablero");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            }
        });
    }
    
    
    @FXML
    public void handleCargarTableros() {
        String email = txtEmail.getText();
        
        try {
        	// Validamos primero que no le den a buscar con el campo vacío
            if (email == null || email.isBlank()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Campo vacío");
                alert.setHeaderText(null);
                alert.setContentText("Por favor, introduce un correo electrónico para buscar.");
                alert.showAndWait();
                return;
            }
            sceneManager.setCurrentUserEmail(email);
            // La vista solo llama al puerto de entrada de la aplicación
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
                    
                    // Comprobamos si el correo buscado es el del dueño
                    if (email.equalsIgnoreCase(t.getEmail())) {
                        etiquetaRol = "Propietario";
                    } 
                    // Si no es el dueño, buscamos su rol en el mapa de permisos
                    else if (t.getPermisos() != null && t.getPermisos().containsKey(email)) {
                        etiquetaRol = t.getPermisos().get(email);
                    } 
                    // Por seguridad, si hay un fallo y no está en ninguno
                    else {
                        etiquetaRol = "Acceso desconocido";
                    }
                    
                    // Añadimos el título + el rol a la lista visual
                    listTableros.getItems().add(t.getTitulo() + " — [" + etiquetaRol + "]");
                });
            }

        } catch (IllegalArgumentException e) {
            // Capturamos el portazo que ha dado el dominio dentro del servicio
        	Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Correo inválido");
            alert.setHeaderText(null);
            alert.setContentText("Correo inválido.");
            alert.showAndWait();
            return;
        }
    }

    @FXML
    public void handleSeleccionarTablero() {
        int index = listTableros.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            BoardDTO tableroSeleccionado = tablerosCargados.get(index);
            
            // Llamamos al manager para que efectúe el viaje de pantalla
            this.sceneManager.navigateToBoard(tableroSeleccionado.getId());
        }
    }
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
    @FXML
    public void handleCerrarSesion() {
        // Esto vacía el currentUserEmail y nos devuelve a la pantalla inicial
        sceneManager.navigateToLogin();
    }
}
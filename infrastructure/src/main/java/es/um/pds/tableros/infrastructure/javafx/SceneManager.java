package es.um.pds.tableros.infrastructure.javafx;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import es.um.pds.tableros.infrastructure.javafx.controllers.BoardViewController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @brief Gestor centralizado de navegación y sesiones visuales en JavaFX.
 * Se encarga de alternar las pantallas de la aplicación sobre el escenario principal,
 * inyectando el contexto de Spring en los cargadores FXML para que los controladores visuales
 * dispongan de inyección de dependencias automática.
 */
@Component
public class SceneManager {

    private final ApplicationContext springContext;
    private Stage primaryStage;
    private String currentUserEmail;

    /**
     * @brief Construye el gestor inyectando el contexto global de Spring.
     * @param springContext Contexto de aplicación para recuperar los Beans de los controladores.
     */
    public SceneManager(ApplicationContext springContext) {
        this.springContext = springContext;
    }
    
    /** @return Email del usuario que mantiene la sesión activa actual en la interfaz. */
    public String getCurrentUserEmail() {
        return currentUserEmail;
    }

    /**
     * @brief Establece el email del usuario logueado en la sesión gráfica actual.
     * @param currentUserEmail Correo del usuario autenticado.
     */
    public void setCurrentUserEmail(String currentUserEmail) {
        this.currentUserEmail = currentUserEmail;
    }

    /**
     * @brief Almacena la referencia de la ventana principal de visualización.
     * @param stage Ventana física activa del sistema.
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
    
    /**
     * @brief Redirecciona la interfaz a la pantalla de Login y purga los datos de sesión.
     */
    public void navigateToLogin() {
        this.currentUserEmail = null; // Limpiamos la sesión
        cambiarEscena("/fxml/Login.fxml", "Iniciar Sesión - Tableros Kanban");
    }
    
    /**
     * @brief Redirecciona la interfaz hacia la pantalla principal del Dashboard de tableros.
     */
    public void navigateToDashboard() {
        cambiarEscena("/fxml/Dashboard.fxml", "Mis Tableros PCEO");
    }

    /**
     * @brief Redirecciona la interfaz hacia la vista detallada de un tablero Kanban específico.
     * Extrae el controlador instanciado por Spring a través de la factoría de FXMLLoader para
     * pasarle el ID del tablero antes de renderizar la escena.
     * @param boardId Identificador único del tablero a inicializar y mostrar.
     * @throws RuntimeException Si acontece un error de lectura de E/S al cargar el archivo FXML.
     */
    public void navigateToBoard(String boardId) {
        try {
            // 1. Configuramos el cargador FXML enlazado al contenedor de Spring
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BoardView.fxml"));
            loader.setControllerFactory(springContext::getBean); // LA MAGIA: Spring crea el controlador

            Parent root = loader.load();

            // 2. Extraemos el controlador del FXML ya instanciado por Spring e inicializamos los datos
            BoardViewController controller = loader.getController();
            controller.inicializarTablero(boardId);

            // 3. Mostramos la escena en la ventana principal
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Tablero Kanban");
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (IOException e) {
            throw new RuntimeException("Error al cargar la pantalla del tablero: " + e.getMessage(), e);
        }
    }

    /**
     * @brief Método privado de utilidad para unificar la lógica de intercambio de escenas simples.
     * Configura la factoría de controladores de Spring para procesar correctamente las dependencias.
     * @param fxmlPath Ruta física del recurso .fxml dentro del directorio resources.
     * @param titulo Título de ventana que recibirá el escenario tras la carga.
     * @throws RuntimeException Si acontece un error crítico durante la carga de la vista FXML.
     */
    private void cambiarEscena(String fxmlPath, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(springContext::getBean);
            
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            primaryStage.setScene(scene);
            primaryStage.setTitle(titulo);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            //TODO Cambiar tipo de excepción
            throw new RuntimeException("Error al cambiar de escena a " + fxmlPath, e);
        }
    }
}
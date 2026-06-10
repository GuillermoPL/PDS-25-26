package es.um.pds.tableros.infrastructure.javafx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import es.um.pds.tableros.infrastructure.javafx.controllers.BoardViewController;

import java.io.IOException;

@Component
public class SceneManager {

    private final ApplicationContext springContext;
    private Stage primaryStage;
    private String currentUserEmail;

    // Inyectamos el contexto global de Spring para poder recuperar los Beans
    public SceneManager(ApplicationContext springContext) {
        this.springContext = springContext;
    }
    
    public String getCurrentUserEmail() {
        return currentUserEmail;
    }

    public void setCurrentUserEmail(String currentUserEmail) {
        this.currentUserEmail = currentUserEmail;
    }

    /**
     * Guarda la referencia de la ventana principal al arrancar.
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Cambia la ventana a la pantalla del Dashboard.
     */
    public void navigateToDashboard() {
        cambiarEscena("/fxml/Dashboard.fxml", "Mis Tableros PCEO");
    }

    /**
     * Cambia la ventana a un tablero específico pasándole su ID al controlador.
     */
    public void navigateToBoard(String boardId) {
        try {
            // 1. Configuramos el cargador FXML enlazado al contenedor de Spring
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BoardView.fxml"));
            loader.setControllerFactory(springContext::getBean); // <--- LA MAGIA: Spring crea el controlador

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
     * Método auxiliar genérico para cargar escenas simples (como el Dashboard).
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
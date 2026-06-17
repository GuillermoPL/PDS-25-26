package es.um.pds.tableros.infrastructure.javafx;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import es.um.pds.tableros.infrastructure.TablerosApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * @brief Adaptador que acopla y coordina los ciclos de vida de JavaFX y Spring Boot.
 * Hereda de la clase Application de JavaFX para gestionar el ciclo completo de inicialización,
 * arranque y apagado unificado de ambos frameworks.
 */
public class JavaFXApplication extends Application {

    private ConfigurableApplicationContext springContext;

    /**
     * @brief Fase de inicialización previa a la interfaz.
     * Levanta de fondo el contenedor de Spring Boot escaneando la clase de configuración principal.
     */
    @Override
    public void init() {
        String[] args = getParameters().getRaw().toArray(new String[0]);
        this.springContext = new SpringApplicationBuilder()
                .sources(TablerosApplication.class)
                .run(args);
    }

    /**
     * @brief Punto de arranque de la interfaz gráfica del sistema.
     * Recupera el gestor de escenas del contexto de Spring, le asigna la ventana primaria y
     * dirige el flujo del usuario hacia la pantalla de login inicial.
     * @param primaryStage Escenario o ventana física principal proporcionada por el sistema operativo.
     */
    @Override
    public void start(Stage primaryStage) {
        // 1. Sacamos el SceneManager del mundo Spring
        SceneManager sceneManager = this.springContext.getBean(SceneManager.class);
        
        // 2. Le entregamos la ventana principal
        sceneManager.setPrimaryStage(primaryStage);
        
        // 3. Ahora viajamos al Login primero
        sceneManager.navigateToLogin();
    }

    /**
     * @brief Fase de parada de la aplicación gráfica.
     * Asegura el cierre limpio y ordenado del contexto de Spring Boot al cerrarse las ventanas,
     * liberando los hilos, conexiones y recursos antes de terminar el proceso.
     */
    @Override
    public void stop() {
        // Al cerrar la ventana de JavaFX, apagamos Spring limpiamente
        this.springContext.close();
        Platform.exit();
    }
}
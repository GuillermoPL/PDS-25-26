package es.um.pds.tableros.infrastructure.javafx;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import es.um.pds.tableros.infrastructure.TablerosApplication; // La clase con el @SpringBootApplication
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class JavaFXApplication extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        // Al inicializar JavaFX, arrancamos de fondo el contenedor de Spring Boot
        String[] args = getParameters().getRaw().toArray(new String[0]);
        this.springContext = new SpringApplicationBuilder()
                .sources(TablerosApplication.class) // Apuesta aquí a tu clase principal de Spring
                .run(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // 1. Sacamos tu SceneManager del mundo Spring
        SceneManager sceneManager = this.springContext.getBean(SceneManager.class);
        
        // 2. Le entregamos la ventana principal
        sceneManager.setPrimaryStage(primaryStage);
        
        // 3. Ahora viajamos al Login primero
        sceneManager.navigateToLogin();
    }

    @Override
    public void stop() {
        // Al cerrar la ventana de JavaFX, apagamos Spring limpiamente
        this.springContext.close();
        Platform.exit();
    }
}
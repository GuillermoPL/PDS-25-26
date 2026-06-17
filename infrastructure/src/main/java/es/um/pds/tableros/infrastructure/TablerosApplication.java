package es.um.pds.tableros.infrastructure;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import es.um.pds.tableros.infrastructure.javafx.JavaFXApplication;
import javafx.application.Application;

/**
 * @brief Clase principal y punto de entrada (Bootstrapper) de la aplicación.
 * Configura el arranque inicial activando el escaneo de componentes de Spring
 * y habilitando el motor de tareas programadas (Scheduling) del sistema.
 */
@SpringBootApplication(scanBasePackages = "es.um.pds.tableros")
@EnableScheduling
public class TablerosApplication {

    /**
     * @brief Método main principal que arranca la ejecución del programa.
     * En lugar de arrancar Spring directamente, delega el control inicial al motor
     * de lanzamiento de interfaces de JavaFX pasándole la clase de inicialización.
     * @param args Argumentos de la línea de comandos.
     */
    public static void main(String[] args) {
        // En lugar de SpringApplication.run, llamamos al launch de JavaFX
        Application.launch(JavaFXApplication.class, args);
    }
}
package es.um.pds.tableros.infrastructure;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import es.um.pds.tableros.infrastructure.javafx.JavaFXApplication;

@SpringBootApplication(scanBasePackages = "es.um.pds.tableros")
@EnableScheduling
public class TablerosApplication {

    public static void main(String[] args) {
        // En lugar de SpringApplication.run, llamamos al launch de JavaFX
        Application.launch(JavaFXApplication.class, args);
    }
}
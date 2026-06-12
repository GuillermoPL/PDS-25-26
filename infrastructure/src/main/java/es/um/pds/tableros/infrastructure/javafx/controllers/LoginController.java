package es.um.pds.tableros.infrastructure.javafx.controllers;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import es.um.pds.tableros.infraestructure.security.AuthSessionManager;
import es.um.pds.tableros.infrastructure.javafx.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

@Component
public class LoginController {

    private final AuthSessionManager sessionManager;
    private final JavaMailSender mailSender;
    private final SceneManager sceneManager;

    @FXML private TextField txtEmail;
    @FXML private Button btnSolicitar;
    @FXML private VBox cajaCodigo;
    @FXML private TextField txtCodigo;
    @FXML private Button btnEntrar;
    @FXML private Label lblMensaje;

    public LoginController(AuthSessionManager sessionManager, JavaMailSender mailSender, SceneManager sceneManager) {
        this.sessionManager = sessionManager;
        this.mailSender = mailSender;
        this.sceneManager = sceneManager;
    }

    @FXML
    public void handleSolicitarCodigo() {
        String email = txtEmail.getText().trim();
        if (email.isBlank()) {
            lblMensaje.setText("Por favor, introduce un correo válido.");
            lblMensaje.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        btnSolicitar.setDisable(true);
        btnSolicitar.setText("Enviando...");
        lblMensaje.setText("");

        try {
            // 1. Generamos el código
            String codigo = sessionManager.generarYGuardarCodigo(email);

            // 2. Enviamos el correo
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("guillermofparralopez@gmail.com");
            message.setTo(email);
            message.setSubject("Tu código de acceso a Tableros Kanban");
            message.setText("Tu código de acceso es: " + codigo + "\n\nSerá válido durante 5 minutos.");
            mailSender.send(message);

            // 3. Activamos la sección del código
            txtEmail.setDisable(true);
            btnSolicitar.setText("Código enviado ✓");
            cajaCodigo.setDisable(false);
            cajaCodigo.setOpacity(1.0);
            lblMensaje.setText("Revisa tu bandeja de entrada (y la carpeta de Spam).");
            lblMensaje.setStyle("-fx-text-fill: #27ae60;"); // Verde

        } catch (Exception e) {
            System.err.println("=== ERROR ENVIO CORREO ===");
            e.printStackTrace();

            Throwable causa = e;
            while (causa != null) {
                System.err.println(
                    causa.getClass().getName() +
                    ": " +
                    causa.getMessage()
                );
                causa = causa.getCause();
            }

            lblMensaje.setText("Error al enviar correo. Mira la consola.");
        }   
    }

    @FXML
    public void handleEntrar() {
        String email = txtEmail.getText().trim();
        String codigo = txtCodigo.getText().trim();

        if (codigo.isBlank()) {
            lblMensaje.setText("Introduce el código recibido.");
            lblMensaje.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        // Verificamos en el mapa de memoria si el código es correcto y no ha caducado
        boolean valido = sessionManager.verificarYRenovarCodigo(email, codigo);

        if (valido) {
            // Guardamos el usuario autenticado en la sesión visual
            sceneManager.setCurrentUserEmail(email);
            // Redirigimos al dashboard
            sceneManager.navigateToDashboard();
        } else {
            lblMensaje.setText("Código incorrecto o caducado. Vuelve a intentarlo.");
            lblMensaje.setStyle("-fx-text-fill: #e74c3c;");
        }
    }
}
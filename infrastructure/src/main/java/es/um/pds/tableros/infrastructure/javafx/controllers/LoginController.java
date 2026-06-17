package es.um.pds.tableros.infrastructure.javafx.controllers;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import es.um.pds.tableros.infrastructure.javafx.SceneManager;
import es.um.pds.tableros.infrastructure.security.AuthSessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * @brief Controlador visual (JavaFX Controller) encargado de gestionar la pantalla de Login.
 * Gestiona el flujo de autenticación mediante el envío y validación de códigos de acceso OTP 
 * (One-Time Password) por correo electrónico, interactuando con la sesión gráfica y los managers de seguridad.
 */
@Component
public class LoginController {

    private final AuthSessionManager sessionManager;
    private final JavaMailSender mailSender;
    private final SceneManager sceneManager;

    /** Caja de texto para que el usuario introduzca su correo electrónico. */
    @FXML private TextField txtEmail;
    
    /** Botón para solicitar el envío del código OTP al correo. */
    @FXML private Button btnSolicitar;
    
    /** Contenedor visual intermedio que se activa tras el envío exitoso del código. */
    @FXML private VBox cajaCodigo;
    
    /** Caja de texto para introducir el código numérico recibido. */
    @FXML private TextField txtCodigo;
    
    /** Botón para procesar la validación del código e iniciar sesión. */
    @FXML private Button btnEntrar;
    
    /** Etiqueta de texto destinada a mostrar mensajes informativos o errores al usuario. */
    @FXML private Label lblMensaje;

    /**
     * @brief Constructor del controlador con inyección de componentes de infraestructura y seguridad.
     * @param sessionManager Almacén y validador de códigos de sesión activos en memoria.
     * @param mailSender Infraestructura técnica de Spring para despachar correos electrónicos.
     * @param sceneManager Coordinador central de la navegación de ventanas en JavaFX.
     */
    public LoginController(AuthSessionManager sessionManager, JavaMailSender mailSender, SceneManager sceneManager) {
        this.sessionManager = sessionManager;
        this.mailSender = mailSender;
        this.sceneManager = sceneManager;
    }

    /**
     * @brief Maneja el evento de pulsación sobre el botón de solicitar código.
     * Lee y valida la cadena de texto del correo, genera el token dinámico de acceso a través 
     * de infraestructura, despacha el mensaje SMTP e interactúa de forma adaptativa con los 
     * componentes gráficos habilitando la sección de introducción del código.
     */
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

    /**
     * @brief Maneja el evento de pulsación sobre el botón de entrar.
     * Extrae el código de la interfaz y lo valida delegando en el control temporal de `AuthSessionManager`.
     * Si la sesión resulta legítima, almacena el email en el SceneManager visual y orquesta la 
     * redirección inmediata de la escena gráfica hacia el Dashboard principal de tableros.
     */
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
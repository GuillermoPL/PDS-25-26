package es.um.pds.tableros.infrastructure.rest;

import org.springframework.web.bind.annotation.*;

import es.um.pds.tableros.infrastructure.security.AuthSessionManager;

import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * @brief Adaptador de Entrada (Input Adapter) REST dedicado a la autenticación del sistema.
 * Expone el punto de entrada público para la solicitud de códigos dinámicos de acceso
 * y coordina la notificación del token al usuario a través del servicio de mensajería (JavaMailSender).
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthEndpoint {

    private final AuthSessionManager sessionManager;
    private final JavaMailSender mailSender;

    /**
     * @brief Constructor con inyección de componentes de seguridad e infraestructura de correo.
     * @param sessionManager Componente que centraliza la memoria y validez de las sesiones.
     * @param mailSender Proveedor técnico de infraestructura para envíos SMTP de Spring.
     */
    public AuthEndpoint(AuthSessionManager sessionManager, JavaMailSender mailSender) {
        this.sessionManager = sessionManager;
        this.mailSender = mailSender;
    }

    /**
     * @brief Genera un código de inicio de sesión y lo despacha al correo del usuario.
     * Este endpoint está explícitamente exento de validación en la aduana del interceptor.
     * @param email Dirección de correo destino introducida en el formulario de login.
     * @return ResponseEntity con estado 200 OK tras completar el proceso de envío.
     */
    @PostMapping("/solicitar-codigo")
    public ResponseEntity<Void> solicitarCodigo(@RequestParam String email) {
        String codigo = sessionManager.generarYGuardarCodigo(email);
        
        // Enviar el correo
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Tu código de acceso a Tableros Kanban");
        message.setText("Tu código de acceso es: " + codigo + "\nEs válido por 5 minutos.");
        mailSender.send(message);
        
        return ResponseEntity.ok().build();
    }
}
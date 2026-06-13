package es.um.pds.tableros.infrastructure.rest;

import org.springframework.web.bind.annotation.*;

import es.um.pds.tableros.infrastructure.security.AuthSessionManager;

import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthEndpoint {

    private final AuthSessionManager sessionManager;
    private final JavaMailSender mailSender;

    public AuthEndpoint(AuthSessionManager sessionManager, JavaMailSender mailSender) {
        this.sessionManager = sessionManager;
        this.mailSender = mailSender;
    }

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
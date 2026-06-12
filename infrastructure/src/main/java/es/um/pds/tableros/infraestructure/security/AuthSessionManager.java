package es.um.pds.tableros.infraestructure.security;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

@Component
public class AuthSessionManager {
    
    // Mapa: Email -> Datos de Sesión
    private final Map<String, SessionData> sesiones = new ConcurrentHashMap<>();

    public String generarYGuardarCodigo(String email) {
        // Genera código de 6 dígitos
        String codigo = String.format("%06d", new Random().nextInt(999999));
        sesiones.put(email, new SessionData(codigo, LocalDateTime.now().plusMinutes(5)));
        return codigo;
    }

    public boolean verificarYRenovarCodigo(String email, String codigo) {
        SessionData data = sesiones.get(email);
        
        if (data == null || !data.codigo().equals(codigo)) {
            return false; // No existe o el código es incorrecto
        }
        
        if (LocalDateTime.now().isAfter(data.expiracion())) {
            sesiones.remove(email); // Ha caducado
            return false;
        }

        // Si es válido, aplicamos la regla: renovar por 5 minutos más
        sesiones.put(email, new SessionData(codigo, LocalDateTime.now().plusMinutes(5)));
        return true;
    }

    private record SessionData(String codigo, LocalDateTime expiracion) {}
}
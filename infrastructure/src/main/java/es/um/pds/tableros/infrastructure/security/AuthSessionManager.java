package es.um.pds.tableros.infrastructure.security;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * @brief Gestor y almacén de sesiones de seguridad volátiles en memoria.
 * Componente centralizado en la infraestructura encargado de la creación, verificación, 
 * control temporal de expiración y renovación de los códigos de sesión.
 */
@Component
public class AuthSessionManager {
    
    /**  Mapa concurrente seguro para hilos (Thread-safe) que asocia el email de un usuario con sus datos de sesión actuales.
     */
    private final Map<String, SessionData> sesiones = new ConcurrentHashMap<>();

    /**
     * @brief Genera un nuevo código numérico aleatorio y crea un registro de sesión para el usuario.
     * El código generado se establece por defecto con una duración máxima de inactividad de 5 minutos.
     * @param email Correo electrónico del usuario autenticado que actúa como clave de la sesión.
     * @return Cadena de texto formateada con el código de seguridad de 6 dígitos.
     */
    public String generarYGuardarCodigo(String email) {
        // Genera código de 6 dígitos
        String codigo = String.format("%06d", new Random().nextInt(999999));
        sesiones.put(email, new SessionData(codigo, LocalDateTime.now().plusMinutes(5)));
        return codigo;
    }

    /**
     * @brief Valida la veracidad y el tiempo de vida de un código de acceso proporcionado por el cliente.
     * Si el código coincide y no ha expirado, se aplica la regla de ventana deslizante extendiendo automáticamente 
     * el tiempo de vida de la sesión otros 5 minutos a partir del momento actual.
     * @param email Correo electrónico del usuario que realiza la petición.
     * @param codigo Código de seguridad enviado en las cabeceras HTTP para su comprobación.
     * @return true si el código es correcto y la sesión está activa; false si es erróneo, inexistente o caducado.
     */
    public boolean verificarYRenovarCodigo(String email, String codigo) {
        SessionData data = sesiones.get(email);
        
        if (data == null || !data.codigo().equals(codigo)) {
            return false; // No existe o el código es incorrecto
        }
        
        if (LocalDateTime.now().isAfter(data.expiracion())) {
            sesiones.remove(email); // Limpieza: Ha caducado, se purga de la memoria
            return false;
        }

        // Si es válido, aplicamos la regla: renovar por 5 minutos más a partir de este instante
        sesiones.put(email, new SessionData(codigo, LocalDateTime.now().plusMinutes(5)));
        return true;
    }

    /**
     * @brief Estructura de datos interna (Record) que almacena los metadatos de una sesión activa.
     * @param codigo Código numérico de autorización.
     * @param expiracion Marca temporal estricta de finalización de validez de la sesión.
     */
    private record SessionData(String codigo, LocalDateTime expiracion) {}
}
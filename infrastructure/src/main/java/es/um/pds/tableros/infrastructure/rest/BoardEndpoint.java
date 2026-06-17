package es.um.pds.tableros.infrastructure.rest;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import es.um.pds.tableros.domain.ports.input.board.BoardService;
import es.um.pds.tableros.domain.ports.input.board.commands.*;
import es.um.pds.tableros.infrastructure.rest.dto.BoardDTO;
import es.um.pds.tableros.infrastructure.mappers.BoardMapper; 

/**
 * @brief Adaptador de Entrada (Input Adapter) REST para la gestión  de tableros.
 * Expone las APIs HTTP operativas mapeando peticiones web estructuradas hacia los comandos e 
 * interfaces de consulta del puerto de entrada {@link BoardService}.
 */
@RestController
@RequestMapping("/api/v1/tableros")
public class BoardEndpoint {

    private static final Logger log = LoggerFactory.getLogger(BoardEndpoint.class);

    private final BoardService boardService;
    private final BoardMapper boardMapper;

    /**
     * @brief Constructor que inyecta el caso de uso del dominio y el traductor de datos.
     * @param boardService Puerto de entrada para las reglas operativas de tableros.
     * @param boardMapper Mapeador para aislar la estructura interna de dominio.
     */
    public BoardEndpoint(BoardService boardService, BoardMapper boardMapper) {
        this.boardService = boardService;
        this.boardMapper = boardMapper;
    }

    /**
     * @brief Recupera la configuración detallada de un tablero Kanban por su ID único.
     * Aplica un control explícito de lectura contrastando el header de sesión 
     * con la lista de permisos internos reconstruidos del agregado.
     * @param id Identificador de ruta del tablero deseado.
     * @param emailUsuario Email del solicitante extraído de la cabecera 'X-User-Email'.
     * @return ResponseEntity conteniendo el BoardDTO con estado 200 OK, 404 NOT FOUND si no existe, o 403 FORBIDDEN si carece de acceso.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BoardDTO> getTablero(
            @PathVariable("id") String id, 
            @RequestHeader(value = "X-User-Email", required = false) String emailUsuario) {
        try {
            Optional<BoardDTO> dtoOpt = boardService.obtenerTableroPorId(id)
                                                    .map(boardMapper::toDTO);
            
            if (dtoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            BoardDTO dto = dtoOpt.get();

            if (emailUsuario == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            boolean esDueno = emailUsuario.equalsIgnoreCase(dto.getEmail());
            boolean tienePermiso = esDueno || (dto.getPermisos() != null && dto.getPermisos().containsKey(emailUsuario));
            
            if (!tienePermiso) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok(dto);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * @brief Da de alta un nuevo tablero Kanban en el sistema.
     * @param dto Datos JSON de entrada con el formato de inicialización del tablero.
     * @return ResponseEntity con el BoardDTO resultante y estado 201 Created, o 400 Bad Request si el formato es inválido.
     */
    @PostMapping
    public ResponseEntity<BoardDTO> createTablero(@Valid @RequestBody BoardDTO dto) { 
        log.info("Petición para crear tablero '{}'", dto.getTitulo());
        
        if (dto.getId() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); 
        }

        try {
            CrearBoardCommand cmd = new CrearBoardCommand(dto.getTitulo(), dto.getEmail());
            var nuevoTablero = boardService.crearNuevoTablero(cmd);
            return ResponseEntity.status(HttpStatus.CREATED).body(boardMapper.toDTO(nuevoTablero));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * @brief Añade una nueva lista (columna de tareas) al tablero especificado.
     * @param id Identificador de ruta del tablero contenedor.
     * @param emailUsuario Email del solicitante para auditoría y verificación de permisos de escritura.
     * @param payload Objeto JSON con los parámetros de la nueva columna (nombre y límite).
     * @return ResponseEntity con estado 200 OK, 403 FORBIDDEN si no tiene permisos, o 400 BAD REQUEST ante anomalías.
     */
    @PostMapping("/{id}/listas")
    public ResponseEntity<Void> anadirLista(
            @PathVariable("id") String id, 
            @RequestHeader(value = "X-User-Email", required = false) String emailUsuario,
            @Valid @RequestBody AnadirListCommandPayload payload) { 
        try {
            AnadirListCommand cmd = new AnadirListCommand(id, payload.nombreLista(), payload.maxCards(), emailUsuario);
            boardService.anadirListaATablero(cmd);
            return ResponseEntity.ok().build();
            
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * @brief Modifica el estado de bloqueo temporal sobre un tablero operativo.
     * @param id Identificador del tablero objetivo.
     * @param emailUsuario Dirección de correo electrónico de quien emite la instrucción.
     * @param bloquear Parámetro de consulta booleano (true para bloquear, false para reanudar).
     * @return ResponseEntity con estado 200 OK, 403 o 400 según las reglas de negocio interceptadas.
     */
    @PutMapping("/{id}/bloqueo")
    public ResponseEntity<Void> cambiarBloqueo(
            @PathVariable("id") String id, 
            @RequestHeader(value = "X-User-Email", required = false) String emailUsuario,
            @RequestParam("bloquear") boolean bloquear) { 
        try {
            CambiarBloqueoBoardCommand cmd = new CambiarBloqueoBoardCommand(id, bloquear, emailUsuario);
            boardService.cambiarEstadoBloqueo(cmd);
            return ResponseEntity.ok().build();
            
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * @brief Concede permisos de acceso (READ o WRITE) a un usuario invitado.
     * @param id Identificador del tablero.
     * @param emailSolicitante Cabecera HTTP que identifica al dueño exclusivo autorizado para realizar la acción.
     * @param payload JSON con los metadatos de la invitación (email invitado y nivel de rol).
     * @return ResponseEntity con estado 200 OK en flujo de éxito.
     */
    @PostMapping("/{id}/permisos")
    public ResponseEntity<Void> compartirTablero(
            @PathVariable("id") String id, 
            @RequestHeader("X-User-Email") String emailSolicitante,
            @Valid @RequestBody PermisosPayload payload) { 
        try {
            CompartirBoardCommand cmd = new CompartirBoardCommand(
                    id, emailSolicitante, payload.emailInvitado(), payload.rol());
            boardService.compartirTablero(cmd);
            return ResponseEntity.ok().build();
            
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * @brief Revoca permanentemente los permisos de visualización o edición a un usuario colaborador.
     * @param id Identificador del tablero.
     * @param emailAEliminar Dirección de correo del invitado al que se le retiran las credenciales.
     * @param emailSolicitante Cabecera de control que valida que la petición provenga del dueño.
     * @return ResponseEntity con estado 200 OK o errores controlados por excepciones de aplicación.
     */
    @DeleteMapping("/{id}/permisos/{emailAEliminar}")
    public ResponseEntity<Void> revocarAcceso(
            @PathVariable("id") String id, 
            @PathVariable("emailAEliminar") String emailAEliminar, 
            @RequestHeader("X-User-Email") String emailSolicitante) {
        try {
            boardService.revocarAcceso(id, emailSolicitante, emailAEliminar);
            return ResponseEntity.ok().build();
            
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * @brief Record auxiliar (Payload DTO) para estructurar el JSON de concesión de permisos.
     * @param emailInvitado Dirección de correo electrónico a añadir.
     * @param rol Cadena representativa ("READ" o "WRITE").
     */
    public record PermisosPayload(String emailInvitado, String rol) {}
    
    /**
     * @brief Record auxiliar (Payload DTO) para estructurar el JSON de creación de columnas.
     * @param nombreLista Título de la columna.
     * @param maxCards Limite de tarjetas concurrentes toleradas.
     */
    public record AnadirListCommandPayload(String nombreLista, Integer maxCards) {}
}
package es.um.pds.tableros.infrastructure.rest;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.um.pds.tableros.domain.ports.input.board.BoardService;
import es.um.pds.tableros.domain.ports.input.board.commands.*;
import es.um.pds.tableros.infrastructure.rest.dto.BoardDTO;
import es.um.pds.tableros.infrastructure.mappers.BoardMapper; 

@RestController
@RequestMapping("/api/v1/tableros")
public class BoardEndpoint {

    private static final Logger log = LoggerFactory.getLogger(BoardEndpoint.class);

    private final BoardService boardService;
    private final BoardMapper boardMapper;

    public BoardEndpoint(BoardService boardService, BoardMapper boardMapper) {
        this.boardService = boardService;
        this.boardMapper = boardMapper;
    }

    // 1. Obtener un tablero por ID
    @GetMapping("/{id}")
    public ResponseEntity<BoardDTO> getTablero(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Email", required = false) String emailUsuario) {
        try {
            // Mapeamos a DTO primero, sin tocar la entidad de dominio
            Optional<BoardDTO> dtoOpt = boardService.obtenerTableroPorId(id)
                                                    .map(boardMapper::toDTO);
            
            if (dtoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            BoardDTO dto = dtoOpt.get();

            // Verificamos permisos leyendo los datos planos del DTO (como hicimos en JavaFX)
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

    // 2. Crear un nuevo tablero
    @PostMapping
    public ResponseEntity<BoardDTO> createTablero(@RequestBody BoardDTO dto) {
        log.info("Petición para crear tablero '{}'", dto.getTitulo());
        
        if (dto.getId() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); 
        }

        try {
            CrearBoardCommand cmd = new CrearBoardCommand(dto.getTitulo(), dto.getEmail());
            // No hay que verificar permisos aquí porque es un tablero nuevo
            var nuevoTablero = boardService.crearNuevoTablero(cmd);
            return ResponseEntity.status(HttpStatus.CREATED).body(boardMapper.toDTO(nuevoTablero));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // 3. Añadir una lista/columna al tablero
    @PostMapping("/{id}/listas")
    public ResponseEntity<Void> anadirLista(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Email", required = false) String emailUsuario,
            @RequestBody AnadirListCommandPayload payload) {
        try {
            // Pasamos el email del usuario al comando. El servicio decidirá si lanza IllegalStateException.
            AnadirListCommand cmd = new AnadirListCommand(id, payload.nombreLista(), payload.maxCards(), emailUsuario);
            boardService.anadirListaATablero(cmd);
            return ResponseEntity.ok().build();
            
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // 4. Cambiar estado de bloqueo
    @PutMapping("/{id}/bloqueo")
    public ResponseEntity<Void> cambiarBloqueo(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Email", required = false) String emailUsuario,
            @RequestParam boolean bloquear) {
        try {
            // Pasamos el email del usuario al comando
            CambiarBloqueoBoardCommand cmd = new CambiarBloqueoBoardCommand(id, bloquear, emailUsuario);
            boardService.cambiarEstadoBloqueo(cmd);
            return ResponseEntity.ok().build();
            
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // 5. Compartir con alguien
    @PostMapping("/{id}/permisos")
    public ResponseEntity<Void> compartirTablero(
            @PathVariable String id,
            @RequestHeader("X-User-Email") String emailSolicitante,
            @RequestBody PermisosPayload payload) {
        try {
            // El comando ya estaba preparado con emailSolicitante
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

    // 6. Revocar acceso
    @DeleteMapping("/{id}/permisos/{emailAEliminar}")
    public ResponseEntity<Void> revocarAcceso(
            @PathVariable String id,
            @PathVariable String emailAEliminar,
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

    // Records auxiliares
    public record PermisosPayload(String emailInvitado, String rol) {}
    public record AnadirListCommandPayload(String nombreLista, Integer maxCards) {}
}
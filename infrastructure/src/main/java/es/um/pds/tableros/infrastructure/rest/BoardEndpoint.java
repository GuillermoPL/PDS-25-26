package es.um.pds.tableros.infrastructure.rest;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid; // Asegúrate de tener este import

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
            @PathVariable("id") String id, // <--- AÑADIR ("id")
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

    // 2. Crear un nuevo tablero
    @PostMapping
    public ResponseEntity<BoardDTO> createTablero(@Valid @RequestBody BoardDTO dto) { // <--- AÑADIR @Valid
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

    // 3. Añadir una lista/columna al tablero
    @PostMapping("/{id}/listas")
    public ResponseEntity<Void> anadirLista(
            @PathVariable("id") String id, // <--- AÑADIR ("id")
            @RequestHeader(value = "X-User-Email", required = false) String emailUsuario,
            @Valid @RequestBody AnadirListCommandPayload payload) { // <--- AÑADIR @Valid
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

    // 4. Cambiar estado de bloqueo
    @PutMapping("/{id}/bloqueo")
    public ResponseEntity<Void> cambiarBloqueo(
            @PathVariable("id") String id, // <--- AÑADIR ("id")
            @RequestHeader(value = "X-User-Email", required = false) String emailUsuario,
            @RequestParam("bloquear") boolean bloquear) { // <--- AÑADIR ("bloquear") por precaución
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

    // 5. Compartir con alguien
    @PostMapping("/{id}/permisos")
    public ResponseEntity<Void> compartirTablero(
            @PathVariable("id") String id, // <--- AÑADIR ("id")
            @RequestHeader("X-User-Email") String emailSolicitante,
            @Valid @RequestBody PermisosPayload payload) { // <--- AÑADIR @Valid
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

    // 6. Revocar acceso
    @DeleteMapping("/{id}/permisos/{emailAEliminar}")
    public ResponseEntity<Void> revocarAcceso(
            @PathVariable("id") String id, // <--- AÑADIR ("id")
            @PathVariable("emailAEliminar") String emailAEliminar, // <--- AÑADIR ("emailAEliminar")
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
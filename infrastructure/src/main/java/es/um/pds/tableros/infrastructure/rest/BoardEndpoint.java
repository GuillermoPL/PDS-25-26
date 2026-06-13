package es.um.pds.tableros.infrastructure.rest;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.board.Email;
import es.um.pds.tableros.domain.ports.input.board.BoardService;
import es.um.pds.tableros.domain.ports.input.board.commands.*;
import es.um.pds.tableros.infrastructure.rest.dto.BoardDTO;
import es.um.pds.tableros.infrastructure.mappers.BoardMapper; 
import es.um.pds.tableros.domain.board.Rol;
import es.um.pds.tableros.domain.ports.input.board.commands.CompartirBoardCommand;
@RestController
@RequestMapping("/api/v1/tableros") // Puedes parametrizarlo con ${...} si quieres
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
            Optional<Board> boardOpt = boardService.obtenerTableroPorId(id);
            if (boardOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            Board board = boardOpt.get();

            // Verificamos que quien pide tiene al menos rol READ
            if (emailUsuario == null || board.obtenerRol(new Email(emailUsuario)) == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok(boardMapper.toDTO(board));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // 2. Crear un nuevo tablero
    @PostMapping
    public ResponseEntity<BoardDTO> createTablero(@RequestBody BoardDTO dto) {
        log.info("Petición para crear tablero '{}'", dto.getTitulo());
        
        if (dto.getId() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // Un tablero nuevo no debe traer ID
        }

        try {
            CrearBoardCommand cmd = new CrearBoardCommand(dto.getTitulo(), dto.getEmail());
            Board nuevoTablero = boardService.crearNuevoTablero(cmd);
            return ResponseEntity.status(HttpStatus.CREATED).body(boardMapper.toDTO(nuevoTablero));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // 3. Añadir una lista/columna al tablero
    // Petición POST a /api/v1/tableros/{id}/listas pasándole un JSON con el nombre y límite max
    @PostMapping("/{id}/listas")
    public ResponseEntity<Void> anadirLista(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Email", required = false) String emailUsuario,
            @RequestBody AnadirListCommandPayload payload) {
        try {
            Board board = boardService.obtenerTableroPorId(id)
                    .orElse(null);
            if (board == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            if (!tienePermisoEscritura(board, emailUsuario)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            AnadirListCommand cmd = new AnadirListCommand(id, payload.nombreLista(), payload.maxCards());
            boardService.anadirListaATablero(cmd);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // 4. Cambiar estado de bloqueo (Bloquear/Desbloquear)
    @PutMapping("/{id}/bloqueo")
    public ResponseEntity<Void> cambiarBloqueo(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Email", required = false) String emailUsuario,
            @RequestParam boolean bloquear) {
        try {
            Board board = boardService.obtenerTableroPorId(id)
                    .orElse(null);
            if (board == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            if (!tienePermisoEscritura(board, emailUsuario)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            boardService.cambiarEstadoBloqueo(new CambiarBloqueoBoardCommand(id, bloquear));
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
 // POST /api/v1/tableros/{id}/permisos  → compartir con alguien
    @PostMapping("/{id}/permisos")
    public ResponseEntity<Void> compartirTablero(
            @PathVariable String id,
            @RequestHeader("X-User-Email") String emailSolicitante,
            @RequestBody PermisosPayload payload) {
        try {
            CompartirBoardCommand cmd = new CompartirBoardCommand(
                    id, emailSolicitante, payload.emailInvitado(), payload.rol());
            boardService.compartirTablero(cmd);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // DELETE /api/v1/tableros/{id}/permisos/{email}  → revocar acceso
    @DeleteMapping("/{id}/permisos/{emailAEliminar}")
    public ResponseEntity<Void> revocarAcceso(
            @PathVariable String id,
            @PathVariable String emailAEliminar,
            @RequestHeader("X-User-Email") String emailSolicitante) {
        try {
            boardService.revocarAcceso(id, emailSolicitante, emailAEliminar);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // Record auxiliar
    public record PermisosPayload(String emailInvitado, String rol) {}
    // Sub-record auxiliar para leer el cuerpo HTTP al crear listas de forma limpia
    public record AnadirListCommandPayload(String nombreLista, Integer maxCards) {}
    private boolean tienePermisoEscritura(Board board, String emailUsuario) {
        if (emailUsuario == null) return false;
        Rol rol = board.obtenerRol(new Email(emailUsuario));
        return Rol.WRITE.equals(rol);
    }
}
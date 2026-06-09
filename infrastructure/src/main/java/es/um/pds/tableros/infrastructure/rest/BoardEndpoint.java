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
import es.um.pds.tableros.domain.ports.input.board.BoardService;
import es.um.pds.tableros.domain.ports.input.board.commands.*;
import es.um.pds.tableros.infrastructure.rest.dto.BoardDTO;
import es.um.pds.tableros.infrastructure.mappers.BoardMapper; 

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
    public ResponseEntity<BoardDTO> getTablero(@PathVariable String id) {
        log.info("Buscando tablero con ID: {}", id);
        try {
            Optional<Board> board = boardService.obtenerTableroPorId(new BoardId(id));
            return board.map(b -> ResponseEntity.ok(boardMapper.toDTO(b)))
                        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
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
    public ResponseEntity<Void> anadirLista(@PathVariable String id, @RequestBody AnadirListCommandPayload payload) {
        log.info("Añadiendo lista al tablero {}", id);
        try {
            AnadirListCommand cmd = new AnadirListCommand(id, payload.nombreLista(), payload.maxCards());
            boardService.anadirListaATablero(cmd);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // 4. Cambiar estado de bloqueo (Bloquear/Desbloquear)
    @PutMapping("/{id}/bloqueo")
    public ResponseEntity<Void> cambiarBloqueo(@PathVariable String id, @RequestParam boolean bloquear) {
        log.info("Cambiando bloqueo del tablero {} a {}", id, bloquear);
        try {
            CambiarBloqueoBoardCommand cmd = new CambiarBloqueoBoardCommand(id, bloquear);
            boardService.cambiarEstadoBloqueo(cmd);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Sub-record auxiliar para leer el cuerpo HTTP al crear listas de forma limpia
    public record AnadirListCommandPayload(String nombreLista, Integer maxCards) {}
}
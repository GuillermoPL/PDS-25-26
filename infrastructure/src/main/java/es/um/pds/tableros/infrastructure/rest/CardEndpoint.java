package es.um.pds.tableros.infrastructure.rest;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.um.pds.tableros.domain.card.Card;
import es.um.pds.tableros.domain.card.CardId;
import es.um.pds.tableros.domain.ports.input.card.CardService;
import es.um.pds.tableros.domain.ports.input.card.commands.CrearCardCommand;
import es.um.pds.tableros.domain.ports.input.card.commands.MoverCardCommand;
import es.um.pds.tableros.infrastructure.rest.dto.CardDTO;
import es.um.pds.tableros.infrastructure.mappers.CardMapper;

@RestController
@RequestMapping("/api/v1/tarjetas")
public class CardEndpoint {

    private static final Logger log = LoggerFactory.getLogger(CardEndpoint.class);

    private final CardService cardService;
    private final CardMapper cardMapper;

    public CardEndpoint(CardService cardService, CardMapper cardMapper) {
        this.cardService = cardService;
        this.cardMapper = cardMapper;
    }

    // 1. Obtener tarjeta por ID
    @GetMapping("/{id}")
    public ResponseEntity<CardDTO> getTarjeta(@PathVariable String id) {
        log.info("Buscando tarjeta con ID: {}", id);
        Optional<Card> card = cardService.obtenerTarjetaPorId(new CardId(id));
        return card.map(c -> ResponseEntity.ok(cardMapper.toDTO(c)))
                   .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // 2. Crear una nueva tarjeta
    @PostMapping
    public ResponseEntity<CardDTO> createTarjeta(@RequestBody CardDTO dto) {
        log.info("Petición para crear tarjeta '{}' en la lista {}", dto.getTitulo(), dto.getListIdActual());
        
        if (dto.getId() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            CrearCardCommand cmd = new CrearCardCommand(
                dto.getBoardId(), 
                dto.getListIdActual(), 
                dto.getTitulo(), 
                dto.getTipo(),
                null
            );
            Card nuevaTarjeta = cardService.crearNuevaTarjeta(cmd);
            return ResponseEntity.status(HttpStatus.CREATED).body(cardMapper.toDTO(nuevaTarjeta));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error al crear la tarjeta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // 3. Mover una tarjeta de lista (Soporta las invariantes de límite de lista y bloqueo de tablero)
    // PUT a /api/v1/tarjetas/{id}/movimiento?targetListId=XXXX
    @PutMapping("/{id}/movimiento")
    public ResponseEntity<Void> moverTarjeta(@PathVariable String id, @RequestBody MoverTarjetaPayload payload) {
        log.info("Petición para mover la tarjeta {} a la lista {}", id, payload.targetListId());
        try {
            MoverCardCommand cmd = new MoverCardCommand(id, payload.boardId(), payload.targetListId());
            cardService.moverTarjeta(cmd);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Movimiento denegado por regla de negocio: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Record auxiliar para recibir los IDs necesarios para procesar el movimiento
    public record MoverTarjetaPayload(String boardId, String targetListId) {}
}
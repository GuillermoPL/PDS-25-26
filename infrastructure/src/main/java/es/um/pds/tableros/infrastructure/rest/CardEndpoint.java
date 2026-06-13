package es.um.pds.tableros.infrastructure.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.um.pds.tableros.domain.ports.input.card.CardService;
import es.um.pds.tableros.domain.ports.input.card.commands.CrearCardCommand;
import es.um.pds.tableros.domain.ports.input.card.commands.MoverCardCommand;
import es.um.pds.tableros.infrastructure.mappers.CardMapper;
import es.um.pds.tableros.infrastructure.rest.dto.CardDTO;
import jakarta.validation.Valid; // Asegúrate de tener este import

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

    @GetMapping("/{id}")
    public ResponseEntity<CardDTO> getTarjeta(@PathVariable("id") String id) { // <--- AÑADIR ("id")
        log.info("Buscando tarjeta con ID: {}", id);
        return cardService.obtenerTarjetaPorId(id)
                   .map(c -> ResponseEntity.ok(cardMapper.toDTO(c)))
                   .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<CardDTO> createTarjeta(
            @RequestHeader(value = "X-User-Email", required = false) String emailUsuario, 
            @Valid @RequestBody CardDTO dto) { // <--- AÑADIR @Valid
            
        log.info("Petición para crear tarjeta '{}' en la lista {}", dto.getTitulo(), dto.getListIdActual());
        
        if (dto.getId() != null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        try {
            String nombreEtiqueta = null;
            String colorEtiqueta = null;
            if (dto.getEtiquetas() != null && !dto.getEtiquetas().isEmpty()) {
                CardDTO.EtiquetaDTO etDto = dto.getEtiquetas().get(0);
                nombreEtiqueta = etDto.getNombre();
                colorEtiqueta = etDto.getColor();
            }

            CrearCardCommand cmd = new CrearCardCommand(
                dto.getBoardId(), dto.getListIdActual(), dto.getTitulo(), 
                dto.getTipo(), nombreEtiqueta, colorEtiqueta, dto.getChecklistItems(),
                emailUsuario
            );
            
            var nuevaTarjeta = cardService.crearNuevaTarjeta(cmd);
            return ResponseEntity.status(HttpStatus.CREATED).body(cardMapper.toDTO(nuevaTarjeta));
            
        } catch (IllegalStateException e) {
            log.error("Permiso denegado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            log.error("Error al crear la tarjeta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}/movimiento")
    public ResponseEntity<Void> moverTarjeta(
            @PathVariable("id") String id, // <--- AÑADIR ("id")
            @RequestHeader(value = "X-User-Email", required = false) String emailUsuario,
            @Valid @RequestBody MoverTarjetaPayload payload) { // <--- AÑADIR @Valid
            
        log.info("Petición para mover la tarjeta {} a la lista {}", id, payload.targetListId());

        try {
            MoverCardCommand cmd = new MoverCardCommand(id, payload.boardId(), payload.targetListId(), emailUsuario);
            cardService.moverTarjeta(cmd);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            log.error("Movimiento/Permiso denegado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            log.error("Error en parámetros: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    public record MoverTarjetaPayload(String boardId, String targetListId) {}
}
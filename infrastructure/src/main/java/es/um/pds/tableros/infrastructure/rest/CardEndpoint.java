package es.um.pds.tableros.infrastructure.rest;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import es.um.pds.tableros.domain.ports.input.card.CardService;
import es.um.pds.tableros.domain.ports.input.card.commands.CrearCardCommand;
import es.um.pds.tableros.domain.ports.input.card.commands.MoverCardCommand;
import es.um.pds.tableros.infrastructure.rest.dto.CardDTO;
import es.um.pds.tableros.infrastructure.mappers.CardMapper;
import jakarta.validation.Valid;

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
    public ResponseEntity<CardDTO> getTarjeta(@PathVariable String id) {
        log.info("Buscando tarjeta con ID: {}", id);
        // Ahora usamos String crudo, no CardId
        return cardService.obtenerTarjetaPorId(id)
                   .map(c -> ResponseEntity.ok(cardMapper.toDTO(c)))
                   .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<CardDTO> createTarjeta(
            @RequestHeader(value = "X-User-Email", required = false) String emailUsuario, 
            @Valid @RequestBody CardDTO dto) {
            
        log.info("Petición para crear tarjeta '{}' en la lista {}", dto.getTitulo(), dto.getListIdActual());
        
        if (dto.getId() != null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        try {
            // Extraemos los datos simples de la etiqueta para pasarlos al comando
            String nombreEtiqueta = null;
            String colorEtiqueta = null;
            if (dto.getEtiquetas() != null && !dto.getEtiquetas().isEmpty()) {
                CardDTO.EtiquetaDTO etDto = dto.getEtiquetas().get(0);
                nombreEtiqueta = etDto.getNombre();
                colorEtiqueta = etDto.getColor();
            }

            // El comando ahora lleva todo el contexto, incluido quién hace la petición
            CrearCardCommand cmd = new CrearCardCommand(
                dto.getBoardId(), dto.getListIdActual(), dto.getTitulo(), 
                dto.getTipo(), nombreEtiqueta, colorEtiqueta, dto.getChecklistItems(),
                emailUsuario
            );
            
            // Si el usuario no tiene permisos, el servicio lanzará IllegalStateException
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
            @PathVariable String id, 
            @RequestHeader(value = "X-User-Email", required = false) String emailUsuario,
            @Valid @RequestBody MoverTarjetaPayload payload) {
            
        log.info("Petición para mover la tarjeta {} a la lista {}", id, payload.targetListId());

        try {
            // El comando lleva el usuario solicitante
            MoverCardCommand cmd = new MoverCardCommand(id, payload.boardId(), payload.targetListId(), emailUsuario);
            cardService.moverTarjeta(cmd);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            log.error("Movimiento/Permiso denegado: {}", e.getMessage());
            // Simplificamos mapeando IllegalStateException a FORBIDDEN por motivos de seguridad/permisos
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            log.error("Error en parámetros: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    public record MoverTarjetaPayload(String boardId, String targetListId) {}
}
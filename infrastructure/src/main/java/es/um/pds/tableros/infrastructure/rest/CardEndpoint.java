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
import jakarta.validation.Valid;

/**
 * @brief Adaptador de Entrada (Input Adapter) REST enfocado a operaciones sobre tarjetas.
 * Proporciona los endpoints públicos necesarios para la obtención, inserción 
 * y traslados de tareas de la aplicación consumiendo el puerto {@link CardService}.
 */
@RestController
@RequestMapping("/api/v1/tarjetas")
public class CardEndpoint {

    private static final Logger log = LoggerFactory.getLogger(CardEndpoint.class);

    private final CardService cardService;
    private final CardMapper cardMapper;

    /**
     * @brief Constructor con inyección automática de dependencias de la subcapa operativa de tarjetas.
     * @param cardService Puerto de entrada para la lógica de casos de uso de tarjetas.
     * @param cardMapper Mapeador para aislar tipos primitivos de objetos de dominio.
     */
    public CardEndpoint(CardService cardService, CardMapper cardMapper) {
        this.cardService = cardService;
        this.cardMapper = cardMapper;
    }

    /**
     * @brief Consulta el estado actual de una tarjeta (tarea o checklist) mediante su ID.
     * @param id Identificador de ruta de la tarjeta buscada.
     * @return ResponseEntity con el CardDTO (200 OK) o un código de estado 404 NOT FOUND si es inexistente.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CardDTO> getTarjeta(@PathVariable("id") String id) { 
        log.info("Buscando tarjeta con ID: {}", id);
        return cardService.obtenerTarjetaPorId(id)
                   .map(c -> ResponseEntity.ok(cardMapper.toDTO(c)))
                   .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * @brief Crea una nueva tarjeta bajo las condiciones de validación del comando del dominio.
     * Procesa la primera posición de etiquetas si vinieran especificadas de forma plana y delega.
     * @param emailUsuario Cabecera con el correo electrónico del autor de la petición (permisos de escritura).
     * @param dto JSON de entrada con la estructura completa de metadatos de la tarjeta.
     * @return ResponseEntity con el CardDTO serializado (201 CREATED) o estados de fallo 403 o 400.
     */
    @PostMapping
    public ResponseEntity<CardDTO> createTarjeta(
            @RequestHeader(value = "X-User-Email", required = false) String emailUsuario, 
            @Valid @RequestBody CardDTO dto) { 
            
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

    /**
     * @brief Ejecuta el traslado de una tarjeta hacia una columna de destino.
     * Invoca de fondo al servicio de dominio encargado de sincronizar contadores y evaluar límites WIP.
     * @param id Identificador de la tarjeta que va a cambiar de ubicación.
     * @param emailUsuario Correo electrónico del operador que ejecuta el traslado.
     * @param payload JSON con los identificadores complementarios necesarios (boardId y targetListId).
     * @return ResponseEntity con estado 200 OK ante un movimiento exitoso o errores mapeados.
     */
    @PutMapping("/{id}/movimiento")
    public ResponseEntity<Void> moverTarjeta(
            @PathVariable("id") String id, 
            @RequestHeader(value = "X-User-Email", required = false) String emailUsuario,
            @Valid @RequestBody MoverTarjetaPayload payload) { 
            
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

    /**
     * @brief Record auxiliar (Payload DTO) empleado para estructurar el cuerpo JSON de traslados.
     * @param boardId Identificador único del tablero Kanban.
     * @param targetListId Identificador único de la columna de destino.
     */
    public record MoverTarjetaPayload(String boardId, String targetListId) {}
}
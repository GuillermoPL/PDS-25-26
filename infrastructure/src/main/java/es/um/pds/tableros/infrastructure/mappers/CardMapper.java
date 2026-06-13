package es.um.pds.tableros.infrastructure.mappers;

import java.util.List;

import org.springframework.stereotype.Component;

import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.board.ListId;
import es.um.pds.tableros.domain.card.Card;
import es.um.pds.tableros.domain.card.CardId;
import es.um.pds.tableros.domain.card.CardType;
import es.um.pds.tableros.domain.card.Etiqueta;
import es.um.pds.tableros.infrastructure.persistence.jpa.entity.CardEntity;
import es.um.pds.tableros.infrastructure.persistence.jpa.entity.EtiquetaEmbeddable;
import es.um.pds.tableros.infrastructure.rest.dto.CardDTO;
@Component
public class CardMapper {

    public CardDTO toDTO(Card card) {
        if (card == null) return null;

        CardDTO dto = new CardDTO();
        dto.setId(card.getId().value());
        dto.setBoardId(card.getBoardId().value());
        dto.setListIdActual(card.getListIdActual().value());
        dto.setTitulo(card.getTitulo());
        dto.setDescripcion(card.getDescripcion());
        dto.setCompletada(card.isCompletada());
        dto.setTipo(card.getTipo().name());
        
        // Mapeamos la lista de Value Objects Etiqueta a DTOs planos
        dto.setEtiquetas(card.getEtiquetas().stream()
                .map(e -> new CardDTO.EtiquetaDTO(e.nombre(), e.color()))
                .toList());
                
        dto.setChecklistItems(card.getChecklistItems());

        return dto;
    }

    public Card toModel(CardDTO dto) {
        if (dto == null) return null;

        CardId cardId = new CardId(dto.getId());
        BoardId boardId = new BoardId(dto.getBoardId());
        ListId listId = new ListId(dto.getListIdActual());
        CardType tipo = CardType.valueOf(dto.getTipo().toUpperCase());

        Card card = new Card(cardId, boardId, listId, dto.getTitulo(), tipo);
        
        // Reconstruimos el estado interno usando los métodos del dominio de forma segura
        if (dto.isCompletada()) {
            card.marcarCompletada();
        }
        
        if (dto.getEtiquetas() != null) {
            dto.getEtiquetas().forEach(e -> card.anadirEtiqueta(new Etiqueta(e.getNombre(), e.getColor())));
        }
        
        if (dto.getChecklistItems() != null && tipo == CardType.CHECKLIST) {
            dto.getChecklistItems().forEach(card::anadirChecklistItem);
        }

        return card;
    }
    /**
     * De objeto de dominio a entidad JPA.
     */
    public CardEntity toEntity(Card card) {
        CardEntity entity = new CardEntity(
            card.getId().value(),
            card.getBoardId().value(),
            card.getListIdActual().value(),
            card.getTitulo(),
            card.getDescripcion(),
            card.isCompletada(),
            card.getTipo().name(),
            new java.util.ArrayList<>(card.getChecklistItems())
        );
        
        // Mapeo Dominio -> Infraestructura
        List<EtiquetaEmbeddable> etiquetasEntity = card.getEtiquetas().stream()
            .map(e -> new EtiquetaEmbeddable(e.nombre(), e.color()))
            .toList();
        entity.setEtiquetas(new java.util.ArrayList<>(etiquetasEntity));
        
        return entity;
    }

    /**
     * De entidad JPA a objeto de dominio.
     */
    public Card toModel(CardEntity entity) {
        if (entity == null) return null;

        CardId cardId   = new CardId(entity.getId());
        BoardId boardId = new BoardId(entity.getBoardId());
        ListId listId   = new ListId(entity.getListIdActual());
        CardType tipo   = CardType.valueOf(entity.getTipo().toUpperCase());

        Card card = new Card(cardId, boardId, listId, entity.getTitulo(), tipo);

        if (entity.getEtiquetas() != null) {
            entity.getEtiquetas().forEach(e -> 
                card.anadirEtiqueta(new Etiqueta(e.getNombre(), e.getColor()))
            );
        }
        
        if (entity.isCompletada()) {
            card.marcarCompletada();
        }
        if (entity.getDescripcion() != null) {
            card.setDescripcion(entity.getDescripcion());
        }
        if (entity.getChecklistItems() != null && tipo == CardType.CHECKLIST) {
            entity.getChecklistItems().forEach(card::anadirChecklistItem);
        }

        return card;
    }
}
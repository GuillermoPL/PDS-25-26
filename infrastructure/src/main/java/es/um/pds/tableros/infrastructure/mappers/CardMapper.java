package es.um.pds.tableros.infrastructure.mappers;

import org.springframework.stereotype.Component;
import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.board.ListId;
import es.um.pds.tableros.domain.card.Card;
import es.um.pds.tableros.domain.card.CardId;
import es.um.pds.tableros.domain.card.CardType;
import es.um.pds.tableros.domain.card.Etiqueta;
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
}
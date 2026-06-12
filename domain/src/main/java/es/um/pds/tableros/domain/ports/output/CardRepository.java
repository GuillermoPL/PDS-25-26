package es.um.pds.tableros.domain.ports.output;

import java.util.Optional;

import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.card.Card;
import es.um.pds.tableros.domain.card.CardId;

import java.util.List;

public interface CardRepository {
    void save(Card card);
    Optional<Card> findById(CardId id);
    List<Card> findByBoardId(BoardId boardId);
    void delete(CardId id);
}
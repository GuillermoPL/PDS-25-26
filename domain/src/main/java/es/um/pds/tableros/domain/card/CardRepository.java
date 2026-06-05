package es.um.pds.tableros.domain.card;

import java.util.Optional;

import es.um.pds.tableros.domain.board.BoardId;

import java.util.List;

public interface CardRepository {
    void save(Card card);
    Optional<Card> findById(CardId id);
    List<Card> findByBoardId(BoardId boardId);
}
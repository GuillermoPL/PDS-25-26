package es.um.pds.tableros.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.card.Card;
import es.um.pds.tableros.domain.card.CardId;
import es.um.pds.tableros.domain.ports.output.CardRepository;
import es.um.pds.tableros.infrastructure.mappers.CardMapper;

@Repository
public class CardRepositoryImpl implements CardRepository {

    @Autowired
    private SpringDataCardRepository springDataCardRepository;

    @Autowired
    private CardMapper cardMapper;

    @Override
    public void save(Card card) {
        springDataCardRepository.save(cardMapper.toEntity(card));
    }

    @Override
    public Optional<Card> findById(CardId id) {
        return springDataCardRepository.findById(id.value())
                .map(cardMapper::toModel);
    }

    @Override
    public List<Card> findByBoardId(BoardId boardId) {
        return springDataCardRepository.findByBoardId(boardId.value())
                .stream()
                .map(cardMapper::toModel)
                .toList();
    }
}
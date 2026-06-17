package es.um.pds.tableros.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.card.Card;
import es.um.pds.tableros.domain.card.CardId;
import es.um.pds.tableros.domain.ports.output.CardRepository;
import es.um.pds.tableros.infrastructure.mappers.CardMapper;

/**
 * @brief Adaptador de Salida (Output Adapter) para la gestión persistente de tarjetas.
 * Implementa la interfaz {@link CardRepository} del dominio, encargándose de traducir y persistir
 * los datos del agregado Card delegando las operaciones sobre la base de datos a Spring Data JPA.
 */
@Repository
public class CardRepositoryImpl implements CardRepository {

    private final SpringDataCardRepository springDataCardRepository;
    private final CardMapper cardMapper;

    /**
     * @brief Constructor con inyección de componentes de infraestructura.
     * @param springDataCardRepository Repositorio nativo de Spring Data JPA para tarjetas.
     * @param cardMapper Mapeador encargado del aislamiento de datos de la tarjeta.
     */
    public CardRepositoryImpl(SpringDataCardRepository springDataCardRepository, CardMapper cardMapper) {
        this.springDataCardRepository = springDataCardRepository;
        this.cardMapper = cardMapper;
    }

    /**
     * @brief Guarda o actualiza el registro completo de una tarjeta en la persistencia.
     * @param card El agregado de dominio Card.
     */
    @Override
    public void save(Card card) {
        springDataCardRepository.save(cardMapper.toEntity(card));
    }

    /**
     * @brief Busca y reconstruye una tarjeta de dominio a partir de su identificador.
     * @param id Identificador único de la tarjeta.
     * @return Un Optional con el modelo de negocio reconstruido, o vacío en su defecto.
     */
    @Override
    public Optional<Card> findById(CardId id) {
        return springDataCardRepository.findById(id.value())
                .map(cardMapper::toModel);
    }

    /**
     * @brief Extrae el conjunto de tarjetas que componen lógicamente un tablero.
     * @param boardId Identificador de la raíz del agregado de tablero contenedor.
     * @return Lista de tarjetas pertenecientes a dicho tablero.
     */
    @Override
    public List<Card> findByBoardId(BoardId boardId) {
        return springDataCardRepository.findByBoardId(boardId.value())
                .stream()
                .map(cardMapper::toModel)
                .toList();
    }
    
    /**
     * @brief Elimina físicamente una tarjeta de la base de datos a través de su ID.
     * @param id Identificador único del registro de la tarjeta.
     */
    @Override
    public void delete(CardId id) {
        springDataCardRepository.deleteById(id.value());
    }
}
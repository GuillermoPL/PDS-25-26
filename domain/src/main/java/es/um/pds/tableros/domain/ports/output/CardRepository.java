package es.um.pds.tableros.domain.ports.output;

import java.util.List;
import java.util.Optional;

import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.card.Card;
import es.um.pds.tableros.domain.card.CardId;

/**
 * @brief Puerto de Salida (Output Port) encargado de la persistencia del agregado Card.
 * Permite a la lógica de negocio acceder y almacenar las tarjetas de forma independiente
 * a la base de datos utilizada.
 */
public interface CardRepository {
    
    /**
     * @brief Inserta o actualiza una tarjeta en el medio de persistencia.
     * @param card La instancia del agregado Card a almacenar.
     */
    void save(Card card);
    
    /**
     * @brief Recupera una tarjeta específica utilizando su identificador.
     * @param id Identificador de dominio de la tarjeta.
     * @return Un Optional con la tarjeta si existe, o vacío en caso contrario.
     */
    Optional<Card> findById(CardId id);
    
    /**
     * @brief Obtiene todas las tarjetas que pertenecen a un tablero determinado.
     * @param boardId Identificador del tablero contenedor.
     * @return Lista de tarjetas asociadas al tablero proporcionado.
     */
    List<Card> findByBoardId(BoardId boardId);
    
    /**
     * @brief Elimina permanentemente una tarjeta del sistema.
     * @param id Identificador de la tarjeta que se desea borrar.
     */
    void delete(CardId id);
}
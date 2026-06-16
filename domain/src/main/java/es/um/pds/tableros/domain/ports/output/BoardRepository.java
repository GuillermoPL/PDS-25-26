package es.um.pds.tableros.domain.ports.output;

import java.util.List;
import java.util.Optional;

import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.board.BoardId;

/**
 * @brief Puerto de Salida (Output Port) para la persistencia del agregado Board.
 * Define el contrato que debe implementar la capa de infraestructura (ej. JPA, MongoDB) 
 * para almacenar y recuperar tableros sin acoplar el dominio a ninguna tecnología específica.
 */
public interface BoardRepository {

    /**
     * @brief Persiste el estado actual de un tablero en el sistema de almacenamiento.
     * Si el tablero no existe, lo inserta; si ya existe, actualiza sus datos.
     * @param board La instancia del agregado Board a guardar.
     */
    void save(Board board);

    /**
     * @brief Busca y reconstruye un tablero a partir de su identificador único.
     * @param id Identificador de dominio del tablero.
     * @return Un Optional con el tablero si se encuentra, o vacío si no existe.
     */
    Optional<Board> findById(BoardId id);

    /**
     * @brief Recupera la colección completa de tableros vinculados a un usuario específico.
     * @param email Correo electrónico utilizado como criterio de búsqueda.
     * @return Lista de tableros asociados a dicho correo.
     */
    List<Board> findByEmail(String email);
    
    /**
     * @brief Extrae todos los tableros registrados en el sistema.
     * @return Lista exhaustiva con todas las instancias de tableros.
     */
    List<Board> findAll();
}
package es.um.pds.tableros.domain.ports.output;

import java.util.List;
import java.util.Optional;

import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.board.BoardId;

public interface BoardRepository {

    /**
     * Guarda o actualiza un tablero en el sistema de persistencia.
     */
    void save(Board board);

    /**
     * Busca un tablero por su identificador único de dominio.
     */
    Optional<Board> findById(BoardId id);

    /**
     * Recupera todos los tableros asociados al correo electrónico de un usuario.
     * Útil para cumplir el requisito de listado por usuario.
     */
    List<Board> findByEmail(String email);
}
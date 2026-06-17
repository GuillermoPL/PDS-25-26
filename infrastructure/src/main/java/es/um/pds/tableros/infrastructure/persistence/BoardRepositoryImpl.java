package es.um.pds.tableros.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.ports.output.BoardRepository;
import es.um.pds.tableros.infrastructure.mappers.BoardMapper;

/**
 * @brief Adaptador de Salida (Output Adapter) para la gestión persistente de tableros.
 * Implementa el puerto del dominio {@link BoardRepository} encapsulando las interacciones técnicas con 
 * el repositorio nativo de Spring Data JPA y aplicando transformaciones de datos a través de mappers.
 */
@Repository
public class BoardRepositoryImpl implements BoardRepository {

    private final SpringDataBoardRepository springDataBoardRepository;
    private final BoardMapper boardMapper;

    /**
     * @brief Constructor con inyección de componentes de infraestructura.
     * @param springDataBoardRepository Repositorio nativo de Spring Data JPA.
     * @param boardMapper Mapeador encargado del intercambio de tipos dominio-entidad.
     */
    public BoardRepositoryImpl(SpringDataBoardRepository springDataBoardRepository, BoardMapper boardMapper) {
        this.springDataBoardRepository = springDataBoardRepository;
        this.boardMapper = boardMapper;
    }

    /**
     * @brief Traduce y persiste de forma unificada el estado de un agregado Board en la base de datos H2.
     * @param board Instancia del agregado puro de dominio.
     */
    @Override
    public void save(Board board) {
        springDataBoardRepository.save(boardMapper.toEntity(board));
    }

    /**
     * @brief Busca un tablero por su identidad de dominio y reconstruye el agregado completo si existe.
     * @param id Identificador único del tablero (Value Object).
     * @return Un Optional conteniendo el objeto de dominio reconstruido, o vacío si no hay coincidencias.
     */
    @Override
    public Optional<Board> findById(BoardId id) {
        return springDataBoardRepository.findById(id.value())
                .map(boardMapper::toModel);
    }

    /**
     * @brief Recupera todos los tableros en los que participa un usuario, ya sea como propietario o invitado.
     * @param email Correo electrónico crudo para filtrar las búsquedas en la base de datos.
     * @return Lista de agregados de dominio Board accesibles por el usuario.
     */
    @Override
    public List<Board> findByEmail(String email) {
        return springDataBoardRepository.findByEmailOrSharedWith(email).stream()
                .map(boardMapper::toModel)
                .toList();
    }
    
    /**
     * @brief Recupera la totalidad de los tableros almacenados en el sistema de persistencia relacional.
     * @return Lista completa de agregados Board de dominio.
     */
    @Override
    public List<Board> findAll() {
        return springDataBoardRepository.findAll()
                .stream()
                .map(boardMapper::toModel)
                .toList();
    }
}
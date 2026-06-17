package es.um.pds.tableros.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.um.pds.tableros.infrastructure.persistence.jpa.entity.CardEntity;

/**
 * @brief Repositorio nativo de Spring Data JPA para gestionar la entidad CardEntity.
 * Expone las operaciones físicas fundamentales del motor Hibernate de base de datos sobre 
 * las tareas y checklists de la aplicación.
 */
public interface SpringDataCardRepository extends JpaRepository<CardEntity, String> {
    
    /**
     * @brief Consulta autogenerada por convención de nombres de Spring Data (Query Method).
     * Recupera de la base de datos relacional la colección de registros cuyo campo BOARD_ID coincida.
     * @param boardId Identificador del tablero en formato de texto plano.
     * @return Lista de registros de tipo CardEntity enlazados a dicho tablero.
     */
    List<CardEntity> findByBoardId(String boardId);
}
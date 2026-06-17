package es.um.pds.tableros.infrastructure.mappers;

import org.springframework.stereotype.Component;

import es.um.pds.tableros.domain.board.ListId;
import es.um.pds.tableros.domain.board.TaskList;
import es.um.pds.tableros.infrastructure.persistence.jpa.entity.BoardEntity;
import es.um.pds.tableros.infrastructure.persistence.jpa.entity.TaskListEntity;

/**
 * @brief Componente traductor (Mapper) para las entidades locales TaskList de columna.
 * Mapea los objetos de negocio dependientes a estructuras de base de datos relacionales vinculadas a un tablero padre.
 */
@Component
public class TaskListMapper {

    /**
     * @brief Traduce un elemento TaskList de dominio a una entidad TaskListEntity de persistencia relacional.
     * Requiere pasar explícitamente la BoardEntity contenedora para mapear la clave foránea Many-to-One.
     * @param taskList Instancia de negocio de la lista de tareas.
     * @param boardEntity Entidad del tablero contenedor pre-calculada en infraestructura.
     * @return El registro de persistencia configurado, o null si taskList es nulo.
     */
    public TaskListEntity toEntity(TaskList taskList, BoardEntity boardEntity) {
        if (taskList == null) return null;
        return new TaskListEntity(
            taskList.getId().value(),
            taskList.getNombre(),
            taskList.getMaxCards(),
            taskList.getNumCardsActual(),
            boardEntity
        );
    }

    /**
     * @brief Reconstruye un objeto de negocio TaskList a partir de una TaskListEntity.
     * @param entity El registro físico procedente de JPA.
     * @return Una instancia del modelo de dominio de tipo TaskList.
     */
    public TaskList toModel(TaskListEntity entity) {
        if (entity == null) return null;
        ListId listId = new ListId(entity.getId());
        return new TaskList(
            listId,
            entity.getNombre(),
            entity.getMaxCards(),
            entity.getNumCardsActual()
        );
    }
}
package es.um.pds.tableros.infrastructure.mappers;

import org.springframework.stereotype.Component;
import es.um.pds.tableros.domain.board.ListId;
import es.um.pds.tableros.domain.board.TaskList;
import es.um.pds.tableros.infrastructure.persistence.jpa.entity.BoardEntity;
import es.um.pds.tableros.infrastructure.persistence.jpa.entity.TaskListEntity;

@Component
public class TaskListMapper {

    /**
     * De objeto de dominio a entidad JPA.
     * Necesita el BoardEntity ya existente para poder establecer la relación ManyToOne.
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
     * De entidad JPA a objeto de dominio.
     * El BoardId no se necesita aquí porque TaskList solo conoce su propio ListId.
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
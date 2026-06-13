package es.um.pds.tableros.infrastructure.mappers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.board.Email;
import es.um.pds.tableros.domain.board.ListId;
import es.um.pds.tableros.domain.board.Rol;
import es.um.pds.tableros.infrastructure.persistence.jpa.entity.AutomationRuleEmbeddable;
import es.um.pds.tableros.infrastructure.persistence.jpa.entity.BoardEntity;
import es.um.pds.tableros.infrastructure.persistence.jpa.entity.TaskListEntity;
import es.um.pds.tableros.infrastructure.rest.dto.BoardDTO;
@Component
public class BoardMapper {
    private final TaskListMapper taskListMapper;

    public BoardMapper(TaskListMapper taskListMapper) {
        this.taskListMapper = taskListMapper;
    }
	
    /**
     * Transforma del Modelo del Dominio al DTO (Para enviar hacia fuera en la API REST)
     */
    public BoardDTO toDTO(Board board) {
        if (board == null) {
            return null;
        }

        BoardDTO dto = new BoardDTO();
        dto.setId(board.getId().value());
        dto.setTitulo(board.getTitulo());
        dto.setEmail(board.getEmail().value());
        dto.setLocked(board.isLocked());
        
        // Mapeamos la lista de objetos de negocio TaskList a una lista simple de Strings con sus nombres
        List<BoardDTO.ListaDTO> listasListasDTO = board.getTasksLists().stream()
                .map(tl -> new BoardDTO.ListaDTO(tl.getId().value(), tl.getNombre()))
                .toList();
        dto.setListas(listasListasDTO);

        // Si tiene asignada una lista de completadas, guardamos su ID plano
        if (board.getListCompletadas() != null) {
            dto.setListCompletadasId(board.getListCompletadas().value());
        }
        dto.setHistorial(board.getHistorial());
        if (board.getPermisos() != null) {
            Map<String, String> permisosDTO = board.getPermisos().entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                    e -> e.getKey().value(),
                    e -> e.getValue().name()
                ));
            dto.setPermisos(permisosDTO);
        }
        if (board.getReglas() != null) {
            List<BoardDTO.ReglaDTO> reglasDTO = board.getReglas().stream()
                .map(r -> new BoardDTO.ReglaDTO(
                    r.id(),
                    r.triggerType().name(),
                    r.triggerPayload(),
                    r.actionType().name()
                ))
                .toList();
            dto.setReglas(reglasDTO);
        }
        return dto;
    }

    /**
     * Transforma del DTO al Modelo del Dominio (Para reconstruir el objeto si viniera del exterior)
     * Nota: Como Board delega la creación de listas internamente, este método monta el agregado base.
     */
    public Board toModel(BoardDTO dto) {
        if (dto == null) {
            return null;
        }

        // Reconstruimos la Raíz del Agregado con su ID de dominio correspondiente
        BoardId boardId = new BoardId(dto.getId());
        Board board = new Board(boardId, dto.getTitulo(), new Email(dto.getEmail()));

        // Recuperamos el estado de bloqueo original
        if (dto.isLocked()) {
            board.lock();
        } else {
            board.unlock();
        }

        // Nota de diseño: Las columnas internas (TaskList) y la lista de completadas 
        // habitualmente se recuperan o añaden de forma controlada a través de los servicios 
        // consultando a la base de datos (JPA) para no perder los IDs reales de las listas, 
        // pero estructuralmente el objeto raíz queda mapeado aquí.

        return board;
    }
    
    /**
     * De objeto de dominio a entidad JPA.
     */
    public BoardEntity toEntity(Board board) {
        if (board == null) return null;

        String listCompletadasId = board.getListCompletadas() != null
                ? board.getListCompletadas().value()
                : null;

        // Creamos primero la BoardEntity sin listas (para pasársela al TaskListMapper)
        BoardEntity boardEntity = new BoardEntity(
                board.getId().value(),
                board.getTitulo(),
                board.getEmail().value(),
                board.isLocked(),
                listCompletadasId,
                new java.util.ArrayList<>(),
                new java.util.ArrayList<>(board.getHistorial()) 
            );

        // Mapeamos cada TaskList pasándole la BoardEntity ya construida
        List<TaskListEntity> taskListEntities = board.getTasksLists().stream()
                .map(tl -> taskListMapper.toEntity(tl, boardEntity))
                .collect(Collectors.toList());

        boardEntity.setTasksLists(taskListEntities);
     // Mapeamos los permisos del dominio (Map<Email, Rol>) a la entidad (Map<String, String>)
        Map<String, String> permisosEntity = board.getPermisos().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().value(),
                        e -> e.getValue().name()
                ));
        boardEntity.setPermisos(permisosEntity);
        if (board.getReglas() != null) {
            List<AutomationRuleEmbeddable> reglasEntity = board.getReglas().stream()
                .map(r -> new AutomationRuleEmbeddable(r.id(), r.triggerType().name(), r.triggerPayload(), r.actionType().name()))
                .collect(Collectors.toList());
            boardEntity.setReglas(reglasEntity);
        }
        return boardEntity;
    }

    /**
     * De entidad JPA a objeto de dominio (reconstrucción completa con listas).
     */
    public Board toModel(BoardEntity entity) {
        if (entity == null) return null;

        BoardId boardId = new BoardId(entity.getId());
        Board board = new Board(boardId, entity.getTitulo(), new Email(entity.getEmail()));

        if (entity.isLocked()) {
            board.lock();
        }

        // Reconstruimos la lista de completadas si existe
        if (entity.getListCompletadasId() != null) {
            board.defineListCompletadas(new ListId(entity.getListCompletadasId()));
        }
        if (entity.getHistorial() != null) {
            board.restoreHistorial(entity.getHistorial());
        }
        // Reconstruimos las TaskLists internas usando el TaskListMapper
        if (entity.getTasksLists() != null) {
            entity.getTasksLists().stream()
                  .map(taskListMapper::toModel)
                  .forEach(board::restoreTaskList);
        }
     // Reconstruimos los permisos desde la entidad
        if (entity.getPermisos() != null) {
            entity.getPermisos().forEach((emailStr, rolStr) ->
                board.restorePermiso(new Email(emailStr), Rol.valueOf(rolStr))
            );
        }
        if (entity.getReglas() != null) {
            List<es.um.pds.tableros.domain.board.AutomationRule> reglasDominio = entity.getReglas().stream()
                .map(re -> new es.um.pds.tableros.domain.board.AutomationRule(
                    re.getId(),
                    es.um.pds.tableros.domain.board.TriggerType.valueOf(re.getTriggerType()),
                    re.getTriggerPayload(),
                    es.um.pds.tableros.domain.board.ActionType.valueOf(re.getActionType())
                )).toList();
            board.restoreReglas(reglasDominio);
        }
        return board;
    }
}
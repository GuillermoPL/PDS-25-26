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

/**
 * @brief Componente traductor (Mapper) para el Agregado Raíz Board.
 * Centraliza la conversión bidireccional del tablero entre el modelo puro de dominio,
 * los objetos de transferencia de datos de red (BoardDTO) para la API REST/JavaFX y las entidades relacionales de JPA (BoardEntity).
 */
@Component
public class BoardMapper {
    private final TaskListMapper taskListMapper;

    /**
     * @brief Construye el mapper inyectando el traductor dependiente para las columnas.
     * @param taskListMapper Instancia del mapper especializado en listas de tareas.
     */
    public BoardMapper(TaskListMapper taskListMapper) {
        this.taskListMapper = taskListMapper;
    }
    
    /**
     * @brief Transforma una instancia del modelo del dominio a un objeto BoardDTO.
     * Aplica proyecciones planas sobre las colecciones y Value Objects para facilitar la serialización JSON.
     * @param board Objeto de dominio con los datos del tablero.
     * @return Instancia mapeada de BoardDTO, o null si el parámetro de entrada es nulo.
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
        
        List<BoardDTO.ListaDTO> listasListasDTO = board.getTasksLists().stream()
                .map(tl -> new BoardDTO.ListaDTO(tl.getId().value(), tl.getNombre()))
                .toList();
        dto.setListas(listasListasDTO);

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
     * @brief Reconstruye la raíz del agregado de dominio Board a partir de un DTO externo.
     * @param dto Objeto plano de transferencia de datos.
     * @return Instancia parcial del agregado Board con sus propiedades inicializadas.
     */
    public Board toModel(BoardDTO dto) {
        if (dto == null) {
            return null;
        }

        BoardId boardId = new BoardId(dto.getId());
        Board board = new Board(boardId, dto.getTitulo(), new Email(dto.getEmail()));

        if (dto.isLocked()) {
            board.lock();
        } else {
            board.unlock();
        }

        return board;
    }
    
    /**
     * @brief Traduce una instancia de dominio Board a una entidad persistente BoardEntity de JPA.
     * Gestiona las referencias cíclicas necesarias para que Hibernate mantenga las claves ajenas en H2.
     * @param board Agregado completo de dominio.
     * @return Instancia mapeada lista para ser almacenada a través del EntityManager o JpaRepository.
     */
    public BoardEntity toEntity(Board board) {
        if (board == null) return null;

        String listCompletadasId = board.getListCompletadas() != null
                ? board.getListCompletadas().value()
                : null;

        BoardEntity boardEntity = new BoardEntity(
                board.getId().value(),
                board.getTitulo(),
                board.getEmail().value(),
                board.isLocked(),
                listCompletadasId,
                new java.util.ArrayList<>(),
                new java.util.ArrayList<>(board.getHistorial()) 
            );

        List<TaskListEntity> taskListEntities = board.getTasksLists().stream()
                .map(tl -> taskListMapper.toEntity(tl, boardEntity))
                .collect(Collectors.toList());

        boardEntity.setTasksLists(taskListEntities);
        
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
     * @brief Reconstruye el Agregado de Dominio Board de forma íntegra a partir de una entidad relacional BoardEntity.
     * Recupera y rehidrata los estados de auditoría, las columnas internas, los mapas de permisos y las reglas automáticas.
     * @param entity Registro persistente extraído de la base de datos H2.
     * @return El Agregado Board con todas sus invariantes internas restauradas.
     */
    public Board toModel(BoardEntity entity) {
        if (entity == null) return null;

        BoardId boardId = new BoardId(entity.getId());
        Board board = new Board(boardId, entity.getTitulo(), new Email(entity.getEmail()));

        if (entity.isLocked()) {
            board.lock();
        }

        if (entity.getListCompletadasId() != null) {
            board.defineListCompletadas(new ListId(entity.getListCompletadasId()));
        }
        if (entity.getHistorial() != null) {
            board.restoreHistorial(entity.getHistorial());
        }
        if (entity.getTasksLists() != null) {
            entity.getTasksLists().stream()
                  .map(taskListMapper::toModel)
                  .forEach(board::restoreTaskList);
        }
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
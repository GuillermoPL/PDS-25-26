package es.um.pds.tableros.infrastructure.mappers;

import java.util.List;
import org.springframework.stereotype.Component;

import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.board.Email;
import es.um.pds.tableros.domain.board.TaskList;
import es.um.pds.tableros.infrastructure.rest.dto.BoardDTO;

@Component
public class BoardMapper {

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
        List<String> nombres = board.getTasksLists().stream()
                .map(TaskList::getNombre)
                .toList();
        dto.setNombresListas(nombres);

        // Si tiene asignada una lista de completadas, guardamos su ID plano
        if (board.getListCompletadas() != null) {
            dto.setListCompletadasId(board.getListCompletadas().value());
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
}
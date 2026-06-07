package es.um.pds.tableros.domain.ports.input.board;

import java.util.List;
import java.util.Optional;

import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.ports.input.board.commands.*;

public interface BoardService {

    // --- CONSULTAS ---
    
    // Para cargar el tablero actual en JavaFX
    Optional<Board> obtenerTableroPorId(BoardId id);
    

    List<Board> obtenerTablerosPorUsuario(String email);


    // --- OPERACIONES (Commands) ---

    // Crea el tablero, genera su BoardId y lo persiste
    Board crearNuevoTablero(CrearBoardCommand cmd);

    // Añade una columna/lista validando que el tablero no esté bloqueado
    void anadirListaATablero(AnadirListCommand cmd);

    // Configura el ListId que actuará como el contenedor de tareas "Done"
    void definirListaCompletadas(DefinirListCompletadasCommand cmd);

    // Bloquea o desbloquea el tablero según el flag del comando
    void cambiarEstadoBloqueo(CambiarBloqueoBoardCommand cmd);
}
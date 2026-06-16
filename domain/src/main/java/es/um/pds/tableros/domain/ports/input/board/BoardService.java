package es.um.pds.tableros.domain.ports.input.board;

import java.util.List;
import java.util.Optional;

import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.ports.input.board.commands.AnadirListCommand;
import es.um.pds.tableros.domain.ports.input.board.commands.CambiarBloqueoBoardCommand;
import es.um.pds.tableros.domain.ports.input.board.commands.CompartirBoardCommand;
import es.um.pds.tableros.domain.ports.input.board.commands.CrearBoardCommand;
import es.um.pds.tableros.domain.ports.input.board.commands.CrearReglaCommand;
import es.um.pds.tableros.domain.ports.input.board.commands.DefinirListCompletadasCommand;

/**
 * @brief Puerto de Entrada (Input Port) que expone los casos de uso de gestión de tableros.
 * Define el contrato que la capa de aplicación o infraestructura debe invocar para
 * interactuar con el dominio de tableros, separando consultas (Queries) de operaciones (Commands).
 */
public interface BoardService {

    // CONSULTAS 
    
    /**
     * @brief Recupera un tablero específico a partir de su identificador.
     * @param boardId Cadena de texto con el ID único del tablero.
     * @return Un Optional que contiene el tablero si existe, o vacío si no se encuentra.
     */
    Optional<Board> obtenerTableroPorId(String boardId);    

    /**
     * @brief Recupera todos los tableros asociados a un usuario específico.
     * @param email Correo electrónico del usuario propietario o invitado.
     * @return Lista de tableros a los que el usuario tiene acceso.
     */
    List<Board> obtenerTablerosPorUsuario(String email);


    // OPERACIONES (Commands) 

    /**
     * @brief Orquesta la creación de un nuevo tablero en el sistema.
     * @param cmd Objeto inmutable con los datos necesarios (título y creador).
     * @return La instancia del tablero de dominio recién creada.
     */
    Board crearNuevoTablero(CrearBoardCommand cmd);

    /**
     * @brief Añade una nueva lista (columna) al tablero especificado.
     * @param cmd Comando con los detalles de la nueva lista y el usuario solicitante.
     * @return El identificador en formato String de la lista recién creada.
     */
    String anadirListaATablero(AnadirListCommand cmd);

    /**
     * @brief Configura qué lista actuará como receptora de las tarjetas marcadas como completadas.
     * @param cmd Comando con los IDs del tablero y la lista designada.
     */
    void definirListaCompletadas(DefinirListCompletadasCommand cmd);

    /**
     * @brief Altera el estado de bloqueo de un tablero (bloqueo/desbloqueo).
     * @param cmd Comando que contiene la orden de bloqueo y el usuario que lo solicita.
     */
    void cambiarEstadoBloqueo(CambiarBloqueoBoardCommand cmd);
    
    /**
     * @brief Concede permisos de acceso a un usuario invitado sobre un tablero.
     * @param cmd Comando con el ID del tablero, el dueño, el invitado y el rol a otorgar.
     */
    void compartirTablero(CompartirBoardCommand cmd);

    /**
     * @brief Elimina los privilegios de acceso de un usuario sobre un tablero.
     * @param boardId Identificador del tablero objetivo.
     * @param emailSolicitante Correo del propietario que solicita la revocación.
     * @param emailAEliminar Correo del usuario al que se le retirará el acceso.
     */
    void revocarAcceso(String boardId, String emailSolicitante, String emailAEliminar);
    
    /**
     * @brief Configura y añade una nueva regla de automatización al tablero.
     * @param cmd Comando con la estructura completa de disparo y acción de la regla.
     */
    void anadirReglaAutomatizacion(CrearReglaCommand cmd);
}
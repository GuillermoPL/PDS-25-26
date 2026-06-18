package es.um.pds.tableros.application.usecases.board;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import es.um.pds.tableros.domain.board.AutomationRule;
import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.board.Email;
import es.um.pds.tableros.domain.board.ListId;
import es.um.pds.tableros.domain.board.Rol;
import es.um.pds.tableros.domain.board.TaskList;
import es.um.pds.tableros.domain.ports.input.board.BoardService;
import es.um.pds.tableros.domain.ports.input.board.commands.AnadirListCommand;
import es.um.pds.tableros.domain.ports.input.board.commands.CambiarBloqueoBoardCommand;
import es.um.pds.tableros.domain.ports.input.board.commands.CompartirBoardCommand;
import es.um.pds.tableros.domain.ports.input.board.commands.CrearBoardCommand;
import es.um.pds.tableros.domain.ports.input.board.commands.CrearReglaCommand;
import es.um.pds.tableros.domain.ports.input.board.commands.DefinirListCompletadasCommand;
import es.um.pds.tableros.domain.ports.output.BoardRepository;

/**
 * @brief Implementación del Puerto de Entrada BoardService. Orquesta los casos de uso
 * relacionados con los tableros, gestionando la persistencia, la validación de permisos
 * y la delegación de reglas de negocio al dominio.
 */
@Service
public class BoardServiceImpl implements BoardService {

    private static final Logger log = LoggerFactory.getLogger(BoardServiceImpl.class);
    private final BoardRepository boardRepository;

    /**
     * @brief Constructor para inyección de dependencias.
     * @param boardRepository Puerto de salida para interactuar con la base de datos de tableros.
     */
    public BoardServiceImpl(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    /**
     * @brief Recupera un tablero encapsulando la conversión de tipos primitivos a Value Objects.
     * @param id El identificador en formato texto crudo.
     * @return Optional con el tablero si es encontrado por el repositorio.
     */
    @Override
    public Optional<Board> obtenerTableroPorId(String id) {
        // La capa de aplicación protege al dominio transformando el String crudo
        return this.boardRepository.findById(new BoardId(id));
    }

    /**
     * @brief Lista los tableros de un usuario, forzando la validación de formato de correo.
     * @param emailRaw Correo electrónico introducido por el usuario.
     * @return Lista de tableros asociados.
     * @throws IllegalArgumentException Si el formato del correo es inválido al instanciar el Value Object Email.
     */
    @Override
    public List<Board> obtenerTablerosPorUsuario(String emailRaw) {
        Email emailValidado = new Email(emailRaw);
        return this.boardRepository.findByEmail(emailValidado.value());
    }

    /**
     * @brief Caso de uso para instanciar un nuevo tablero y persistirlo inicialmente.
     * @param cmd Estructura con el título y el creador.
     * @return El tablero completamente inicializado.
     */
    @Override
    public Board crearNuevoTablero(CrearBoardCommand cmd) {
        log.info("Creando nuevo tablero '{}' para el usuario {}", cmd.titulo(), cmd.emailCreator());

        BoardId nuevoBoardId = BoardId.generate();
        Board nuevoTablero = new Board(nuevoBoardId, cmd.titulo(), new Email(cmd.emailCreator()));

        nuevoTablero.registrarEvento("Tablero creado por " + cmd.emailCreator());
        this.boardRepository.save(nuevoTablero);

        return nuevoTablero;
    }

    /**
     * @brief Caso de uso para añadir una columna, garantizando previamente que el usuario tenga permisos.
     * @param cmd Comando con los datos de la lista y el usuario que hace la solicitud.
     * @return El ID de la lista generada.
     * @throws IllegalArgumentException Si el tablero no existe.
     * @throws IllegalStateException Si el usuario no tiene permisos o el tablero está bloqueado.
     */
    @Override
    public String anadirListaATablero(AnadirListCommand cmd) {
        log.info("Añadiendo lista '{}' al tablero {}", cmd.nombreLista(), cmd.boardId());

        Board board = this.boardRepository.findById(new BoardId(cmd.boardId()))
                .orElseThrow(() -> new IllegalArgumentException("El tablero especificado no existe"));

        // Verificación de seguridad delegada al servicio
        verificarPermisoEscritura(board, cmd.emailSolicitante());

        TaskList nuevaLista = board.addList(cmd.nombreLista(), cmd.maxCards());
        board.registrarEvento("Lista añadida: " + cmd.nombreLista());

        this.boardRepository.save(board);
        return nuevaLista.getId().value();
    }

    /**
     * @brief Modifica el estado de bloqueo del tablero para pausar o reanudar su operativa.
     * @param cmd Comando con el ID del tablero, la bandera de bloqueo y el usuario solicitante.
     * @throws IllegalArgumentException Si el tablero no existe.
     * @throws IllegalStateException Si el usuario carece de permisos de escritura.
     */
    @Override
    public void cambiarEstadoBloqueo(CambiarBloqueoBoardCommand cmd) {
        log.info("Cambiando estado de bloqueo del tablero {} a: {}", cmd.boardId(), cmd.bloquear());

        Board board = this.boardRepository.findById(new BoardId(cmd.boardId()))
                .orElseThrow(() -> new IllegalArgumentException("El tablero especificado no existe"));

        // Verificación de seguridad delegada al servicio
        verificarPermisoEscritura(board, cmd.emailSolicitante());

        if (cmd.bloquear()) {
            board.lock();
            board.registrarEvento("Tablero bloqueado temporalmente");
        } else {
            board.unlock();
            board.registrarEvento("Tablero desbloqueado");
        }

        this.boardRepository.save(board);
    }

    /**
     * @brief Concede acceso a un nuevo usuario sobre el tablero validando que quien invita sea el dueño.
     * @param cmd Comando con el correo del dueño, el del invitado y el rol a otorgar.
     * @throws IllegalStateException Si el usuario solicitante no es el dueño original del tablero.
     */
    @Override
    public void compartirTablero(CompartirBoardCommand cmd) {
        log.info("Compartiendo tablero {} con {}", cmd.boardId(), cmd.emailInvitado());

        Board board = boardRepository.findById(new BoardId(cmd.boardId()))
                .orElseThrow(() -> new IllegalArgumentException("El tablero no existe"));

        if (!board.getEmail().equals(new Email(cmd.emailSolicitante()))) {
            throw new IllegalStateException("Solo el dueño puede compartir el tablero");
        }

        Rol rol = Rol.valueOf(cmd.rol().toUpperCase());
        board.compartirCon(new Email(cmd.emailInvitado()), rol);
        board.registrarEvento("Tablero compartido con " + cmd.emailInvitado() + " con rol " + rol);

        boardRepository.save(board);
    }

    /**
     * @brief Elimina los privilegios de un usuario sobre el tablero, verificando autoridad.
     * @param boardId Identificador del tablero objetivo.
     * @param emailSolicitante Correo de quien realiza la petición (debe ser el dueño).
     * @param emailAEliminar Correo del usuario que perderá el acceso.
     * @throws IllegalStateException Si el solicitante no es el dueño.
     */
    @Override
    public void revocarAcceso(String boardId, String emailSolicitante, String emailAEliminar) {
        log.info("Revocando acceso de {} al tablero {}", emailAEliminar, boardId);

        Board board = boardRepository.findById(new BoardId(boardId))
                .orElseThrow(() -> new IllegalArgumentException("El tablero no existe"));

        if (!board.getEmail().equals(new Email(emailSolicitante))) {
            throw new IllegalStateException("Solo el dueño puede revocar accesos");
        }

        board.revocarAcceso(new Email(emailAEliminar));
        board.registrarEvento("Acceso revocado para " + emailAEliminar);

        boardRepository.save(board);
    }

    /**
     * @brief Establece qué lista de tareas será considerada como el sumidero de las tarjetas finalizadas.
     * @param cmd Comando con el ID del tablero y el ID de la lista elegida.
     * @throws IllegalArgumentException Si la lista destino no se encuentra dentro del tablero.
     */
    @Override
    public void definirListaCompletadas(DefinirListCompletadasCommand cmd) {
        log.info("Configurando la lista {} como la de tareas completadas del tablero {}", cmd.listId(), cmd.boardId());

        Board board = this.boardRepository.findById(new BoardId(cmd.boardId()))
                .orElseThrow(() -> new IllegalArgumentException("El tablero especificado no existe"));

        ListId listId = new ListId(cmd.listId());

        boolean listaExiste = board.getTasksLists().stream()
                .anyMatch(lista -> lista.getId().equals(listId));

        if (!listaExiste) {
            throw new IllegalArgumentException("La lista especificada no pertenece a este tablero");
        }

        board.defineListCompletadas(listId);
        board.registrarEvento(String.format("Lista '%s' configurada como completadas", board.obtenerNombreLista(listId)));
        
        this.boardRepository.save(board);
    }

    /**
     * @brief Registra una nueva regla de automatización generando su identificador interno.
     * @param cmd Comando con la estructura del evento disparador y su acción resultante.
     */
    @Override
    public void anadirReglaAutomatizacion(CrearReglaCommand cmd) {
        log.info("Añadiendo automatización al tablero {}", cmd.boardId());

        Board board = boardRepository.findById(new BoardId(cmd.boardId()))
                .orElseThrow(() -> new IllegalArgumentException("El tablero no existe"));

        AutomationRule regla = new AutomationRule(
            java.util.UUID.randomUUID().toString(),
            es.um.pds.tableros.domain.board.TriggerType.valueOf(cmd.triggerType()),
            cmd.triggerPayload(),
            es.um.pds.tableros.domain.board.ActionType.valueOf(cmd.actionType())
        );

        board.anadirRegla(regla);
        board.registrarEvento("Nueva regla de automatización creada");

        boardRepository.save(board);
    }

    /**
     * @brief Método auxiliar interno (Helper) para aplicar la seguridad en los casos de uso.
     * @param board El tablero donde se quiere realizar la acción.
     * @param emailSolicitante El correo del usuario que ejecuta el comando.
     * @throws IllegalStateException Si el usuario no está autenticado o carece de permisos de escritura.
     */
    private void verificarPermisoEscritura(Board board, String emailSolicitante) {
        if (emailSolicitante == null || emailSolicitante.isBlank()) {
            throw new IllegalStateException("Usuario no autenticado");
        }
        Rol rol = board.obtenerRol(new Email(emailSolicitante));
        if (!Rol.WRITE.equals(rol)) {
            throw new IllegalStateException("El usuario no tiene permisos de escritura en este tablero");
        }
    }
    
    /**
     * @brief Implementación del caso de uso para renombrar un tablero. 
     * 1. Recupera el agregado Board a través del puerto de salida (Repositorio).
     * 2. Evalúa las políticas de seguridad del dominio, verificando que el usuario solicitante 
     * sea el propietario absoluto o disponga de permisos explícitos de escritura (Rol.WRITE).
     * 3. Delega la mutación al método protegido del agregado.
     * 4. Persiste el nuevo estado a través del repositorio.
     * * @param cmd Comando con los datos encapsulados de la petición.
     * @throws IllegalArgumentException Si el tablero no existe en la persistencia.
     * @throws IllegalStateException Si el usuario no tiene los privilegios de escritura necesarios.
     */
    @Override
    public void renombrarTablero(es.um.pds.tableros.domain.ports.input.board.commands.RenombrarBoardCommand cmd) {
        Board board = boardRepository.findById(new BoardId(cmd.boardId()))
                .orElseThrow(() -> new IllegalArgumentException("Tablero no encontrado"));

        // Validar permisos
        boolean esDueno = board.getEmail().value().equals(cmd.emailUsuario());
        boolean tienePermisoWrite = board.getPermisos().containsKey(new Email(cmd.emailUsuario())) &&
                                    board.getPermisos().get(new Email(cmd.emailUsuario())) == Rol.WRITE;

        if (!esDueno && !tienePermisoWrite) {
            throw new IllegalStateException("Solo los usuarios con permiso de escritura pueden modificar el tablero.");
        }

        // Modificar y guardar
        board.modificarTitulo(cmd.nuevoTitulo());
        boardRepository.save(board);
    }
}
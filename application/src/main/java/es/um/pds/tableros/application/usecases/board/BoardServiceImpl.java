package es.um.pds.tableros.application.usecases.board;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
import es.um.pds.tableros.domain.ports.input.board.commands.DefinirListCompletadasCommand;
import es.um.pds.tableros.domain.ports.output.BoardRepository;

@Service
public class BoardServiceImpl implements BoardService {

    private static final Logger log = LoggerFactory.getLogger(BoardServiceImpl.class);

    private final BoardRepository boardRepository;

    // Inyección por constructor del puerto de salida
    public BoardServiceImpl(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Override
    public Optional<Board> obtenerTableroPorId(BoardId id) {
        return this.boardRepository.findById(id);
    }

    @Override
    public List<Board> obtenerTablerosPorUsuario(String emailRaw) {
        // 1. La capa de aplicación transforma el String crudo al Value Object del Dominio
        // Si el formato es incorrecto, el dominio lanzará la IllegalArgumentException AQUÍ
        Email emailValidado = new Email(emailRaw);

        // 2. Si pasa la validación, procedemos a consultar el puerto de salida
        return this.boardRepository.findByEmail(emailValidado.value());
    }

    @Override
    public Board crearNuevoTablero(CrearBoardCommand cmd) {
        log.info("Creando nuevo tablero '{}' para el usuario {}", cmd.titulo(), cmd.emailCreator());

        // Generamos un identificador único de dominio para el nuevo tablero
        BoardId nuevoBoardId = BoardId.generate();
        
        // Instanciamos el agregado pasándole los parámetros del comando
        Board nuevoTablero = new Board(nuevoBoardId, cmd.titulo(), new Email(cmd.emailCreator()));

        nuevoTablero.registrarEvento("Tablero creado por " + cmd.emailCreator());
        
        // Persistimos el nuevo tablero a través del puerto de salida
        this.boardRepository.save(nuevoTablero);

        return nuevoTablero;
    }

    @Override
    public String anadirListaATablero(AnadirListCommand cmd) {

        log.info("Añadiendo lista '{}' al tablero {}",
                 cmd.nombreLista(),
                 cmd.boardId());

        Board board = this.boardRepository
                .findById(new BoardId(cmd.boardId()))
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "El tablero especificado no existe"
                    )
                );

        TaskList nuevaLista =
                board.addList(
                        cmd.nombreLista(),
                        cmd.maxCards()
                );

        board.registrarEvento(
                "Lista añadida: " + cmd.nombreLista()
        );

        this.boardRepository.save(board);

        return nuevaLista.getId().value();
    }

    @Override
    public void definirListaCompletadas(DefinirListCompletadasCommand cmd) {
        log.info("Configurando la lista {} como la de tareas completadas del tablero {}", cmd.listId(), cmd.boardId());

        Board board = this.boardRepository.findById(new BoardId(cmd.boardId()))
                .orElseThrow(() -> new IllegalArgumentException("El tablero especificado no existe"));

        ListId listId = new ListId(cmd.listId());

        // Validamos que la lista pertenezca realmente al tablero antes de marcarla
        boolean listaExiste = board.getTasksLists().stream()
                .anyMatch(lista -> lista.getId().equals(listId));

        if (!listaExiste) {
            throw new IllegalArgumentException("La lista especificada no pertenece a este tablero");
        }

        // Modificamos el estado del agregado
        board.defineListCompletadas(listId);

        board.registrarEvento(
        	    String.format("Lista '%s' configurada como completadas", board.obtenerNombreLista(listId))
        	);
        
        // Sincronizamos con el repositorio
        this.boardRepository.save(board);
    }

    @Override
    public void cambiarEstadoBloqueo(CambiarBloqueoBoardCommand cmd) {
        log.info("Cambiando estado de bloqueo del tablero {} a: {}", cmd.boardId(), cmd.bloquear());

        Board board = this.boardRepository.findById(new BoardId(cmd.boardId()))
                .orElseThrow(() -> new IllegalArgumentException("El tablero especificado no existe"));

        // Modificamos las invariantes según venga la bandera del comando
        if (cmd.bloquear()) {
            board.lock();
            board.registrarEvento("Tablero bloqueado temporalmente");
        } else {
            board.unlock();
            board.registrarEvento("Tablero desbloqueado");
        }

        // Persistimos los cambios
        this.boardRepository.save(board);
    }
    
    @Override
    public void compartirTablero(CompartirBoardCommand cmd) {
        log.info("Compartiendo tablero {} con {}", cmd.boardId(), cmd.emailInvitado());

        Board board = boardRepository.findById(new BoardId(cmd.boardId()))
                .orElseThrow(() -> new IllegalArgumentException("El tablero no existe"));

        // Solo el dueño puede compartir
        if (!board.getEmail().equals(new Email(cmd.emailSolicitante()))) {
            throw new IllegalStateException("Solo el dueño puede compartir el tablero");
        }

        Rol rol = Rol.valueOf(cmd.rol().toUpperCase());
        board.compartirCon(new Email(cmd.emailInvitado()), rol);
        board.registrarEvento("Tablero compartido con " + cmd.emailInvitado() + " con rol " + rol);

        boardRepository.save(board);
    }

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
}
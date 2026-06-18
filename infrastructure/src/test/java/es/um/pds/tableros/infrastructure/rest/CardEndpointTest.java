package es.um.pds.tableros.infrastructure.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.board.Email;
import es.um.pds.tableros.domain.board.ListId;
import es.um.pds.tableros.domain.card.Card;
import es.um.pds.tableros.domain.card.CardId;
import es.um.pds.tableros.domain.card.CardType;
import es.um.pds.tableros.domain.ports.output.BoardRepository;
import es.um.pds.tableros.domain.ports.output.CardRepository;
import es.um.pds.tableros.infrastructure.rest.dto.CardDTO;
import es.um.pds.tableros.infrastructure.security.AuthSessionManager;

/**
 * @brief Pruebas de integración transaccionales para el Adaptador REST de Tarjetas (CardEndpoint).
 * Evalúa las llamadas de creación y traslados de flujos Kanban, interactuando con los repositorios 
 * reales sobre la base de datos relacional H2 embebida.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CardEndpointTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final BoardRepository boardRepository;
    private final CardRepository cardRepository;
    private final AuthSessionManager sessionManager; 

    private String codigoValido; 
    private static final String EMAIL_TEST = "alumno@um.es"; 

    private static final String BASE = "/api/v1/tarjetas";
    
    private Board boardBase;
    private String idListaOrigen;
    private String idListaDestino;

    /**
     * @brief Constructor con inyección completa para las pruebas del endpoint de tarjetas.
     * @param mockMvc Controlador de peticiones web virtuales.
     * @param objectMapper Lector/Escritor JSON.
     * @param boardRepository Puerto de salida relacional de tableros.
     * @param cardRepository Puerto de salida relacional de tarjetas.
     * @param sessionManager Gestor de control de sesión OTP.
     */
    @Autowired
    public CardEndpointTest(MockMvc mockMvc, ObjectMapper objectMapper, BoardRepository boardRepository, CardRepository cardRepository, AuthSessionManager sessionManager) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.boardRepository = boardRepository;
        this.cardRepository = cardRepository;
        this.sessionManager = sessionManager;
    }

    /**
     * @brief Prepara el escenario transaccional previo a cada test.
     * Genera la sesión válida de elusión de seguridad e instancia y persiste de forma real un tablero 
     * de pruebas equipado con dos columnas ("To Do" e "In Progress") para dar soporte físico relacional.
     */
    @BeforeEach
    void setUp() {
        // Generamos un código válido en memoria antes de cada test para engañar al Interceptor
        this.codigoValido = sessionManager.generarYGuardarCodigo(EMAIL_TEST);

        BoardId boardId = BoardId.generate();
        boardBase = new Board(boardId, "Tablero de Pruebas", new Email(EMAIL_TEST));
        
        boardBase.addList("To Do", 10);
        boardBase.addList("In Progress", 5);
        
        idListaOrigen = boardBase.getTasksLists().get(0).getId().value();
        idListaDestino = boardBase.getTasksLists().get(1).getId().value();
        
        boardRepository.save(boardBase);
    }

    /**
     * @brief Comprueba la inserción legítima de tareas mediante payloads JSON, verificando que se 
     * calcula su persistencia y se retorna un HTTP 201 con su nuevo identificador único de base de datos.
     */
    @Test
    void createTarjeta_datosValidos_devuelve201YTarjetaConId() throws Exception {
        String json = """
                {
                    "boardId": "%s",
                    "listIdActual": "%s",
                    "titulo": "Implementar Tests de Integración",
                    "tipo": "TASK"
                }
                """.formatted(boardBase.getId().value(), idListaOrigen);

        MvcResult result = mockMvc.perform(post(BASE)
                        .header("X-User-Email", EMAIL_TEST) 
                        .header("X-Auth-Code", codigoValido) 
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated()) 
                .andExpect(jsonPath("$.titulo").value("Implementar Tests de Integración"))
                .andExpect(jsonPath("$.tipo").value("TASK"))
                .andExpect(jsonPath("$.listIdActual").value(idListaOrigen))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        CardDTO tarjetaGuardada = objectMapper.readValue(responseJson, CardDTO.class);
        assertNotNull(tarjetaGuardada.getId());
    }

    /**
     * @brief Valida el rechazo defensivo ante payloads que intentan violar la inmutabilidad de la identidad forzando IDs.
     */
    @Test
    void createTarjeta_conIdForzado_devuelve400() throws Exception {
        String json = """
                {
                    "id": "hack-id-forzado",
                    "boardId": "%s",
                    "listIdActual": "%s",
                    "titulo": "Intento Invalido",
                    "tipo": "TASK"
                }
                """.formatted(boardBase.getId().value(), idListaOrigen);

        mockMvc.perform(post(BASE)
                        .header("X-User-Email", EMAIL_TEST) 
                        .header("X-Auth-Code", codigoValido) 
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    /**
     * @brief Valida la obtención y lectura REST de una tarjeta inyectada previamente de forma directa en el repositorio.
     */
    @Test
    void getTarjeta_existente_devuelve200() throws Exception {
        CardId cardId = CardId.generate();
        Card card = new Card(cardId, boardBase.getId(), new ListId(idListaOrigen), "Tarjeta a buscar", CardType.TASK);
        cardRepository.save(card);

        mockMvc.perform(get(BASE + "/" + cardId.value())
                        .header("X-User-Email", EMAIL_TEST) 
                        .header("X-Auth-Code", codigoValido) 
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.id").value(cardId.value()))
                .andExpect(jsonPath("$.titulo").value("Tarjeta a buscar"));
    }

    /**
     * @brief Prueba de integración de extremo a extremo (End-to-End integration) para el traslado de tarjetas.
     * Genera la tarjeta, emite la petición PUT simulando la acción visual de Drag and Drop del usuario y 
     * comprueba de manera asertiva final consultando al repositorio que el estado interno en la base de datos 
     * H2 mutó de forma consistente hacia la columna destino.
     */
    @Test
    void moverTarjeta_movimientoValido_devuelve200() throws Exception {
        CardId cardId = CardId.generate();
        Card card = new Card(cardId, boardBase.getId(), new ListId(idListaOrigen), "Tarea Movible", CardType.TASK);
        cardRepository.save(card);
        
        boardBase.registrarMovimientoTarjeta(null, new ListId(idListaOrigen));
        boardRepository.save(boardBase);

        String jsonPayload = """
                {
                    "boardId": "%s",
                    "targetListId": "%s"
                }
                """.formatted(boardBase.getId().value(), idListaDestino);

        mockMvc.perform(put(BASE + "/" + cardId.value() + "/movimiento")
                        .header("X-User-Email", EMAIL_TEST) 
                        .header("X-Auth-Code", codigoValido) 
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk()); 

        Card tarjetaEnBd = cardRepository.findById(cardId).orElseThrow();
        assertEquals(idListaDestino, tarjetaEnBd.getListIdActual().value(), "La tarjeta debería haber cambiado de lista en la BD");
    }
}
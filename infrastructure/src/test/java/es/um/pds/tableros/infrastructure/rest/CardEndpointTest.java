package es.um.pds.tableros.infrastructure.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
import es.um.pds.tableros.infrastructure.security.AuthSessionManager; // AÑADIDO

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CardEndpointTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final BoardRepository boardRepository;
    private final CardRepository cardRepository;
    private final AuthSessionManager sessionManager; // AÑADIDO

    private String codigoValido; // AÑADIDO
    private static final String EMAIL_TEST = "alumno@um.es"; // AÑADIDO

    @Autowired
    public CardEndpointTest(MockMvc mockMvc, ObjectMapper objectMapper, BoardRepository boardRepository, CardRepository cardRepository, AuthSessionManager sessionManager) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.boardRepository = boardRepository;
        this.cardRepository = cardRepository;
        this.sessionManager = sessionManager;
    }

    private static final String BASE = "/api/v1/tarjetas";
    
    private Board boardBase;
    private String idListaOrigen;
    private String idListaDestino;

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
                        .header("X-User-Email", EMAIL_TEST) // AÑADIDO
                        .header("X-Auth-Code", codigoValido) // AÑADIDO
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
                        .header("X-User-Email", EMAIL_TEST) // AÑADIDO
                        .header("X-Auth-Code", codigoValido) // AÑADIDO
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTarjeta_existente_devuelve200() throws Exception {
        CardId cardId = CardId.generate();
        Card card = new Card(cardId, boardBase.getId(), new ListId(idListaOrigen), "Tarjeta a buscar", CardType.TASK);
        cardRepository.save(card);

        mockMvc.perform(get(BASE + "/" + cardId.value())
                        .header("X-User-Email", EMAIL_TEST) // AÑADIDO
                        .header("X-Auth-Code", codigoValido) // AÑADIDO
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.id").value(cardId.value()))
                .andExpect(jsonPath("$.titulo").value("Tarjeta a buscar"));
    }

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
                        .header("X-User-Email", EMAIL_TEST) // AÑADIDO
                        .header("X-Auth-Code", codigoValido) // AÑADIDO
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk()); 

        Card tarjetaEnBd = cardRepository.findById(cardId).orElseThrow();
        assertEquals(idListaDestino, tarjetaEnBd.getListIdActual().value(), "La tarjeta debería haber cambiado de lista en la BD");
    }
}
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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CardEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Inyectamos los repositorios reales para preparar el escenario de pruebas
    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private CardRepository cardRepository;

    private static final String BASE = "/api/v1/tarjetas";
    
    private Board boardBase;
    private String idListaOrigen;
    private String idListaDestino;

    @BeforeEach
    void setUp() {
        // Creamos un escenario base real en la base de datos antes de cada test
        BoardId boardId = BoardId.generate();
        boardBase = new Board(boardId, "Tablero de Pruebas", new Email("alumno@um.es"));
        
        // Añadimos dos columnas/listas de ejemplo al tablero
        boardBase.addList("To Do", 10);
        boardBase.addList("In Progress", 5);
        
        idListaOrigen = boardBase.getTasksLists().get(0).getId().value();
        idListaDestino = boardBase.getTasksLists().get(1).getId().value();
        
        // Guardamos el tablero en la BD para que los servicios puedan encontrarlo
        boardRepository.save(boardBase);
    }

    @Test
    void createTarjeta_datosValidos_devuelve201YTarjetaConId() throws Exception {
        // Construimos el JSON dinámicamente inyectando los IDs reales creados en el setUp()
        String json = """
                {
                    "boardId": "%s",
                    "listIdActual": "%s",
                    "titulo": "Implementar Tests de Integración",
                    "tipo": "TASK"
                }
                """.formatted(boardBase.getId().value(), idListaOrigen);

        MvcResult result = mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated()) // 201 Created
                .andExpect(jsonPath("$.titulo").value("Implementar Tests de Integración"))
                .andExpect(jsonPath("$.tipo").value("TASK"))
                .andExpect(jsonPath("$.listIdActual").value(idListaOrigen))
                .andReturn();

        // Verificamos que se le haya asignado un ID de dominio único
        String responseJson = result.getResponse().getContentAsString();
        CardDTO tarjetaGuardada = objectMapper.readValue(responseJson, CardDTO.class);
        assertNotNull(tarjetaGuardada.getId());
    }

    @Test
    void createTarjeta_conIdForzado_devuelve400() throws Exception {
        // Enviar un ID en una creación debe ser rechazado por vuestro endpoint (400 Bad Request)
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTarjeta_existente_devuelve200() throws Exception {
        // Guardamos directamente una tarjeta física en la base de datos
        CardId cardId = CardId.generate();
        Card card = new Card(cardId, boardBase.getId(), new ListId(idListaOrigen), "Tarjeta a buscar", CardType.TASK);
        cardRepository.save(card);

        // Intentamos recuperarla a través del endpoint GET
        mockMvc.perform(get(BASE + "/" + cardId.value())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(jsonPath("$.id").value(cardId.value()))
                .andExpect(jsonPath("$.titulo").value("Tarjeta a buscar"));
    }

    @Test
    void moverTarjeta_movimientoValido_devuelve200() throws Exception {
        // 1. Insertamos una tarjeta en la lista de origen en la BD
        CardId cardId = CardId.generate();
        Card card = new Card(cardId, boardBase.getId(), new ListId(idListaOrigen), "Tarea Movible", CardType.TASK);
        cardRepository.save(card);
        
        // Sincronizamos el contador del tablero para simular que la tarjeta ya estaba ahí metida
        boardBase.registrarMovimientoTarjeta(null, new ListId(idListaOrigen));
        boardRepository.save(boardBase);

        // 2. Preparamos el JSON del payload
        String jsonPayload = """
                {
                    "boardId": "%s",
                    "targetListId": "%s"
                }
                """.formatted(boardBase.getId().value(), idListaDestino);

        // 3. Ejecutamos el PUT de movimiento
        mockMvc.perform(put(BASE + "/" + cardId.value() + "/movimiento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk()); // 200 OK

        // 4. Verificación de seguridad extra: Consultamos la BD real para confirmar el cambio
        Card tarjetaEnBd = cardRepository.findById(cardId).orElseThrow();
        assertEquals(idListaDestino, tarjetaEnBd.getListIdActual().value(), "La tarjeta debería haber cambiado de lista en la BD");
    }
}
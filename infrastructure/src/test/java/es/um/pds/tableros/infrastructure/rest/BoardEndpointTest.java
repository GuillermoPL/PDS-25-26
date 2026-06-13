package es.um.pds.tableros.infrastructure.rest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import es.um.pds.tableros.infrastructure.rest.dto.BoardDTO;
import es.um.pds.tableros.infrastructure.security.AuthSessionManager;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BoardEndpointTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final AuthSessionManager sessionManager; // AÑADIDO

    private String codigoValido; // AÑADIDO
    private static final String EMAIL_TEST = "alumno@um.es"; // AÑADIDO

    @Autowired
    public BoardEndpointTest(MockMvc mockMvc, ObjectMapper objectMapper, AuthSessionManager sessionManager) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.sessionManager = sessionManager;
    }

    private static final String BASE = "/api/v1/tableros";

    @BeforeEach
    void setUp() {
        // Generamos un código válido en memoria antes de cada test para engañar al Interceptor
        this.codigoValido = sessionManager.generarYGuardarCodigo(EMAIL_TEST);
    }

    @Test
    void createTablero_datosValidos_devuelve201YTableroConId() throws Exception {
        String json = """
                {
                    "titulo": "Tablero de Integración",
                    "email": "alumno@um.es"
                }
                """;

        MvcResult result = mockMvc.perform(post(BASE)
                        .header("X-User-Email", EMAIL_TEST) // AÑADIDO
                        .header("X-Auth-Code", codigoValido) // AÑADIDO
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated()) 
                .andExpect(jsonPath("$.titulo").value("Tablero de Integración"))
                .andExpect(jsonPath("$.email").value("alumno@um.es"))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        BoardDTO tableroGuardado = objectMapper.readValue(responseJson, BoardDTO.class);
        
        assertNotNull(tableroGuardado.getId(), "El tablero guardado en BD debería tener un ID autogenerado");
    }

    @Test
    void createTablero_conIdForzado_devuelve400() throws Exception {
        String json = """
                {
                    "id": "intentohackeo123",
                    "titulo": "Tablero Hack",
                    "email": "alumno@um.es"
                }
                """;

        mockMvc.perform(post(BASE)
                        .header("X-User-Email", EMAIL_TEST) // AÑADIDO
                        .header("X-Auth-Code", codigoValido) // AÑADIDO
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void getTablero_idInexistente_devuelve404() throws Exception {
        mockMvc.perform(get(BASE + "/id_que_no_existe")
                        .header("X-User-Email", EMAIL_TEST) // AÑADIDO
                        .header("X-Auth-Code", codigoValido) // AÑADIDO
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
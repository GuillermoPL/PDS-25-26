package es.um.pds.tableros.test.infrastructure.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BoardEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE = "/api/v1/tableros";

    @Test
    void createTablero_datosValidos_devuelve201YTableroConId() throws Exception {
        // 1. Preparamos el JSON que vamos a enviar
        String json = """
                {
                    "titulo": "Tablero de Integración",
                    "email": "alumno@um.es"
                }
                """;

        // 2. Ejecutamos la petición POST real contra toda la aplicación
        MvcResult result = mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated()) // Esperamos un 201 CREATED
                .andExpect(jsonPath("$.titulo").value("Tablero de Integración"))
                .andExpect(jsonPath("$.email").value("alumno@um.es"))
                .andReturn();

        // 3. Podemos extraer la respuesta para comprobar que la BD le ha asignado un ID real
        String responseJson = result.getResponse().getContentAsString();
        BoardDTO tableroGuardado = objectMapper.readValue(responseJson, BoardDTO.class);
        
        assertNotNull(tableroGuardado.getId(), "El tablero guardado en BD debería tener un ID autogenerado");
    }

    @Test
    void createTablero_conIdForzado_devuelve400() throws Exception {
        // Si mandamos un ID en la creación, el endpoint debe rechazarlo por seguridad
        String json = """
                {
                    "id": "intentohackeo123",
                    "titulo": "Tablero Hack",
                    "email": "hacker@um.es"
                }
                """;

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest()); // Esperamos un 400 BAD REQUEST
    }
    
    @Test
    void getTablero_idInexistente_devuelve404() throws Exception {
        // Buscamos un tablero que sabemos que no existe en la BD
        mockMvc.perform(get(BASE + "/id_que_no_existe")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Esperamos un 404 NOT FOUND
    }
}
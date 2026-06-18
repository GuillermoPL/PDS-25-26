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

/**
 * @brief Pruebas de integración para el Adaptador REST de Tableros (BoardEndpoint).
 * Llevan a cabo el levantamiento del contexto completo de Spring Boot mapeando las solicitudes 
 * HTTP simuladas mediante MockMvc y validando el comportamiento integral frente al interceptor de seguridad.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BoardEndpointTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final AuthSessionManager sessionManager; 

    private String codigoValido; 
    private static final String EMAIL_TEST = "alumno@um.es"; 

    /**
     * @brief Constructor cableado automáticamente para inyectar los simuladores de red y componentes.
     * @param mockMvc Abstracción técnica de Spring para lanzar peticiones HTTP virtuales sin levantar el servidor Tomcat.
     * @param objectMapper Serializador/Deserializador JSON de Jackson.
     * @param sessionManager Gestor de infraestructura de seguridad para precargar sesiones legítimas.
     */
    @Autowired
    public BoardEndpointTest(MockMvc mockMvc, ObjectMapper objectMapper, AuthSessionManager sessionManager) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.sessionManager = sessionManager;
    }

    private static final String BASE = "/api/v1/tableros";

    /**
     * @brief Inicialización de pre-condiciones de entorno.
     * Genera e inyecta dinámicamente un token dinámico OTP legítimo en memoria antes de cada ejecución 
     * de test para superar de forma controlada la aduana del AuthInterceptor.
     */
    @BeforeEach
    void setUp() {
        this.codigoValido = sessionManager.generarYGuardarCodigo(EMAIL_TEST);
    }

    /**
     * @brief Verifica que el envío de payloads correctos crea un tablero devolviendo HTTP 201 Created 
     * junto con los datos planos estructurados correspondientes en base de datos H2.
     */
    @Test
    void createTablero_datosValidos_devuelve201YTableroConId() throws Exception {
        String json = """
                {
                    "titulo": "Tablero de Integración",
                    "email": "alumno@um.es"
                }
                """;

        MvcResult result = mockMvc.perform(post(BASE)
                        .header("X-User-Email", EMAIL_TEST) 
                        .header("X-Auth-Code", codigoValido) 
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

    /**
     * @brief Valida que los intentos maliciosos de forzar una clave primaria ID desde el exterior son 
     * interceptados defensivamente por el controlador devolviendo un HTTP 400 Bad Request.
     */
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
                        .header("X-User-Email", EMAIL_TEST) 
                        .header("X-Auth-Code", codigoValido) 
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
    
    /**
     * @brief Comprueba que las peticiones sobre recursos inexistentes se manejan de manera semántica 
     * respondiendo con un HTTP 404 Not Found estándar.
     */
    @Test
    void getTablero_idInexistente_devuelve404() throws Exception {
        mockMvc.perform(get(BASE + "/id_que_no_existe")
                        .header("X-User-Email", EMAIL_TEST) 
                        .header("X-Auth-Code", codigoValido) 
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
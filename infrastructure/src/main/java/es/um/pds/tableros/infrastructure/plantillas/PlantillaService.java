package es.um.pds.tableros.infrastructure.plantillas;

import java.io.InputStream;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.ports.input.board.BoardService;
import es.um.pds.tableros.domain.ports.input.board.commands.AnadirListCommand;
import es.um.pds.tableros.domain.ports.input.board.commands.CrearBoardCommand;
import es.um.pds.tableros.domain.ports.input.card.CardService;
import es.um.pds.tableros.domain.ports.input.card.commands.CrearCardCommand;

/**
 * @brief Componente utilitario de infraestructura encargado de automatizar la creación de entornos.
 * Lee archivos de texto `.yaml` situados bajo la ruta de recursos (`resources/plantillas/`)
 * y simula secuencialmente la invocación de comandos de los casos de uso para generar tableros completos.
 */
@Component
public class PlantillaService {

    private final BoardService boardService;
    private final CardService cardService;

    /**
     * @brief Constructor con inyección automática de los puertos de entrada correspondientes al dominio.
     * @param boardService Servicio de aplicación encargado del control operacional de tableros.
     * @param cardService Servicio de aplicación encargado del ciclo operacional de tarjetas.
     */
    public PlantillaService(BoardService boardService, CardService cardService) {
        this.boardService = boardService;
        this.cardService = cardService;
    }

    /**
     * @brief Orquesta la lectura de una plantilla de disco y ejecuta el flujo completo de creación de datos.
     * Carga el archivo mediante un `ClassPathResource`, realiza el parseo de datos estructurados con un 
     * `ObjectMapper` parametrizado con `YAMLFactory` y realiza de manera ordenada la inserción lógica 
     * del tablero raíz, sus listas locales y las subtareas estipuladas.
     * @param nombreArchivoYaml Nombre físico con extensión del recurso de configuración (ej: "scrum.yaml").
     * @param emailUsuario Dirección de correo electrónico del usuario solicitante que figurará como dueño.
     * @throws RuntimeException Si ocurre un fallo de E/S leyendo el archivo o si se viola alguna invariante al procesar los comandos.
     */
    public void crearTableroDesdePlantilla(String nombreArchivoYaml, String emailUsuario) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        
        try (InputStream inputStream = new ClassPathResource("plantillas/" + nombreArchivoYaml).getInputStream()) {
            
            // 1. Parsear el YAML
            PlantillaYamlDTO plantilla = mapper.readValue(inputStream, PlantillaYamlDTO.class);
            
            // 2. Crear el Tablero base
            CrearBoardCommand crearTableroCmd = new CrearBoardCommand(plantilla.getTitulo(), emailUsuario);
            Board nuevoTablero = boardService.crearNuevoTablero(crearTableroCmd);
            String boardId = nuevoTablero.getId().value(); 
            
            // 3. Crear las Listas y sus Tarjetas
            if (plantilla.getListas() != null) {
                for (PlantillaYamlDTO.ListaYamlDTO lista : plantilla.getListas()) {
                    // AÑADIDO: Pasamos emailUsuario al final para superar el control de seguridad de los casos de uso
                    AnadirListCommand anadirListaCmd = new AnadirListCommand(boardId, lista.getNombre(), null, emailUsuario);
                    String listId = boardService.anadirListaATablero(anadirListaCmd);
                    
                    if (lista.getTarjetas() != null) {
                        for (String tituloTarjeta : lista.getTarjetas()) {
                            // AÑADIDO: Pasamos los dos nulls para las etiquetas (nombre y color), null para la checklist y emailUsuario
                            CrearCardCommand crearCardCmd = new CrearCardCommand(
                                boardId, 
                                listId, 
                                tituloTarjeta, 
                                "TASK", // Por defecto hacemos que sean tarjetas simples (TASK)
                                null,   // nombreEtiqueta
                                null,   // colorEtiqueta
                                null,   // checklistItems
                                emailUsuario // AÑADIDO para el control de auditoría y permisos de escritura
                            );
                            cardService.crearNuevaTarjeta(crearCardCmd);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el tablero desde la plantilla: " + e.getMessage(), e);
        }
    }
}
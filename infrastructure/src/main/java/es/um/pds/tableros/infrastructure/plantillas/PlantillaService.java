package es.um.pds.tableros.infrastructure.plantillas;

import java.io.InputStream;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.ports.input.board.BoardService;
import es.um.pds.tableros.domain.ports.input.board.commands.AnadirListCommand;
import es.um.pds.tableros.domain.ports.input.board.commands.CrearBoardCommand;
import es.um.pds.tableros.domain.ports.input.card.CardService;
import es.um.pds.tableros.domain.ports.input.card.commands.CrearCardCommand;

@Service
public class PlantillaService {

    private final BoardService boardService;
    private final CardService cardService;

    public PlantillaService(BoardService boardService, CardService cardService) {
        this.boardService = boardService;
        this.cardService = cardService;
    }

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
                    // AÑADIDO: Pasamos emailUsuario al final
                    AnadirListCommand anadirListaCmd = new AnadirListCommand(boardId, lista.getNombre(), null, emailUsuario);
                    String listId = boardService.anadirListaATablero(anadirListaCmd);
                    
                    if (lista.getTarjetas() != null) {
                        for (String tituloTarjeta : lista.getTarjetas()) {
                            // AÑADIDO: Pasamos los dos nulls para las etiquetas (nombre y color), null para la checklist y emailUsuario
                            CrearCardCommand crearCardCmd = new CrearCardCommand(
                                boardId, 
                                listId, 
                                tituloTarjeta, 
                                "TASK", // Por defecto hacemos que sean tareas simples
                                null,   // nombreEtiqueta
                                null,   // colorEtiqueta
                                null,   // checklistItems
                                emailUsuario // AÑADIDO
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
package es.um.pds.tableros.domain.ports.input.card;

import java.util.List;
import java.util.Optional;

import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.card.Card;
import es.um.pds.tableros.domain.card.CardId;
import es.um.pds.tableros.domain.ports.input.card.commands.AnadirEtiquetaCommand;
import es.um.pds.tableros.domain.ports.input.card.commands.CrearCardCommand;
import es.um.pds.tableros.domain.ports.input.card.commands.MoverCardCommand;

public interface CardService {

    // Consultas básicas
    Optional<Card> obtenerTarjetaPorId(CardId id);
    
    List<Card> obtenerTarjetasPorTablero(String boardId);
    
    // Operaciones de negocio basadas en comandos
    Card crearNuevaTarjeta(CrearCardCommand cmd);

    void moverTarjeta(MoverCardCommand cmd);
    
    void anadirEtiqueta(AnadirEtiquetaCommand cmd);
    
}

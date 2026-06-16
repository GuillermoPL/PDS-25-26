package es.um.pds.tableros.domain.ports.input.card;

import java.util.List;
import java.util.Optional;

import es.um.pds.tableros.domain.card.Card;
import es.um.pds.tableros.domain.ports.input.card.commands.AnadirEtiquetaCommand;
import es.um.pds.tableros.domain.ports.input.card.commands.CrearCardCommand;
import es.um.pds.tableros.domain.ports.input.card.commands.MoverCardCommand;

/**
 * @brief Puerto de Entrada (Input Port) que expone los casos de uso para la gestión de tarjetas.
 * Define el contrato para interactuar con el dominio de las tarjetas, separando claramente
 * las consultas de lectura de las operaciones de mutación (Commands).
 */
public interface CardService {

    // CONSULTAS 

    /**
     * @brief Recupera la información de una tarjeta específica mediante su identificador.
     * @param id Cadena de texto con el ID único de la tarjeta.
     * @return Un Optional con la tarjeta si existe en el sistema, o vacío en caso contrario.
     */
    Optional<Card> obtenerTarjetaPorId(String id);
    
    /**
     * @brief Recupera todas las tarjetas asociadas a un tablero específico.
     * @param boardId Identificador del tablero del cual se quieren extraer las tarjetas.
     * @return Lista completa de tarjetas pertenecientes a dicho tablero.
     */
    List<Card> obtenerTarjetasPorTablero(String boardId);

    // OPERACIONES (Commands) 

    /**
     * @brief Orquesta la creación de una nueva tarjeta y su inserción en una lista.
     * @param cmd Comando con los datos requeridos (tablero, lista destino, título, tipo, etc.).
     * @return La instancia de la tarjeta recién creada en el dominio.
     */
    Card crearNuevaTarjeta(CrearCardCommand cmd);

    /**
     * @brief Gestiona el traslado de una tarjeta desde su lista actual hacia una nueva.
     * Invoca las reglas de negocio pertinentes del tablero (ej. límites o bloqueos).
     * @param cmd Comando con los identificadores de la tarjeta, el tablero y la lista destino.
     */
    void moverTarjeta(MoverCardCommand cmd);
    
    /**
     * @brief Asigna una nueva etiqueta visual (color/categoría) a una tarjeta existente.
     * @param cmd Comando con los detalles de la etiqueta y el identificador de la tarjeta objetivo.
     */
    void anadirEtiqueta(AnadirEtiquetaCommand cmd);
}
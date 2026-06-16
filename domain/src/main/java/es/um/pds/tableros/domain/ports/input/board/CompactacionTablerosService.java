package es.um.pds.tableros.domain.ports.input.board;

/**
 * @brief Puerto de Entrada (Input Port) encargado del mantenimiento en lote de tableros.
 * Expone el caso de uso para ejecutar procesos pesados, como archivar tarjetas o limpiar
 * listas obsoletas de manera asíncrona o programada.
 */
public interface CompactacionTablerosService {
    
    /**
     * @brief Ejecuta el proceso de compactación sobre los tableros del sistema.
     */
    void ejecutarCompactacion();
}
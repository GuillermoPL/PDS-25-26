package es.um.pds.tableros.domain.ports.input.board.commands;

/**
 * @brief Objeto de Comando Inmutable (Record) para la operación de renombrar un tablero.
 * * Transporta de forma segura los datos requeridos desde los adaptadores de entrada 
 * (como la interfaz JavaFX o la API REST) hacia los casos de uso de la aplicación, 
 * garantizando que los parámetros no sean alterados durante el flujo de ejecución.
 * * @param boardId Identificador único del tablero objetivo.
 * @param nuevoTitulo Cadena de texto con el nuevo título a asignar.
 * @param emailUsuario Correo electrónico del usuario que solicita la acción (para auditoría y permisos).
 */
public record RenombrarBoardCommand(String boardId, String nuevoTitulo, String emailUsuario) {}
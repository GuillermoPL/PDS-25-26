package es.um.pds.tableros.domain.board;

/**
 * @brief Enumera los niveles de permisos disponibles para los usuarios invitados a un tablero.
 * * @note Forma parte del Value Object semántico que define el control de acceso en la entidad Board.
 */
public enum Rol {
    /** El usuario puede visualizar el tablero y sus tarjetas, pero no alterarlo. */
    READ,  
    /** El usuario posee capacidades operativas: añadir tarjetas, moverlas, editar, etc. */
    WRITE  
}
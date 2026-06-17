package es.um.pds.tableros.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.um.pds.tableros.infrastructure.persistence.jpa.entity.BoardEntity;

/**
 * @brief Repositorio nativo de Spring Data JPA para gestionar la entidad BoardEntity.
 * Ofrece todas las capacidades CRUD estándar sobre la tabla "BOARD" en la base de datos H2
 * y define consultas semánticas personalizadas mediante JPQL.
 */
public interface SpringDataBoardRepository extends JpaRepository<BoardEntity, String> {

    /**
     * @brief Consulta relacional personalizada escrita en JPQL que extrae los tableros de un usuario.
     * Realiza un LEFT JOIN con el mapa de elementos embebidos de control de permisos para seleccionar 
     * aquellos tableros donde el email del usuario coincide con el propietario del tablero (b.email)
     * O actúa como clave K (KEY(p)) en el mapa relacional BOARD_PERMISOS.
     * @param email Dirección de correo electrónico del usuario a evaluar.
     * @return Lista de entidades BoardEntity que cumplen con alguna de las dos condiciones de acceso.
     */
    @Query("SELECT DISTINCT b FROM BoardEntity b LEFT JOIN b.permisos p WHERE b.email = :email OR KEY(p) = :email")
    List<BoardEntity> findByEmailOrSharedWith(@Param("email") String email);
    
}
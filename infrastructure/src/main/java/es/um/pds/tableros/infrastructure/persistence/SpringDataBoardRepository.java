package es.um.pds.tableros.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.um.pds.tableros.infrastructure.persistence.jpa.entity.BoardEntity;

public interface SpringDataBoardRepository extends JpaRepository<BoardEntity, String> {

    // Añade este método con la consulta personalizada:
    @Query("SELECT DISTINCT b FROM BoardEntity b LEFT JOIN b.permisos p WHERE b.email = :email OR KEY(p) = :email")
    List<BoardEntity> findByEmailOrSharedWith(@Param("email") String email);
    
}
package es.um.pds.tableros.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import es.um.pds.tableros.infrastructure.persistence.jpa.entity.BoardEntity;

public interface SpringDataBoardRepository extends JpaRepository<BoardEntity, String> {
    List<BoardEntity> findByEmail(String email);
}
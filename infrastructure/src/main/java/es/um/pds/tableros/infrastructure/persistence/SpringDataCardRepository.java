package es.um.pds.tableros.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.um.pds.tableros.infrastructure.persistence.jpa.entity.CardEntity;

public interface SpringDataCardRepository extends JpaRepository<CardEntity, String> {
    List<CardEntity> findByBoardId(String boardId);
}
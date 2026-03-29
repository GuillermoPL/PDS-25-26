package es.um.pds.tableros.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cards")
public class CardEntity {
    @Id
    private String id;
    
    // TODO: Añadir mapeos de columnas: board_id, list_id, title, etc.
    // TODO: Crear métodos estáticos para convertir de Dominio a Entity y viceversa (Mappers).
}
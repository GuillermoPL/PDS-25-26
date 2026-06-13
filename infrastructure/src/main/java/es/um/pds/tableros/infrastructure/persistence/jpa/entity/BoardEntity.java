package es.um.pds.tableros.infrastructure.persistence.jpa.entity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
@Entity
@Table(name = "BOARD")
public class BoardEntity {

    @Id
    @Column(name = "ID", nullable = false) // Obligatorio por ser Clave Primaria
    private String id;

    @Column(name = "TITULO", nullable = false) // El título no puede ser nulo
    private String titulo;

    @Column(name = "EMAIL_DUEÑO", nullable = false) // El dueño es obligatorio
    private String email;

    @Column(name = "IS_LOCKED", nullable = false)
    private boolean isLocked;

    @Column(name = "LIST_COMPLETADAS_ID", nullable = true) // Puede ser nulo si no se ha definido aún
    private String listCompletadasId;

    @OneToMany(mappedBy = "board", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskListEntity> tasksLists = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "BOARD_HISTORIAL", joinColumns = @JoinColumn(name = "BOARD_ID"))
    @Column(name = "EVENTO")
    private List<String> historial = new ArrayList<>();
    
 // Nuevo campo en BoardEntity, junto a los demás @ElementCollection
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "BOARD_PERMISOS", joinColumns = @JoinColumn(name = "BOARD_ID"))
    @MapKeyColumn(name = "EMAIL_USUARIO")
    @Column(name = "ROL")
    @Enumerated(EnumType.STRING)  // Guarda "READ"/"WRITE" como texto
    private Map<String, String> permisos = new HashMap<>();
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "BOARD_REGLAS", joinColumns = @JoinColumn(name = "BOARD_ID"))
    private List<AutomationRuleEmbeddable> reglas = new ArrayList<>();
    
    public BoardEntity() {}

    public BoardEntity(String id, String titulo, String email, boolean isLocked, 
            String listCompletadasId, List<TaskListEntity> tasksLists, 
            List<String> historial) {
    	this.id = id;
    	this.titulo = titulo;
    	this.email = email;
    	this.isLocked = isLocked;
    	this.listCompletadasId = listCompletadasId;
    	this.tasksLists = tasksLists;
    	this.historial = historial != null ? historial : new ArrayList<>();
}

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { this.isLocked = locked; }
    public String getListCompletadasId() { return listCompletadasId; }
    public void setListCompletadasId(String listCompletadasId) { this.listCompletadasId = listCompletadasId; }
    public List<TaskListEntity> getTasksLists() { return tasksLists; }
    public void setTasksLists(List<TaskListEntity> tasksLists) { this.tasksLists = tasksLists; }
    public List<String> getHistorial() { return historial; }
    public void setHistorial(List<String> historial) { this.historial = historial; }
    public Map<String, String> getPermisos() { return permisos; }
    public void setPermisos(Map<String, String> permisos) { this.permisos = permisos; }
    public List<AutomationRuleEmbeddable> getReglas() { return reglas; }
    public void setReglas(List<AutomationRuleEmbeddable> reglas) { this.reglas = reglas; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoardEntity)) return false;
        BoardEntity other = (BoardEntity) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
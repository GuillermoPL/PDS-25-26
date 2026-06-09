package es.um.pds.tableros.infrastructure.rest.dto;

import java.util.List;

public class BoardDTO {
    private String id;
    private String titulo;
    private String email;
    private boolean locked;
    private List<String> nombresListas; // Enviamos solo datos planos al exterior
    private String listCompletadasId;

    // Constructor vacío obligatorio para Jackson (JSON)
    public BoardDTO() {}

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public List<String> getNombresListas() { return nombresListas; }
    public void setNombresListas(List<String> nombresListas) { this.nombresListas = nombresListas; }

    public String getListCompletadasId() { return listCompletadasId; }
    public void setListCompletadasId(String listCompletadasId) { this.listCompletadasId = listCompletadasId; }
}
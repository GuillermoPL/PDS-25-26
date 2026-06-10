package es.um.pds.tableros.infrastructure.rest.dto;

import java.util.List;

public class BoardDTO {
    private String id;
    private String titulo;
    private String email;
    private boolean locked;
    private List<String> nombresListas; // Enviamos solo datos planos al exterior
    private String listCompletadasId;
    private List<String> historial;
    private List<ListaDTO> listas;
    
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
    
    public List<String> getHistorial() { return historial; }
    public void setHistorial(List<String> historial) { this.historial = historial; }
    
    public List<ListaDTO> getListas() { return listas; }
    public void setListas(List<ListaDTO> listas) { this.listas = listas; }
    
    public static class ListaDTO {
        private String id;
        private String nombre;

        public ListaDTO() {}
        
        public ListaDTO(String id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
    }
    
}
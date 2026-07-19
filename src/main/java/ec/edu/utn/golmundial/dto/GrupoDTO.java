package ec.edu.utn.golmundial.dto;

/**
 * Información pública de un grupo del torneo.
 */
public class GrupoDTO {

    private String codigo;
    private String nombre;

    public GrupoDTO() {
    }

    public GrupoDTO(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }
        public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}

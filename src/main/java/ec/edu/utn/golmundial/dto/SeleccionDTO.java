package ec.edu.utn.golmundial.dto;

/**
 * Información pública de una selección.
 */
public class SeleccionDTO {

    private Long id;
    private String codigoFifa;
    private String nombre;
    private String grupo;
    private String confederacion;
    private boolean anfitrion;
    private String clasificacion;

    public SeleccionDTO() {
    }

    public SeleccionDTO(
            Long id,
            String codigoFifa,
            String nombre,
            String grupo,
            String confederacion,
            boolean anfitrion,
            String clasificacion
    ) {
        this.id = id;
        this.codigoFifa = codigoFifa;
        this.nombre = nombre;
        this.grupo = grupo;
        this.confederacion = confederacion;
        this.anfitrion = anfitrion;
        this.clasificacion = clasificacion;
    }

    public Long getId() {
        return id;
    }

    public String getCodigoFifa() {
        return codigoFifa;
    }

    public String getNombre() {
        return nombre;
    }

    public String getGrupo() {
        return grupo;
    }

    public String getConfederacion() {
        return confederacion;
    }

    public boolean isAnfitrion() {
        return anfitrion;
    }

    public String getClasificacion() {
        return clasificacion;
    }public void setId(Long id) {
    this.id = id;
    }

    public void setCodigoFifa(String codigoFifa) {
        this.codigoFifa = codigoFifa;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public void setConfederacion(String confederacion) {
        this.confederacion = confederacion;
    }

    public void setAnfitrion(boolean anfitrion) {
        this.anfitrion = anfitrion;
    }

    public void setClasificacion(String clasificacion) {
        this.clasificacion = clasificacion;
    }

}

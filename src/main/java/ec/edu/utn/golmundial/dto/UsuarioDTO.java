package ec.edu.utn.golmundial.dto;

/**
 * Información pública de una cuenta.
 *
 * Nunca expone el hash, la sal ni otros datos
 * sensibles de la contraseña.
 */
public class UsuarioDTO {

    private Long id;
    private String username;
    private String nombre;
    private String rol;
    private boolean activo;
    private boolean cambioPasswordObligatorio;
    private String fechaCreacionUtc;
    private String fechaActualizacionUtc;

    public UsuarioDTO() {
    }

    public UsuarioDTO(
            Long id,
            String username,
            String nombre,
            String rol,
            boolean activo,
            boolean cambioPasswordObligatorio,
            String fechaCreacionUtc,
            String fechaActualizacionUtc
    ) {
        this.id = id;
        this.username = username;
        this.nombre = nombre;
        this.rol = rol;
        this.activo = activo;
        this.cambioPasswordObligatorio =
                cambioPasswordObligatorio;
        this.fechaCreacionUtc = fechaCreacionUtc;
        this.fechaActualizacionUtc =
                fechaActualizacionUtc;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public boolean isCambioPasswordObligatorio() {
        return cambioPasswordObligatorio;
    }

    public String getFechaCreacionUtc() {
        return fechaCreacionUtc;
    }

    public String getFechaActualizacionUtc() {
        return fechaActualizacionUtc;
    }
    public void setId(Long id) {
    this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public void setCambioPasswordObligatorio(
            boolean cambioPasswordObligatorio) {

        this.cambioPasswordObligatorio =
                cambioPasswordObligatorio;
    }

    public void setFechaCreacionUtc(
            String fechaCreacionUtc) {

        this.fechaCreacionUtc =
                fechaCreacionUtc;
    }

    public void setFechaActualizacionUtc(
            String fechaActualizacionUtc) {

        this.fechaActualizacionUtc =
                fechaActualizacionUtc;
    }
}

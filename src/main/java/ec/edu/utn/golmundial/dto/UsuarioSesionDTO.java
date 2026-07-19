package ec.edu.utn.golmundial.dto;

/**
 * Información pública del usuario autenticado.
 */
public class UsuarioSesionDTO {

    private Long id;
    private String username;
    private String nombre;
    private String rol;
    private boolean activo;
    private boolean cambioPasswordObligatorio;

    public UsuarioSesionDTO() {
    }

    public UsuarioSesionDTO(
            Long id,
            String username,
            String nombre,
            String rol,
            boolean activo,
            boolean cambioPasswordObligatorio
    ) {
        this.id = id;
        this.username = username;
        this.nombre = nombre;
        this.rol = rol;
        this.activo = activo;
        this.cambioPasswordObligatorio =
                cambioPasswordObligatorio;
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
            boolean cambioPasswordObligatorio
    ) {
        this.cambioPasswordObligatorio =
                cambioPasswordObligatorio;
    }
}

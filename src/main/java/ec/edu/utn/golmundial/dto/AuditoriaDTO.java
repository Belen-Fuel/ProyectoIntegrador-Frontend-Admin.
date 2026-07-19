package ec.edu.utn.golmundial.dto;

public class AuditoriaDTO {

    private Long id;
    private String accion;
    private String entidad;
    private Long entidadId;
    private String usuarioReferencia;
    private String detalle;
    private String fechaHoraUtc;

    public AuditoriaDTO() {
    }

    public AuditoriaDTO(
            Long id,
            String accion,
            String entidad,
            Long entidadId,
            String usuarioReferencia,
            String detalle,
            String fechaHoraUtc
    ) {
        this.id = id;
        this.accion = accion;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.usuarioReferencia = usuarioReferencia;
        this.detalle = detalle;
        this.fechaHoraUtc = fechaHoraUtc;
    }

    public Long getId() {
        return id;
    }

    public String getAccion() {
        return accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public Long getEntidadId() {
        return entidadId;
    }

    public String getUsuarioReferencia() {
        return usuarioReferencia;
    }

    public String getDetalle() {
        return detalle;
    }

    public String getFechaHoraUtc() {
        return fechaHoraUtc;
    }
    public void setId(Long id) {
    this.id = id;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public void setEntidadId(Long entidadId) {
        this.entidadId = entidadId;
    }

    public void setUsuarioReferencia(String usuarioReferencia) {
        this.usuarioReferencia = usuarioReferencia;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public void setFechaHoraUtc(String fechaHoraUtc) {
        this.fechaHoraUtc = fechaHoraUtc;
    }
}

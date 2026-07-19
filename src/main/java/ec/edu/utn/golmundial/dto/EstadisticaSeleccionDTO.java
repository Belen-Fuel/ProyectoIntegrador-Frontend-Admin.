package ec.edu.utn.golmundial.dto;

/**
 * Estadísticas acumuladas de una selección
 * durante todo el torneo.
 */
public class EstadisticaSeleccionDTO {

    private Long seleccionId;
    private String codigoFifa;
    private String seleccion;
    private String grupo;
    private String confederacion;

    private int jugados;
    private int ganados;
    private int empatados;
    private int perdidos;

    private int golesFavor;
    private int golesContra;
    private int diferenciaGoles;

    private int puntos;

    public EstadisticaSeleccionDTO() {
    }

    public EstadisticaSeleccionDTO(
            Long seleccionId,
            String codigoFifa,
            String seleccion,
            String grupo,
            String confederacion,
            int jugados,
            int ganados,
            int empatados,
            int perdidos,
            int golesFavor,
            int golesContra,
            int diferenciaGoles,
            int puntos
    ) {
        this.seleccionId = seleccionId;
        this.codigoFifa = codigoFifa;
        this.seleccion = seleccion;
        this.grupo = grupo;
        this.confederacion = confederacion;
        this.jugados = jugados;
        this.ganados = ganados;
        this.empatados = empatados;
        this.perdidos = perdidos;
        this.golesFavor = golesFavor;
        this.golesContra = golesContra;
        this.diferenciaGoles = diferenciaGoles;
        this.puntos = puntos;
    }

    public Long getSeleccionId() {
        return seleccionId;
    }

    public String getCodigoFifa() {
        return codigoFifa;
    }

    public String getSeleccion() {
        return seleccion;
    }

    public String getGrupo() {
        return grupo;
    }

    public String getConfederacion() {
        return confederacion;
    }

    public int getJugados() {
        return jugados;
    }

    public int getGanados() {
        return ganados;
    }

    public int getEmpatados() {
        return empatados;
    }

    public int getPerdidos() {
        return perdidos;
    }

    public int getGolesFavor() {
        return golesFavor;
    }

    public int getGolesContra() {
        return golesContra;
    }

    public int getDiferenciaGoles() {
        return diferenciaGoles;
    }

    public int getPuntos() {
        return puntos;
    }
    public void setSeleccionId(Long seleccionId) {
    this.seleccionId = seleccionId;
    }

    public void setCodigoFifa(String codigoFifa) {
        this.codigoFifa = codigoFifa;
    }

    public void setSeleccion(String seleccion) {
        this.seleccion = seleccion;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public void setConfederacion(String confederacion) {
        this.confederacion = confederacion;
    }

    public void setJugados(int jugados) {
        this.jugados = jugados;
    }

    public void setGanados(int ganados) {
        this.ganados = ganados;
    }

    public void setEmpatados(int empatados) {
        this.empatados = empatados;
    }

    public void setPerdidos(int perdidos) {
        this.perdidos = perdidos;
    }

    public void setGolesFavor(int golesFavor) {
        this.golesFavor = golesFavor;
    }

    public void setGolesContra(int golesContra) {
        this.golesContra = golesContra;
    }

    public void setDiferenciaGoles(int diferenciaGoles) {
        this.diferenciaGoles = diferenciaGoles;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }
}

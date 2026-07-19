package ec.edu.utn.golmundial.bean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ec.edu.utn.golmundial.dto.EstadisticaSeleccionDTO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Named("reporteBean")
@ViewScoped
public class ReporteBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String URL_RESUMEN =
            "http://localhost:8080/golmundial-estadisticas/api/datos/resumen";

    private static final String URL_ESTADISTICAS =
            "http://localhost:8080/golmundial-estadisticas/api/estadisticas/selecciones";

    @Inject
    private LoginBean loginBean;

    private String torneo;
    private Long roles = 0L;
    private Long usuarios = 0L;
    private Long fases = 0L;
    private Long grupos = 0L;
    private Long sedes = 0L;
    private Long selecciones = 0L;
    private Long partidos = 0L;
    private String estado;

    private String grupoSeleccionado;

    private List<EstadisticaSeleccionDTO> estadisticas =
            new ArrayList<>();

    @PostConstruct
    public void init() {

        if (!sesionValida()) {
            redirigirAlLogin();
            return;
        }

        cargarReportes();
    }

    public void cargarReportes() {
        cargarResumen();
        cargarEstadisticas();
    }

    public void cargarResumen() {

        try (Client cliente = ClientBuilder.newClient();
             Response respuesta = cliente
                     .target(URL_RESUMEN)
                     .request(MediaType.APPLICATION_JSON)
                     .get()) {

            if (respuesta.getStatus()
                    == Response.Status.OK.getStatusCode()) {

                Map<String, Object> datos =
                        respuesta.readEntity(
                                new GenericType<Map<String, Object>>() {
                                }
                        );

                torneo = obtenerTexto(datos, "torneo");
                roles = obtenerNumero(datos, "roles");
                usuarios = obtenerNumero(datos, "usuarios");
                fases = obtenerNumero(datos, "fases");
                grupos = obtenerNumero(datos, "grupos");
                sedes = obtenerNumero(datos, "sedes");
                selecciones = obtenerNumero(datos, "selecciones");
                partidos = obtenerNumero(datos, "partidos");
                estado = obtenerTexto(datos, "estado");

                return;
            }

            mostrarErrorRespuesta(
                    respuesta,
                    "No se pudo cargar el resumen."
            );

        } catch (Exception e) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    "No fue posible cargar el resumen de datos."
            );

            e.printStackTrace();
        }
    }

    public void cargarEstadisticas() {

        try (Client cliente = ClientBuilder.newClient()) {

            var destino = cliente.target(URL_ESTADISTICAS);

            if (grupoSeleccionado != null
                    && !grupoSeleccionado.isBlank()) {

                destino = destino.queryParam(
                        "grupo",
                        grupoSeleccionado
                );
            }

            try (Response respuesta = destino
                    .request(MediaType.APPLICATION_JSON)
                    .get()) {

                if (respuesta.getStatus()
                        == Response.Status.OK.getStatusCode()) {

                    estadisticas = respuesta.readEntity(
                            new GenericType<List<EstadisticaSeleccionDTO>>() {
                            }
                    );

                    if (estadisticas == null) {
                        estadisticas = new ArrayList<>();
                    }

                    return;
                }

                estadisticas = new ArrayList<>();

                mostrarErrorRespuesta(
                        respuesta,
                        "No se pudieron cargar las estadísticas."
                );
            }

        } catch (Exception e) {

            estadisticas = new ArrayList<>();

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    "No fue posible cargar las estadísticas."
            );

            e.printStackTrace();
        }
    }

    public void limpiarFiltro() {
        grupoSeleccionado = null;
        cargarEstadisticas();
    }

    private Long obtenerNumero(
            Map<String, Object> datos,
            String clave
    ) {

        if (datos == null) {
            return 0L;
        }

        Object valor = datos.get(clave);

        if (valor instanceof Number numero) {
            return numero.longValue();
        }

        if (valor != null) {

            try {
                return Long.parseLong(valor.toString());
            } catch (NumberFormatException ignored) {
            }
        }

        return 0L;
    }

    private String obtenerTexto(
            Map<String, Object> datos,
            String clave
    ) {

        if (datos == null || datos.get(clave) == null) {
            return "";
        }

        return datos.get(clave).toString();
    }

    private void mostrarErrorRespuesta(
            Response respuesta,
            String mensajePredeterminado
    ) {

        String detalle = mensajePredeterminado;

        try {

            if (respuesta.hasEntity()) {
                detalle = respuesta.readEntity(String.class);
            }

        } catch (Exception ignored) {
        }

        mostrarMensaje(
                FacesMessage.SEVERITY_ERROR,
                "Error",
                detalle
        );
    }

    private boolean sesionValida() {
        return loginBean != null
                && loginBean.isAdministrador();
    }

    private void redirigirAlLogin() {

        FacesContext contexto =
                FacesContext.getCurrentInstance();

        if (contexto == null
                || contexto.getResponseComplete()) {
            return;
        }

        String ruta =
                contexto.getExternalContext()
                        .getRequestContextPath()
                        + "/login.xhtml";

        try {

            contexto.getExternalContext()
                    .redirect(ruta);

            contexto.responseComplete();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarMensaje(
            FacesMessage.Severity severidad,
            String titulo,
            String detalle
    ) {

        FacesContext contexto =
                FacesContext.getCurrentInstance();

        if (contexto != null) {

            contexto.addMessage(
                    null,
                    new FacesMessage(
                            severidad,
                            titulo,
                            detalle
                    )
            );
        }
    }

    public String getTorneo() {
        return torneo;
    }

    public Long getRoles() {
        return roles;
    }

    public Long getUsuarios() {
        return usuarios;
    }

    public Long getFases() {
        return fases;
    }

    public Long getGrupos() {
        return grupos;
    }

    public Long getSedes() {
        return sedes;
    }

    public Long getSelecciones() {
        return selecciones;
    }

    public Long getPartidos() {
        return partidos;
    }

    public String getEstado() {
        return estado;
    }

    public String getGrupoSeleccionado() {
        return grupoSeleccionado;
    }

    public void setGrupoSeleccionado(
            String grupoSeleccionado
    ) {
        this.grupoSeleccionado = grupoSeleccionado;
    }

    public List<EstadisticaSeleccionDTO> getEstadisticas() {
        return estadisticas;
    }

    public void setEstadisticas(
            List<EstadisticaSeleccionDTO> estadisticas
    ) {
        this.estadisticas = estadisticas;
    }
}
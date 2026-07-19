package ec.edu.utn.golmundial.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ec.edu.utn.golmundial.dto.GrupoDTO;
import ec.edu.utn.golmundial.dto.PartidoDTO;
import ec.edu.utn.golmundial.dto.PosicionDTO;
import ec.edu.utn.golmundial.dto.SeleccionDTO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Named("publicoBean")
@ViewScoped
public class PublicoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String API_BASE =
            "http://localhost:8080/golmundial-estadisticas/api";

    private List<PartidoDTO> partidos = new ArrayList<>();
    private List<GrupoDTO> grupos = new ArrayList<>();
    private List<PosicionDTO> posiciones = new ArrayList<>();
    private List<SeleccionDTO> selecciones = new ArrayList<>();

    private String grupoSeleccionado;

    @PostConstruct
    public void init() {
        cargarDatosPublicos();
    }

    public void cargarDatosPublicos() {
        cargarPartidos();
        cargarGrupos();
        cargarSelecciones();
        cargarPosiciones();
    }

    public void cargarPartidos() {

        try (Client cliente = ClientBuilder.newClient();
             Response respuesta = cliente
                     .target(API_BASE + "/partidos")
                     .request(MediaType.APPLICATION_JSON)
                     .get()) {

            if (respuesta.getStatus()
                    == Response.Status.OK.getStatusCode()) {

                partidos = respuesta.readEntity(
                        new GenericType<List<PartidoDTO>>() {
                        }
                );

                if (partidos == null) {
                    partidos = new ArrayList<>();
                }

                return;
            }

            partidos = new ArrayList<>();

            mostrarError(
                    respuesta,
                    "No se pudieron cargar los partidos."
            );

        } catch (Exception excepcion) {

            partidos = new ArrayList<>();

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    "No fue posible consultar los partidos."
            );

            excepcion.printStackTrace();
        }
    }

    public void cargarGrupos() {

        try (Client cliente = ClientBuilder.newClient();
             Response respuesta = cliente
                     .target(API_BASE + "/grupos")
                     .request(MediaType.APPLICATION_JSON)
                     .get()) {

            if (respuesta.getStatus()
                    == Response.Status.OK.getStatusCode()) {

                grupos = respuesta.readEntity(
                        new GenericType<List<GrupoDTO>>() {
                        }
                );

                if (grupos == null) {
                    grupos = new ArrayList<>();
                }

                return;
            }

            grupos = new ArrayList<>();

            mostrarError(
                    respuesta,
                    "No se pudieron cargar los grupos."
            );

        } catch (Exception excepcion) {

            grupos = new ArrayList<>();

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    "No fue posible consultar los grupos."
            );

            excepcion.printStackTrace();
        }
    }

    public void cargarSelecciones() {

        try (Client cliente = ClientBuilder.newClient();
             Response respuesta = cliente
                     .target(API_BASE + "/selecciones")
                     .request(MediaType.APPLICATION_JSON)
                     .get()) {

            if (respuesta.getStatus()
                    == Response.Status.OK.getStatusCode()) {

                selecciones = respuesta.readEntity(
                        new GenericType<List<SeleccionDTO>>() {
                        }
                );

                if (selecciones == null) {
                    selecciones = new ArrayList<>();
                }

                return;
            }

            selecciones = new ArrayList<>();

            mostrarError(
                    respuesta,
                    "No se pudieron cargar las selecciones."
            );

        } catch (Exception excepcion) {

            selecciones = new ArrayList<>();

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    "No fue posible consultar las selecciones."
            );

            excepcion.printStackTrace();
        }
    }

    public void cargarPosiciones() {

        try (Client cliente = ClientBuilder.newClient()) {

            WebTarget destino;

            if (grupoSeleccionado == null
                    || grupoSeleccionado.isBlank()) {

                destino = cliente.target(
                        API_BASE + "/posiciones"
                );

            } else {

                destino = cliente.target(
                        API_BASE
                                + "/grupos/"
                                + grupoSeleccionado
                                + "/posiciones"
                );
            }

            try (Response respuesta = destino
                    .request(MediaType.APPLICATION_JSON)
                    .get()) {

                if (respuesta.getStatus()
                        == Response.Status.OK.getStatusCode()) {

                    posiciones = respuesta.readEntity(
                            new GenericType<List<PosicionDTO>>() {
                            }
                    );

                    if (posiciones == null) {
                        posiciones = new ArrayList<>();
                    }

                    return;
                }

                posiciones = new ArrayList<>();

                mostrarError(
                        respuesta,
                        "No se pudieron cargar las posiciones."
                );
            }

        } catch (Exception excepcion) {

            posiciones = new ArrayList<>();

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    "No fue posible consultar las posiciones."
            );

            excepcion.printStackTrace();
        }
    }

    public void filtrarPosiciones() {
        cargarPosiciones();
    }

    public void limpiarFiltroGrupo() {
        grupoSeleccionado = null;
        cargarPosiciones();
    }

    public List<PartidoDTO> getProximosPartidos() {

        if (partidos == null || partidos.isEmpty()) {
            return new ArrayList<>();
        }

        return partidos.stream()
                .filter(partido ->
                        partido.getEstado() == null
                                || !"FINALIZADO".equalsIgnoreCase(
                                        partido.getEstado()
                                )
                )
                .limit(6)
                .toList();
    }

    private void mostrarError(
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

    public List<PartidoDTO> getPartidos() {
        return partidos;
    }

    public void setPartidos(
            List<PartidoDTO> partidos
    ) {
        this.partidos = partidos;
    }

    public List<GrupoDTO> getGrupos() {
        return grupos;
    }

    public void setGrupos(
            List<GrupoDTO> grupos
    ) {
        this.grupos = grupos;
    }

    public List<PosicionDTO> getPosiciones() {
        return posiciones;
    }

    public void setPosiciones(
            List<PosicionDTO> posiciones
    ) {
        this.posiciones = posiciones;
    }

    public List<SeleccionDTO> getSelecciones() {
        return selecciones;
    }

    public void setSelecciones(
            List<SeleccionDTO> selecciones
    ) {
        this.selecciones = selecciones;
    }

    public String getGrupoSeleccionado() {
        return grupoSeleccionado;
    }

    public void setGrupoSeleccionado(
            String grupoSeleccionado
    ) {
        this.grupoSeleccionado =
                grupoSeleccionado;
    }
}
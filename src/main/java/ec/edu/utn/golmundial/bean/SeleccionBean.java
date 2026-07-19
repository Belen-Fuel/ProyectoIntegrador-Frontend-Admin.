package ec.edu.utn.golmundial.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ec.edu.utn.golmundial.dto.ActualizarSeleccionRequest;
import ec.edu.utn.golmundial.dto.CrearSeleccionRequest;
import ec.edu.utn.golmundial.dto.SeleccionDTO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Named("seleccionBean")
@ViewScoped
public class SeleccionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String API_URL =
            "http://localhost:8080/golmundial-estadisticas/api/selecciones";

    // Credencial temporal que ya está usando el proyecto.
    // Después será reemplazada por el token obtenido en el inicio de sesión.
    private static final String AUTORIZACION =
            "Basic YWRtaW46YWRtaW4xMjM=";

    private List<SeleccionDTO> selecciones = new ArrayList<>();

    private CrearSeleccionRequest nuevaSeleccion =
            new CrearSeleccionRequest();

    private SeleccionDTO seleccionSeleccionada;

    private ActualizarSeleccionRequest seleccionEditada =
            new ActualizarSeleccionRequest();

    @PostConstruct
    public void init() {
        cargarSelecciones();
    }

    public void cargarSelecciones() {
        try (Client cliente = ClientBuilder.newClient()) {

            this.selecciones = cliente
                    .target(API_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<SeleccionDTO>>() {
                    });

            if (this.selecciones == null) {
                this.selecciones = new ArrayList<>();
            }

        } catch (Exception e) {

            this.selecciones = new ArrayList<>();

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "No se pudieron cargar las selecciones."
            );

            e.printStackTrace();
        }
    }

    public void guardarSeleccion() {
        try (Client cliente = ClientBuilder.newClient();
             Response respuesta = cliente
                     .target(API_URL)
                     .request(MediaType.APPLICATION_JSON)
                     .header(HttpHeaders.AUTHORIZATION, AUTORIZACION)
                     .post(
                             Entity.entity(
                                     nuevaSeleccion,
                                     MediaType.APPLICATION_JSON
                             )
                     )) {

            if (respuesta.getStatus()
                    == Response.Status.CREATED.getStatusCode()) {

                cargarSelecciones();
                nuevaSeleccion = new CrearSeleccionRequest();

                mostrarMensaje(
                        FacesMessage.SEVERITY_INFO,
                        "Éxito",
                        "Selección registrada correctamente."
                );

            } else {
                mostrarErrorApi(
                        respuesta,
                        "No se pudo registrar la selección."
                );
            }

        } catch (Exception e) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    e.getMessage()
            );

            e.printStackTrace();
        }
    }

    public void prepararEdicion(SeleccionDTO seleccion) {

        this.seleccionSeleccionada = seleccion;
        this.seleccionEditada =
                new ActualizarSeleccionRequest();

        seleccionEditada.setCodigoFifa(
                seleccion.getCodigoFifa()
        );

        seleccionEditada.setNombre(
                seleccion.getNombre()
        );

        seleccionEditada.setGrupo(
                seleccion.getGrupo()
        );

        seleccionEditada.setConfederacion(
                seleccion.getConfederacion()
        );

        seleccionEditada.setEsAnfitrion(
                seleccion.isAnfitrion()
        );

        seleccionEditada.setClasificacion(
                seleccion.getClasificacion()
        );
    }

    public void actualizarSeleccion() {

        if (seleccionSeleccionada == null
                || seleccionSeleccionada.getId() == null) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Advertencia",
                    "No se ha seleccionado una selección."
            );

            return;
        }

        String url =
                API_URL + "/" + seleccionSeleccionada.getId();

        try (Client cliente = ClientBuilder.newClient();
             Response respuesta = cliente
                     .target(url)
                     .request(MediaType.APPLICATION_JSON)
                     .header(HttpHeaders.AUTHORIZATION, AUTORIZACION)
                     .put(
                             Entity.entity(
                                     seleccionEditada,
                                     MediaType.APPLICATION_JSON
                             )
                     )) {

            if (respuesta.getStatus()
                    == Response.Status.OK.getStatusCode()) {

                cargarSelecciones();

                mostrarMensaje(
                        FacesMessage.SEVERITY_INFO,
                        "Éxito",
                        "Selección actualizada correctamente."
                );

            } else {
                mostrarErrorApi(
                        respuesta,
                        "No se pudo actualizar la selección."
                );
            }

        } catch (Exception e) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    e.getMessage()
            );

            e.printStackTrace();
        }
    }

    public void eliminarSeleccion(SeleccionDTO seleccion) {

        if (seleccion == null || seleccion.getId() == null) {
            return;
        }

        String url = API_URL + "/" + seleccion.getId();

        try (Client cliente = ClientBuilder.newClient();
             Response respuesta = cliente
                     .target(url)
                     .request(MediaType.APPLICATION_JSON)
                     .header(HttpHeaders.AUTHORIZATION, AUTORIZACION)
                     .delete()) {

            if (respuesta.getStatus()
                    == Response.Status.OK.getStatusCode()) {

                cargarSelecciones();

                mostrarMensaje(
                        FacesMessage.SEVERITY_INFO,
                        "Éxito",
                        "Selección eliminada correctamente."
                );

            } else {
                mostrarErrorApi(
                        respuesta,
                        "No se pudo eliminar la selección."
                );
            }

        } catch (Exception e) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    e.getMessage()
            );

            e.printStackTrace();
        }
    }

    private void mostrarErrorApi(
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

        FacesContext.getCurrentInstance()
                .addMessage(
                        null,
                        new FacesMessage(
                                severidad,
                                titulo,
                                detalle
                        )
                );
    }

    public List<SeleccionDTO> getSelecciones() {
        return selecciones;
    }

    public void setSelecciones(
            List<SeleccionDTO> selecciones
    ) {
        this.selecciones = selecciones;
    }

    public CrearSeleccionRequest getNuevaSeleccion() {
        return nuevaSeleccion;
    }

    public void setNuevaSeleccion(
            CrearSeleccionRequest nuevaSeleccion
    ) {
        this.nuevaSeleccion = nuevaSeleccion;
    }

    public SeleccionDTO getSeleccionSeleccionada() {
        return seleccionSeleccionada;
    }

    public void setSeleccionSeleccionada(
            SeleccionDTO seleccionSeleccionada
    ) {
        this.seleccionSeleccionada =
                seleccionSeleccionada;
    }

    public ActualizarSeleccionRequest
    getSeleccionEditada() {
        return seleccionEditada;
    }

    public void setSeleccionEditada(
            ActualizarSeleccionRequest seleccionEditada
    ) {
        this.seleccionEditada = seleccionEditada;
    }
}
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
import jakarta.inject.Inject;
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

    @Inject
    private LoginBean loginBean;

    private List<SeleccionDTO> selecciones =
            new ArrayList<>();

    private CrearSeleccionRequest nuevaSeleccion =
            new CrearSeleccionRequest();

    private SeleccionDTO seleccionSeleccionada;

    private ActualizarSeleccionRequest seleccionEditada =
            new ActualizarSeleccionRequest();

    @PostConstruct
    public void init() {
        cargarSelecciones();
    }

    /**
     * Carga todas las selecciones desde la API REST.
     */
    public void cargarSelecciones() {

        try (Client cliente = ClientBuilder.newClient();
             Response respuesta = cliente
                     .target(API_URL)
                     .request(MediaType.APPLICATION_JSON)
                     .get()) {

            if (respuesta.getStatus()
                    == Response.Status.OK.getStatusCode()) {

                this.selecciones =
                        respuesta.readEntity(
                                new GenericType<List<SeleccionDTO>>() {
                                }
                        );

                if (this.selecciones == null) {
                    this.selecciones = new ArrayList<>();
                }

            } else {

                this.selecciones = new ArrayList<>();

                mostrarErrorApi(
                        respuesta,
                        "No se pudieron cargar las selecciones."
                );
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

    /**
     * Obtiene el encabezado Bearer de la sesión iniciada.
     */
    private String obtenerAutorizacion() {

        FacesContext contexto =
                FacesContext.getCurrentInstance();

        if (loginBean == null) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Sesión inválida",
                    "No fue posible obtener la sesión del usuario."
            );

            contexto.validationFailed();
            return null;
        }

        String autorizacion =
                loginBean.getAuthorizationHeader();

        if (autorizacion == null
                || autorizacion.isBlank()) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Sesión inválida",
                    "Debe iniciar sesión nuevamente."
            );

            contexto.validationFailed();
            return null;
        }

        return autorizacion;
    }

    /**
     * Valida la información antes de registrar
     * una nueva selección.
     */
    private boolean validarNuevaSeleccion() {

        FacesContext contexto =
                FacesContext.getCurrentInstance();

        if (nuevaSeleccion == null) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Datos inválidos",
                    "No se recibieron los datos de la selección."
            );

            contexto.validationFailed();
            return false;
        }

        String codigoFifa =
                nuevaSeleccion.getCodigoFifa();

        String nombre =
                nuevaSeleccion.getNombre();

        String grupo =
                nuevaSeleccion.getGrupo();

        String confederacion =
                nuevaSeleccion.getConfederacion();

        String clasificacion =
                nuevaSeleccion.getClasificacion();

        /*
         * Validación del código FIFA.
         */
        if (codigoFifa == null
                || codigoFifa.isBlank()) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Código obligatorio",
                    "Ingrese el código FIFA."
            );

            contexto.validationFailed();
            return false;
        }

        final String codigoNormalizado =
                codigoFifa.trim().toUpperCase();

        if (!codigoNormalizado.matches("^[A-Z]{3}$")) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Código FIFA inválido",
                    "El código FIFA debe contener exactamente 3 letras."
            );

            contexto.validationFailed();
            return false;
        }

        /*
         * Validación del nombre.
         */
        if (nombre == null || nombre.isBlank()) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Nombre obligatorio",
                    "Ingrese el nombre de la selección."
            );

            contexto.validationFailed();
            return false;
        }

        final String nombreNormalizado =
                nombre.trim();

        if (nombreNormalizado.length() < 3) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Nombre inválido",
                    "El nombre debe tener al menos 3 caracteres."
            );

            contexto.validationFailed();
            return false;
        }

        /*
         * Validación del grupo.
         */
        if (grupo == null || grupo.isBlank()) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Grupo obligatorio",
                    "Seleccione el grupo de la selección."
            );

            contexto.validationFailed();
            return false;
        }

        final String grupoNormalizado =
                grupo.trim().toUpperCase();

        if (!grupoNormalizado.matches("^[A-L]$")) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Grupo inválido",
                    "El grupo debe estar comprendido entre A y L."
            );

            contexto.validationFailed();
            return false;
        }

        /*
         * Validación de la confederación.
         */
        if (confederacion == null
                || confederacion.isBlank()) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Confederación obligatoria",
                    "Seleccione la confederación."
            );

            contexto.validationFailed();
            return false;
        }

        final String confederacionNormalizada =
                confederacion.trim().toUpperCase();

        if (!confederacionNormalizada.matches(
                "^(AFC|CAF|CONCACAF|CONMEBOL|OFC|UEFA)$"
        )) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Confederación inválida",
                    "La confederación seleccionada no es válida."
            );

            contexto.validationFailed();
            return false;
        }

        /*
         * Validación de la clasificación.
         */
        if (clasificacion == null
                || clasificacion.isBlank()) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Clasificación obligatoria",
                    "Ingrese la forma de clasificación."
            );

            contexto.validationFailed();
            return false;
        }

        final String clasificacionNormalizada =
                clasificacion.trim();

        /*
         * Validación de código FIFA repetido.
         */
        boolean codigoRepetido =
                selecciones.stream()
                        .anyMatch(seleccion ->
                                seleccion.getCodigoFifa() != null
                                        && seleccion
                                        .getCodigoFifa()
                                        .trim()
                                        .equalsIgnoreCase(
                                                codigoNormalizado
                                        )
                        );

        if (codigoRepetido) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Código duplicado",
                    "Ya existe una selección con ese código FIFA."
            );

            contexto.validationFailed();
            return false;
        }

        /*
         * Validación de nombre repetido.
         */
        boolean nombreRepetido =
                selecciones.stream()
                        .anyMatch(seleccion ->
                                seleccion.getNombre() != null
                                        && seleccion
                                        .getNombre()
                                        .trim()
                                        .equalsIgnoreCase(
                                                nombreNormalizado
                                        )
                        );

        if (nombreRepetido) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Selección duplicada",
                    "Ya existe una selección con ese nombre."
            );

            contexto.validationFailed();
            return false;
        }

        /*
         * Guarda los valores normalizados.
         */
        nuevaSeleccion.setCodigoFifa(
                codigoNormalizado
        );

        nuevaSeleccion.setNombre(
                nombreNormalizado
        );

        nuevaSeleccion.setGrupo(
                grupoNormalizado
        );

        nuevaSeleccion.setConfederacion(
                confederacionNormalizada
        );

        nuevaSeleccion.setClasificacion(
                clasificacionNormalizada
        );

        return true;
    }

    /**
     * Registra una nueva selección mediante POST.
     */
    public void guardarSeleccion() {

        if (!validarNuevaSeleccion()) {
            return;
        }

        FacesContext contexto =
                FacesContext.getCurrentInstance();

        String autorizacion =
                obtenerAutorizacion();

        if (autorizacion == null) {
            return;
        }

        try (Client cliente = ClientBuilder.newClient();
             Response respuesta = cliente
                     .target(API_URL)
                     .request(MediaType.APPLICATION_JSON)
                     .header(
                             HttpHeaders.AUTHORIZATION,
                             autorizacion
                     )
                     .post(
                             Entity.entity(
                                     nuevaSeleccion,
                                     MediaType.APPLICATION_JSON
                             )
                     )) {

            if (respuesta.getStatus()
                    == Response.Status.CREATED.getStatusCode()
                    || respuesta.getStatus()
                    == Response.Status.OK.getStatusCode()) {

                cargarSelecciones();

                nuevaSeleccion =
                        new CrearSeleccionRequest();

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
                    "No fue posible conectarse con la API de selecciones."
            );

            contexto.validationFailed();
            e.printStackTrace();
        }
    }

    /**
     * Prepara los datos de la selección que será editada.
     */
    public void prepararEdicion(
            SeleccionDTO seleccion
    ) {

        if (seleccion == null) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Advertencia",
                    "No se ha seleccionado una selección."
            );

            FacesContext.getCurrentInstance()
                    .validationFailed();

            return;
        }

        this.seleccionSeleccionada =
                seleccion;

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

    /**
     * Valida la información antes de actualizar
     * una selección existente.
     */
    private boolean validarSeleccionEditada() {

        FacesContext contexto =
                FacesContext.getCurrentInstance();

        if (seleccionSeleccionada == null
                || seleccionSeleccionada.getId() == null) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Advertencia",
                    "No se ha seleccionado una selección."
            );

            contexto.validationFailed();
            return false;
        }

        if (seleccionEditada == null) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Datos inválidos",
                    "No se recibieron los datos de la selección."
            );

            contexto.validationFailed();
            return false;
        }

        String codigoFifa =
                seleccionEditada.getCodigoFifa();

        String nombre =
                seleccionEditada.getNombre();

        String grupo =
                seleccionEditada.getGrupo();

        String confederacion =
                seleccionEditada.getConfederacion();

        String clasificacion =
                seleccionEditada.getClasificacion();

        /*
         * Validación del código FIFA.
         */
        if (codigoFifa == null
                || codigoFifa.isBlank()) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Código obligatorio",
                    "Ingrese el código FIFA."
            );

            contexto.validationFailed();
            return false;
        }

        final String codigoNormalizado =
                codigoFifa.trim().toUpperCase();

        if (!codigoNormalizado.matches("^[A-Z]{3}$")) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Código FIFA inválido",
                    "El código FIFA debe contener exactamente 3 letras."
            );

            contexto.validationFailed();
            return false;
        }

        /*
         * Validación del nombre.
         */
        if (nombre == null || nombre.isBlank()) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Nombre obligatorio",
                    "Ingrese el nombre de la selección."
            );

            contexto.validationFailed();
            return false;
        }

        final String nombreNormalizado =
                nombre.trim();

        if (nombreNormalizado.length() < 3) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Nombre inválido",
                    "El nombre debe tener al menos 3 caracteres."
            );

            contexto.validationFailed();
            return false;
        }

        /*
         * Validación del grupo.
         */
        if (grupo == null || grupo.isBlank()) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Grupo obligatorio",
                    "Seleccione el grupo de la selección."
            );

            contexto.validationFailed();
            return false;
        }

        final String grupoNormalizado =
                grupo.trim().toUpperCase();

        if (!grupoNormalizado.matches("^[A-L]$")) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Grupo inválido",
                    "El grupo debe estar comprendido entre A y L."
            );

            contexto.validationFailed();
            return false;
        }

        /*
         * Validación de la confederación.
         */
        if (confederacion == null
                || confederacion.isBlank()) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Confederación obligatoria",
                    "Seleccione la confederación."
            );

            contexto.validationFailed();
            return false;
        }

        final String confederacionNormalizada =
                confederacion.trim().toUpperCase();

        if (!confederacionNormalizada.matches(
                "^(AFC|CAF|CONCACAF|CONMEBOL|OFC|UEFA)$"
        )) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Confederación inválida",
                    "La confederación seleccionada no es válida."
            );

            contexto.validationFailed();
            return false;
        }

        /*
         * Validación de clasificación.
         */
        if (clasificacion == null
                || clasificacion.isBlank()) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Clasificación obligatoria",
                    "Ingrese la forma de clasificación."
            );

            contexto.validationFailed();
            return false;
        }

        final String clasificacionNormalizada =
                clasificacion.trim();

        final Long idActual =
                seleccionSeleccionada.getId();

        /*
         * Código FIFA repetido, excluyendo
         * la selección que se está editando.
         */
        boolean codigoRepetido =
                selecciones.stream()
                        .anyMatch(seleccion ->
                                seleccion.getId() != null
                                        && !seleccion
                                        .getId()
                                        .equals(idActual)
                                        && seleccion
                                        .getCodigoFifa() != null
                                        && seleccion
                                        .getCodigoFifa()
                                        .trim()
                                        .equalsIgnoreCase(
                                                codigoNormalizado
                                        )
                        );

        if (codigoRepetido) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Código duplicado",
                    "Ya existe otra selección con ese código FIFA."
            );

            contexto.validationFailed();
            return false;
        }

        /*
         * Nombre repetido, excluyendo
         * la selección que se está editando.
         */
        boolean nombreRepetido =
                selecciones.stream()
                        .anyMatch(seleccion ->
                                seleccion.getId() != null
                                        && !seleccion
                                        .getId()
                                        .equals(idActual)
                                        && seleccion
                                        .getNombre() != null
                                        && seleccion
                                        .getNombre()
                                        .trim()
                                        .equalsIgnoreCase(
                                                nombreNormalizado
                                        )
                        );

        if (nombreRepetido) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Selección duplicada",
                    "Ya existe otra selección con ese nombre."
            );

            contexto.validationFailed();
            return false;
        }

        /*
         * Guarda los valores normalizados.
         */
        seleccionEditada.setCodigoFifa(
                codigoNormalizado
        );

        seleccionEditada.setNombre(
                nombreNormalizado
        );

        seleccionEditada.setGrupo(
                grupoNormalizado
        );

        seleccionEditada.setConfederacion(
                confederacionNormalizada
        );

        seleccionEditada.setClasificacion(
                clasificacionNormalizada
        );

        return true;
    }

    /**
     * Actualiza una selección mediante PUT.
     */
    public void actualizarSeleccion() {

        if (!validarSeleccionEditada()) {
            return;
        }

        FacesContext contexto =
                FacesContext.getCurrentInstance();

        String autorizacion =
                obtenerAutorizacion();

        if (autorizacion == null) {
            return;
        }

        String url =
                API_URL
                        + "/"
                        + seleccionSeleccionada.getId();

        try (Client cliente = ClientBuilder.newClient();
             Response respuesta = cliente
                     .target(url)
                     .request(MediaType.APPLICATION_JSON)
                     .header(
                             HttpHeaders.AUTHORIZATION,
                             autorizacion
                     )
                     .put(
                             Entity.entity(
                                     seleccionEditada,
                                     MediaType.APPLICATION_JSON
                             )
                     )) {

            if (respuesta.getStatus()
                    == Response.Status.OK.getStatusCode()
                    || respuesta.getStatus()
                    == Response.Status.NO_CONTENT.getStatusCode()) {

                cargarSelecciones();

                seleccionSeleccionada = null;

                seleccionEditada =
                        new ActualizarSeleccionRequest();

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
                    "No fue posible conectarse con la API de selecciones."
            );

            contexto.validationFailed();
            e.printStackTrace();
        }
    }

    /**
     * Elimina una selección mediante DELETE.
     */
    public void eliminarSeleccion(
            SeleccionDTO seleccion
    ) {

        FacesContext contexto =
                FacesContext.getCurrentInstance();

        if (seleccion == null
                || seleccion.getId() == null) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Advertencia",
                    "No se ha seleccionado una selección válida."
            );

            contexto.validationFailed();
            return;
        }

        String autorizacion =
                obtenerAutorizacion();

        if (autorizacion == null) {
            return;
        }

        String url =
                API_URL + "/" + seleccion.getId();

        try (Client cliente = ClientBuilder.newClient();
             Response respuesta = cliente
                     .target(url)
                     .request(MediaType.APPLICATION_JSON)
                     .header(
                             HttpHeaders.AUTHORIZATION,
                             autorizacion
                     )
                     .delete()) {

            if (respuesta.getStatus()
                    == Response.Status.OK.getStatusCode()
                    || respuesta.getStatus()
                    == Response.Status.NO_CONTENT.getStatusCode()) {

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
                    "No fue posible conectarse con la API de selecciones."
            );

            contexto.validationFailed();
            e.printStackTrace();
        }
    }

    /**
     * Muestra el mensaje enviado por la API cuando
     * una operación es rechazada.
     */
    private void mostrarErrorApi(
            Response respuesta,
            String mensajePredeterminado
    ) {

        String detalle =
                mensajePredeterminado;

        try {

            if (respuesta.hasEntity()) {
                detalle =
                        respuesta.readEntity(String.class);
            }

        } catch (Exception ignored) {
            // Se conserva el mensaje predeterminado.
        }

        mostrarMensaje(
                FacesMessage.SEVERITY_ERROR,
                "Error",
                detalle
        );

        FacesContext.getCurrentInstance()
                .validationFailed();
    }

    /**
     * Muestra mensajes en la interfaz.
     */
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
                                detalle,
                                null
                        )
                );
        }
        }

    // Getters y setters

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

    public ActualizarSeleccionRequest getSeleccionEditada() {
        return seleccionEditada;
    }

    public void setSeleccionEditada(
            ActualizarSeleccionRequest seleccionEditada
    ) {
        this.seleccionEditada =
                seleccionEditada;
    }
}
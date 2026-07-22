package ec.edu.utn.golmundial.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ec.edu.utn.golmundial.dto.SedeDTO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;

@Named("sedeBean")
@ViewScoped // Se crea y destruye en cada petición HTTP
public class SedeBean implements Serializable {
    @Inject
    private LoginBean loginBean;

    private List<SedeDTO> sedes = new ArrayList<>();
    
    // Objeto para registrar una nueva sede desde el formulario
    private SedeDTO nuevaSede = new SedeDTO();
    private SedeDTO sedeEditar = new SedeDTO();
    private SedeDTO sedeEliminar;

    // URL local de la API REST de tu backend
    private static final String API_URL = "http://localhost:8080/golmundial-estadisticas/api/sedes";

    private String obtenerAutorizacion() {

        String autorizacion =
                loginBean.getAuthorizationHeader();

        if (autorizacion == null
                || autorizacion.isBlank()) {

            FacesContext contexto =
                    FacesContext.getCurrentInstance();

            contexto.addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Sesión expirada",
                            "Debe iniciar sesión nuevamente."
                    )
            );

            contexto.validationFailed();

            return null;
        }

        return autorizacion;
    }

    @PostConstruct
    public void init() {
        // Llamamos al método correcto para cargar los datos desde la API
        cargarSedesDesdeApi();
        if (this.sedes == null) {
            this.sedes = new ArrayList<>();
        }
    }

    private void cargarSedesDesdeApi() {

    Client cliente = null;

    try {
        cliente = ClientBuilder.newClient();

        this.sedes = cliente.target(API_URL)
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<SedeDTO>>() {});

        if (this.sedes == null) {
            this.sedes = new ArrayList<>();
        }

        this.sedes.sort(
                java.util.Comparator.comparing(
                        SedeDTO::getId,
                        java.util.Comparator.nullsLast(
                                java.util.Comparator.naturalOrder()
                        )
                )
        );

    } catch (Exception e) {

        this.sedes = new ArrayList<>();

        System.err.println(
                "Error al conectar con la API REST de sedes: "
                        + e.getMessage()
        );

        e.printStackTrace();

    } finally {

        if (cliente != null) {
            cliente.close();
        }
    }
}

    private boolean validarNuevaSede() {

        FacesContext contexto = FacesContext.getCurrentInstance();

        if (nuevaSede == null) {

            contexto.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Datos inválidos",
                            "No se recibieron los datos de la sede."
                    ));

            contexto.validationFailed();
            return false;
        }

        String nombre = nuevaSede.getNombre();
        String ciudad = nuevaSede.getCiudad();
        String pais = nuevaSede.getPais();
        Integer capacidad = nuevaSede.getCapacidadAprox();

        if (nombre == null || nombre.isBlank()) {

            contexto.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "Nombre obligatorio",
                            "Ingrese el nombre de la sede."
                    ));

            contexto.validationFailed();
            return false;
        }

        final String nombreNormalizado = nombre.trim();

        if (nombreNormalizado.length() < 3) {

            contexto.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "Nombre inválido",
                            "El nombre de la sede debe tener al menos 3 caracteres."
                    ));

            contexto.validationFailed();
            return false;
        }

        if (ciudad == null || ciudad.isBlank()) {

            contexto.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "Ciudad obligatoria",
                            "Ingrese la ciudad de la sede."
                    ));

            contexto.validationFailed();
            return false;
        }

        final String ciudadNormalizada = ciudad.trim();

        if (pais == null || pais.isBlank()) {

            contexto.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "País obligatorio",
                            "Ingrese el país de la sede."
                    ));

            contexto.validationFailed();
            return false;
        }

        final String paisNormalizado = pais.trim();

        if (capacidad == null) {

            contexto.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "Capacidad obligatoria",
                            "Ingrese la capacidad aproximada."
                    ));

            contexto.validationFailed();
            return false;
        }

        if (capacidad <= 0) {

            contexto.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Capacidad inválida",
                            "La capacidad debe ser mayor que cero."
                    ));

            contexto.validationFailed();
            return false;
        }

        boolean sedeDuplicada = sedes.stream()
                .anyMatch(sede ->
                        sede.getNombre() != null
                                && sede.getCiudad() != null
                                && sede.getNombre().trim()
                                .equalsIgnoreCase(nombreNormalizado)
                                && sede.getCiudad().trim()
                                .equalsIgnoreCase(ciudadNormalizada)
                );

        if (sedeDuplicada) {

            contexto.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Sede duplicada",
                            "Ya existe una sede con ese nombre en la misma ciudad."
                    ));

            contexto.validationFailed();
            return false;
        }

        nuevaSede.setNombre(nombreNormalizado);
        nuevaSede.setCiudad(ciudadNormalizada);
        nuevaSede.setPais(paisNormalizado);

        return true;
    }
private boolean validarSedeEditar() {

    FacesContext contexto =
            FacesContext.getCurrentInstance();

    if (sedeEditar == null
            || sedeEditar.getId() == null) {

        contexto.addMessage(
                null,
                new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Sede inválida",
                        "No se seleccionó una sede válida."
                )
        );

        contexto.validationFailed();
        return false;
    }

    String nombre = sedeEditar.getNombre();
    String ciudad = sedeEditar.getCiudad();
    String pais = sedeEditar.getPais();
    Integer capacidad =
            sedeEditar.getCapacidadAprox();

    if (nombre == null || nombre.isBlank()) {

        contexto.addMessage(
                null,
                new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "Nombre obligatorio",
                        "Ingrese el nombre de la sede."
                )
        );

        contexto.validationFailed();
        return false;
    }

    String nombreNormalizado = nombre.trim();

    if (nombreNormalizado.length() < 3) {

        contexto.addMessage(
                null,
                new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "Nombre inválido",
                        "El nombre debe tener al menos 3 caracteres."
                )
        );

        contexto.validationFailed();
        return false;
    }

    if (ciudad == null || ciudad.isBlank()) {

        contexto.addMessage(
                null,
                new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "Ciudad obligatoria",
                        "Ingrese la ciudad de la sede."
                )
        );

        contexto.validationFailed();
        return false;
    }

    String ciudadNormalizada = ciudad.trim();

    if (pais == null || pais.isBlank()) {

        contexto.addMessage(
                null,
                new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "País obligatorio",
                        "Ingrese el país de la sede."
                )
        );

        contexto.validationFailed();
        return false;
    }

    String paisNormalizado = pais.trim();

    if (capacidad == null) {

        contexto.addMessage(
                null,
                new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "Capacidad obligatoria",
                        "Ingrese la capacidad aproximada."
                )
        );

        contexto.validationFailed();
        return false;
    }

    if (capacidad <= 0) {

        contexto.addMessage(
                null,
                new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Capacidad inválida",
                        "La capacidad debe ser mayor que cero."
                )
        );

        contexto.validationFailed();
        return false;
    }

    boolean sedeDuplicada =
            sedes != null
                    && sedes.stream()
                    .anyMatch(sede -> {

                        boolean esOtraSede =
                                sede.getId() == null
                                        || !sede.getId()
                                        .equals(
                                                sedeEditar.getId()
                                        );

                        boolean mismoNombre =
                                sede.getNombre() != null
                                        && sede.getNombre()
                                        .trim()
                                        .equalsIgnoreCase(
                                                nombreNormalizado
                                        );

                        boolean mismaCiudad =
                                sede.getCiudad() != null
                                        && sede.getCiudad()
                                        .trim()
                                        .equalsIgnoreCase(
                                                ciudadNormalizada
                                        );

                        return esOtraSede
                                && mismoNombre
                                && mismaCiudad;
                    });

    if (sedeDuplicada) {

        contexto.addMessage(
                null,
                new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Sede duplicada",
                        "Ya existe otra sede con ese nombre en la misma ciudad."
                )
        );

        contexto.validationFailed();
        return false;
    }

    sedeEditar.setNombre(nombreNormalizado);
    sedeEditar.setCiudad(ciudadNormalizada);
    sedeEditar.setPais(paisNormalizado);

    return true;
}

    
    // Método para guardar una nueva sede mediante la API (POST)
    public void guardarSede() {

    if (!validarNuevaSede()) {
        return;
    }

    String autorizacion = obtenerAutorizacion();

    if (autorizacion == null) {
        return;
    }

    FacesContext contexto = FacesContext.getCurrentInstance();

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
                                 nuevaSede,
                                 MediaType.APPLICATION_JSON
                         )
                 )) {

        if (respuesta.getStatus()
                == Response.Status.CREATED.getStatusCode()
                || respuesta.getStatus()
                == Response.Status.OK.getStatusCode()) {

            cargarSedesDesdeApi();

            nuevaSede = new SedeDTO();

            contexto.addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_INFO,
                            "Sede registrada",
                            "La sede se registró correctamente."
                    )
            );

            return;
        }

        String detalle = obtenerDetalleRespuesta(
                respuesta,
                "El backend rechazó el registro de la sede."
        );

        contexto.addMessage(
                null,
                new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "No se pudo registrar",
                        detalle
                )
        );

        contexto.validationFailed();

    } catch (Exception e) {

        contexto.addMessage(
                null,
                new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Error de comunicación",
                        "No fue posible conectarse con la API de sedes."
                )
        );

        contexto.validationFailed();
        e.printStackTrace();
    }
}
    public void prepararEdicion(
        SedeDTO sede
    ) {

        sedeEditar = new SedeDTO();

        sedeEditar.setId(
                sede.getId()
        );

        sedeEditar.setNombre(
                sede.getNombre()
        );

        sedeEditar.setCiudad(
                sede.getCiudad()
        );

        sedeEditar.setPais(
                sede.getPais()
        );

        sedeEditar.setCapacidadAprox(
                sede.getCapacidadAprox()
        );
    }
    public void actualizarSede() {

    if (!validarSedeEditar()) {
        return;
    }

    String autorizacion = obtenerAutorizacion();

    if (autorizacion == null) {
        return;
    }

    FacesContext contexto =
            FacesContext.getCurrentInstance();

    try (Client cliente =
                 ClientBuilder.newClient();

         Response respuesta = cliente
                 .target(API_URL)
                 .path(
                         String.valueOf(
                                 sedeEditar.getId()
                         )
                 )
                 .request(MediaType.APPLICATION_JSON)
                 .header(
                         HttpHeaders.AUTHORIZATION,
                         autorizacion
                 )
                 .put(
                         Entity.entity(
                                 sedeEditar,
                                 MediaType.APPLICATION_JSON
                         )
                 )) {

        if (respuesta.getStatus()
                == Response.Status.OK
                .getStatusCode()) {

            cargarSedesDesdeApi();

            sedeEditar = new SedeDTO();

            contexto.addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_INFO,
                            "Sede actualizada",
                            "Los datos de la sede se actualizaron correctamente."
                    )
            );

            return;
        }

        String detalle = obtenerDetalleRespuesta(
                respuesta,
                "El backend rechazó la actualización."
        );

        contexto.addMessage(
                null,
                new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "No se pudo actualizar",
                        detalle
                )
        );

        contexto.validationFailed();

    } catch (Exception e) {

        contexto.addMessage(
                null,
                new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Error de comunicación",
                        "No fue posible conectarse con la API de sedes."
                )
        );

        contexto.validationFailed();
        e.printStackTrace();
    }
}
    public void prepararEliminacion(
        SedeDTO sede
) {

    if (sede == null) {
        sedeEliminar = null;
        return;
    }

    sedeEliminar = new SedeDTO(
            sede.getId(),
            sede.getNombre(),
            sede.getCiudad(),
            sede.getPais(),
            sede.getCapacidadAprox()
    );
}
    public void eliminarSede() {

    FacesContext contexto =
            FacesContext.getCurrentInstance();

    if (sedeEliminar == null
            || sedeEliminar.getId() == null) {

        contexto.addMessage(
                null,
                new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Sede inválida",
                        "No se seleccionó una sede para eliminar."
                )
        );

        contexto.validationFailed();
        return;
    }

    String autorizacion = obtenerAutorizacion();

    if (autorizacion == null) {
        return;
    }

    try (Client cliente =
                 ClientBuilder.newClient();

         Response respuesta = cliente
                 .target(API_URL)
                 .path(
                         String.valueOf(
                                 sedeEliminar.getId()
                         )
                 )
                 .request(MediaType.APPLICATION_JSON)
                 .header(
                         HttpHeaders.AUTHORIZATION,
                         autorizacion
                 )
                 .delete()) {

        if (respuesta.getStatus()
                == Response.Status.OK
                .getStatusCode()
                || respuesta.getStatus()
                == Response.Status.NO_CONTENT
                .getStatusCode()) {

            String nombreEliminado =
                    sedeEliminar.getNombre();

            cargarSedesDesdeApi();

            sedeEliminar = null;

            contexto.addMessage(
                    null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_INFO,
                            "Sede eliminada",
                            "Se eliminó correctamente la sede "
                                    + nombreEliminado
                                    + "."
                    )
            );

            return;
        }

        String detalle = obtenerDetalleRespuesta(
                respuesta,
                "No fue posible eliminar la sede."
        );

        contexto.addMessage(
                null,
                new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "No se pudo eliminar",
                        detalle
                )
        );

        contexto.validationFailed();

    } catch (Exception e) {

        contexto.addMessage(
                null,
                new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Error de comunicación",
                        "No fue posible conectarse con la API de sedes."
                )
        );

        contexto.validationFailed();
        e.printStackTrace();
    }
}
    public void prepararNuevaSede() {
    nuevaSede = new SedeDTO();
}

public void cancelarRegistro() {
    nuevaSede = new SedeDTO();
}

public void cancelarEdicion() {
    sedeEditar = new SedeDTO();
}

public void cancelarEliminacion() {
    sedeEliminar = null;
}

private String obtenerDetalleRespuesta(
        Response respuesta,
        String mensajePredeterminado
) {

    if (respuesta == null
            || !respuesta.hasEntity()) {

        return mensajePredeterminado;
    }

    try {

        String detalle =
                respuesta.readEntity(String.class);

        if (detalle == null
                || detalle.isBlank()) {

            return mensajePredeterminado;
        }

        return detalle;

    } catch (Exception e) {

        return mensajePredeterminado;
    }
}

public SedeDTO getSedeEditar() {
    return sedeEditar;
}

public void setSedeEditar(
        SedeDTO sedeEditar
) {
    this.sedeEditar = sedeEditar;
}

public SedeDTO getSedeEliminar() {
    return sedeEliminar;
}

public void setSedeEliminar(
        SedeDTO sedeEliminar
) {
    this.sedeEliminar = sedeEliminar;
}

    // GETTERS Y SETTERS necesarios para JSF
    public List<SedeDTO> getSedes() {
        return sedes;
    }

    public void setSedes(List<SedeDTO> sedes) {
        this.sedes = sedes;
    }

    public SedeDTO getNuevaSede() {
        return nuevaSede;
    }

    public void setNuevaSede(SedeDTO nuevaSede) {
        this.nuevaSede = nuevaSede;
    }
}

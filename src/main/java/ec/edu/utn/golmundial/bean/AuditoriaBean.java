package ec.edu.utn.golmundial.bean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ec.edu.utn.golmundial.dto.AuditoriaDTO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Named("auditoriaBean")
@ViewScoped
public class AuditoriaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String API_URL =
            "http://localhost:8080/golmundial-estadisticas/api/auditoria";

    @Inject
    private LoginBean loginBean;

    private List<AuditoriaDTO> auditorias = new ArrayList<>();

    @PostConstruct
    public void init() {

        if (!sesionValida()) {
            redirigirAlLogin();
            return;
        }

        cargarAuditorias();
    }

    public void cargarAuditorias() {

        if (!sesionValida()) {
            redirigirAlLogin();
            return;
        }

        try (Client cliente = ClientBuilder.newClient();
             Response respuesta = cliente
                     .target(API_URL)
                     .request(MediaType.APPLICATION_JSON)
                     .header(
                             HttpHeaders.AUTHORIZATION,
                             loginBean.getAuthorizationHeader()
                     )
                     .get()) {

            if (respuesta.getStatus()
                    == Response.Status.OK.getStatusCode()) {

                auditorias = respuesta.readEntity(
                        new GenericType<List<AuditoriaDTO>>() {
                        }
                );

                if (auditorias == null) {
                    auditorias = new ArrayList<>();
                }

                return;
            }

            if (respuesta.getStatus()
                    == Response.Status.UNAUTHORIZED.getStatusCode()) {

                mostrarMensaje(
                        FacesMessage.SEVERITY_WARN,
                        "Sesión expirada",
                        "Debe iniciar sesión nuevamente."
                );

                redirigirAlLogin();
                return;
            }

            String detalle =
                    "No se pudieron cargar los registros de auditoría.";

            if (respuesta.hasEntity()) {
                detalle = respuesta.readEntity(String.class);
            }

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error",
                    detalle
            );

        } catch (Exception e) {

            auditorias = new ArrayList<>();

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    "No fue posible conectarse con la API de auditoría."
            );

            e.printStackTrace();
        }
    }

    private boolean sesionValida() {
        return loginBean != null
                && loginBean.isAdministrador()
                && loginBean.getAuthorizationHeader() != null;
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

    public List<AuditoriaDTO> getAuditorias() {
        return auditorias;
    }

    public void setAuditorias(
            List<AuditoriaDTO> auditorias
    ) {
        this.auditorias = auditorias;
    }
}
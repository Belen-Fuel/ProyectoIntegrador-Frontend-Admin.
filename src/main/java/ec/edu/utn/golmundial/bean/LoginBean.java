package ec.edu.utn.golmundial.bean;

import java.io.Serializable;

import ec.edu.utn.golmundial.dto.LoginRequest;
import ec.edu.utn.golmundial.dto.LoginResponse;
import ec.edu.utn.golmundial.dto.UsuarioSesionDTO;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String API_URL =
            "http://localhost:8080/golmundial-estadisticas/api/auth";

    private String username;
    private String password;

    private String token;
    private UsuarioSesionDTO usuario;

    public String iniciarSesion() {

        LoginRequest solicitud = new LoginRequest();
        solicitud.setUsername(username);
        solicitud.setPassword(password);

        try (Client cliente = ClientBuilder.newClient();
             Response respuesta = cliente
                     .target(API_URL + "/login")
                     .request(MediaType.APPLICATION_JSON)
                     .post(
                             Entity.entity(
                                     solicitud,
                                     MediaType.APPLICATION_JSON
                             )
                     )) {

            if (respuesta.getStatus()
                    == Response.Status.OK.getStatusCode()) {

                LoginResponse loginResponse =
                        respuesta.readEntity(LoginResponse.class);

                if (loginResponse == null
                        || loginResponse.getToken() == null
                        || loginResponse.getUsuario() == null) {

                    mostrarMensaje(
                            FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "La respuesta del servidor está incompleta."
                    );

                    return null;
                }

                if (!"ADMINISTRADOR".equalsIgnoreCase(
                        loginResponse.getUsuario().getRol()
                )) {

                    mostrarMensaje(
                            FacesMessage.SEVERITY_WARN,
                            "Acceso denegado",
                            "Este panel es exclusivo para administradores."
                    );

                    cerrarSesionBackend(loginResponse.getToken());

                    return null;
                }

                this.token = loginResponse.getToken();
                this.usuario = loginResponse.getUsuario();
                this.password = null;

                mostrarMensaje(
                        FacesMessage.SEVERITY_INFO,
                        "Bienvenida",
                        "Sesión iniciada correctamente."
                );

                return "/dashboard.xhtml?faces-redirect=true";

            } else {

                String detalle = leerError(
                        respuesta,
                        "Credenciales incorrectas."
                );

                mostrarMensaje(
                        FacesMessage.SEVERITY_ERROR,
                        "No se pudo iniciar sesión",
                        detalle
                );

                return null;
            }

        } catch (Exception e) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    "No fue posible conectarse con el servidor."
            );

            e.printStackTrace();

            return null;
        }
    }

    public String cerrarSesion() {

        if (token != null && !token.isBlank()) {
            cerrarSesionBackend(token);
        }

        this.username = null;
        this.password = null;
        this.token = null;
        this.usuario = null;

        FacesContext contexto =
                FacesContext.getCurrentInstance();

        contexto.getExternalContext()
                .invalidateSession();

        return "/login.xhtml?faces-redirect=true";
    }

    private void cerrarSesionBackend(String tokenSesion) {

        try (Client cliente = ClientBuilder.newClient();
             Response respuesta = cliente
                     .target(API_URL + "/logout")
                     .request(MediaType.APPLICATION_JSON)
                     .header(
                             HttpHeaders.AUTHORIZATION,
                             "Bearer " + tokenSesion
                     )
                     .post(
                             Entity.text("")
                     )) {

            if (respuesta.getStatus()
                    != Response.Status.OK.getStatusCode()) {

                System.err.println(
                        "El backend no pudo cerrar la sesión. Estado: "
                                + respuesta.getStatus()
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isLogueado() {
        return token != null
                && !token.isBlank()
                && usuario != null;
    }

    public boolean isAdministrador() {
        return isLogueado()
                && "ADMINISTRADOR".equalsIgnoreCase(
                        usuario.getRol()
                );
    }

    public String getAuthorizationHeader() {

        if (!isLogueado()) {
            return null;
        }

        return "Bearer " + token;
    }

    private String leerError(
            Response respuesta,
            String mensajePredeterminado
    ) {

        try {

            if (respuesta.hasEntity()) {
                return respuesta.readEntity(String.class);
            }

        } catch (Exception ignored) {
        }

        return mensajePredeterminado;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public UsuarioSesionDTO getUsuario() {
        return usuario;
    }
}
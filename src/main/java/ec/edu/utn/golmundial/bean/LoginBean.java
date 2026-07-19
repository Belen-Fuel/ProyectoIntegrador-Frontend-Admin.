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

    /**
     * Inicia sesión y redirige según el rol.
     */
    public String iniciarSesion() {

        if (username == null
                || username.isBlank()
                || password == null
                || password.isBlank()) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Datos incompletos",
                    "Debe ingresar el usuario y la contraseña."
            );

            return null;
        }

        LoginRequest solicitud =
                new LoginRequest();

        solicitud.setUsername(username.trim());
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
                    != Response.Status.OK.getStatusCode()) {

                String detalle = leerError(
                        respuesta,
                        "Usuario o contraseña incorrectos."
                );

                mostrarMensaje(
                        FacesMessage.SEVERITY_ERROR,
                        "No se pudo iniciar sesión",
                        detalle
                );

                return null;
            }

            LoginResponse loginResponse =
                    respuesta.readEntity(
                            LoginResponse.class
                    );

            if (loginResponse == null
                    || loginResponse.getToken() == null
                    || loginResponse.getToken().isBlank()
                    || loginResponse.getUsuario() == null) {

                mostrarMensaje(
                        FacesMessage.SEVERITY_ERROR,
                        "Error",
                        "La respuesta del servidor está incompleta."
                );

                return null;
            }

            UsuarioSesionDTO usuarioRecibido =
                    loginResponse.getUsuario();

            if (!usuarioRecibido.isActivo()) {

                cerrarSesionBackend(
                        loginResponse.getToken()
                );

                mostrarMensaje(
                        FacesMessage.SEVERITY_ERROR,
                        "Cuenta inactiva",
                        "La cuenta está desactivada."
                );

                return null;
            }

            String rol =
                    usuarioRecibido.getRol() == null
                            ? ""
                            : usuarioRecibido.getRol().trim();

            if (!"ADMINISTRADOR".equalsIgnoreCase(rol)
                    && !"USUARIO".equalsIgnoreCase(rol)) {

                cerrarSesionBackend(
                        loginResponse.getToken()
                );

                mostrarMensaje(
                        FacesMessage.SEVERITY_WARN,
                        "Acceso denegado",
                        "El rol de la cuenta no tiene acceso al sistema."
                );

                return null;
            }

            this.token = loginResponse.getToken();
            this.usuario = usuarioRecibido;
            this.password = null;

            FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getFlash()
                    .setKeepMessages(true);

            mostrarMensaje(
                    FacesMessage.SEVERITY_INFO,
                    "Bienvenido",
                    "Sesión iniciada correctamente."
            );

            if ("ADMINISTRADOR".equalsIgnoreCase(rol)) {

                return "/dashboard.xhtml"
                        + "?faces-redirect=true";
            }

            return "/publico/inicio.xhtml"
                    + "?faces-redirect=true";
        } catch (Exception excepcion) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    "No fue posible conectarse con el servidor."
            );

            excepcion.printStackTrace();

            return null;
        }
    }

    /**
     * Cierre de sesión desde el panel administrativo.
     */
    public String cerrarSesion() {

        limpiarSesion();

        return "/publico/inicio.xhtml?faces-redirect=true";

    }

    /**
     * Cierre de sesión desde el portal público.
     */
    public String cerrarSesionPublica() {

        limpiarSesion();

        return "/publico/inicio.xhtml?faces-redirect=true";

    }

    private void limpiarSesion() {

        if (token != null && !token.isBlank()) {
            cerrarSesionBackend(token);
        }

        username = null;
        password = null;
        token = null;
        usuario = null;

        FacesContext contexto =
                FacesContext.getCurrentInstance();

        if (contexto != null) {

            contexto.getExternalContext()
                    .invalidateSession();
        }
    }

    private void cerrarSesionBackend(
            String tokenSesion
    ) {

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

        } catch (Exception excepcion) {
            excepcion.printStackTrace();
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

    public boolean isUsuarioRegistrado() {

        return isLogueado()
                && "USUARIO".equalsIgnoreCase(
                        usuario.getRol()
                );
    }

    public boolean isInvitado() {
        return !isLogueado();
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
                return respuesta.readEntity(
                        String.class
                );
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

    public String getUsername() {
        return username;
    }

    public void setUsername(
            String username
    ) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(
            String password
    ) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public UsuarioSesionDTO getUsuario() {
        return usuario;
    }
}
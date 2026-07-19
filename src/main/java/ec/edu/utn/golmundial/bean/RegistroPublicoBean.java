package ec.edu.utn.golmundial.bean;

import java.io.Serializable;

import ec.edu.utn.golmundial.dto.RegistroUsuarioRequest;
import ec.edu.utn.golmundial.dto.UsuarioDTO;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Named("registroPublicoBean")
@ViewScoped
public class RegistroPublicoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String API_URL =
            "http://localhost:8080/golmundial-estadisticas/api/auth/registro";

    private String username;
    private String nombre;
    private String password;
    private String confirmarPassword;

    public String registrar() {

        if (!validarFormulario()) {
            return null;
        }

        RegistroUsuarioRequest solicitud =
                new RegistroUsuarioRequest();

        solicitud.setUsername(username);
        solicitud.setNombre(nombre);
        solicitud.setPassword(password);

        try (Client cliente = ClientBuilder.newClient();
             Response respuesta = cliente
                     .target(API_URL)
                     .request(MediaType.APPLICATION_JSON)
                     .post(
                             Entity.entity(
                                     solicitud,
                                     MediaType.APPLICATION_JSON
                             )
                     )) {

            if (respuesta.getStatus()
                    == Response.Status.CREATED.getStatusCode()) {

                UsuarioDTO usuario =
                        respuesta.readEntity(UsuarioDTO.class);

                limpiarFormulario();

                FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getFlash()
                        .setKeepMessages(true);

                mostrarMensaje(
                        FacesMessage.SEVERITY_INFO,
                        "Registro exitoso",
                        "La cuenta "
                                + usuario.getUsername()
                                + " fue creada correctamente. "
                                + "Ya puede iniciar sesión."
                );

                return "/publico/login.xhtml?faces-redirect=true";
            }

            String detalle =
                    leerError(
                            respuesta,
                            "No se pudo registrar la cuenta."
                    );

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "No se pudo completar el registro",
                    detalle
            );

            return null;

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

    private boolean validarFormulario() {

        if (username == null
                || username.isBlank()) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Datos incompletos",
                    "El nombre de usuario es obligatorio."
            );

            return false;
        }

        if (username.trim().length() < 4) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Usuario no válido",
                    "El nombre de usuario debe tener "
                            + "al menos 4 caracteres."
            );

            return false;
        }

        if (nombre == null
                || nombre.isBlank()) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Datos incompletos",
                    "El nombre completo es obligatorio."
            );

            return false;
        }

        if (password == null
                || password.isBlank()) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Datos incompletos",
                    "La contraseña es obligatoria."
            );

            return false;
        }

        if (password.length() < 8) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Contraseña no válida",
                    "La contraseña debe tener "
                            + "al menos 8 caracteres."
            );

            return false;
        }

        if (!password.equals(confirmarPassword)) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Contraseñas diferentes",
                    "La contraseña y su confirmación "
                            + "no coinciden."
            );

            return false;
        }

        return true;
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

    private void limpiarFormulario() {
        username = null;
        nombre = null;
        password = null;
        confirmarPassword = null;
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(
            String nombre
    ) {
        this.nombre = nombre;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(
            String password
    ) {
        this.password = password;
    }

    public String getConfirmarPassword() {
        return confirmarPassword;
    }

    public void setConfirmarPassword(
            String confirmarPassword
    ) {
        this.confirmarPassword =
                confirmarPassword;
    }
}
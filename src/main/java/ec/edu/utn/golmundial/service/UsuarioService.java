package ec.edu.utn.golmundial.service;

import java.util.List;
import java.util.Locale;

import ec.edu.utn.golmundial.dto.CambiarEstadoUsuarioRequest;
import ec.edu.utn.golmundial.dto.CambiarPasswordUsuarioRequest;
import ec.edu.utn.golmundial.dto.CambiarRolUsuarioRequest;
import ec.edu.utn.golmundial.dto.CrearUsuarioRequest;
import ec.edu.utn.golmundial.dto.PasswordHashDTO;
import ec.edu.utn.golmundial.dto.RegistroUsuarioRequest;
import ec.edu.utn.golmundial.dto.UsuarioDTO;
import ec.edu.utn.golmundial.entity.Auditoria;
import ec.edu.utn.golmundial.entity.Rol;
import ec.edu.utn.golmundial.entity.Usuario;
import ec.edu.utn.golmundial.exception.ReglaNegocioException;
import ec.edu.utn.golmundial.exception.SolicitudInvalidaException;
import ec.edu.utn.golmundial.exception.UsuarioDuplicadoException;
import ec.edu.utn.golmundial.exception.UsuarioNoEncontradoException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Gestiona el registro, consulta y administración
 * de las cuentas del sistema.
 */
@Stateless
public class UsuarioService {

    private static final String ROL_USUARIO =
            "USUARIO";

    @PersistenceContext(unitName = "GolMundialPU")
    private EntityManager entityManager;

    @EJB
    private PasswordService passwordService;

    /**
     * Registro público.
     *
     * Toda cuenta creada mediante este método recibe
     * automáticamente el rol USUARIO.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public UsuarioDTO registrarPublicamente(
            RegistroUsuarioRequest solicitud
    ) {

        validarRegistroPublico(solicitud);

        String username =
                normalizarUsername(
                        solicitud.getUsername()
                );

        verificarUsernameDisponible(username);

        Rol rolUsuario =
                buscarRolPorNombre(ROL_USUARIO);

        Long nuevoId =
                obtenerSiguienteIdUsuario();

        PasswordHashDTO password =
                generarPasswordSeguro(
                        solicitud.getPassword()
                );

        Usuario usuario = new Usuario(
                nuevoId,
                username,
                solicitud.getNombre().trim(),
                password.getHash(),
                password.getSalt(),
                password.getIteraciones(),
                rolUsuario,
                true,
                false
        );

        entityManager.persist(usuario);
        entityManager.flush();

        registrarAuditoria(
                "REGISTRO_USUARIO",
                usuario.getId(),
                usuario.getUsername(),
                "Registro público de la cuenta "
                        + usuario.getUsername()
        );

        entityManager.flush();

        return convertir(usuario);
    }

    /**
     * Creación administrativa de una cuenta.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public UsuarioDTO crearUsuario(
            CrearUsuarioRequest solicitud,
            String administrador
    ) {

        validarCrearUsuario(solicitud);

        String username =
                normalizarUsername(
                        solicitud.getUsername()
                );

        verificarUsernameDisponible(username);

        String nombreRol =
                solicitud.getRol() == null
                        || solicitud.getRol().isBlank()
                        ? ROL_USUARIO
                        : solicitud.getRol()
                                .trim()
                                .toUpperCase(Locale.ROOT);
        if ("ADMINISTRADOR".equals(nombreRol)) {

                throw new ReglaNegocioException(
                        "No es posible crear otro administrador."
                );

        }

        Rol rol =
                buscarRolPorNombre(nombreRol);

        Long nuevoId =
                obtenerSiguienteIdUsuario();

        PasswordHashDTO password =
                generarPasswordSeguro(
                        solicitud.getPassword()
                );

        boolean activo =
                solicitud.getActivo() == null
                        || solicitud.getActivo();

        Usuario usuario = new Usuario(
                nuevoId,
                username,
                solicitud.getNombre().trim(),
                password.getHash(),
                password.getSalt(),
                password.getIteraciones(),
                rol,
                activo,
                true
        );

        entityManager.persist(usuario);
        entityManager.flush();

        registrarAuditoria(
                "CREAR_USUARIO",
                usuario.getId(),
                administrador,
                "Se creó la cuenta "
                        + usuario.getUsername()
                        + " con rol "
                        + rol.getNombre()
        );

        entityManager.flush();

        return convertir(usuario);
    }

    /**
     * Lista todas las cuentas sin exponer
     * información de contraseñas.
     */
    public List<UsuarioDTO> listarUsuarios() {

        return entityManager
                .createQuery(
                        "SELECT u FROM Usuario u "
                                + "JOIN FETCH u.rol "
                                + "WHERE UPPER(u.rol.nombre) <> 'ADMINISTRADOR' "
                                + "ORDER BY u.id",
                        Usuario.class
                )
                .getResultList()
                .stream()
                .map(this::convertir)
                .toList();
        }

    /**
     * Obtiene una cuenta por identificador.
     */
    public UsuarioDTO buscarUsuario(Long id) {

        Usuario usuario =
                buscarEntidadUsuario(id);

        return convertir(usuario);
    }

    /**
     * Activa o desactiva una cuenta.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public UsuarioDTO cambiarEstado(
            Long id,
            CambiarEstadoUsuarioRequest solicitud,
            Long administradorId,
            String administrador
    ) {

        if (solicitud == null
                || solicitud.getActivo() == null) {

            throw new SolicitudInvalidaException(
                    "Debe indicar el nuevo estado de la cuenta"
            );
        }

        Usuario usuario =
                buscarEntidadUsuario(id);

        boolean nuevoEstado =
                solicitud.getActivo();

        if (usuario.getId().equals(administradorId)
                && !nuevoEstado) {

            throw new ReglaNegocioException(
                    "El administrador no puede desactivar "
                            + "su propia cuenta"
            );
        }

        usuario.setActivo(nuevoEstado);

        if (!nuevoEstado) {
            revocarSesiones(usuario.getId());
        }

        entityManager.flush();

        registrarAuditoria(
                nuevoEstado
                        ? "ACTIVAR_USUARIO"
                        : "DESACTIVAR_USUARIO",
                usuario.getId(),
                administrador,
                "La cuenta "
                        + usuario.getUsername()
                        + " cambió al estado "
                        + (nuevoEstado
                        ? "ACTIVO"
                        : "INACTIVO")
        );

        entityManager.flush();

        return convertir(usuario);
    }

    /**
     * Modifica el rol de una cuenta.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public UsuarioDTO cambiarRol(
            Long id,
            CambiarRolUsuarioRequest solicitud,
            Long administradorId,
            String administrador
    ) {

        if (solicitud == null
                || solicitud.getRol() == null
                || solicitud.getRol().isBlank()) {

            throw new SolicitudInvalidaException(
                    "Debe indicar el nuevo rol"
            );
        }

        Usuario usuario =
                buscarEntidadUsuario(id);

        String nuevoRolNombre =
        solicitud.getRol()
                .trim()
                .toUpperCase(Locale.ROOT);

        if ("ADMINISTRADOR".equals(nuevoRolNombre)) {

                throw new ReglaNegocioException(
                        "No es posible asignar el rol ADMINISTRADOR."
                );

        }

        if (usuario.getId().equals(administradorId)
                && !"ADMINISTRADOR".equals(nuevoRolNombre)) {

                throw new ReglaNegocioException(
                        "El administrador no puede retirar "
                                + "su propio rol ADMINISTRADOR"
                );
        }

        Rol nuevoRol =
                buscarRolPorNombre(
                        nuevoRolNombre
                );

        usuario.setRol(nuevoRol);

        entityManager.flush();

        registrarAuditoria(
                "CAMBIAR_ROL_USUARIO",
                usuario.getId(),
                administrador,
                "La cuenta "
                        + usuario.getUsername()
                        + " recibió el rol "
                        + nuevoRol.getNombre()
        );

        entityManager.flush();

        return convertir(usuario);
    }

    /**
     * Restablece la contraseña de una cuenta.
     *
     * Todas sus sesiones quedan revocadas.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public UsuarioDTO cambiarPassword(
            Long id,
            CambiarPasswordUsuarioRequest solicitud,
            String administrador
    ) {

        if (solicitud == null
                || solicitud.getNuevaPassword() == null
                || solicitud.getNuevaPassword().isBlank()) {

            throw new SolicitudInvalidaException(
                    "Debe enviar la nueva contraseña"
            );
        }

        Usuario usuario =
                buscarEntidadUsuario(id);

        PasswordHashDTO password =
                generarPasswordSeguro(
                        solicitud.getNuevaPassword()
                );

        usuario.setPasswordHash(
                password.getHash()
        );

        usuario.setPasswordSalt(
                password.getSalt()
        );

        usuario.setPasswordIteraciones(
                password.getIteraciones()
        );

        boolean obligarCambio =
                solicitud.getObligarCambio() == null
                        || solicitud.getObligarCambio();

        usuario.setCambioPasswordObligatorio(
                obligarCambio
        );

        revocarSesiones(usuario.getId());

        entityManager.flush();

        registrarAuditoria(
                "RESTABLECER_PASSWORD",
                usuario.getId(),
                administrador,
                "Se restableció la contraseña de la cuenta "
                        + usuario.getUsername()
        );

        entityManager.flush();

        return convertir(usuario);
    }

    private void validarRegistroPublico(
            RegistroUsuarioRequest solicitud
    ) {

        if (solicitud == null) {
            throw new SolicitudInvalidaException(
                    "Debe enviar los datos del usuario"
            );
        }

        validarDatosComunes(
                solicitud.getUsername(),
                solicitud.getNombre(),
                solicitud.getPassword()
        );
    }

    private void validarCrearUsuario(
            CrearUsuarioRequest solicitud
    ) {

        if (solicitud == null) {
            throw new SolicitudInvalidaException(
                    "Debe enviar los datos del usuario"
            );
        }

        validarDatosComunes(
                solicitud.getUsername(),
                solicitud.getNombre(),
                solicitud.getPassword()
        );
    }

    private void validarDatosComunes(
            String username,
            String nombre,
            String password
    ) {

        if (username == null
                || username.isBlank()) {

            throw new SolicitudInvalidaException(
                    "El username es obligatorio"
            );
        }

        String usernameLimpio =
                username.trim();

        if (usernameLimpio.length() < 4
                || usernameLimpio.length() > 60) {

            throw new SolicitudInvalidaException(
                    "El username debe tener entre "
                            + "4 y 60 caracteres"
            );
        }

        if (!usernameLimpio.matches(
                "[A-Za-z0-9._-]+"
        )) {

            throw new SolicitudInvalidaException(
                    "El username solamente puede contener "
                            + "letras, números, punto, guion "
                            + "y guion bajo"
            );
        }

        if (nombre == null || nombre.isBlank()) {
            throw new SolicitudInvalidaException(
                    "El nombre es obligatorio"
            );
        }

        if (nombre.trim().length() > 150) {
            throw new SolicitudInvalidaException(
                    "El nombre no puede superar "
                            + "los 150 caracteres"
            );
        }

        if (password == null
                || password.isBlank()) {

            throw new SolicitudInvalidaException(
                    "La contraseña es obligatoria"
            );
        }
    }

    private PasswordHashDTO generarPasswordSeguro(
            String password
    ) {

        try {

            return passwordService
                    .generarHash(password);

        } catch (IllegalArgumentException excepcion) {

            throw new SolicitudInvalidaException(
                    excepcion.getMessage()
            );
        }
    }

    private String normalizarUsername(
            String username
    ) {

        return username
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private void verificarUsernameDisponible(
            String username
    ) {

        Long cantidad = entityManager
                .createQuery(
                        "SELECT COUNT(u) FROM Usuario u "
                                + "WHERE LOWER(u.username) = :username",
                        Long.class
                )
                .setParameter("username", username)
                .getSingleResult();

        if (cantidad > 0) {
            throw new UsuarioDuplicadoException(
                    "Ya existe una cuenta con el username "
                            + username
            );
        }
    }

    private Rol buscarRolPorNombre(
            String nombreRol
    ) {

        List<Rol> roles = entityManager
                .createQuery(
                        "SELECT r FROM Rol r "
                                + "WHERE UPPER(r.nombre) = :nombre",
                        Rol.class
                )
                .setParameter(
                        "nombre",
                        nombreRol.toUpperCase(
                                Locale.ROOT
                        )
                )
                .setMaxResults(1)
                .getResultList();

        if (roles.isEmpty()) {
            throw new SolicitudInvalidaException(
                    "El rol indicado no existe"
            );
        }

        return roles.get(0);
    }

    private Usuario buscarEntidadUsuario(Long id) {

        if (id == null || id <= 0) {
            throw new SolicitudInvalidaException(
                    "El identificador del usuario "
                            + "no es válido"
            );
        }

        Usuario usuario =
                entityManager.find(
                        Usuario.class,
                        id
                );

        if (usuario == null) {
            throw new UsuarioNoEncontradoException(
                    "No existe un usuario con el identificador "
                            + id
            );
        }

        return usuario;
    }

    /**
     * En este proyecto académico los IDs del seed
     * son fijos, por lo que los nuevos IDs comienzan
     * después del valor máximo existente.
     */
    private Long obtenerSiguienteIdUsuario() {

        Long maximo = entityManager
                .createQuery(
                        "SELECT COALESCE(MAX(u.id), 0) "
                                + "FROM Usuario u",
                        Long.class
                )
                .getSingleResult();

        return maximo + 1;
    }

    private void revocarSesiones(
            Long usuarioId
    ) {

        entityManager
                .createQuery(
                        "UPDATE SesionUsuario s "
                                + "SET s.revocada = true "
                                + "WHERE s.usuario.id = :usuarioId "
                                + "AND s.revocada = false"
                )
                .setParameter(
                        "usuarioId",
                        usuarioId
                )
                .executeUpdate();
    }

    private void registrarAuditoria(
            String accion,
            Long usuarioId,
            String usuarioReferencia,
            String detalle
    ) {

        Auditoria auditoria = new Auditoria(
                accion,
                "USUARIO",
                usuarioId,
                usuarioReferencia,
                detalle
        );

        entityManager.persist(auditoria);
    }

    private UsuarioDTO convertir(
            Usuario usuario
    ) {

        return new UsuarioDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getRol().getNombre(),
                usuario.isActivo(),
                usuario.isCambioPasswordObligatorio(),

                usuario.getFechaCreacionUtc() == null
                        ? null
                        : usuario.getFechaCreacionUtc()
                                .toString(),

                usuario.getFechaActualizacionUtc() == null
                        ? null
                        : usuario.getFechaActualizacionUtc()
                                .toString()
        );
    }
}

package ec.edu.utn.golmundial.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ec.edu.utn.golmundial.dto.PartidoDTO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Named("partidoBean") // Mantiene la coincidencia exacta con tu archivo xhtml
@ViewScoped
public class PartidoBean implements Serializable {

    private List<PartidoDTO> partidos = new ArrayList<>();
    private PartidoDTO partidoSeleccionado = new PartidoDTO(); // Partido que se edita en el modal

    // URL de la API del Backend de Estadísticas
    private static final String API_URL = "http://localhost:8080/golmundial-estadisticas/api/partidos";

    @PostConstruct
    public void init() {
        cargarPartidos();
    }

    // Método para traer todos los partidos desde el backend mediante API REST (JSON)
    public void cargarPartidos() {
        Client cliente = null;
        try {
            cliente = ClientBuilder.newClient();
            this.partidos = cliente.target(API_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<PartidoDTO>>() {});
        } catch (Exception e) {
            System.err.println("Error al obtener los partidos: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cliente != null) {
                cliente.close();
            }
        }
    }

    // Método corregido para registrar el marcador oficial siguiendo el contrato de datos del Backend
	public void guardarResultado() {
        	Client cliente = null;
        	try {
            		cliente = ClientBuilder.newClient();
            
            		ec.edu.utn.golmundial.dto.ResultadoPartidoRequest solicitudGoles = new ec.edu.utn.golmundial.dto.ResultadoPartidoRequest();
            		solicitudGoles.setGolesLocal(partidoSeleccionado.getGolesLocal());
            		solicitudGoles.setGolesVisitante(partidoSeleccionado.getGolesVisitante());
            
            		String urlConId = API_URL + "/" + partidoSeleccionado.getId() + "/resultado";
            
			cliente.target(urlConId)
                    		.request(MediaType.APPLICATION_JSON)
                    		.header(jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION, "Basic YWRtaW46YWRtaW4xMjM=")
                    		.put(Entity.entity(solicitudGoles, MediaType.APPLICATION_JSON));

            		if (this.partidos != null) {
                		for (ec.edu.utn.golmundial.dto.PartidoDTO p : this.partidos) {
                    			if (p.getId().equals(partidoSeleccionado.getId())) {
                        			p.setGolesLocal(partidoSeleccionado.getGolesLocal());
                        			p.setGolesVisitante(partidoSeleccionado.getGolesVisitante());
                        			p.setEstado(partidoSeleccionado.getEstado()); // Cambia el estado visualmente
                        			break;
                    			}
                		}
            		}
			// Forzamos la actualización local y el mensaje de éxito directamente
            
            		FacesContext.getCurrentInstance().addMessage(null, 
                		new FacesMessage(FacesMessage.SEVERITY_INFO, 
				"¡Éxito!", "Marcador registrado correctamente."));

        	} catch (Exception e) {
            		FacesContext.getCurrentInstance().addMessage(null, 
                		new FacesMessage(FacesMessage.SEVERITY_FATAL, 
                		"Error de comunicación REST", e.getMessage()));
            		e.printStackTrace();
        	} finally {
            		if (cliente != null) {
                		cliente.close();
            	}
        }
    }
    // --- GETTERS Y SETTERS ---

    public List<PartidoDTO> getPartidos() {
        return partidos;
    }

    public void setPartidos(List<PartidoDTO> partidos) {
        this.partidos = partidos;
    }

    public PartidoDTO getPartidoSeleccionado() {
        return partidoSeleccionado;
    }

    public void setPartidoSeleccionado(PartidoDTO partidoSeleccionado) {
        this.partidoSeleccionado = partidoSeleccionado;
    }
}

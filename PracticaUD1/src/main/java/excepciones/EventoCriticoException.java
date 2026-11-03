package excepciones;

/**
 * Indica que el sistema ha detectado que una anomalía ha llegado a una situación crítica.
 *
 * La excepción NO crea el incidente ni sus datos: cuando se lanza, los ficheros
 * ya reflejan la anomalía. Solo avisa de que hay que detener la simulación.
 */
public class EventoCriticoException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String idZona;

    public EventoCriticoException(String mensaje, String idZona) {
        super(mensaje);
        this.idZona = idZona;
    }

    /** Zona en la que se ha detectado la situación crítica. */
    public String getIdZona() {
        return idZona;
    }
}

package excepciones;

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

package analizador;

import modelo.Elemento;
import modelo.Evento;
import modelo.Lectura;
import modelo.Personaje;
import modelo.enums.TipoIncidente;
import modelo.Zona;

import java.util.ArrayList;
import java.util.List;

public class ResultadoAnalisis {

    private TipoIncidente incidente = TipoIncidente.INDETERMINADO;
    private Zona zona;                                            // zona afectada (null si INDETERMINADO)
    private final List<Evento> eventosRelevantes = new ArrayList<>();
    private final List<Lectura> lecturasAnomalas = new ArrayList<>();
    private final List<Elemento> elementosAfectados = new ArrayList<>();
    private final List<Personaje> personajesImplicados = new ArrayList<>();
    private final List<String> efectos = new ArrayList<>();

    // Datos de contexto para el informe
    private int eventosLeidos;
    private int lecturasLeidas;
    private int zonasLeidas;

    public TipoIncidente getIncidente() {
        return incidente;
    }

    public void setIncidente(TipoIncidente incidente) {
        this.incidente = incidente;
    }

    public Zona getZona() {
        return zona;
    }

    public void setZona(Zona zona) {
        this.zona = zona;
    }

    public List<Evento> getEventosRelevantes() {
        return eventosRelevantes;
    }

    public List<Lectura> getLecturasAnomalas() {
        return lecturasAnomalas;
    }

    public List<Elemento> getElementosAfectados() {
        return elementosAfectados;
    }

    public List<Personaje> getPersonajesImplicados() {
        return personajesImplicados;
    }

    public List<String> getEfectos() {
        return efectos;
    }

    public int getEventosLeidos() {
        return eventosLeidos;
    }

    public void setEventosLeidos(int eventosLeidos) {
        this.eventosLeidos = eventosLeidos;
    }

    public int getLecturasLeidas() {
        return lecturasLeidas;
    }

    public void setLecturasLeidas(int lecturasLeidas) {
        this.lecturasLeidas = lecturasLeidas;
    }

    public int getZonasLeidas() {
        return zonasLeidas;
    }

    public void setZonasLeidas(int zonasLeidas) {
        this.zonasLeidas = zonasLeidas;
    }

}

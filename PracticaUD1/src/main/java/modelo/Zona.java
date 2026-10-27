package modelo;

import modelo.enums.EstadoZona;

import java.util.ArrayList;
import java.util.List;

public class Zona {

    private String id;
    private String nombre;
    private boolean restringida;
    private EstadoZona estado;
    private List<Elemento> elementos;


    //Constructor

    //vacio para JSON
    public Zona() {
    }

    public Zona(String id, String nombre, boolean restringida, EstadoZona estado, List<Elemento> elementos) {
        this.id = id;
        this.nombre = nombre;
        this.restringida = restringida;
        this.estado = estado;
        this.elementos = elementos;
    }
    // GETTERS Y SETTERS


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isRestringida() {
        return restringida;
    }

    public void setRestringida(boolean restringida) {
        this.restringida = restringida;
    }

    public EstadoZona getEstado() {
        return estado;
    }

    public void setEstado(EstadoZona estado) {
        this.estado = estado;
    }

    public List<Elemento> getElementos() {
        return elementos;
    }

    public void setElementos(List<Elemento> elementos) {
        this.elementos = elementos;
    }

    //ToString


    @Override
    public String toString() {
        return "Zona{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", restringida=" + restringida +
                ", estado=" + estado +
                ", elementos=" + elementos +
                '}';
    }
}

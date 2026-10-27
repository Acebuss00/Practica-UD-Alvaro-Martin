package modelo;

import java.util.ArrayList;
import java.util.List;

public class ciudad {

    private String nombre;
    private List<Zona> zonas = new ArrayList<>();
    private List<Personaje> personajes = new ArrayList<>();

    //Constructor

    public ciudad() {
    }

    public ciudad(String nombre, List<Zona> zonas, List<Personaje> personajes) {
        this.nombre = nombre;
        this.zonas = zonas;
        this.personajes = personajes;
    }

    //Getter y Setter


    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Zona> getZonas() {
        return zonas;
    }

    public void setZonas(List<Zona> zonas) {
        this.zonas = zonas;
    }

    public List<Personaje> getPersonajes() {
        return personajes;
    }

    public void setPersonajes(List<Personaje> personajes) {
        this.personajes = personajes;
    }

    //ToString


    @Override
    public String toString() {
        return "ciudad{" +
                "nombre='" + nombre + '\'' +
                ", zonas=" + zonas +
                ", personajes=" + personajes +
                '}';
    }
}

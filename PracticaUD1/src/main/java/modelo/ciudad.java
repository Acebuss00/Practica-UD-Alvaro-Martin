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

    //Metodos

    //Busca una zona por su id ("Z01"). Devuelve null si no existe.

    public Zona buscarZona(String idZona) {
        for (Zona zona : zonas) {
            if (zona.getId().equals(idZona)) {
                return zona;
            }
        }
        return null;
    }


    /**
     * Busca en qué zona está un elemento ("R05" -> la zona que lo contiene).
     * Es lo que permite relacionar una Lectura del binario con el JSON.
     * Devuelve null si ningún elemento tiene ese id.
     */
    public Zona buscarZonaDeElemento(String idElemento) {
        for (Zona zona : zonas) {
            for (Elemento elemento : zona.getElementos()) {
                if (elemento.getId().equals(idElemento)) {
                    return zona;
                }
            }
        }
        return null;
    }


    // Busca un elemento por id en cualquier zona. Devuelve null si no existe.
    public Elemento buscarElemento(String idElemento) {
        for (Zona zona : zonas) {
            for (Elemento elemento : zona.getElementos()) {
                if (elemento.getId().equals(idElemento)) {
                    return elemento;
                }
            }
        }
        return null;
    }


    // Busca un personaje por id ("PJ001"). Devuelve null si no existe.

    public Personaje buscarPersonaje(String idPersonaje) {
        for (Personaje personaje : personajes) {
            if (personaje.getId().equals(idPersonaje)) {
                return personaje;
            }
        }
        return null;
    }
}


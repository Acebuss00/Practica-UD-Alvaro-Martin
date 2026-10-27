package modelo;

import modelo.enums.FaccionPersonaje;

import java.util.ArrayList;
import java.util.List;

public class Personaje {

    //Los personajes estarán basados en el universo de World of Warcraft

    private String id;
    private String nombre;
    private FaccionPersonaje faccion;
    private List<String> zonasPermitidas = new ArrayList<>();


    //Constructores

    public Personaje() {
    }

    public Personaje(String id, String nombre, FaccionPersonaje faccion, List<String> zonasPermitidas) {
        this.id = id;
        this.nombre = nombre;
        this.faccion = faccion;
        this.zonasPermitidas = zonasPermitidas;
    }

    //Getter y Setter


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

    public FaccionPersonaje getFaccion() {
        return faccion;
    }

    public void setFaccion(FaccionPersonaje faccion) {
        this.faccion = faccion;
    }

    public List<String> getZonasPermitidas() {
        return zonasPermitidas;
    }

    public void setZonasPermitidas(List<String> zonasPermitidas) {
        this.zonasPermitidas = zonasPermitidas;
    }

    //ToString


    @Override
    public String toString() {
        return "Personaje{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", faccion=" + faccion +
                ", zonasPermitidas=" + zonasPermitidas +
                '}';
    }
}

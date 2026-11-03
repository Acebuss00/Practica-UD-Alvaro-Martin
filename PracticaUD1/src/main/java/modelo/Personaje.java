package modelo;

import modelo.enums.FaccionPersonaje;

import java.util.ArrayList;
import java.util.List;

/**
 * Personaje con acceso a la ciudad. Se guarda dentro de ciudad.xml.
 */
public class Personaje {

    private String id;                    // "PJ001"
    private String nombre;
    private FaccionPersonaje faccion;
    private List<String> zonasPermitidas; // ids de zona: "Z01", "Z04"...

    public Personaje() {
        this.zonasPermitidas = new ArrayList<>();
    }

    public Personaje(String id, String nombre, FaccionPersonaje faccion) {
        this.id = id;
        this.nombre = nombre;
        this.faccion = faccion;
        this.zonasPermitidas = new ArrayList<>();
    }

    public Personaje(String id, String nombre, FaccionPersonaje faccion, List<String> zonasPermitidas) {
        this.id = id;
        this.nombre = nombre;
        this.faccion = faccion;
        this.zonasPermitidas = zonasPermitidas;
    }

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

    /** Devuelve true si el personaje tiene permiso para entrar en esa zona. */
    public boolean puedeEntrarEn(String idZona) {
        return zonasPermitidas.contains(idZona);
    }

    @Override
    public String toString() {
        return "Personaje{id='" + id + "', nombre='" + nombre + "', faccion=" + faccion
                + ", zonasPermitidas=" + zonasPermitidas + "}";
    }
}

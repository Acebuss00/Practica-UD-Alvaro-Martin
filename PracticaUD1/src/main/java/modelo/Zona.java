package modelo;

import modelo.enums.EstadoZona;

import java.util.ArrayList;
import java.util.List;

/**
 * Zona de la ciudad de Ventormenta (Puerta principal, Distrito de los Magos...).
 * Se guarda dentro de ciudad.xml y contiene los elementos que hay en ella.
 */
public class Zona {

    // 1. ATRIBUTOS: siempre private
    private String id;              // "Z01"
    private String nombre;          // "Puerta principal"
    private boolean restringida;    // true si solo pueden entrar personajes con permiso
    private EstadoZona estado;      // NORMAL, EMERGENCIA, CUARENTENA
    private List<Elemento> elementos;

    // 2. CONSTRUCTOR VACÍO: crea la zona con la lista de elementos ya inicializada
    public Zona() {
        this.elementos = new ArrayList<>();
    }

    // 3. CONSTRUCTOR COMPLETO: para crear zonas cómodamente desde el simulador
    public Zona(String id, String nombre, boolean restringida, EstadoZona estado) {
        this.id = id;
        this.nombre = nombre;
        this.restringida = restringida;
        this.estado = estado;
        this.elementos = new ArrayList<>();
    }

    public Zona(String id, String nombre, boolean restringida, EstadoZona estado, List<Elemento> elementos) {
        this.id = id;
        this.nombre = nombre;
        this.restringida = restringida;
        this.estado = estado;
        this.elementos = elementos;
    }

    // 4. GETTERS Y SETTERS
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

    // Con boolean el getter empieza por "is" en vez de "get"
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

    // 5. toString: muy útil para depurar y para los logs
    @Override
    public String toString() {
        return "Zona{id='" + id + "', nombre='" + nombre + "', restringida=" + restringida
                + ", estado=" + estado + ", elementos=" + elementos.size() + "}";
    }
}

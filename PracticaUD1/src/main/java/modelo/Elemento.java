package modelo;

import modelo.enums.EstadoElemento;
import modelo.enums.TipoElemento;

/**
 * Cualquier cosa que hay dentro de una zona: guardia, portal, cristal, almacén, puerta o runa.
 * Se guarda dentro de ciudad.xml.
 */
public class Elemento {

    private String id;               // "G01", "P02", "R05"...
    private TipoElemento tipo;
    private EstadoElemento estado;

    public Elemento() {
    }

    public Elemento(String id, TipoElemento tipo, EstadoElemento estado) {
        this.id = id;
        this.tipo = tipo;
        this.estado = estado;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TipoElemento getTipo() {
        return tipo;
    }

    public void setTipo(TipoElemento tipo) {
        this.tipo = tipo;
    }

    public EstadoElemento getEstado() {
        return estado;
    }

    public void setEstado(EstadoElemento estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Elemento{id='" + id + "', tipo=" + tipo + ", estado=" + estado + "}";
    }
}

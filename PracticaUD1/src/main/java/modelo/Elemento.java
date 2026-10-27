package modelo;

import modelo.enums.EstadoElemento;
import modelo.enums.TipoElemento;

public class Elemento {

    private String id;
    private TipoElemento tipo;
    private EstadoElemento estado;


    //Constructores


    public Elemento() {
    }

    public Elemento(String id, TipoElemento tipo, EstadoElemento estado) {
        this.id = id;
        this.tipo = tipo;
        this.estado = estado;
    }


    //Getter y Setter


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

    //ToString


    @Override
    public String toString() {
        return "Elemento{" +
                "id='" + id + '\'' +
                ", tipo=" + tipo +
                ", estado=" + estado +
                '}';
    }
}

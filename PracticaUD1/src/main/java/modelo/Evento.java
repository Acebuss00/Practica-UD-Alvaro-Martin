package modelo;

import modelo.enums.TipoEvento;

import java.time.LocalDateTime;

public class Evento {

    private LocalDateTime fechaHora;
    private String idZona;
    private String idElemento;
    private TipoEvento tipo;
    private String descripcion;


    //Constructores


    public Evento() {
    }

    public Evento(LocalDateTime fechaHora, String idZona, String idElemento, TipoEvento tipo, String descripcion) {
        this.fechaHora = fechaHora;
        this.idZona = idZona;
        this.idElemento = idElemento;
        this.tipo = tipo;
        this.descripcion = descripcion;
    }


    //Getter y Setter


    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getIdZona() {
        return idZona;
    }

    public void setIdZona(String idZona) {
        this.idZona = idZona;
    }

    public String getIdElemento() {
        return idElemento;
    }

    public void setIdElemento(String idElemento) {
        this.idElemento = idElemento;
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public void setTipo(TipoEvento tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    //ToString


    @Override
    public String toString() {
        return "Evento{" +
                "fechaHora=" + fechaHora +
                ", idZona='" + idZona + '\'' +
                ", idElemento='" + idElemento + '\'' +
                ", tipo=" + tipo +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}

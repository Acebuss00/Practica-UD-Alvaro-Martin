package modelo;

import modelo.enums.TipoRuna;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Lectura {

    public static final int TAMANO_REGISTRO = 4 + 4 + 8 + 8 + 1;

    private int idRuna;
    private TipoRuna tipoRuna;
    private double valor;
    private long timestamp;
    private boolean anomala;

    public Lectura() {
    }

    public Lectura(int idRuna, TipoRuna tipoRuna, double valor, long timestamp, boolean anomala) {
        this.idRuna = idRuna;
        this.tipoRuna = tipoRuna;
        this.valor = valor;
        this.timestamp = timestamp;
        this.anomala = anomala;
    }

    public int getIdRuna() {
        return idRuna;
    }

    public void setIdRuna(int idRuna) {
        this.idRuna = idRuna;
    }

    public TipoRuna getTipoRuna() {
        return tipoRuna;
    }

    public void setTipoRuna(TipoRuna tipoRuna) {
        this.tipoRuna = tipoRuna;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isAnomala() {
        return anomala;
    }

    public void setAnomala(boolean anomala) {
        this.anomala = anomala;
    }


    public String getCodigoRuna(){


    }
    public LocalDateTime getFechaHora() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }
}

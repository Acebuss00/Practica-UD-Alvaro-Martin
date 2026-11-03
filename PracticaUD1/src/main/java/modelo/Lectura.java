package modelo;

import modelo.enums.TipoRuna;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Una medición tomada por una runa de vigilancia.
 * Se guarda en lecturas.dat como un registro de tamaño fijo (RandomAccessFile).
 *
 * Distribución del registro en el fichero:
 *   idRuna     int      4 bytes
 *   tipoRuna   int      4 bytes  (se guarda el ordinal() del enum)
 *   valor      double   8 bytes
 *   timestamp  long     8 bytes  (milisegundos desde 1970)
 *   anomala    boolean  1 byte
 *   ---------------------------
 *   TOTAL              25 bytes
 */
public class Lectura {

    // Tamaño de cada registro en el fichero binario. Se usa para calcular el seek():
    // posicion = numeroRegistro * TAMANO_REGISTRO
    public static final int TAMANO_REGISTRO = 4 + 4 + 8 + 8 + 1;

    // 1. ATRIBUTOS
    private int idRuna;          // 5 representa a la runa "R05"
    private TipoRuna tipoRuna;   // MANA, TEMPERATURA...
    private double valor;        // la medición
    private long timestamp;      // momento de la lectura en milisegundos
    private boolean anomala;     // true si el valor está fuera de rango

    // 2. CONSTRUCTOR VACÍO
    public Lectura() {
    }

    // 3. CONSTRUCTOR COMPLETO
    public Lectura(int idRuna, TipoRuna tipoRuna, double valor, long timestamp, boolean anomala) {
        this.idRuna = idRuna;
        this.tipoRuna = tipoRuna;
        this.valor = valor;
        this.timestamp = timestamp;
        this.anomala = anomala;
    }

    // 4. GETTERS Y SETTERS
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

    // 5. MÉTODOS DE AYUDA

    /** Convierte el número de runa al id que aparece en el XML: 5 -> "R05". */
    public String getCodigoRuna() {
        return String.format("R%02d", idRuna);
    }

    /** Convierte el timestamp en una fecha legible, útil para el informe y los logs. */
    public LocalDateTime getFechaHora() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    @Override
    public String toString() {
        return "Lectura{runa=" + getCodigoRuna() + ", tipo=" + tipoRuna + ", valor=" + valor
                + ", fecha=" + getFechaHora() + ", anomala=" + anomala + "}";
    }
}

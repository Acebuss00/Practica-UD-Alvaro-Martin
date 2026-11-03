package modelo.enums;

/**
 * Tipos de medición que puede tomar una runa de vigilancia, con su rango normal.
 *
 * IMPORTANTE: en el fichero binario se guarda la posición (ordinal) de cada valor:
 * MANA = 0, ENERGIA_ARCANA = 1, TEMPERATURA = 2...
 * Si cambias el orden o insertas valores en medio, los ficheros antiguos se leerán mal.
 * Los valores nuevos se añaden siempre al final.
 */
public enum TipoRuna {
    //                    mínimo  máximo  unidad
    MANA                 (500,    1000,   "pts"),
    ENERGIA_ARCANA       (200,    700,    "ua"),
    TEMPERATURA          (5,      40,     "ºC"),
    CORRUPCION_NECROTICA (0,      20,     "%"),
    CONTAMINACION        (0,      15,     "ppm"),
    DETECCION_SIGILO     (0,      10,     "%");

    private final double minimo;
    private final double maximo;
    private final String unidad;

    TipoRuna(double minimo, double maximo, String unidad) {
        this.minimo = minimo;
        this.maximo = maximo;
        this.unidad = unidad;
    }

    public double getMinimo() {
        return minimo;
    }

    public double getMaximo() {
        return maximo;
    }

    public String getUnidad() {
        return unidad;
    }

    /** true si el valor está dentro del rango normal de funcionamiento. */
    public boolean enRango(double valor) {
        return valor >= minimo && valor <= maximo;
    }
}

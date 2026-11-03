package modelo.enums;

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

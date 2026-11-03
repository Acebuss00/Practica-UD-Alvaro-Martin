package modelo.enums;

/**
 * Incidentes que puede sufrir la ciudad.
 *
 * El simulador lo usa para elegir qué incidente provocar y el analizador para expresar
 * su conclusión. NUNCA se pasa un valor de este enum de un programa al otro:
 * el analizador lo deduce solo a partir de los ficheros de datos.
 */
public enum TipoIncidente {

    INCURSION_PLAGA("Incursión de la Plaga",
            "La Plaga ha abierto una brecha en la Puerta principal: la ciudad está expuesta a una invasión de no-muertos."),
    PORTAL_INESTABLE("Portal inestable",
            "Los portales del Distrito de los Magos son inestables: los viajes deben suspenderse hasta reparar la red arcana."),
    COLAPSO_BARRERA("Colapso de la barrera arcana",
            "Los cristales de maná se han agotado y la barrera de la Fortaleza está caída: la Fortaleza queda sin protección mágica."),
    ATAQUE_DRAGON("Ataque de dragón",
            "Un dragón ha atacado el Casco Antiguo: los almacenes han ardido y se han perdido reservas de la ciudad."),
    INFILTRACION_HORDA("Infiltración de un espía de la Horda",
            "Un agente de la Horda ha accedido a una zona restringida de la Fortaleza: la seguridad de la ciudad está comprometida."),
    GRANO_CONTAMINADO("Grano contaminado (la Peste)",
            "El grano de los almacenes está contaminado con la Peste: el Casco Antiguo debe permanecer en cuarentena."),
    INDETERMINADO("Indeterminado",
            "Los datos no permiten identificar ninguno de los incidentes conocidos.");

    private final String descripcion;
    private final String conclusion;

    TipoIncidente(String descripcion, String conclusion) {
        this.descripcion = descripcion;
        this.conclusion = conclusion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getConclusion() {
        return conclusion;
    }
}

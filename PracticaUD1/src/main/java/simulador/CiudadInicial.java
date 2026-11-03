package simulador;

import modelo.ciudad;
import modelo.Elemento;
import modelo.enums.EstadoElemento;
import modelo.enums.EstadoZona;
import modelo.enums.FaccionPersonaje;
import modelo.Personaje;
import modelo.enums.TipoElemento;
import modelo.enums.TipoRuna;
import modelo.Zona;

/**
 * Crea la ciudad de Ventormenta tal y como está al empezar la simulación.
 *
 * Zonas y runas:
 *   Z01 Puerta principal       R01 CORRUPCION_NECROTICA, R02 DETECCION_SIGILO, R03 TEMPERATURA
 *   Z02 Distrito de los Magos  R04 ENERGIA_ARCANA,       R05 DETECCION_SIGILO, R06 TEMPERATURA
 *   Z03 Casco Antiguo          R07 CONTAMINACION,        R08 TEMPERATURA,      R09 DETECCION_SIGILO
 *   Z04 Fortaleza (restringida) R10 MANA,                R11 TEMPERATURA,      R12 DETECCION_SIGILO
 */
public class CiudadInicial {

    public static final int NUM_RUNAS = 12;

    /**
     * Tipo de cada runa. La posición del array es el número de runa - 1:
     * TIPOS_RUNA[0] es R01, TIPOS_RUNA[11] es R12.
     */
    public static final TipoRuna[] TIPOS_RUNA = {
            TipoRuna.CORRUPCION_NECROTICA, TipoRuna.DETECCION_SIGILO, TipoRuna.TEMPERATURA,   // Z01
            TipoRuna.ENERGIA_ARCANA,       TipoRuna.DETECCION_SIGILO, TipoRuna.TEMPERATURA,   // Z02
            TipoRuna.CONTAMINACION,        TipoRuna.TEMPERATURA,      TipoRuna.DETECCION_SIGILO, // Z03
            TipoRuna.MANA,                 TipoRuna.TEMPERATURA,      TipoRuna.DETECCION_SIGILO  // Z04
    };

    private CiudadInicial() {
        // Solo tiene métodos estáticos
    }

    public static TipoRuna tipoDeRuna(int idRuna) {
        return TIPOS_RUNA[idRuna - 1];
    }

    public static ciudad crear() {
        ciudad ciudad = new ciudad("Ventormenta");

        Zona puerta = new Zona("Z01", "Puerta principal", false, EstadoZona.NORMAL);
        anadir(puerta, "G01", TipoElemento.GUARDIA);
        anadir(puerta, "G02", TipoElemento.GUARDIA);
        anadir(puerta, "PU01", TipoElemento.PUERTA);
        anadirRunas(puerta, 1, 3);

        Zona magos = new Zona("Z02", "Distrito de los Magos", false, EstadoZona.NORMAL);
        anadir(magos, "P01", TipoElemento.PORTAL);
        anadir(magos, "P02", TipoElemento.PORTAL);
        anadir(magos, "M01", TipoElemento.MAGO);
        anadir(magos, "G03", TipoElemento.GUARDIA);
        anadirRunas(magos, 4, 6);

        Zona casco = new Zona("Z03", "Casco Antiguo", false, EstadoZona.NORMAL);
        anadir(casco, "A01", TipoElemento.ALMACEN);
        anadir(casco, "A02", TipoElemento.ALMACEN);
        anadir(casco, "G04", TipoElemento.GUARDIA);
        anadirRunas(casco, 7, 9);

        Zona fortaleza = new Zona("Z04", "Fortaleza", true, EstadoZona.NORMAL);
        anadir(fortaleza, "C01", TipoElemento.CRISTAL);
        anadir(fortaleza, "C02", TipoElemento.CRISTAL);
        anadir(fortaleza, "BA01", TipoElemento.BARRERA);
        anadir(fortaleza, "G05", TipoElemento.GUARDIA);
        anadir(fortaleza, "PU02", TipoElemento.PUERTA);
        anadirRunas(fortaleza, 10, 12);

        ciudad.getZonas().add(puerta);
        ciudad.getZonas().add(magos);
        ciudad.getZonas().add(casco);
        ciudad.getZonas().add(fortaleza);

        ciudad.getPersonajes().add(personaje("PJ001", "Jaina Valiente", "Z01", "Z02", "Z03", "Z04"));
        ciudad.getPersonajes().add(personaje("PJ002", "Varian Wrynn", "Z01", "Z02", "Z03", "Z04"));
        ciudad.getPersonajes().add(personaje("PJ003", "Mathias Shaw", "Z01", "Z02", "Z03", "Z04"));
        ciudad.getPersonajes().add(personaje("PJ004", "Comerciante de Dalaran", "Z01", "Z02", "Z03"));
        ciudad.getPersonajes().add(personaje("PJ005", "Granjero de Villadorada", "Z01", "Z03"));

        return ciudad;
    }

    private static void anadir(Zona zona, String id, TipoElemento tipo) {
        zona.getElementos().add(new Elemento(id, tipo, EstadoElemento.OPERATIVO));
    }

    private static void anadirRunas(Zona zona, int desde, int hasta) {
        for (int i = desde; i <= hasta; i++) {
            anadir(zona, String.format("R%02d", i), TipoElemento.RUNA);
        }
    }

    private static Personaje personaje(String id, String nombre, String... zonas) {
        Personaje p = new Personaje(id, nombre, FaccionPersonaje.ALIANZA);
        for (String zona : zonas) {
            p.getZonasPermitidas().add(zona);
        }
        return p;
    }
}

package simulador;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import modelo.enums.EstadoElemento;
import modelo.enums.EstadoZona;
import modelo.enums.FaccionPersonaje;
import modelo.Personaje;
import modelo.enums.TipoEvento;
import modelo.enums.TipoIncidente;

import java.io.IOException;
import java.util.Map;

/**
 * Genera las consecuencias de cada incidente, paso a paso.
 *
 * Cada incidente dura PASOS ciclos y en cada uno escribe en los tres ficheros
 * mediante el Escenario (los mismos métodos que la operativa normal):
 *   - paso 1: síntoma inicial, la medición todavía está dentro de rango
 *   - pasos 2 a 4: la medición sale de rango y la estructura se va deteriorando
 * Al tercer valor fuera de rango seguido el Escenario detecta la situación crítica.
 */
public class MotorIncidentes {

    private static final Logger log = LogManager.getLogger(MotorIncidentes.class);

    public static final int PASOS = 4;

    private final Escenario escenario;

    public MotorIncidentes(Escenario escenario) {
        this.escenario = escenario;
    }

    /**
     * Ejecuta un paso del incidente.
     * @param valores valores de las runas de este ciclo (nº de runa -> valor). El incidente
     *                sustituye el valor de la runa a la que afecta.
     */
    public void ejecutarPaso(TipoIncidente tipo, int paso, Map<Integer, Double> valores) throws IOException {
        log.debug("Paso {} de {}", paso, tipo);
        switch (tipo) {
            case INCURSION_PLAGA -> pasoPlaga(paso, valores);
            case PORTAL_INESTABLE -> pasoPortal(paso, valores);
            case COLAPSO_BARRERA -> pasoBarrera(paso, valores);
            case ATAQUE_DRAGON -> pasoDragon(paso, valores);
            case INFILTRACION_HORDA -> pasoHorda(paso, valores);
            case GRANO_CONTAMINADO -> pasoGrano(paso, valores);
            default -> throw new IllegalArgumentException("Incidente no simulable: " + tipo);
        }
    }

    // ---------- 1. Incursión de la Plaga · Z01 · R01 corrupción necrótica (normal 0-20 %) ----------
    private void pasoPlaga(int paso, Map<Integer, Double> valores) throws IOException {
        switch (paso) {
            case 1 -> {
                valores.put(1, 15.0);
                escenario.registrarEvento("Z01", "G01", TipoEvento.AVISO, "Los cuervos abandonan las murallas");
            }
            case 2 -> {
                valores.put(1, 45.0);
                escenario.registrarEvento("Z01", "G01", TipoEvento.AVISTAMIENTO,
                        "Grupo de necrófagos junto a la puerta principal");
            }
            case 3 -> {
                valores.put(1, 80.0);
                escenario.registrarEvento("Z01", "G02", TipoEvento.AVISTAMIENTO,
                        "Una abominación golpea la puerta, el guardia G01 no responde");
                escenario.cambiarEstado("G01", EstadoElemento.CAIDO);
                escenario.cambiarEstadoZona("Z01", EstadoZona.EMERGENCIA);
            }
            case 4 -> {
                valores.put(1, 95.0);
                escenario.registrarEvento("Z01", "PU01", TipoEvento.AVISO, "La puerta principal cede");
                escenario.cambiarEstado("PU01", EstadoElemento.BRECHA);
                escenario.cambiarEstado("G02", EstadoElemento.CAIDO);
            }
            default -> { }
        }
    }

    // ---------- 2. Portal inestable · Z02 · R04 energía arcana (normal 200-700 ua) ----------
    private void pasoPortal(int paso, Map<Integer, Double> valores) throws IOException {
        switch (paso) {
            case 1 -> {
                valores.put(4, 690.0);
                escenario.registrarEvento("Z02", "P01", TipoEvento.VIAJE_PORTAL, "Viaje a Forjaz con retraso");
            }
            case 2 -> {
                valores.put(4, 920.0);
                escenario.registrarEvento("Z02", "P01", TipoEvento.AVISO, "Interferencias en el portal a Forjaz");
                escenario.cambiarEstado("P01", EstadoElemento.INESTABLE);
            }
            case 3 -> {
                valores.put(4, 40.0);
                escenario.registrarEvento("Z02", "P02", TipoEvento.AVISO,
                        "Viaje fallido: el viajero aparece en un destino equivocado");
                escenario.cambiarEstado("P02", EstadoElemento.INESTABLE);
                escenario.cambiarEstadoZona("Z02", EstadoZona.EMERGENCIA);
            }
            case 4 -> {
                valores.put(4, 980.0);
                escenario.registrarEvento("Z02", "M01", TipoEvento.AVISO,
                        "El mago canalizador se desmaya intentando estabilizar los portales");
                escenario.cambiarEstado("M01", EstadoElemento.AGOTADO);
            }
            default -> { }
        }
    }

    // ---------- 3. Colapso de la barrera arcana · Z04 · R10 maná (normal 500-1000 pts) ----------
    private void pasoBarrera(int paso, Map<Integer, Double> valores) throws IOException {
        switch (paso) {
            case 1 -> {
                valores.put(10, 520.0);
                escenario.registrarEvento("Z04", "C01", TipoEvento.RECARGA, "Recarga lenta del cristal C01");
            }
            case 2 -> {
                valores.put(10, 300.0);
                escenario.registrarEvento("Z04", "C01", TipoEvento.AVISO, "Recarga fallida del cristal C01");
                escenario.cambiarEstado("C01", EstadoElemento.AGOTADO);
            }
            case 3 -> {
                valores.put(10, 120.0);
                escenario.registrarEvento("Z04", "C02", TipoEvento.AVISO, "Recarga fallida del cristal C02");
                escenario.cambiarEstado("C01", EstadoElemento.VACIO);
                escenario.cambiarEstado("C02", EstadoElemento.AGOTADO);
            }
            case 4 -> {
                valores.put(10, 0.0);
                escenario.registrarEvento("Z04", "BA01", TipoEvento.AVISO,
                        "La barrera arcana de la Fortaleza se apaga");
                escenario.cambiarEstado("C02", EstadoElemento.VACIO);
                escenario.cambiarEstado("BA01", EstadoElemento.DESACTIVADA);
                escenario.cambiarEstadoZona("Z04", EstadoZona.EMERGENCIA);
            }
            default -> { }
        }
    }

    // ---------- 4. Ataque de dragón · Z03 · R08 temperatura (normal 5-40 ºC) ----------
    private void pasoDragon(int paso, Map<Integer, Double> valores) throws IOException {
        switch (paso) {
            case 1 -> {
                valores.put(8, 38.0);
                escenario.registrarEvento("Z03", "G04", TipoEvento.AVISTAMIENTO,
                        "Una sombra enorme sobrevuela el Casco Antiguo");
            }
            case 2 -> {
                valores.put(8, 180.0);
                escenario.registrarEvento("Z03", "A01", TipoEvento.AVISO, "Fuego en el tejado del almacén A01");
                escenario.cambiarEstado("A01", EstadoElemento.EN_LLAMAS);
                escenario.cambiarEstadoZona("Z03", EstadoZona.EMERGENCIA);
            }
            case 3 -> {
                valores.put(8, 420.0);
                escenario.registrarEvento("Z03", "G04", TipoEvento.AVISTAMIENTO,
                        "Un dragón negro escupe fuego sobre los almacenes");
                escenario.cambiarEstado("A02", EstadoElemento.EN_LLAMAS);
            }
            case 4 -> {
                valores.put(8, 650.0);
                escenario.registrarEvento("Z03", "A01", TipoEvento.AVISO, "El almacén A01 se derrumba");
                escenario.cambiarEstado("A01", EstadoElemento.DESTRUIDO);
                escenario.cambiarEstado("G04", EstadoElemento.CAIDO);
            }
            default -> { }
        }
    }

    // ---------- 5. Infiltración de la Horda · Z04 · R12 detección de sigilo (normal 0-10 %) ----------
    private void pasoHorda(int paso, Map<Integer, Double> valores) throws IOException {
        switch (paso) {
            case 1 -> {
                valores.put(12, 8.0);
                Personaje espia = new Personaje("PJ099", "Mercader encapuchado", FaccionPersonaje.HORDA);
                espia.getZonasPermitidas().add("Z01");
                escenario.registrarPersonaje(espia);
                escenario.registrarEvento("Z01", "PJ099", TipoEvento.ACCESO,
                        "Un mercader encapuchado entra por la puerta principal");
            }
            case 2 -> {
                valores.put(12, 65.0);
                escenario.registrarEvento("Z04", "PJ099", TipoEvento.ACCESO,
                        "Acceso a la Fortaleza por la puerta de servicio");
            }
            case 3 -> {
                valores.put(12, 85.0);
                escenario.registrarEvento("Z04", "G05", TipoEvento.AVISO,
                        "La cerradura de la puerta PU02 aparece forzada");
                escenario.cambiarEstado("PU02", EstadoElemento.COMPROMETIDA);
            }
            case 4 -> {
                valores.put(12, 97.0);
                escenario.registrarEvento("Z04", "PJ099", TipoEvento.ACCESO,
                        "Visto cerca de la sala de mapas de la Fortaleza");
                escenario.cambiarEstadoZona("Z04", EstadoZona.EMERGENCIA);
            }
            default -> { }
        }
    }

    // ---------- 6. Grano contaminado · Z03 · R07 contaminación (normal 0-15 ppm) ----------
    private void pasoGrano(int paso, Map<Integer, Double> valores) throws IOException {
        switch (paso) {
            case 1 -> {
                valores.put(7, 12.0);
                escenario.registrarEvento("Z03", "A02", TipoEvento.REPARTO_GRANO,
                        "Reparto de grano del almacén A02");
            }
            case 2 -> {
                valores.put(7, 40.0);
                escenario.registrarEvento("Z03", "A02", TipoEvento.AVISO,
                        "Varios ciudadanos con fiebre tras el reparto del almacén A02");
                escenario.cambiarEstado("A02", EstadoElemento.CONTAMINADO);
            }
            case 3 -> {
                valores.put(7, 75.0);
                escenario.registrarEvento("Z03", "A02", TipoEvento.AVISO,
                        "La enfermedad se extiende por el Casco Antiguo");
                escenario.cambiarEstadoZona("Z03", EstadoZona.CUARENTENA);
            }
            case 4 -> {
                valores.put(7, 90.0);
                escenario.registrarEvento("Z03", "A01", TipoEvento.AVISO,
                        "El grano del almacén A01 también está contaminado");
                escenario.cambiarEstado("A01", EstadoElemento.CONTAMINADO);
            }
            default -> { }
        }
    }
}

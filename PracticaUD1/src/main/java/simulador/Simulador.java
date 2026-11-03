package simulador;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import excepciones.EventoCriticoException;
import modelo.Elemento;
import modelo.enums.EstadoElemento;
import modelo.Personaje;
import modelo.enums.TipoElemento;
import modelo.enums.TipoEvento;
import modelo.enums.TipoIncidente;
import modelo.Zona;
import persistencia.Rutas;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Simulador del sistema de defensa de Ventormenta.
 *
 * Uso:
 *   Simulador                              modo normal: puede ocurrir un incidente al azar
 *   Simulador --incidente=ATAQUE_DRAGON    modo prueba: provoca ese incidente
 *   Simulador --incidente=NINGUNO          solo operativa normal
 * Opciones: --ciclos=30  --semilla=1234  --datos=datos
 */
public class Simulador {

    private static final Logger log = LogManager.getLogger(Simulador.class);

    public static final int CICLOS_POR_DEFECTO = 30;
    /** En modo normal, probabilidad de que ocurra un incidente durante la simulación. */
    private static final double PROBABILIDAD_INCIDENTE = 0.85;
    /** En modo prueba, el incidente empieza en este ciclo (antes hay operativa normal). */
    private static final int CICLO_INICIO_PRUEBA = 5;

    private final Escenario escenario;
    private final MotorIncidentes motor;
    private final Random random;

    public Simulador(Rutas rutas, long semilla) {
        this.random = new Random(semilla);
        this.escenario = new Escenario(rutas, random);
        this.motor = new MotorIncidentes(escenario);
    }

    // =====================================================================
    // MODOS DE EJECUCIÓN
    // =====================================================================

    /** Modo normal: el incidente, el tipo y el momento se eligen al azar (o ninguno). */
    public void ejecutarAleatorio(int ciclos) throws IOException, EventoCriticoException {
        TipoIncidente incidente = null;
        int cicloInicio = -1;
        if (random.nextDouble() < PROBABILIDAD_INCIDENTE) {
            TipoIncidente[] posibles = incidentesSimulables();
            incidente = posibles[random.nextInt(posibles.length)];
            int ultimoInicio = Math.max(2, ciclos - MotorIncidentes.PASOS + 1);
            cicloInicio = 2 + random.nextInt(ultimoInicio - 1);
        }
        ejecutar(incidente, cicloInicio, ciclos);
    }

    /** Modo prueba: provoca siempre el incidente indicado. */
    public void ejecutarIncidente(TipoIncidente incidente, int ciclos) throws IOException, EventoCriticoException {
        ejecutar(incidente, CICLO_INICIO_PRUEBA, Math.max(ciclos, CICLO_INICIO_PRUEBA + MotorIncidentes.PASOS));
    }

    /** Solo operativa normal, sin incidentes. */
    public void ejecutarNormal(int ciclos) throws IOException, EventoCriticoException {
        ejecutar(null, -1, ciclos);
    }

    /**
     * Bucle principal. En cada ciclo:
     *   1. operativa normal (un acontecimiento en el TXT)
     *   2. si hay incidente en curso, su paso correspondiente (TXT y XML)
     *   3. mediciones de las 12 runas (BIN)
     *   4. comprobación de situación crítica (puede lanzar EventoCriticoException)
     */
    private void ejecutar(TipoIncidente incidente, int cicloInicio, int ciclos)
            throws IOException, EventoCriticoException {
        escenario.inicializar(ciclos);
        if (incidente != null) {
            // Esto va al log técnico, nunca a los ficheros de datos
            log.info("Incidente programado: {} a partir del ciclo {}", incidente, cicloInicio);
        }

        try {
            for (int ciclo = 1; ciclo <= ciclos; ciclo++) {
                escenario.avanzarReloj();
                operativaNormal();

                Map<Integer, Double> valores = valoresNormales();
                int paso = ciclo - cicloInicio + 1;
                if (incidente != null && paso >= 1 && paso <= MotorIncidentes.PASOS) {
                    motor.ejecutarPaso(incidente, paso, valores);
                }
                for (int idRuna = 1; idRuna <= CiudadInicial.NUM_RUNAS; idRuna++) {
                    escenario.registrarLectura(idRuna, valores.get(idRuna));
                }

                escenario.comprobarSituacionCritica();
            }
        } finally {
            log.info("Datos escritos: {} eventos, {} lecturas", escenario.getEventosEscritos(),
                    escenario.getLecturasEscritas());
        }
    }

    // =====================================================================
    // OPERATIVA NORMAL
    // =====================================================================

    /** Un acontecimiento habitual de la ciudad, elegido al azar. */
    private void operativaNormal() throws IOException {
        switch (random.nextInt(5)) {
            case 0 -> operacionElemento(TipoElemento.GUARDIA, TipoEvento.CAMBIO_TURNO, "Relevo de guardia de %s");
            case 1 -> operacionElemento(TipoElemento.PORTAL, TipoEvento.VIAJE_PORTAL, "Viaje por el portal %s sin incidencias");
            case 2 -> operacionElemento(TipoElemento.CRISTAL, TipoEvento.RECARGA, "Recarga completa del cristal %s");
            case 3 -> operacionElemento(TipoElemento.ALMACEN, TipoEvento.REPARTO_GRANO, "Reparto de grano del almacén %s");
            default -> accesoAutorizado();
        }
    }

    /** Elige un elemento OPERATIVO de ese tipo y registra la operación. */
    private void operacionElemento(TipoElemento tipo, TipoEvento evento, String descripcion) throws IOException {
        List<String[]> candidatos = new ArrayList<>();     // {idZona, idElemento}
        for (Zona zona : escenario.getCiudad().getZonas()) {
            for (Elemento e : zona.getElementos()) {
                if (e.getTipo() == tipo && e.getEstado() == EstadoElemento.OPERATIVO) {
                    candidatos.add(new String[]{zona.getId(), e.getId()});
                }
            }
        }
        if (candidatos.isEmpty()) {
            accesoAutorizado();
            return;
        }
        String[] elegido = candidatos.get(random.nextInt(candidatos.size()));
        escenario.registrarEvento(elegido[0], elegido[1], evento, String.format(descripcion, elegido[1]));
    }

    /** Un personaje de la Alianza entra en una de las zonas que tiene permitidas. */
    private void accesoAutorizado() throws IOException {
        List<Personaje> personajes = new ArrayList<>(escenario.getCiudad().getPersonajes());
        personajes.removeIf(p -> !p.getId().matches("PJ00\\d"));   // solo los habitantes iniciales
        Personaje p = personajes.get(random.nextInt(personajes.size()));
        String zona = p.getZonasPermitidas().get(random.nextInt(p.getZonasPermitidas().size()));
        escenario.registrarEvento(zona, p.getId(), TipoEvento.ACCESO, p.getNombre() + " accede a la zona " + zona);
    }

    private Map<Integer, Double> valoresNormales() {
        Map<Integer, Double> valores = new HashMap<>();
        for (int idRuna = 1; idRuna <= CiudadInicial.NUM_RUNAS; idRuna++) {
            valores.put(idRuna, escenario.valorNormal(CiudadInicial.tipoDeRuna(idRuna)));
        }
        return valores;
    }

    /** Todos los incidentes menos INDETERMINADO, que solo lo usa el analizador. */
    public static TipoIncidente[] incidentesSimulables() {
        List<TipoIncidente> lista = new ArrayList<>(List.of(TipoIncidente.values()));
        lista.remove(TipoIncidente.INDETERMINADO);
        return lista.toArray(new TipoIncidente[0]);
    }

    // =====================================================================
    // MAIN
    // =====================================================================

    public static void main(String[] args) {
        String incidente = null;          // null = aleatorio
        TipoIncidente tipo = null;
        int ciclos = CICLOS_POR_DEFECTO;
        long semilla = System.currentTimeMillis();
        Path datos = Path.of("datos");

        try {
            for (String arg : args) {
                if (arg.startsWith("--incidente=")) {
                    incidente = arg.substring("--incidente=".length()).toUpperCase();
                    if (!incidente.equals("NINGUNO")) {
                        tipo = TipoIncidente.valueOf(incidente);   // falla si no existe
                    }
                } else if (arg.startsWith("--ciclos=")) {
                    ciclos = Integer.parseInt(arg.substring("--ciclos=".length()));
                } else if (arg.startsWith("--semilla=")) {
                    semilla = Long.parseLong(arg.substring("--semilla=".length()));
                } else if (arg.startsWith("--datos=")) {
                    datos = Path.of(arg.substring("--datos=".length()));
                } else {
                    throw new IllegalArgumentException("Argumento desconocido: " + arg);
                }
            }
        } catch (IllegalArgumentException e) {   // NumberFormatException también es IllegalArgumentException
            log.error("Argumentos incorrectos: {}", e.getMessage());
            System.err.println("Uso: Simulador [--incidente=TIPO|NINGUNO] [--ciclos=N] [--semilla=N] [--datos=carpeta]");
            System.err.println("Incidentes: " + List.of(incidentesSimulables()));
            return;
        }

        log.info("=== Inicio del simulador (incidente={}, ciclos={}, semilla={}) ===",
                incidente == null ? "ALEATORIO" : incidente, ciclos, semilla);
        Simulador simulador = new Simulador(new Rutas(datos, Path.of("salida")), semilla);

        try {
            if (incidente == null) {
                simulador.ejecutarAleatorio(ciclos);
            } else if (incidente.equals("NINGUNO")) {
                simulador.ejecutarNormal(ciclos);
            } else {
                simulador.ejecutarIncidente(tipo, ciclos);
            }
            log.info("Simulación terminada sin situaciones críticas");
        } catch (EventoCriticoException e) {
            // Los datos ya están escritos: aquí solo se registra y se termina de forma controlada
            log.error("Evento crítico capturado en {}: {}", e.getIdZona(), e.getMessage());
        } catch (IOException e) {
            log.error("Error de E/S durante la simulación", e);
        }
        log.info("=== Fin del simulador ===");
    }
}

package simulador;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import excepciones.EventoCriticoException;
import modelo.ciudad;
import modelo.Elemento;
import modelo.enums.EstadoElemento;
import modelo.enums.EstadoZona;
import modelo.Evento;
import modelo.Lectura;
import modelo.Personaje;
import modelo.enums.TipoEvento;
import modelo.enums.TipoRuna;
import modelo.Zona;
import persistencia.PersistenciaBin;
import persistencia.PersistenciaTexto;
import persistencia.PersistenciaXML;
import persistencia.Rutas;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;

/**
 * Estado de la simulación y ÚNICA puerta de escritura a los tres ficheros de datos.
 *
 * Tanto la operativa normal como los incidentes escriben a través de estos métodos,
 * así una anomalía queda registrada igual que cualquier otro dato del sistema.
 */
public class Escenario {

    private static final Logger log = LogManager.getLogger(Escenario.class);

    /** Lecturas seguidas fuera de rango a partir de las que la situación es crítica. */
    public static final int UMBRAL_CRITICO = 3;

    private final PersistenciaTexto texto;
    private final PersistenciaBin binaria;
    private final PersistenciaXML xml;
    private final Rutas rutas;
    private final Random random;

    private ciudad ciudad;
    private LocalDateTime reloj;                 // hora simulada: avanza un minuto por ciclo
    private final int[] anomaliasSeguidas = new int[CiudadInicial.NUM_RUNAS + 1]; // índice = nº de runa

    private int eventosEscritos;
    private int lecturasEscritas;

    public Escenario(Rutas rutas, Random random) {
        this.rutas = rutas;
        this.random = random;
        this.texto = new PersistenciaTexto(rutas.getEventos());
        this.binaria = new PersistenciaBin(rutas.getLecturas());
        this.xml = new PersistenciaXML(rutas.getCiudad());
    }

    /**
     * Prepara una simulación nueva: crea las carpetas, borra los datos de la ejecución
     * anterior, guarda la ciudad inicial y reserva los 12 registros de estado del binario.
     * El reloj simulado empieza "ciclos" minutos antes de ahora, para que la simulación
     * termine aproximadamente en la hora actual.
     */
    public void inicializar(int ciclos) throws IOException {
        rutas.crearDirectorios();
        Files.deleteIfExists(rutas.getEventos());
        Files.deleteIfExists(rutas.getLecturas());
        Files.deleteIfExists(rutas.getCiudad());

        ciudad = CiudadInicial.crear();
        guardarCiudad();

        reloj = LocalDateTime.now().withNano(0).minusMinutes(ciclos);
        long ms = aMilisegundos(reloj);
        // Registros 0..11: estado actual de cada runa, con un primer valor normal
        for (int idRuna = 1; idRuna <= CiudadInicial.NUM_RUNAS; idRuna++) {
            TipoRuna tipo = CiudadInicial.tipoDeRuna(idRuna);
            binaria.anadirLectura(new Lectura(idRuna, tipo, valorNormal(tipo), ms, false));
        }
        log.info("Ficheros de datos inicializados en {}", rutas.getCarpetaDatos().toAbsolutePath().normalize());
    }

    /** Avanza el reloj simulado un minuto (un ciclo de la simulación). */
    public void avanzarReloj() {
        reloj = reloj.plusMinutes(1);
    }

    // =====================================================================
    // ESCRITURA EN LOS FICHEROS
    // =====================================================================

    /** TXT: añade un acontecimiento al histórico. */
    public void registrarEvento(String idZona, String idElemento, TipoEvento tipo, String descripcion)
            throws IOException {
        texto.escribirEvento(new Evento(reloj, idZona, idElemento, tipo, descripcion));
        eventosEscritos++;
        log.debug("Evento {} en {} ({}): {}", tipo, idZona, idElemento, descripcion);
    }

    /**
     * BIN: guarda una medición de una runa.
     *   1. modifica su registro de estado actual (seek a la posición de la runa)
     *   2. añade la medición al final del histórico
     */
    public void registrarLectura(int idRuna, double valor) throws IOException {
        TipoRuna tipo = CiudadInicial.tipoDeRuna(idRuna);
        boolean anomala = !tipo.enRango(valor);
        Lectura lectura = new Lectura(idRuna, tipo, valor, aMilisegundos(reloj), anomala);

        binaria.actualizarEstadoRuna(lectura);
        binaria.anadirLectura(lectura);
        lecturasEscritas++;

        if (anomala) {
            anomaliasSeguidas[idRuna]++;
            log.warn("Lectura fuera de rango: {} {} = {} {} (rango {}-{})", lectura.getCodigoRuna(), tipo,
                    valor, tipo.getUnidad(), tipo.getMinimo(), tipo.getMaximo());
        } else {
            anomaliasSeguidas[idRuna] = 0;
            log.debug("Lectura {} {} = {}", lectura.getCodigoRuna(), tipo, valor);
        }
    }

    /** XML: cambia el estado de un elemento y guarda la estructura. */
    public void cambiarEstado(String idElemento, EstadoElemento estado) throws IOException {
        Elemento elemento = ciudad.buscarElemento(idElemento);
        elemento.setEstado(estado);
        guardarCiudad();
        log.info("Estado de {} -> {}", idElemento, estado);
    }

    /** XML: cambia el estado de una zona y guarda la estructura. */
    public void cambiarEstadoZona(String idZona, EstadoZona estado) throws IOException {
        ciudad.buscarZona(idZona).setEstado(estado);
        guardarCiudad();
        log.info("Estado de la zona {} -> {}", idZona, estado);
    }

    /** XML: registra un personaje nuevo en la ciudad (por ejemplo, un visitante en la puerta). */
    public void registrarPersonaje(Personaje personaje) throws IOException {
        ciudad.getPersonajes().add(personaje);
        guardarCiudad();
        log.info("Personaje registrado en la puerta: {}", personaje.getId());
    }

    private void guardarCiudad() throws IOException {
        xml.guardarCiudad(ciudad);
    }

    // =====================================================================
    // DETECCIÓN DE LA SITUACIÓN CRÍTICA
    // =====================================================================

    /**
     * Comprueba si alguna runa lleva UMBRAL_CRITICO lecturas seguidas fuera de rango.
     * Se llama DESPUÉS de escribir los datos del ciclo, así cuando salta la excepción
     * los tres ficheros ya reflejan la anomalía.
     */
    public void comprobarSituacionCritica() throws EventoCriticoException, IOException {
        for (int idRuna = 1; idRuna <= CiudadInicial.NUM_RUNAS; idRuna++) {
            if (anomaliasSeguidas[idRuna] >= UMBRAL_CRITICO) {
                // Acceso directo: leemos solo el registro de estado de esa runa
                Lectura actual = binaria.leerEstadoRuna(idRuna);
                Zona zona = ciudad.buscarZonaDeElemento(actual.getCodigoRuna());
                throw new EventoCriticoException(String.format(
                        "Runa %s (%s) en %s: %d lecturas seguidas fuera de rango, valor actual %.1f %s",
                        actual.getCodigoRuna(), actual.getTipoRuna(), zona.getId(), anomaliasSeguidas[idRuna],
                        actual.getValor(), actual.getTipoRuna().getUnidad()), zona.getId());
            }
        }
    }

    // =====================================================================
    // AYUDAS
    // =====================================================================

    /** Valor aleatorio dentro del rango normal (con un margen del 10% en cada extremo). */
    public double valorNormal(TipoRuna tipo) {
        double margen = (tipo.getMaximo() - tipo.getMinimo()) * 0.1;
        double valor = tipo.getMinimo() + margen
                + random.nextDouble() * (tipo.getMaximo() - tipo.getMinimo() - 2 * margen);
        return Math.round(valor * 10) / 10.0;   // un decimal
    }

    private long aMilisegundos(LocalDateTime fecha) {
        return fecha.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public ciudad getCiudad() {
        return ciudad;
    }

    public Random getRandom() {
        return random;
    }

    public int getEventosEscritos() {
        return eventosEscritos;
    }

    public int getLecturasEscritas() {
        return lecturasEscritas;
    }
}

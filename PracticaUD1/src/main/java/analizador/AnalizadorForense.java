package analizador;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import modelo.ciudad;
import modelo.Evento;
import modelo.Lectura;
import persistencia.PersistenciaBin;
import persistencia.PersistenciaTexto;
import persistencia.PersistenciaXML;
import persistencia.Rutas;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Analizador forense: programa independiente del simulador.
 *
 * Solo recibe la ubicación de los ficheros de datos y trabaja únicamente con su contenido.
 * No lee los logs ni recibe el tipo de incidente.
 *
 * Uso: AnalizadorForense [--datos=datos] [--salida=salida]
 */
public class AnalizadorForense {

    private static final Logger log = LogManager.getLogger(AnalizadorForense.class);

    private final Rutas rutas;

    public AnalizadorForense(Rutas rutas) {
        this.rutas = rutas;
    }

    /** Lee los tres ficheros, identifica el incidente y genera las tres salidas. */
    public ResultadoAnalisis analizar() throws IOException {
        // 1. Cargar las tres fuentes
        List<Evento> eventos = new PersistenciaTexto(rutas.getEventos()).leerEventos();
        PersistenciaBin binaria = new PersistenciaBin(rutas.getLecturas());
        List<Lectura> historico = binaria.leerHistorico();
        ciudad ciudad = new PersistenciaXML(rutas.getCiudad()).cargarCiudad();
        log.info("Datos cargados: {} eventos, {} lecturas, {} zonas, {} personajes",
                eventos.size(), historico.size(), ciudad.getZonas().size(), ciudad.getPersonajes().size());

        // 2. Relacionar la información e identificar el incidente
        ResultadoAnalisis resultado = new DetectorIncidentes().detectar(eventos, historico, ciudad);
        log.warn("{} lecturas fuera de rango relacionadas con el resultado", resultado.getLecturasAnomalas().size());
        log.info("Incidente identificado: {}", resultado.getIncidente());

        // 3. Acceso directo: valor actual de cada runa implicada, sin recorrer el histórico
        Set<Integer> runas = new HashSet<>();
        for (Lectura l : resultado.getLecturasAnomalas()) {
            runas.add(l.getIdRuna());
        }
        for (int idRuna : runas) {
            if (binaria.numeroRegistros() >= idRuna) {
                Lectura actual = binaria.leerEstadoRuna(idRuna);
                resultado.getEfectos().add(String.format("Valor actual de %s: %.1f %s", actual.getCodigoRuna(),
                        actual.getValor(), actual.getTipoRuna().getUnidad()));
            }
        }

        // 4. Salidas
        new GeneradorSalidas(rutas).generar(resultado);
        return resultado;
    }

    public static void main(String[] args) {
        Path datos = Path.of("datos");
        Path salida = Path.of("salida");
        for (String arg : args) {
            if (arg.startsWith("--datos=")) {
                datos = Path.of(arg.substring("--datos=".length()));
            } else if (arg.startsWith("--salida=")) {
                salida = Path.of(arg.substring("--salida=".length()));
            } else {
                log.error("Argumento desconocido: {}", arg);
                System.err.println("Uso: AnalizadorForense [--datos=carpeta] [--salida=carpeta]");
                return;
            }
        }

        log.info("=== Inicio del analizador forense (datos={}) ===", datos);
        try {
            ResultadoAnalisis resultado = new AnalizadorForense(new Rutas(datos, salida)).analizar();
            System.out.println("Incidente detectado: " + resultado.getIncidente());
            System.out.println("Informe en " + salida.resolve("informe.txt"));
        } catch (NoSuchFileException e) {
            log.error("Falta un fichero de datos: {}. Ejecuta antes el simulador.", e.getMessage());
        } catch (IOException e) {
            log.error("No se pudieron analizar los datos", e);
        }
        log.info("=== Fin del analizador forense ===");
    }
}

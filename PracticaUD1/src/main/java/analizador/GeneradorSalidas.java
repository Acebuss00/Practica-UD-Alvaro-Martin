package analizador;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import modelo.Elemento;
import modelo.Evento;
import modelo.Lectura;
import modelo.Personaje;
import modelo.Zona;
import persistencia.PersistenciaXML;
import persistencia.Rutas;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Escribe las tres salidas del análisis. Cada una guarda una parte distinta del resultado:
 *   informe.txt   -> conclusión legible: incidente, evidencias resumidas, efectos y conclusión
 *   anomalias.dat -> solo las lecturas fuera de rango usadas para decidir (mismo formato de 25 bytes)
 *   afectados.xml -> solo la zona afectada, sus elementos dañados y los personajes implicados
 */
public class GeneradorSalidas {

    private static final Logger log = LogManager.getLogger(GeneradorSalidas.class);
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final Rutas rutas;

    public GeneradorSalidas(Rutas rutas) {
        this.rutas = rutas;
    }

    public void generar(ResultadoAnalisis resultado) throws IOException {
        rutas.crearDirectorios();
        escribirInforme(resultado);
        escribirAnomalias(resultado);
        escribirAfectados(resultado);
        log.info("Salidas generadas en {}", rutas.getCarpetaSalida().toAbsolutePath().normalize());
    }

    // ---------- TXT (java.io: PrintWriter) ----------

    private void escribirInforme(ResultadoAnalisis r) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(rutas.getInforme().toFile(), StandardCharsets.UTF_8))) {
            out.println("=== INFORME FORENSE · SISTEMA DE DEFENSA DE VENTORMENTA ===");
            out.println("Fecha del análisis: " + LocalDateTime.now().format(FORMATO));
            out.println("Datos analizados: " + r.getEventosLeidos() + " eventos (TXT), "
                    + r.getLecturasLeidas() + " lecturas (BIN), " + r.getZonasLeidas() + " zonas (XML)");
            out.println();
            out.println("INCIDENTE DETECTADO: " + r.getIncidente() + " (" + r.getIncidente().getDescripcion() + ")");
            if (r.getZona() != null) {
                out.println("Zona afectada: " + r.getZona().getId() + " · " + r.getZona().getNombre());
            }
            out.println();

            if (!r.getEventosRelevantes().isEmpty()) {
                out.println("ACONTECIMIENTOS RELACIONADOS (eventos.txt)");
                for (Evento e : r.getEventosRelevantes()) {
                    out.println("  " + e.getFechaHora().format(FORMATO) + "  " + e.getTipo() + "  "
                            + e.getIdElemento() + "  " + e.getDescripcion());
                }
                out.println();
            }

            out.println("EFECTOS DETECTADOS");
            for (String efecto : r.getEfectos()) {
                out.println("  - " + efecto);
            }
            out.println();
            out.println("CONCLUSIÓN");
            out.println("  " + r.getIncidente().getConclusion());
        }
    }

    // ---------- BIN (DataOutputStream secuencial) ----------

    private void escribirAnomalias(ResultadoAnalisis r) throws IOException {
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(rutas.getAnomalias().toFile())))) {
            for (Lectura l : r.getLecturasAnomalas()) {
                out.writeInt(l.getIdRuna());
                out.writeInt(l.getTipoRuna().ordinal());
                out.writeDouble(l.getValor());
                out.writeLong(l.getTimestamp());
                out.writeBoolean(true);
            }
        }
        log.info("{} lecturas anómalas guardadas en {}", r.getLecturasAnomalas().size(),
                rutas.getAnomalias().getFileName());
    }

    // ---------- XML (DOM) ----------

    private void escribirAfectados(ResultadoAnalisis r) throws IOException {
        Document doc = PersistenciaXML.nuevoDocumento();
        Element raiz = doc.createElement("analisis");
        raiz.setAttribute("incidente", r.getIncidente().name());
        raiz.setAttribute("fecha", LocalDateTime.now().withNano(0).toString());
        doc.appendChild(raiz);

        Zona zona = r.getZona();
        if (zona != null) {
            Element zonaXml = doc.createElement("zona");
            zonaXml.setAttribute("id", zona.getId());
            zonaXml.setAttribute("nombre", zona.getNombre());
            zonaXml.setAttribute("estado", zona.getEstado().name());
            zonaXml.setAttribute("restringida", String.valueOf(zona.isRestringida()));
            raiz.appendChild(zonaXml);

            // Elementos dañados
            for (Elemento e : r.getElementosAfectados()) {
                Element el = doc.createElement("elemento");
                el.setAttribute("id", e.getId());
                el.setAttribute("tipo", e.getTipo().name());
                el.setAttribute("estado", e.getEstado().name());
                zonaXml.appendChild(el);
            }

            // Runas que han dado las lecturas anómalas (relación con anomalias.dat)
            Map<String, Integer> anomaliasPorRuna = new HashMap<>();
            Map<String, String> tipoPorRuna = new HashMap<>();
            for (Lectura l : r.getLecturasAnomalas()) {
                anomaliasPorRuna.merge(l.getCodigoRuna(), 1, Integer::sum);
                tipoPorRuna.put(l.getCodigoRuna(), l.getTipoRuna().name());
            }
            for (Map.Entry<String, Integer> entrada : anomaliasPorRuna.entrySet()) {
                Element runa = doc.createElement("runa");
                runa.setAttribute("id", entrada.getKey());
                runa.setAttribute("tipo", tipoPorRuna.get(entrada.getKey()));
                runa.setAttribute("lecturasAnomalas", String.valueOf(entrada.getValue()));
                zonaXml.appendChild(runa);
            }
        }

        if (!r.getPersonajesImplicados().isEmpty()) {
            Element personajes = doc.createElement("personajesImplicados");
            for (Personaje p : r.getPersonajesImplicados()) {
                Element pj = doc.createElement("personaje");
                pj.setAttribute("id", p.getId());
                pj.setAttribute("nombre", p.getNombre());
                pj.setAttribute("faccion", p.getFaccion().name());
                pj.setAttribute("zonasPermitidas", String.join(",", p.getZonasPermitidas()));
                personajes.appendChild(pj);
            }
            raiz.appendChild(personajes);
        }

        PersistenciaXML.escribirDocumento(doc, rutas.getAfectados());
    }
}
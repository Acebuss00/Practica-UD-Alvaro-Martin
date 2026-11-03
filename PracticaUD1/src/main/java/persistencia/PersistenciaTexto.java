package persistencia;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import modelo.Evento;
import modelo.enums.TipoEvento;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Escribe y lee eventos.txt usando java.io clásico (BufferedWriter / BufferedReader).
 *
 * Formato de cada línea (separador ";"):
 *   fechaHora;idZona;idElemento;tipo;descripcion
 * Ejemplo:
 *   2026-11-02T18:45:12;Z01;G03;AVISTAMIENTO;Grupo de necrófagos junto a la puerta
 */
public class PersistenciaTexto {

    private static final Logger log = LogManager.getLogger(PersistenciaTexto.class);

    private static final String SEPARADOR = ";";
    private static final int NUM_CAMPOS = 5;

    private final Path fichero;

    public PersistenciaTexto(Path fichero) {
        this.fichero = fichero;
    }

    /**
     * Añade un evento al final del fichero.
     * FileWriter con "true" abre en modo AÑADIR: no borra lo que ya había.
     */
    public void escribirEvento(Evento evento) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(fichero.toFile(), StandardCharsets.UTF_8, true))) {
            writer.write(aLinea(evento));
            writer.newLine();
        }
    }

    /**
     * Lee todos los eventos del fichero.
     * Si una línea está mal formada, se salta y se avisa en el log, pero el programa no se cae.
     * Si el fichero no existe, devuelve una lista vacía.
     */
    public List<Evento> leerEventos() throws IOException {
        List<Evento> eventos = new ArrayList<>();

        if (Files.notExists(fichero)) {
            log.warn("No existe el fichero de eventos: {}", fichero);
            return eventos;
        }

        try (BufferedReader reader = new BufferedReader(
                new FileReader(fichero.toFile(), StandardCharsets.UTF_8))) {
            String linea;
            int numeroLinea = 0;
            while ((linea = reader.readLine()) != null) {
                numeroLinea++;
                if (linea.isBlank()) {
                    continue;
                }
                try {
                    eventos.add(desdeLinea(linea));
                } catch (IllegalArgumentException | DateTimeParseException e) {
                    log.warn("Línea {} de {} ignorada por formato incorrecto: {}",
                            numeroLinea, fichero.getFileName(), linea);
                }
            }
        }
        log.debug("{} eventos leídos de {}", eventos.size(), fichero.getFileName());
        return eventos;
    }

    // ---------- Conversión Evento <-> línea de texto ----------

    private String aLinea(Evento evento) {
        return evento.getFechaHora() + SEPARADOR
                + evento.getIdZona() + SEPARADOR
                + evento.getIdElemento() + SEPARADOR
                + evento.getTipo() + SEPARADOR
                + limpiar(evento.getDescripcion());
    }

    /**
     * Convierte una línea en Evento.
     * Lanza IllegalArgumentException si faltan campos o el tipo no existe,
     * y DateTimeParseException si la fecha está mal.
     */
    private Evento desdeLinea(String linea) {
        // El límite NUM_CAMPOS evita que se partan más trozos de los esperados
        String[] partes = linea.split(SEPARADOR, NUM_CAMPOS);
        if (partes.length != NUM_CAMPOS) {
            throw new IllegalArgumentException("Se esperaban " + NUM_CAMPOS + " campos");
        }
        LocalDateTime fecha = LocalDateTime.parse(partes[0]);
        TipoEvento tipo = TipoEvento.valueOf(partes[3]); // lanza IllegalArgumentException si no existe
        return new Evento(fecha, partes[1], partes[2], tipo, partes[4]);
    }

    /** Quita el separador y los saltos de línea de la descripción para no romper el formato. */
    private String limpiar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace(SEPARADOR, ",").replace("\n", " ").replace("\r", " ");
    }
}

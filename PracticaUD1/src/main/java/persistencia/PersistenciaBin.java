package persistencia;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import modelo.Lectura;
import modelo.enums.TipoRuna;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona lecturas.dat: registros de tamaño fijo (Lectura.TAMANO_REGISTRO = 25 bytes).
 *
 * Como todos los registros miden lo mismo, la posición en bytes de cualquiera se calcula:
 *   posicionEnBytes = numeroRegistro * 25
 * y con raf.seek() saltamos directamente a él sin leer los anteriores.
 *
 * Operaciones obligatorias del enunciado:
 *   - anadirLectura     -> añadir registros al final
 *   - leerLectura       -> leer uno concreto por acceso directo
 *   - modificarLectura  -> cambiar uno sin reescribir el fichero
 *
 * Organización de lecturas.dat:
 *   registros 0 a 11  -> ESTADO ACTUAL: último valor de cada runa (R01 en el 0, R12 en el 11).
 *                        Se modifican en su sitio con seek cada vez que llega una medición.
 *   registros 12 en adelante -> HISTÓRICO: todas las mediciones, añadidas al final.
 * Así se puede consultar el valor actual de una runa saltando directamente a su registro,
 * sin recorrer el histórico.
 */
public class PersistenciaBin {

    private static final Logger log = LogManager.getLogger(PersistenciaBin.class);

    /** Número de registros reservados al principio para el estado actual (uno por runa). */
    public static final int REGISTROS_ESTADO = 12;

    private final Path fichero;

    public PersistenciaBin(Path fichero) {
        this.fichero = fichero;
    }

    /**
     * Añade una lectura al final del fichero.
     * @return el número de registro donde se ha guardado (0, 1, 2...)
     */
    public int anadirLectura(Lectura lectura) throws IOException {
        // "rw" crea el fichero si no existe
        try (RandomAccessFile raf = new RandomAccessFile(fichero.toFile(), "rw")) {
            int numeroRegistro = (int) (raf.length() / Lectura.TAMANO_REGISTRO);
            raf.seek((long) numeroRegistro * Lectura.TAMANO_REGISTRO);
            escribirRegistro(raf, lectura);
            return numeroRegistro;
        }
    }

    /**
     * Lee un único registro por acceso directo, sin recorrer los anteriores.
     * @param numeroRegistro posición del registro (0 = el primero)
     */
    public Lectura leerLectura(int numeroRegistro) throws IOException {
        // "r" = solo lectura: si el fichero no existe lanza FileNotFoundException
        try (RandomAccessFile raf = new RandomAccessFile(fichero.toFile(), "r")) {
            comprobarPosicion(raf, numeroRegistro);
            raf.seek((long) numeroRegistro * Lectura.TAMANO_REGISTRO);
            return leerRegistro(raf);
        }
    }

    /**
     * Sobrescribe un registro existente. Solo se escriben esos 25 bytes;
     * el resto del fichero no se toca.
     */
    public void modificarLectura(int numeroRegistro, Lectura lectura) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(fichero.toFile(), "rw")) {
            comprobarPosicion(raf, numeroRegistro);
            raf.seek((long) numeroRegistro * Lectura.TAMANO_REGISTRO);
            escribirRegistro(raf, lectura);
        }
    }

    /** Número de registros completos que hay en el fichero. */
    public int numeroRegistros() throws IOException {
        if (Files.notExists(fichero)) {
            return 0;
        }
        try (RandomAccessFile raf = new RandomAccessFile(fichero.toFile(), "r")) {
            if (raf.length() % Lectura.TAMANO_REGISTRO != 0) {
                log.warn("{} tiene un registro incompleto al final ({} bytes sobrantes)",
                        fichero.getFileName(), raf.length() % Lectura.TAMANO_REGISTRO);
            }
            return (int) (raf.length() / Lectura.TAMANO_REGISTRO);
        }
    }

    /**
     * Lee TODAS las lecturas de principio a fin. Lo usará el analizador.
     * Aquí no hace falta acceso directo, así que se usa un DataInputStream secuencial,
     * que con el buffer es más rápido para recorrer el fichero entero.
     * Si al final hay un registro incompleto, se ignora.
     */
    public List<Lectura> leerTodas() throws IOException {
        List<Lectura> lecturas = new ArrayList<>();
        if (Files.notExists(fichero)) {
            log.warn("No existe el fichero de lecturas: {}", fichero);
            return lecturas;
        }

        int total = numeroRegistros();
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(fichero.toFile())))) {
            for (int i = 0; i < total; i++) {
                int idRuna = in.readInt();
                int tipo = in.readInt();
                double valor = in.readDouble();
                long timestamp = in.readLong();
                boolean anomala = in.readBoolean();
                try {
                    lecturas.add(new Lectura(idRuna, convertirTipo(tipo), valor, timestamp, anomala));
                } catch (IOException e) {
                    log.warn("Registro {} ignorado: {}", i, e.getMessage());
                }
            }
        } catch (EOFException e) {
            log.warn("Fin de fichero inesperado en {}", fichero.getFileName());
        }
        log.debug("{} lecturas leídas de {}", lecturas.size(), fichero.getFileName());
        return lecturas;
    }

    // ---------- Zona de estado actual (acceso directo por número de runa) ----------

    /** Lee el último valor de una runa: R01 está en el registro 0, R12 en el 11. */
    public Lectura leerEstadoRuna(int idRuna) throws IOException {
        return leerLectura(idRuna - 1);
    }

    /** Sobrescribe el último valor de la runa de esa lectura, sin tocar el resto del fichero. */
    public void actualizarEstadoRuna(Lectura lectura) throws IOException {
        modificarLectura(lectura.getIdRuna() - 1, lectura);
    }

    /** Devuelve solo el histórico (registros a partir del 12), sin la zona de estado actual. */
    public List<Lectura> leerHistorico() throws IOException {
        List<Lectura> todas = leerTodas();
        if (todas.size() <= REGISTROS_ESTADO) {
            return new ArrayList<>();
        }
        return new ArrayList<>(todas.subList(REGISTROS_ESTADO, todas.size()));
    }

    // ---------- Métodos privados ----------

    /** Escribe los 25 bytes de una lectura en la posición actual del puntero. */
    private void escribirRegistro(RandomAccessFile raf, Lectura lectura) throws IOException {
        raf.writeInt(lectura.getIdRuna());                 // 4 bytes
        raf.writeInt(lectura.getTipoRuna().ordinal());     // 4 bytes
        raf.writeDouble(lectura.getValor());               // 8 bytes
        raf.writeLong(lectura.getTimestamp());             // 8 bytes
        raf.writeBoolean(lectura.isAnomala());             // 1 byte
    }

    /** Lee los 25 bytes de una lectura desde la posición actual del puntero. */
    private Lectura leerRegistro(RandomAccessFile raf) throws IOException {
        int idRuna = raf.readInt();
        TipoRuna tipo = convertirTipo(raf.readInt());
        double valor = raf.readDouble();
        long timestamp = raf.readLong();
        boolean anomala = raf.readBoolean();
        return new Lectura(idRuna, tipo, valor, timestamp, anomala);
    }

    /** Convierte el número guardado en el fichero al enum. Falla si el número no existe. */
    private TipoRuna convertirTipo(int ordinal) throws IOException {
        TipoRuna[] tipos = TipoRuna.values();
        if (ordinal < 0 || ordinal >= tipos.length) {
            throw new IOException("Tipo de runa desconocido en el fichero: " + ordinal);
        }
        return tipos[ordinal];
    }

    /** Comprueba que el número de registro existe antes de hacer seek. */
    private void comprobarPosicion(RandomAccessFile raf, int numeroRegistro) throws IOException {
        long total = raf.length() / Lectura.TAMANO_REGISTRO;
        if (numeroRegistro < 0 || numeroRegistro >= total) {
            throw new IllegalArgumentException("El registro " + numeroRegistro
                    + " no existe (hay " + total + " registros)");
        }
    }
}

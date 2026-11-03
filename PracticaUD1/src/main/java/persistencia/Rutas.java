package persistencia;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Centraliza todas las rutas del proyecto usando NIO.2 (Path y Files).
 *
 * Todas las rutas son RELATIVAS: por defecto "datos/" y "salida/" dentro de la carpeta
 * desde la que se ejecuta el programa. En los tests se usa una carpeta temporal (@TempDir).
 * Así el proyecto funciona en cualquier ordenador sin tocar el código.
 */
public class Rutas {

    private final Path carpetaDatos;
    private final Path carpetaSalida;

    /** datos/ y salida/ en la carpeta en la que se ejecuta el programa. */
    public Rutas() {
        this(Path.of("datos"), Path.of("salida"));
    }

    /** datos/ y salida/ dentro de una carpeta base (se usa en los tests). */
    public Rutas(Path base) {
        this(base.resolve("datos"), base.resolve("salida"));
    }

    /** Carpetas concretas, por ejemplo las recibidas por argumento. */
    public Rutas(Path carpetaDatos, Path carpetaSalida) {
        this.carpetaDatos = carpetaDatos;
        this.carpetaSalida = carpetaSalida;
    }

    // ---------- Carpetas ----------

    public Path getCarpetaDatos() {
        return carpetaDatos;
    }

    public Path getCarpetaSalida() {
        return carpetaSalida;
    }

    // ---------- Ficheros del simulador (entrada del analizador) ----------

    public Path getEventos() {
        return carpetaDatos.resolve("eventos.txt");
    }

    public Path getLecturas() {
        return carpetaDatos.resolve("lecturas.dat");
    }

    public Path getCiudad() {
        return carpetaDatos.resolve("ciudad.xml");
    }

    // ---------- Ficheros generados por el analizador ----------

    public Path getInforme() {
        return carpetaSalida.resolve("informe.txt");
    }

    public Path getAnomalias() {
        return carpetaSalida.resolve("anomalias.dat");
    }

    public Path getAfectados() {
        return carpetaSalida.resolve("afectados.xml");
    }

    /**
     * Crea las carpetas de datos y salida si no existen.
     * Files.createDirectories no falla si la carpeta ya existe.
     */
    public void crearDirectorios() throws IOException {
        Files.createDirectories(carpetaDatos);
        Files.createDirectories(carpetaSalida);
    }
}

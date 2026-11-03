package persistencia;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Rutas {

    private final Path base;

    //Rutas relativas a donde se ejecuta el programa
    public Rutas() {
        this(Path.of("."));
    }

    //Rutas a partir de una carpeta concreta
    public Rutas(Path base) {
        this.base = base;
    }

    //Carpetas

    public Path getCarpetaDatos() {
        return base.resolve("datos");
    }

    public Path getCarpetaSalida() {
        return base.resolve("salida");
    }

    //Ficheros simulador

    public Path getEventos() {
        return getCarpetaDatos().resolve("eventos.txt");
    }

    public Path getLecturas() {
        return getCarpetaDatos().resolve("lecturas.dat");
    }

    public Path getCiudad() {
        return getCarpetaDatos().resolve("ciudad.json");
    }


    //Crea las carpetas datos/ y salida/ si no existen. No salta excepcion si la carpeta ya existe

    public void crearDirectorios() throws IOException {
        Files.createDirectories(getCarpetaDatos());
        Files.createDirectories(getCarpetaSalida());
    }
}

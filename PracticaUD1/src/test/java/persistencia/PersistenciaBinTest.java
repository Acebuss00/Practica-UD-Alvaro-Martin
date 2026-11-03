package persistencia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import modelo.Lectura;
import modelo.enums.TipoRuna;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistenciaBinTest {

    @TempDir
    Path carpetaTemporal;

    private Path fichero;
    private PersistenciaBin persistencia;

    @BeforeEach
    void preparar() throws IOException {
        fichero = carpetaTemporal.resolve("lecturas.dat");
        persistencia = new PersistenciaBin(fichero);

        // Tres lecturas de partida: registros 0, 1 y 2
        persistencia.anadirLectura(new Lectura(1, TipoRuna.MANA, 850.0, 1000L, false));
        persistencia.anadirLectura(new Lectura(5, TipoRuna.TEMPERATURA, 22.5, 2000L, false));
        persistencia.anadirLectura(new Lectura(9, TipoRuna.CONTAMINACION, 3.0, 3000L, false));
    }

    @Test
    void anadirColocaLosRegistrosAlFinal() throws IOException {
        int posicion = persistencia.anadirLectura(new Lectura(12, TipoRuna.DETECCION_SIGILO, 1.0, 4000L, true));

        assertEquals(3, posicion);
        assertEquals(4, persistencia.numeroRegistros());
        // 4 registros x 25 bytes = 100 bytes exactos
        assertEquals(4L * Lectura.TAMANO_REGISTRO, Files.size(fichero));
    }

    @Test
    void leeUnRegistroConcretoPorAccesoDirecto() throws IOException {
        Lectura lectura = persistencia.leerLectura(1);

        assertEquals(5, lectura.getIdRuna());
        assertEquals("R05", lectura.getCodigoRuna());
        assertEquals(TipoRuna.TEMPERATURA, lectura.getTipoRuna());
        assertEquals(22.5, lectura.getValor());
        assertEquals(2000L, lectura.getTimestamp());
        assertFalse(lectura.isAnomala());
    }

    @Test
    void modificaUnRegistroSinTocarLosDemas() throws IOException {
        long tamanoAntes = Files.size(fichero);

        persistencia.modificarLectura(1, new Lectura(5, TipoRuna.TEMPERATURA, 450.0, 2500L, true));

        Lectura modificada = persistencia.leerLectura(1);
        assertEquals(450.0, modificada.getValor());
        assertTrue(modificada.isAnomala());

        // Los vecinos siguen igual y el fichero no ha crecido
        assertEquals(850.0, persistencia.leerLectura(0).getValor());
        assertEquals(3.0, persistencia.leerLectura(2).getValor());
        assertEquals(tamanoAntes, Files.size(fichero));
    }

    @Test
    void leerUnRegistroQueNoExisteLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> persistencia.leerLectura(7));
        assertThrows(IllegalArgumentException.class, () -> persistencia.leerLectura(-1));
    }

    @Test
    void leerTodasIgnoraUnRegistroIncompletoAlFinal() throws IOException {
        // Simulamos un fichero corrupto: 10 bytes sueltos que no forman un registro
        Files.write(fichero, new byte[10], StandardOpenOption.APPEND);

        List<Lectura> todas = persistencia.leerTodas();

        assertEquals(3, todas.size());
        assertEquals(TipoRuna.CONTAMINACION, todas.get(2).getTipoRuna());
    }
}

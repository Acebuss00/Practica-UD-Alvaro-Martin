package persistencia;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import modelo.Evento;
import modelo.enums.TipoEvento;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistenciaTextoTest {

    // JUnit crea una carpeta temporal nueva para cada test y la borra al terminar
    @TempDir
    Path carpetaTemporal;

    @Test
    void escribeYRecuperaEventos() throws IOException {
        PersistenciaTexto persistencia = new PersistenciaTexto(carpetaTemporal.resolve("eventos.txt"));
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 2, 18, 45, 12);

        persistencia.escribirEvento(new Evento(fecha, "Z01", "G03",
                TipoEvento.AVISTAMIENTO, "Necrófagos junto a la puerta"));
        persistencia.escribirEvento(new Evento(fecha.plusMinutes(1), "Z02", "P01",
                TipoEvento.VIAJE_PORTAL, "Viaje a Forjaz"));

        List<Evento> eventos = persistencia.leerEventos();

        assertEquals(2, eventos.size());
        Evento primero = eventos.get(0);
        assertEquals(fecha, primero.getFechaHora());
        assertEquals("Z01", primero.getIdZona());
        assertEquals("G03", primero.getIdElemento());
        assertEquals(TipoEvento.AVISTAMIENTO, primero.getTipo());
        assertEquals("Necrófagos junto a la puerta", primero.getDescripcion());
    }

    @Test
    void elSeparadorDeLaDescripcionNoRompeElFormato() throws IOException {
        PersistenciaTexto persistencia = new PersistenciaTexto(carpetaTemporal.resolve("eventos.txt"));

        persistencia.escribirEvento(new Evento(LocalDateTime.now(), "Z03", "A01",
                TipoEvento.REPARTO_GRANO, "Grano; segunda remesa"));

        assertEquals("Grano, segunda remesa", persistencia.leerEventos().get(0).getDescripcion());
    }

    @Test
    void ignoraLineasMalFormadas() throws IOException {
        Path fichero = carpetaTemporal.resolve("eventos.txt");
        PersistenciaTexto persistencia = new PersistenciaTexto(fichero);
        persistencia.escribirEvento(new Evento(LocalDateTime.now(), "Z01", "-",
                TipoEvento.CAMBIO_TURNO, "Relevo de guardia"));

        // Metemos a mano líneas corruptas: pocos campos, fecha mala y tipo inexistente
        Files.writeString(fichero,
                "esto no es un evento\n"
                        + "ayer;Z01;-;AVISO;fecha incorrecta\n"
                        + "2026-11-02T10:00:00;Z01;-;BAILE;tipo inexistente\n",
                StandardCharsets.UTF_8, StandardOpenOption.APPEND);

        assertEquals(1, persistencia.leerEventos().size());
    }

    @Test
    void ficheroInexistenteDevuelveListaVacia() throws IOException {
        PersistenciaTexto persistencia = new PersistenciaTexto(carpetaTemporal.resolve("no-existe.txt"));
        assertTrue(persistencia.leerEventos().isEmpty());
    }
}

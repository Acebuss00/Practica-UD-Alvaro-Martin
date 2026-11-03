package simulador;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import excepciones.EventoCriticoException;
import modelo.ciudad;
import modelo.enums.EstadoElemento;
import modelo.Lectura;
import modelo.enums.TipoIncidente;
import modelo.enums.TipoRuna;
import persistencia.PersistenciaBin;
import persistencia.PersistenciaTexto;
import persistencia.PersistenciaXML;
import persistencia.Rutas;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimuladorTest {

    private static final long SEMILLA = 42;

    @TempDir
    Path carpetaTemporal;

    @Test
    void elIncidenteProvocaUnaSituacionCritica() {
        Simulador simulador = new Simulador(new Rutas(carpetaTemporal), SEMILLA);

        EventoCriticoException e = assertThrows(EventoCriticoException.class,
                () -> simulador.ejecutarIncidente(TipoIncidente.COLAPSO_BARRERA, 20));

        assertEquals("Z04", e.getIdZona());
        assertTrue(e.getMessage().contains("R10"));
    }

    @Test
    void losDatosYaEstanEscritosCuandoSaltaLaExcepcion() throws IOException {
        Rutas rutas = new Rutas(carpetaTemporal);
        Simulador simulador = new Simulador(rutas, SEMILLA);

        assertThrows(EventoCriticoException.class,
                () -> simulador.ejecutarIncidente(TipoIncidente.COLAPSO_BARRERA, 20));

        // TXT: el aviso del cristal está en el histórico
        assertTrue(new PersistenciaTexto(rutas.getEventos()).leerEventos().stream()
                .anyMatch(ev -> ev.getDescripcion().contains("Recarga fallida")));

        // BIN: el registro de estado de R10 tiene el último valor (0 de maná), leído por acceso directo
        Lectura estadoR10 = new PersistenciaBin(rutas.getLecturas()).leerEstadoRuna(10);
        assertEquals(TipoRuna.MANA, estadoR10.getTipoRuna());
        assertEquals(0.0, estadoR10.getValor());
        assertTrue(estadoR10.isAnomala());

        // XML: la barrera está desactivada
        ciudad ciudad = new PersistenciaXML(rutas.getCiudad()).cargarCiudad();
        assertEquals(EstadoElemento.DESACTIVADA, ciudad.buscarElemento("BA01").getEstado());
    }

    @Test
    void laOperativaNormalNoProduceAnomalias() throws IOException {
        Rutas rutas = new Rutas(carpetaTemporal);
        Simulador simulador = new Simulador(rutas, SEMILLA);

        assertDoesNotThrow(() -> simulador.ejecutarNormal(30));

        List<Lectura> historico = new PersistenciaBin(rutas.getLecturas()).leerHistorico();
        assertEquals(30 * CiudadInicial.NUM_RUNAS, historico.size());
        assertFalse(historico.stream().anyMatch(Lectura::isAnomala));
        assertEquals(30, Files.readAllLines(rutas.getEventos()).size());
    }
}

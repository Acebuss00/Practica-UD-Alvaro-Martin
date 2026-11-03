package integracion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import analizador.AnalizadorForense;
import excepciones.EventoCriticoException;
import modelo.Lectura;
import modelo.enums.TipoIncidente;
import modelo.enums.TipoRuna;
import persistencia.PersistenciaBin;
import persistencia.Rutas;
import simulador.Simulador;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Flujo completo: datos -> incidente -> persistencia -> análisis -> salidas.
 */
class IntegracionTest {

    @TempDir
    Path carpetaTemporal;

    @Test
    void flujoCompletoGranoContaminado() throws Exception {
        Rutas rutas = new Rutas(carpetaTemporal);

        // 1-3. El simulador trabaja con normalidad, provoca el incidente y lo persiste
        assertThrows(EventoCriticoException.class,
                () -> new Simulador(rutas, 2026).ejecutarIncidente(TipoIncidente.GRANO_CONTAMINADO, 15));
        assertTrue(Files.exists(rutas.getEventos()));
        assertTrue(Files.exists(rutas.getLecturas()));
        assertTrue(Files.exists(rutas.getCiudad()));

        // 4. El analizador trabaja solo con la carpeta de datos
        new AnalizadorForense(rutas).analizar();

        // 5. Las tres salidas existen y cada una tiene su parte del resultado
        String informe = Files.readString(rutas.getInforme());
        assertTrue(informe.contains("GRANO_CONTAMINADO"));
        assertTrue(informe.contains("CONCLUSIÓN"));

        List<Lectura> anomalas = new PersistenciaBin(rutas.getAnomalias()).leerTodas();
        assertEquals(3, anomalas.size());
        assertTrue(anomalas.stream().allMatch(l -> l.getTipoRuna() == TipoRuna.CONTAMINACION));
        assertEquals(0, Files.size(rutas.getAnomalias()) % Lectura.TAMANO_REGISTRO);

        String afectados = Files.readString(rutas.getAfectados());
        assertTrue(afectados.contains("id=\"Z03\""));
        assertTrue(afectados.contains("CONTAMINADO"));
        assertFalse(afectados.contains("id=\"Z01\""));   // solo la zona afectada, no la ciudad entera
    }
}

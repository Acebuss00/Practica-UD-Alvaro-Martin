package analizador;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import excepciones.EventoCriticoException;
import modelo.enums.TipoIncidente;
import persistencia.Rutas;
import simulador.Simulador;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DetectorIncidentesTest {

    @TempDir
    Path carpetaTemporal;

    /**
     * Un caso por cada incidente: el simulador lo provoca con una semilla fija
     * y el analizador, leyendo solo los ficheros, debe identificar el mismo.
     */
    @ParameterizedTest
    @EnumSource(value = TipoIncidente.class, mode = EnumSource.Mode.EXCLUDE, names = "INDETERMINADO")
    void identificaCadaIncidente(TipoIncidente incidente) throws IOException {
        Rutas rutas = new Rutas(carpetaTemporal);
        assertThrows(EventoCriticoException.class,
                () -> new Simulador(rutas, 7).ejecutarIncidente(incidente, 20));

        ResultadoAnalisis resultado = new AnalizadorForense(rutas).analizar();

        assertEquals(incidente, resultado.getIncidente());
        assertNotNull(resultado.getZona());
        assertFalse(resultado.getLecturasAnomalas().isEmpty());
        assertFalse(resultado.getElementosAfectados().isEmpty());
    }

    @Test
    void sinIncidenteElResultadoEsIndeterminado() throws Exception {
        Rutas rutas = new Rutas(carpetaTemporal);
        new Simulador(rutas, 7).ejecutarNormal(25);

        ResultadoAnalisis resultado = new AnalizadorForense(rutas).analizar();

        assertEquals(TipoIncidente.INDETERMINADO, resultado.getIncidente());
    }
}

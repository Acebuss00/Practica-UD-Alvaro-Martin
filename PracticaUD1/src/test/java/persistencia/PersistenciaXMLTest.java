package persistencia;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import modelo.ciudad;
import modelo.Elemento;
import modelo.enums.EstadoElemento;
import modelo.enums.EstadoZona;
import modelo.enums.FaccionPersonaje;
import modelo.Personaje;
import modelo.enums.TipoElemento;
import modelo.Zona;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistenciaXMLTest {

    @TempDir
    Path carpetaTemporal;

    /** ciudad pequeña de prueba: dos zonas y un personaje. */
    private ciudad crearCiudadDePrueba() {
        ciudad ciudad = new ciudad("Ventormenta");

        Zona puerta = new Zona("Z01", "Puerta principal", false, EstadoZona.NORMAL);
        puerta.getElementos().add(new Elemento("G01", TipoElemento.GUARDIA, EstadoElemento.OPERATIVO));
        puerta.getElementos().add(new Elemento("R01", TipoElemento.RUNA, EstadoElemento.OPERATIVO));

        Zona fortaleza = new Zona("Z04", "Fortaleza", true, EstadoZona.NORMAL);
        fortaleza.getElementos().add(new Elemento("C01", TipoElemento.CRISTAL, EstadoElemento.OPERATIVO));
        fortaleza.getElementos().add(new Elemento("R05", TipoElemento.RUNA, EstadoElemento.OPERATIVO));

        ciudad.getZonas().add(puerta);
        ciudad.getZonas().add(fortaleza);

        Personaje jaina = new Personaje("PJ001", "Jaina", FaccionPersonaje.ALIANZA);
        jaina.getZonasPermitidas().add("Z01");
        jaina.getZonasPermitidas().add("Z04");
        ciudad.getPersonajes().add(jaina);

        return ciudad;
    }

    @Test
    void guardaYCargaLaCiudad() throws IOException {
        PersistenciaXML persistencia = new PersistenciaXML(carpetaTemporal.resolve("ciudad.xml"));

        persistencia.guardarCiudad(crearCiudadDePrueba());
        ciudad cargada = persistencia.cargarCiudad();

        assertEquals("Ventormenta", cargada.getNombre());
        assertEquals(2, cargada.getZonas().size());
        Zona fortaleza = cargada.buscarZona("Z04");
        assertEquals("Fortaleza", fortaleza.getNombre());
        assertTrue(fortaleza.isRestringida());
        assertEquals(2, fortaleza.getElementos().size());
        assertEquals(TipoElemento.CRISTAL, fortaleza.getElementos().get(0).getTipo());

        Personaje jaina = cargada.buscarPersonaje("PJ001");
        assertEquals(FaccionPersonaje.ALIANZA, jaina.getFaccion());
        assertTrue(jaina.puedeEntrarEn("Z04"));
    }

    @Test
    void losCambiosDeEstadoSeConservan() throws IOException {
        PersistenciaXML persistencia = new PersistenciaXML(carpetaTemporal.resolve("ciudad.xml"));
        ciudad ciudad = crearCiudadDePrueba();
        persistencia.guardarCiudad(ciudad);

        // Simulamos un incidente: la barrera de la fortaleza cae
        ciudad.buscarElemento("C01").setEstado(EstadoElemento.VACIO);
        ciudad.buscarZona("Z04").setEstado(EstadoZona.EMERGENCIA);
        persistencia.guardarCiudad(ciudad);

        ciudad cargada = persistencia.cargarCiudad();
        assertEquals(EstadoElemento.VACIO, cargada.buscarElemento("C01").getEstado());
        assertEquals(EstadoZona.EMERGENCIA, cargada.buscarZona("Z04").getEstado());
    }

    @Test
    void relacionaUnaRunaConSuZona() throws IOException {
        PersistenciaXML persistencia = new PersistenciaXML(carpetaTemporal.resolve("ciudad.xml"));
        persistencia.guardarCiudad(crearCiudadDePrueba());
        ciudad cargada = persistencia.cargarCiudad();

        // "R05" es lo que devuelve Lectura.getCodigoRuna() para idRuna = 5
        assertEquals("Z04", cargada.buscarZonaDeElemento("R05").getId());
        assertNull(cargada.buscarZonaDeElemento("R99"));
    }

    @Test
    void ficheroInexistenteLanzaExcepcion() {
        PersistenciaXML persistencia = new PersistenciaXML(carpetaTemporal.resolve("no-existe.xml"));
        assertThrows(NoSuchFileException.class, persistencia::cargarCiudad);
    }

    @Test
    void xmlConEstadoInventadoLanzaExcepcion() throws IOException {
        Path fichero = carpetaTemporal.resolve("ciudad.xml");
        Files.writeString(fichero, """
                <ciudad nombre="Ventormenta">
                  <zonas>
                    <zona id="Z01" restringida="false" estado="FIESTA">
                      <nombre>Puerta</nombre>
                      <elementos/>
                    </zona>
                  </zonas>
                  <personajes/>
                </ciudad>
                """);

        assertThrows(IOException.class, new PersistenciaXML(fichero)::cargarCiudad);
    }

    @Test
    void xmlMalFormadoLanzaExcepcion() throws IOException {
        Path fichero = carpetaTemporal.resolve("ciudad.xml");
        Files.writeString(fichero, "<ciudad nombre=\"Ventormenta\"><zonas>");   // sin cerrar

        assertThrows(IOException.class, new PersistenciaXML(fichero)::cargarCiudad);
    }
}

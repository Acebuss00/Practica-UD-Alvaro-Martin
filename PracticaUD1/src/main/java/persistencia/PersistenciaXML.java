package persistencia;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import modelo.ciudad;
import modelo.Elemento;
import modelo.enums.EstadoElemento;
import modelo.enums.EstadoZona;
import modelo.enums.FaccionPersonaje;
import modelo.Personaje;
import modelo.enums.TipoElemento;
import modelo.Zona;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Guarda y carga ciudad.xml con DOM (modelo de árbol).
 *
 * Se usa DOM porque el simulador necesita tener la ciudad entera en memoria,
 * cambiar el estado de zonas y elementos y volver a guardarla. DOM permite leer,
 * recorrer, modificar y escribir el árbol completo. SAX solo sirve para leer
 * secuencialmente y no permite escribir.
 *
 * Estructura del fichero:
 * <pre>
 * &lt;ciudad nombre="Ventormenta"&gt;
 *   &lt;zonas&gt;
 *     &lt;zona id="Z01" restringida="false" estado="NORMAL"&gt;
 *       &lt;nombre&gt;Puerta principal&lt;/nombre&gt;
 *       &lt;elementos&gt;
 *         &lt;elemento id="G01" tipo="GUARDIA" estado="OPERATIVO"/&gt;
 *       &lt;/elementos&gt;
 *     &lt;/zona&gt;
 *   &lt;/zonas&gt;
 *   &lt;personajes&gt;
 *     &lt;personaje id="PJ001" faccion="ALIANZA"&gt;
 *       &lt;nombre&gt;Jaina&lt;/nombre&gt;
 *       &lt;zonaPermitida&gt;Z01&lt;/zonaPermitida&gt;
 *     &lt;/personaje&gt;
 *   &lt;/personajes&gt;
 * &lt;/ciudad&gt;
 * </pre>
 * Los ids y estados van como atributos (son datos cortos que identifican);
 * los nombres y listas van como elementos hijos.
 */
public class PersistenciaXML {

    private static final Logger log = LogManager.getLogger(PersistenciaXML.class);

    private final Path fichero;

    public PersistenciaXML(Path fichero) {
        this.fichero = fichero;
    }

    // =====================================================================
    // ESCRITURA: objetos Java -> árbol DOM -> fichero
    // =====================================================================

    /**
     * Guarda la ciudad completa en el fichero (sustituye su contenido anterior).
     */
    public void guardarCiudad(ciudad ciudad) throws IOException {
        try {
            // 1. Crear un documento vacío
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            // 2. Construir el árbol
            Element raiz = doc.createElement("ciudad");
            raiz.setAttribute("nombre", ciudad.getNombre());
            doc.appendChild(raiz);

            Element zonasXml = doc.createElement("zonas");
            raiz.appendChild(zonasXml);
            for (Zona zona : ciudad.getZonas()) {
                zonasXml.appendChild(crearNodoZona(doc, zona));
            }

            Element personajesXml = doc.createElement("personajes");
            raiz.appendChild(personajesXml);
            for (Personaje personaje : ciudad.getPersonajes()) {
                personajesXml.appendChild(crearNodoPersonaje(doc, personaje));
            }

            // 3. Escribir el árbol en el fichero
            escribirDocumento(doc, fichero);
            log.debug("Ciudad guardada en {}", fichero.getFileName());

        } catch (ParserConfigurationException e) {
            // La convertimos en IOException para que quien llame solo tenga que tratar un tipo
            throw new IOException("No se pudo crear el documento XML", e);
        }
    }

    // =====================================================================
    // UTILIDADES DOM (también las usa el analizador para escribir afectados.xml)
    // =====================================================================

    /** Crea un documento DOM vacío. */
    public static Document nuevoDocumento() throws IOException {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new IOException("No se pudo crear el documento XML", e);
        }
    }

    /**
     * Escribe un árbol DOM en un fichero con un Transformer.
     * Primero escribe en un fichero temporal y luego lo mueve encima del real (NIO.2),
     * para que el destino nunca quede a medias si el programa se cae escribiendo.
     */
    public static void escribirDocumento(Document doc, Path destino) throws IOException {
        Path temporal = destino.resolveSibling(destino.getFileName() + ".tmp");
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(new DOMSource(doc), new StreamResult(temporal.toFile()));
        } catch (TransformerException e) {
            throw new IOException("No se pudo escribir " + destino.getFileName(), e);
        }
        Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
    }

    private Element crearNodoZona(Document doc, Zona zona) {
        Element zonaXml = doc.createElement("zona");
        zonaXml.setAttribute("id", zona.getId());
        zonaXml.setAttribute("restringida", String.valueOf(zona.isRestringida()));
        zonaXml.setAttribute("estado", zona.getEstado().name());

        zonaXml.appendChild(crearNodoTexto(doc, "nombre", zona.getNombre()));

        Element elementosXml = doc.createElement("elementos");
        for (Elemento elemento : zona.getElementos()) {
            Element elementoXml = doc.createElement("elemento");
            elementoXml.setAttribute("id", elemento.getId());
            elementoXml.setAttribute("tipo", elemento.getTipo().name());
            elementoXml.setAttribute("estado", elemento.getEstado().name());
            elementosXml.appendChild(elementoXml);
        }
        zonaXml.appendChild(elementosXml);
        return zonaXml;
    }

    private Element crearNodoPersonaje(Document doc, Personaje personaje) {
        Element personajeXml = doc.createElement("personaje");
        personajeXml.setAttribute("id", personaje.getId());
        personajeXml.setAttribute("faccion", personaje.getFaccion().name());
        personajeXml.appendChild(crearNodoTexto(doc, "nombre", personaje.getNombre()));
        for (String idZona : personaje.getZonasPermitidas()) {
            personajeXml.appendChild(crearNodoTexto(doc, "zonaPermitida", idZona));
        }
        return personajeXml;
    }

    /** Crea un elemento con texto dentro: <etiqueta>texto</etiqueta> */
    private Element crearNodoTexto(Document doc, String etiqueta, String texto) {
        Element nodo = doc.createElement(etiqueta);
        nodo.setTextContent(texto);
        return nodo;
    }

    // =====================================================================
    // LECTURA: fichero -> árbol DOM -> objetos Java
    // =====================================================================

    /**
     * Carga la ciudad desde el XML.
     * @throws NoSuchFileException si el fichero no existe
     * @throws IOException si el XML está mal formado, le falta algún atributo
     *                     o tiene un valor que no existe en los enums
     */
    public ciudad cargarCiudad() throws IOException {
        if (Files.notExists(fichero)) {
            throw new NoSuchFileException(fichero.toString());
        }
        try {
            // 1. Parsear el fichero entero y obtener el árbol
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            // Sin esto el parser escribe "[Fatal Error]" directamente en la consola;
            // así el error solo llega como excepción y lo registramos con Log4j
            builder.setErrorHandler(new DefaultHandler());
            Document doc = builder.parse(fichero.toFile());
            doc.getDocumentElement().normalize();

            // 2. Recorrer el árbol y crear los objetos
            Element raiz = doc.getDocumentElement();
            ciudad ciudad = new ciudad(atributoObligatorio(raiz, "nombre"));

            NodeList zonas = raiz.getElementsByTagName("zona");
            for (int i = 0; i < zonas.getLength(); i++) {
                ciudad.getZonas().add(leerZona((Element) zonas.item(i)));
            }

            NodeList personajes = raiz.getElementsByTagName("personaje");
            for (int i = 0; i < personajes.getLength(); i++) {
                ciudad.getPersonajes().add(leerPersonaje((Element) personajes.item(i)));
            }

            log.debug("Ciudad cargada: {} zonas, {} personajes",
                    ciudad.getZonas().size(), ciudad.getPersonajes().size());
            return ciudad;

        } catch (SAXException | IllegalArgumentException e) {
            // SAXException: XML mal formado
            // IllegalArgumentException: valueOf() con un estado que no existe, o atributo que falta
            log.error("{} tiene un formato incorrecto: {}", fichero.getFileName(), e.getMessage());
            throw new IOException("Formato incorrecto en " + fichero.getFileName() + ": " + e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            throw new IOException("No se pudo crear el parser XML", e);
        }
    }

    private Zona leerZona(Element zonaXml) {
        Zona zona = new Zona(
                atributoObligatorio(zonaXml, "id"),
                textoDeHijo(zonaXml, "nombre"),
                Boolean.parseBoolean(zonaXml.getAttribute("restringida")),
                EstadoZona.valueOf(atributoObligatorio(zonaXml, "estado")));

        NodeList elementos = zonaXml.getElementsByTagName("elemento");
        for (int i = 0; i < elementos.getLength(); i++) {
            Element elementoXml = (Element) elementos.item(i);
            zona.getElementos().add(new Elemento(
                    atributoObligatorio(elementoXml, "id"),
                    TipoElemento.valueOf(atributoObligatorio(elementoXml, "tipo")),
                    EstadoElemento.valueOf(atributoObligatorio(elementoXml, "estado"))));
        }
        return zona;
    }

    private Personaje leerPersonaje(Element personajeXml) {
        Personaje personaje = new Personaje(
                atributoObligatorio(personajeXml, "id"),
                textoDeHijo(personajeXml, "nombre"),
                FaccionPersonaje.valueOf(atributoObligatorio(personajeXml, "faccion")));

        NodeList permitidas = personajeXml.getElementsByTagName("zonaPermitida");
        for (int i = 0; i < permitidas.getLength(); i++) {
            personaje.getZonasPermitidas().add(permitidas.item(i).getTextContent().trim());
        }
        return personaje;
    }

    /**
     * Devuelve un atributo y falla si no está.
     * getAttribute() devuelve "" (no null) cuando el atributo no existe, por eso se comprueba isEmpty().
     */
    private String atributoObligatorio(Element nodo, String nombre) {
        String valor = nodo.getAttribute(nombre);
        if (valor.isEmpty()) {
            throw new IllegalArgumentException("Falta el atributo '" + nombre + "' en <" + nodo.getTagName() + ">");
        }
        return valor;
    }

    /** Devuelve el texto del primer hijo con esa etiqueta, o "" si no existe. */
    private String textoDeHijo(Element padre, String etiqueta) {
        NodeList hijos = padre.getElementsByTagName(etiqueta);
        if (hijos.getLength() == 0) {
            return "";
        }
        return hijos.item(0).getTextContent().trim();
    }
}

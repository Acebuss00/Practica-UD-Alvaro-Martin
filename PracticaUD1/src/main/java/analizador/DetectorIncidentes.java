package analizador;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import modelo.ciudad;
import modelo.Elemento;
import modelo.enums.EstadoElemento;
import modelo.enums.EstadoZona;
import modelo.Evento;
import modelo.enums.FaccionPersonaje;
import modelo.Lectura;
import modelo.Personaje;
import modelo.enums.TipoElemento;
import modelo.enums.TipoEvento;
import modelo.enums.TipoIncidente;
import modelo.enums.TipoRuna;
import modelo.Zona;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Identifica el incidente relacionando las tres fuentes de datos.
 *
 * Un incidente se reconoce en una zona solo si aparecen A LA VEZ:
 *   BIN -> lecturas fuera de rango del tipo de runa del incidente, en runas de esa zona
 *          (la zona de cada runa se obtiene del XML: "R10" está dentro de Z04)
 *   TXT -> acontecimientos de ese tipo en esa zona, referidos a un elemento del tipo esperado
 *          (el tipo del elemento también se obtiene del XML)
 *   XML -> elementos de esa zona en alguno de los estados de daño del incidente
 * Si ninguna regla se cumple, el resultado es INDETERMINADO.
 */
public class DetectorIncidentes {

    private static final Logger log = LogManager.getLogger(DetectorIncidentes.class);

    /**
     * Condiciones de un incidente.
     * Un record es una clase de solo datos: Java crea el constructor y los getters
     * (incidente(), runa()...) automáticamente.
     *
     * @param elementoTxt tipo del elemento al que debe referirse el evento del TXT.
     *                    Si es null, el evento debe ser el acceso de un personaje no autorizado.
     */
    private record Regla(TipoIncidente incidente, TipoRuna runa, TipoEvento eventoTxt,
                         TipoElemento elementoTxt, Set<EstadoElemento> estadosXml) {
    }

    private static final List<Regla> REGLAS = List.of(
            new Regla(TipoIncidente.INCURSION_PLAGA, TipoRuna.CORRUPCION_NECROTICA,
                    TipoEvento.AVISTAMIENTO, TipoElemento.GUARDIA, Set.of(EstadoElemento.CAIDO, EstadoElemento.BRECHA)),
            new Regla(TipoIncidente.PORTAL_INESTABLE, TipoRuna.ENERGIA_ARCANA,
                    TipoEvento.AVISO, TipoElemento.PORTAL, Set.of(EstadoElemento.INESTABLE)),
            new Regla(TipoIncidente.COLAPSO_BARRERA, TipoRuna.MANA,
                    TipoEvento.AVISO, TipoElemento.CRISTAL, Set.of(EstadoElemento.VACIO, EstadoElemento.DESACTIVADA)),
            new Regla(TipoIncidente.ATAQUE_DRAGON, TipoRuna.TEMPERATURA,
                    TipoEvento.AVISTAMIENTO, TipoElemento.GUARDIA, Set.of(EstadoElemento.EN_LLAMAS, EstadoElemento.DESTRUIDO)),
            new Regla(TipoIncidente.INFILTRACION_HORDA, TipoRuna.DETECCION_SIGILO,
                    TipoEvento.ACCESO, null, Set.of(EstadoElemento.COMPROMETIDA)),
            new Regla(TipoIncidente.GRANO_CONTAMINADO, TipoRuna.CONTAMINACION,
                    TipoEvento.AVISO, TipoElemento.ALMACEN, Set.of(EstadoElemento.CONTAMINADO))
    );

    /**
     * Evalúa todas las reglas en todas las zonas y se queda con la que tenga más
     * lecturas anómalas que la respalden.
     */
    public ResultadoAnalisis detectar(List<Evento> eventos, List<Lectura> lecturas, ciudad ciudad) {
        ResultadoAnalisis mejor = null;

        for (Regla regla : REGLAS) {
            for (Zona zona : ciudad.getZonas()) {
                ResultadoAnalisis candidato = evaluar(regla, zona, eventos, lecturas, ciudad);
                if (candidato != null && (mejor == null
                        || candidato.getLecturasAnomalas().size() > mejor.getLecturasAnomalas().size())) {
                    mejor = candidato;
                }
            }
        }

        if (mejor == null) {
            log.info("Ninguna regla se cumple en las tres fuentes: resultado INDETERMINADO");
            mejor = new ResultadoAnalisis();
            mejor.getEfectos().add("No se han encontrado evidencias coincidentes en TXT, BIN y XML.");
            // Aunque no se identifique el incidente, se conservan las lecturas anómalas que haya
            for (Lectura l : lecturas) {
                if (!l.getTipoRuna().enRango(l.getValor())) {
                    mejor.getLecturasAnomalas().add(l);
                }
            }
        }
        mejor.setEventosLeidos(eventos.size());
        mejor.setLecturasLeidas(lecturas.size());
        mejor.setZonasLeidas(ciudad.getZonas().size());
        return mejor;
    }

    /** Devuelve el resultado si la regla se cumple en esa zona, o null si no. */
    private ResultadoAnalisis evaluar(Regla regla, Zona zona, List<Evento> eventos,
                                      List<Lectura> lecturas, ciudad ciudad) {
        // 1. BIN: lecturas fuera de rango del tipo de la regla en runas de esta zona
        List<Lectura> anomalas = new ArrayList<>();
        for (Lectura l : lecturas) {
            Zona zonaRuna = ciudad.buscarZonaDeElemento(l.getCodigoRuna());
            if (l.getTipoRuna() == regla.runa() && !regla.runa().enRango(l.getValor())
                    && zonaRuna != null && zonaRuna.getId().equals(zona.getId())) {
                anomalas.add(l);
            }
        }
        if (anomalas.isEmpty()) {
            return null;
        }

        // 2. TXT: acontecimientos del tipo esperado en esta zona
        List<Evento> relevantes = new ArrayList<>();
        List<Personaje> intrusos = new ArrayList<>();
        for (Evento e : eventos) {
            if (e.getTipo() != regla.eventoTxt() || !e.getIdZona().equals(zona.getId())) {
                continue;
            }
            if (regla.elementoTxt() != null) {
                Elemento elemento = ciudad.buscarElemento(e.getIdElemento());
                if (elemento != null && elemento.getTipo() == regla.elementoTxt()) {
                    relevantes.add(e);
                }
            } else {
                Personaje p = ciudad.buscarPersonaje(e.getIdElemento());
                if (p != null && (p.getFaccion() == FaccionPersonaje.HORDA || !p.puedeEntrarEn(zona.getId()))) {
                    relevantes.add(e);
                    if (!intrusos.contains(p)) {
                        intrusos.add(p);
                    }
                }
            }
        }
        if (relevantes.isEmpty()) {
            log.debug("{} en {}: hay lecturas anómalas pero no eventos que lo confirmen", regla.incidente(), zona.getId());
            return null;
        }

        // 3. XML: elementos de la zona en un estado de daño del incidente
        boolean danoConfirmado = false;
        for (Elemento e : zona.getElementos()) {
            if (regla.estadosXml().contains(e.getEstado())) {
                danoConfirmado = true;
            }
        }
        if (!danoConfirmado) {
            log.debug("{} en {}: falta el daño en la estructura XML", regla.incidente(), zona.getId());
            return null;
        }

        log.info("Regla {} cumplida en {}: {} lecturas anómalas, {} eventos relacionados",
                regla.incidente(), zona.getId(), anomalas.size(), relevantes.size());
        return construirResultado(regla, zona, anomalas, relevantes, intrusos);
    }

    private ResultadoAnalisis construirResultado(Regla regla, Zona zona, List<Lectura> anomalas,
                                                 List<Evento> relevantes, List<Personaje> intrusos) {
        ResultadoAnalisis r = new ResultadoAnalisis();
        r.setIncidente(regla.incidente());
        r.setZona(zona);
        r.getLecturasAnomalas().addAll(anomalas);
        r.getEventosRelevantes().addAll(relevantes);
        r.getPersonajesImplicados().addAll(intrusos);

        // Efectos: todo lo que no está operativo en la zona
        if (zona.getEstado() != EstadoZona.NORMAL) {
            r.getEfectos().add("Zona " + zona.getId() + " (" + zona.getNombre() + ") en estado " + zona.getEstado());
        }
        for (Elemento e : zona.getElementos()) {
            if (e.getEstado() != EstadoElemento.OPERATIVO) {
                r.getElementosAfectados().add(e);
                r.getEfectos().add(nombreTipo(e.getTipo()) + " " + e.getId() + ": " + e.getEstado());
            }
        }
        for (Personaje p : intrusos) {
            r.getEfectos().add("Acceso no autorizado de " + p.getId() + " (" + p.getNombre() + ", "
                    + p.getFaccion() + ") a " + zona.getId());
        }

        // El valor más extremo es el que más se aleja del rango normal (por arriba o por abajo)
        double peor = anomalas.get(0).getValor();
        for (Lectura l : anomalas) {
            if (distanciaAlRango(regla.runa(), l.getValor()) > distanciaAlRango(regla.runa(), peor)) {
                peor = l.getValor();
            }
        }
        r.getEfectos().add(String.format("%d lecturas de %s fuera de rango (normal %.0f-%.0f %s, valor más extremo %.1f)",
                anomalas.size(), regla.runa(), regla.runa().getMinimo(), regla.runa().getMaximo(),
                regla.runa().getUnidad(), peor));
        return r;
    }

    private double distanciaAlRango(TipoRuna tipo, double valor) {
        if (valor < tipo.getMinimo()) {
            return tipo.getMinimo() - valor;
        }
        return Math.max(0, valor - tipo.getMaximo());
    }

    private String nombreTipo(TipoElemento tipo) {
        String nombre = tipo.name().toLowerCase();
        return Character.toUpperCase(nombre.charAt(0)) + nombre.substring(1);
    }
}

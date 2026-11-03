# Practica-UD-Alvaro-Martin · Sistema de defensa de Ventormenta

Repositorio creado para la actividad de la Unidad 1, realizado por Álvaro Martín para la asignatura Acceso a Datos de DAM2.

**Repositorio:** https://github.com/Acebuss00/Practica-UD-Alvaro-Martin

Aplicación Java con dos programas independientes, ambientada en World of Warcraft:

- **Simulador**: representa la operativa diaria de la defensa de Ventormenta (guardias, portales, cristales de maná, almacenes, accesos de personajes y runas de vigilancia que miden lo que ocurre en cada zona). Escribe y actualiza tres ficheros de datos y puede provocar uno de los seis incidentes previstos.
- **Analizador forense**: lee únicamente esos tres ficheros, relaciona su información, identifica qué incidente ha ocurrido y genera sus propios tres ficheros de resultado.

## Requisitos

- Java 17 o superior
- Maven 3.8 o superior (o IntelliJ IDEA, que lo trae integrado)
- Dependencias (las descarga Maven): Log4j 2 (`log4j-api`, `log4j-core`) y JUnit 5 (`junit-jupiter`). El XML se procesa con DOM, incluido en Java.

## Estructura

El proyecto Maven está en la carpeta `PracticaUD1/`. **Todos los comandos se ejecutan desde ahí.**

```
Practica-UD-Alvaro-Martin/
├── README.md
├── IA.md
└── PracticaUD1/
    ├── pom.xml
    ├── src/main/java/
    │   ├── modelo/          ciudad, Zona, Elemento, Personaje, Evento, Lectura
    │   ├── modelo/enums/    EstadoZona, EstadoElemento, TipoElemento, TipoEvento, TipoRuna, FaccionPersonaje, TipoIncidente
    │   ├── persistencia/    Rutas, PersistenciaTexto, PersistenciaBin, PersistenciaXML
    │   ├── excepciones/     EventoCriticoException
    │   ├── simulador/       Simulador (main), Escenario, MotorIncidentes, CiudadInicial
    │   └── analizador/      AnalizadorForense (main), DetectorIncidentes, ResultadoAnalisis, GeneradorSalidas
    ├── src/main/resources/log4j2.xml       configuración de Log4j 2
    ├── src/test/                           pruebas JUnit 5 (+ log4j2-test.xml)
    ├── datos/        ficheros generados por el simulador
    ├── salida/       ficheros generados por el analizador
    ├── logs/runtime/      log rotativo (aplicacion.log + históricos .gz)
    └── logs/evidencias/   un log por ejecución con fecha y hora
```

## Ficheros de datos y cómo se relacionan

| Fichero | Tecnología | Qué guarda |
|---|---|---|
| `datos/eventos.txt` | java.io (`BufferedWriter`/`BufferedReader`) | Histórico de acontecimientos: `fecha;zona;elemento;tipo;descripción` |
| `datos/lecturas.dat` | `RandomAccessFile` | Mediciones de las 12 runas en registros fijos de 25 bytes |
| `datos/ciudad.xml` | DOM | Estructura de la ciudad: zonas, elementos con su estado, personajes y permisos |

**Registro binario (25 bytes):** `idRuna int(4) · tipoRuna int(4) · valor double(8) · timestamp long(8) · anómala boolean(1)`.
Los registros 0 a 11 son el **estado actual** de cada runa (R01 en el 0 … R12 en el 11) y se **modifican** en su sitio con `seek(n × 25)` en cada medición. A partir del registro 12 se **añade** el histórico. El simulador y el analizador **leen** el estado de una runa concreta por acceso directo.

**Relación entre ficheros:**

- Los ids (`Z01`, `R10`, `C01`, `PJ099`…) unen las tres fuentes. Una lectura de la runa 10 del binario se sitúa en la zona Z04 porque en `ciudad.xml` el elemento `R10` está dentro de Z04.
- En el TXT, el campo elemento contiene el id de un elemento del XML o, en los accesos, el id de un personaje. El analizador cruza ese id con el XML para saber su tipo, su facción y sus permisos.
- La marca temporal del ciclo es la misma en el TXT y en el BIN.

## Incidentes

| Incidente | Zona | TXT | BIN | XML |
|---|---|---|---|---|
| `INCURSION_PLAGA` | Z01 Puerta principal | Avistamientos de no-muertos por los guardias | `CORRUPCION_NECROTICA` > 20 % en R01 | Guardias `CAIDO`, puerta `BRECHA` |
| `PORTAL_INESTABLE` | Z02 Distrito de los Magos | Avisos de interferencias y viajes fallidos | `ENERGIA_ARCANA` fuera de 200–700 en R04 | Portales `INESTABLE`, mago `AGOTADO` |
| `COLAPSO_BARRERA` | Z04 Fortaleza | Recargas fallidas de cristales | `MANA` < 500 en R10 | Cristales `VACIO`, barrera `DESACTIVADA` |
| `ATAQUE_DRAGON` | Z03 Casco Antiguo | Avistamiento de un dragón | `TEMPERATURA` > 40 ºC en R08 | Almacenes `EN_LLAMAS` / `DESTRUIDO` |
| `INFILTRACION_HORDA` | Z04 Fortaleza | Accesos de un personaje sin permiso | `DETECCION_SIGILO` > 10 % en R12 | Personaje de la `HORDA`, puerta `COMPROMETIDA` |
| `GRANO_CONTAMINADO` | Z03 Casco Antiguo | Ciudadanos enfermos tras un reparto | `CONTAMINACION` > 15 ppm en R07 | Almacenes `CONTAMINADO`, zona `CUARENTENA` |

Cada incidente evoluciona en 4 ciclos. Cuando una runa acumula **3 lecturas seguidas fuera de rango**, el simulador lanza `EventoCriticoException`. En ese momento los datos ya están escritos en los tres ficheros. El `catch` lo registra con Log4j y termina de forma controlada.

El analizador reconoce un incidente solo si en la misma zona coinciden las tres fuentes. Si no coinciden, el resultado es `INDETERMINADO`.

## Ficheros de salida del analizador

| Fichero | Contenido |
|---|---|
| `salida/informe.txt` | Informe legible: incidente, zona, acontecimientos relacionados, efectos y conclusión |
| `salida/anomalias.dat` | Solo las lecturas fuera de rango usadas para decidir, en el mismo formato de 25 bytes (`DataOutputStream`) |
| `salida/afectados.xml` | Solo la zona afectada, sus elementos dañados, las runas implicadas y los personajes implicados (DOM) |

## Ejecución

Todos los comandos se lanzan desde la carpeta `PracticaUD1/`, porque las rutas son relativas (`cd PracticaUD1`).

**Simulador**

```bash
# Modo normal: puede ocurrir un incidente aleatorio (o ninguno)
mvn -q compile exec:java -Dexec.mainClass=simulador.Simulador

# Modo prueba: provoca un incidente concreto
mvn -q compile exec:java -Dexec.mainClass=simulador.Simulador -Dexec.args="--incidente=ATAQUE_DRAGON"

# Solo operativa normal
mvn -q compile exec:java -Dexec.mainClass=simulador.Simulador -Dexec.args="--incidente=NINGUNO"
```

Opciones: `--incidente=TIPO|NINGUNO`, `--ciclos=30`, `--semilla=1234` (repite la misma ejecución), `--datos=carpeta`.

**Analizador forense**

```bash
mvn -q compile exec:java -Dexec.mainClass=analizador.AnalizadorForense
# con otras carpetas:
mvn -q compile exec:java -Dexec.mainClass=analizador.AnalizadorForense -Dexec.args="--datos=datos --salida=salida"
```

**En IntelliJ:** botón verde junto al `main` de `Simulador` o de `AnalizadorForense`. Los argumentos se ponen en *Run → Edit Configurations → Program arguments*.

## Pruebas automáticas

```bash
mvn test
```

En IntelliJ: clic derecho en `src/test/java` → *Run 'All Tests'*. Los tests usan `@TempDir`, así que no tocan las carpetas reales `datos/` y `salida/`.

| Test | Qué comprueba |
|---|---|
| `PersistenciaTextoTest` | Escritura y lectura del TXT, separador en la descripción, líneas corruptas, fichero inexistente |
| `PersistenciaBinTest` | Añadir, leer por acceso directo y modificar un registro con `RandomAccessFile`; registro incompleto |
| `PersistenciaXMLTest` | Guardar y cargar con DOM, cambios de estado, relación runa → zona, XML mal formado o con valores inválidos |
| `SimuladorTest` | La excepción crítica salta y los datos ya están persistidos antes; la operativa normal no da anomalías |
| `DetectorIncidentesTest` | Test parametrizado: el analizador identifica cada uno de los 6 incidentes, e `INDETERMINADO` sin incidente |
| `IntegracionTest` | Flujo completo: datos → incidente → persistencia → análisis → 3 salidas |

## Logs (Log4j 2)

- `logs/evidencias/ejecucion_dd-MM-yyyy_HH-mm-ss.log`: un fichero nuevo por cada ejecución.
- `logs/runtime/aplicacion.log`: log activo con rotación por tamaño (10 KB). Los históricos se llaman `aplicacion-yyyy-MM-dd-N.log.gz`, van comprimidos y se conservan como máximo 5.
- Formato de línea: `dd/MM/yyyy HH:mm:ss.SSS NIVEL Clase - mensaje`.

El analizador no lee los logs: solo usa `datos/`.

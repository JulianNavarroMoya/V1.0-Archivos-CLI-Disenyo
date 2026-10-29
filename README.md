# Tracker de Candidaturas Laborales — v1.0

Aplicación **CLI por argumentos** para gestionar candidaturas laborales, desarrollada
en **Java 21** con **Maven**. La persistencia se realiza exclusivamente en ficheros de
texto plano con campos separados por punto y coma.

## Arquitectura

```text
src/main/java/org/example/
├── Main.java                                  # Punto de entrada
├── exception/
│   └── TrackerException.java                  # Error de dominio con mensaje amigable
├── model/
│   └── Candidatura.java                       # POJO + serialización CSV
├── repository/
│   ├── CandidaturaRepository.java             # Contrato de persistencia
│   └── impl/
│       └── CandidaturaFileRepository.java     # Implementación NIO sobre candidaturas.txt
├── service/
│   └── CandidaturaService.java                # Reglas de negocio y filtros con Streams
└── cli/
    └── CliRunner.java                         # Parseo de args, tablas y fichas
```

La interfaz `CandidaturaRepository` desacopla la persistencia para poder migrar en la
v2.0 a JDBC y en la v3.0 a Hibernate sin modificar el servicio.

## Requisitos

- JDK 21 o superior.
- Maven 3.9 o superior.

## Compilar y empaquetar

Desde la raíz del proyecto:

```bash
mvn clean package
```

El JAR ejecutable se genera en `target/tracker.jar` y, además, se copia
automáticamente a la raíz del proyecto como `tracker.jar`:

```text
target/tracker.jar
tracker.jar
```

## Ejecutar

Desde la raíz del proyecto puedes ejecutar el JAR de cualquiera de estas dos
formas (ambas son válidas):

```powershell
# Windows
java -jar tracker.jar listar
java -jar target\tracker.jar listar
```

```bash
# Linux / macOS
java -jar tracker.jar listar
java -jar target/tracker.jar listar
```

Al empaquetar con `mvn clean package`, Maven deja el JAR tanto en `target/`
como en la raíz del proyecto, de modo que `java -jar tracker.jar ...` ya no
falla con `Error: Unable to access jarfile tracker.jar`.

Si el JAR todavía no existe, los lanzadores lo generan automáticamente (con
Maven si está instalado o, en su defecto, con `javac`) y ejecutan siempre desde
la raíz del proyecto para que `candidaturas.txt` esté siempre en el mismo sitio:

```powershell
# Windows
.\tracker.cmd listar --estado pendiente --empresa Indra
```

```bash
# Linux / macOS
./tracker.sh listar --estado pendiente --empresa Indra
```

## Formato de datos

Fichero principal: `candidaturas.txt` (se crea automáticamente si no existe).

```text
id;titulo;empresa;estado;dd/MM/yyyy
13;Desarrollador Backend Java;Indra;PENDIENTE;26/10/2026
```

Fichero semilla de prueba: `candidaturas.csv` (15 candidaturas, varias de Indra en
estado `PENDIENTE`).

## Comandos

```text
listar [--estado ESTADO] [--empresa TEXTO]
anadir --titulo "..." --empresa "..." --estado ESTADO [--fecha dd/MM/yyyy]
ver <id>
borrar <id>
importar <archivo.csv>
ayuda | --help
```

Los comandos funcionan con o sin guion inicial (`listar` / `-listar`) y también se
acepta `anadir` sin eñe para terminales con problemas de codificación.

## Pruebas de las historias de usuario (Windows PowerShell)

```powershell
# 0. Estado limpio (opcional)
Remove-Item candidaturas.txt -ErrorAction SilentlyContinue

# HU-02: listar sin datos
java -jar target\tracker.jar listar

# HU-01: importar el CSV de ejemplo (asigna IDs 1..15)
java -jar target\tracker.jar importar candidaturas.csv

# HU-02: listar todas las candidaturas
java -jar target\tracker.jar listar

# HU-03: filtro combinado, case-insensitive
java -jar target\tracker.jar listar --estado pendiente --empresa Indra

# HU-04: añadir con fecha explícita y sin fecha (fecha actual)
java -jar target\tracker.jar anadir --titulo "Desarrollador Backend Java" --empresa "Indra" --estado pendiente --fecha 26/10/2026
java -jar target\tracker.jar anadir --titulo "AI Engineer" --empresa "OpenAI" --estado entrevista

# HU-05: ver el detalle de la candidatura 13
java -jar target\tracker.jar ver 13

# HU-06: borrar la candidatura 13 y comprobar que ya no existe
java -jar target\tracker.jar borrar 13
java -jar target\tracker.jar ver 13

# Ayuda y errores controlados
java -jar target\tracker.jar ayuda
java -jar target\tracker.jar listar -estado pendiente -empresa Indra
java -jar target\tracker.jar ver abc
java -jar target\tracker.jar importar noexiste.csv
```

En Linux/macOS, sustituye `target\tracker.jar` por `target/tracker.jar` y
`Remove-Item candidaturas.txt -ErrorAction SilentlyContinue` por
`rm -f candidaturas.txt`.

> **Nota de codificación:** la aplicación escribe la consola en UTF-8. Si utilizas
> una consola antigua de Windows y ves mal las tildes, ejecuta antes `chcp 65001`.

## Características de la v1.0

- CLI puro por argumentos: sin `Scanner` ni menús interactivos.
- Persistencia NIO con `Files`, `Path` y `StandardCharsets.UTF_8`.
- IDs autoincrementales (`max(id) + 1`).
- Importación de CSV con o sin cabecera y con 4 o 5 columnas.
- Filtros combinables y case-insensitive con Streams.
- Validación estricta de fechas `dd/MM/yyyy`, incluidos días imposibles.
- Errores controlados con `[ERROR]` y ejemplos de uso, sin stacktraces.

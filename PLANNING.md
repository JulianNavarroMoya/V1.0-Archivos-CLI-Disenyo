# PLANNING & CONTEXTO TÉCNICO — Tracker de Candidaturas Laborales (v1.0)

> **Propósito de este documento:** Este archivo actúa como **Fuente de Verdad (Single Source of Truth)** para el equipo de desarrollo y como **archivo de contexto para el agente de IA (OpenCode en IntelliJ IDEA)**. Cualquier generación de código debe respetar estrictamente las reglas arquitectónicas, de negocio y de formato aquí definidas.

---

## 1. Datos Generales del Proyecto
* **Nombre del Proyecto:** `V1.0-Archivos-CLI-Disenyo` (Tracker de Candidaturas Laborales)
* **Repositorio GitHub:** `https://github.com/JulianNavarroMoya/V1.0-Archivos-CLI-Disenyo`
* **Equipo de Desarrollo (2 integrantes):**
  * **Integrante 1:** Julián Navarro Moya
  * **Integrante 2:** Chakir Hachmi
* **Módulos implicados:** Acceso a Datos (RA1 - Ficheros) + Desarrollo de Interfaces (RA4 - DCU, Prototipado Figma y Manual de Estilo).
* **Visión evolutiva del proyecto (Importante para la arquitectura):**
  * **v1.0 (Actual):** Persistencia en archivos de texto (`.txt` / `.csv` separados por `;`) + Interfaz por argumentos de línea de comandos (CLI) + Diseño en Figma y Manual de Estilo.
  * **v2.0 (Próxima):** Migración de persistencia a **JDBC** (bases de datos relacionales) + Interfaz gráfica en **JavaFX con Scene Builder**.
  * **v3.0 (Final):** Migración a **Hibernate (ORM)** + **JavaFX avanzado** (estadísticas) + Integración con **Ollama4j** (asistente IA local).

---

## 2. Reglas Estrictas para el Agente IA (OpenCode)
Cuando generes o refactorices código en este proyecto, **CUMPLIRÁS SIEMPRE** estas 7 reglas innegociables:

1. **PROHIBIDO usar `Scanner` o menús interactivos (`while(true)`):** La aplicación es una herramienta CLI pura orientada a argumentos (`String[] args`). Recibe los argumentos en `main`, ejecuta la acción correspondiente, imprime el resultado por consola y **termina su ejecución inmediatamente**.
2. **Compatibilidad dual de comandos (Con y sin guion inicial):** En el documento de requisitos los comandos aparecen a veces sin guion (`listar`, `añadir`, `ver`, `borrar`, `importar`) y en las Historias de Usuario con guion (`-listar`, `-añadir`, `-ver`, `-borrar`, `-importar`). El parser CLI **debe normalizar el primer argumento** (`args[0].replaceFirst("^-+", "").toLowerCase()`) y aceptar ambas variantes (incluso `anadir` sin `ñ` por compatibilidad de codificación en terminales Windows).
3. **Persistencia exclusiva en texto plano separado por punto y coma (`;`):**
   * Archivo principal de datos: `candidaturas.txt` (en la raíz del directorio de ejecución).
   * Si `candidaturas.txt` no existe al arrancar cualquier operación, el sistema debe crearlo vacío automáticamente sin lanzar error.
   * No se utilizan bases de datos, JSON ni XML en la v1.0.
4. **Formato de Fecha Estándar:** Todas las fechas se leen, validan y persisten en formato `dd/MM/yyyy` usando `DateTimeFormatter.ofPattern("dd/MM/yyyy")` y `java.time.LocalDate`.
5. **Desacoplamiento mediante el patrón Repository:** Toda la lógica de lectura/escritura de ficheros debe estar encapsulada en `CandidaturaFileRepository` que implementa la interfaz `CandidaturaRepository`. `CandidaturaService` solo debe depender de la interfaz `CandidaturaRepository` para facilitar el cambio a JDBC en la v2.0.
6. **Gestión de Errores Amigable (Cero StackTraces crudos al usuario):** Cualquier error (argumentos faltantes, ID no numérico, ID no encontrado en el archivo, archivo CSV a importar no existente, fecha mal formateada) debe capturarse y mostrarse en consola con un mensaje claro en español precedido de `[ERROR]`, finalizando limpiamente.
7. **Compatibilidad Java LTS:** Código limpio compatible con **Java 27** (evitar APIs experimentales), usando `java.nio.file.Files`, `Path`, `StandardCharsets.UTF_8` y Streams API.

---

## 3. Arquitectura del Proyecto y Estructura de Paquetes

El paquete base actual en `src/main/java` se organizará de la siguiente manera (puede ubicarse bajo `org.example` o `es.tracker`):

```text
V1.0-Archivos-CLI-Disenyo/
├── pom.xml                                  # Configuración Maven (Java 27 + maven-jar-plugin)
├── PLANNING.md                              # Contexto del proyecto para IA y planificación
├── README.md                                # Documentación técnica exhaustiva del proyecto
├── candidaturas.txt                         # Archivo principal de persistencia (generado/leído por la app)
├── candidaturas.csv                         # Archivo semilla de prueba para el comando 'importar'
└── src/
    └── main/
        └── java/
            └── org/example/
                ├── Main.java                # Punto de entrada: instancia capas y delega args a CliRunner
                ├── model/
                │   └── Candidatura.java     # Entidad POJO (id, titulo, empresa, estado, fecha)
                ├── repository/
                │   ├── CandidaturaRepository.java       # Interfaz CRUD desacoplada
                │   └── CandidaturaFileRepository.java   # Implementación NIO sobre 'candidaturas.txt'
                ├── service/
                │   └── CandidaturaService.java          # Lógica de negocio, validaciones y filtros con Streams
                ├── cli/
                │   └── CliRunner.java                   # Parseo de String[] args, flags (--estado, etc.) y tablas ASCII
                └── exception/
                    └── TrackerException.java            # Excepción controlada de dominio/validación
```

---

## 4. Especificación del Modelo de Datos (`candidaturas.txt` y `.csv`)

Cada línea del archivo `candidaturas.txt` representa una única candidatura con 5 campos separados por `;`:

```text
id;titulo;empresa;estado;fecha
```

### Ejemplo de contenido en `candidaturas.txt`:
```csv
1;Desarrollador Backend Java;Indra;PENDIENTE;15/10/2026
2;Fullstack Developer Junior;NTT Data;ENTREVISTA;18/10/2026
3;Ingeniero de Datos;Accenture;RECHAZADO;20/10/2026
13;JavaFX Desktop Developer;Babel;OFERTA;26/10/2026
```

### Reglas de persistencia y formato CSV:
* **Generación de ID (`id`):** Numérico (`long`), único y autoincremental. Al añadir o importar una nueva candidatura, su ID será `max(id_actuales) + 1` (empezando en `1` si el archivo está vacío).
* **Sanitización:** Si el usuario introduce un `;` dentro del título o empresa, se reemplazará o validará para no corromper las columnas del archivo.
* **Importación (`importar <archivo.csv>`):** El archivo CSV externo puede contener líneas con 4 campos (`titulo;empresa;estado;fecha`) o 5 campos (`id;titulo;empresa;estado;fecha`), y puede tener o no primera línea de cabecera (`id;titulo...`). El importador debe detectar si la primera línea es cabecera para ignorarla y asignar nuevos IDs correlativos a los registros importados para evitar colisiones de IDs en `candidaturas.txt`.

---

## 5. Especificación Exacta de Comandos CLI (Historias de Usuario HU-01 a HU-06)

| ID | Historia de Usuario | Sintaxis Soportada en Línea de Comandos | Comportamiento Esperado y Validaciones |
| :--- | :--- | :--- | :--- |
| **HU-01** | Importar desde CSV | `importar candidaturas.csv`<br>`-importar candidaturas.csv` | Verifica que el archivo existe. Lee todas las líneas válidas, les asigna ID secuencial, las añade a `candidaturas.txt` y muestra: `[OK] Se han importado X candidaturas correctamente desde candidaturas.csv`. |
| **HU-02** | Listar todas las candidaturas | `listar`<br>`-listar` | Lee `candidaturas.txt` y muestra una tabla ASCII formateada con columnas `ID`, `TÍTULO`, `EMPRESA`, `ESTADO`, `FECHA` y el total de registros al final. Si está vacío, muestra `[INFO] No hay candidaturas registradas.` |
| **HU-03** | Filtrar candidaturas | `listar --estado X --empresa Y`<br>`-listar --estado X`<br>`-listar --empresa Y` | Filtra las candidaturas de forma **case-insensitive**. `--estado` comprueba coincidencia con el estado y `--empresa` permite coincidencia parcial o exacta (`contains`). Ambos filtros son combinables. |
| **HU-04** | Añadir nueva candidatura | `añadir --titulo "X" --empresa "Y" --estado Z --fecha dd/MM/yyyy`<br>`-añadir --titulo "X" --empresa "Y" --estado Z` | Valida que `--titulo`, `--empresa` y `--estado` no estén vacíos. Si `--fecha` se omite (como en el ejemplo de la tabla HU-04), asigna automáticamente `LocalDate.now()`. Si se incluye `--fecha`, valida el patrón `dd/MM/yyyy`. Guarda en el fichero y muestra la candidatura creada con su nuevo ID. |
| **HU-05** | Ver detalle de una candidatura | `ver 13`<br>`-ver 13` | Valida que se haya pasado un ID numérico. Busca la candidatura con ID `13`. Si existe, imprime una ficha detallada en consola. Si no existe, muestra `[ERROR] No existe ninguna candidatura con el ID 13.` |
| **HU-06** | Borrar una candidatura | `borrar 13`<br>`-borrar 13` | Valida que se haya pasado un ID numérico. Si el ID `13` existe, lo elimina reescribiendo `candidaturas.txt` sin ese registro y muestra `[OK] Candidatura con ID 13 eliminada correctamente.` Si no existe, informa del error. |
| **EXTRA** | Ayuda / Uso incorrecto | `ayuda` / `--help` / Sin argumentos | Si se ejecuta sin argumentos o con un comando desconocido, muestra el mensaje de error correspondiente seguido de la guía rápida de comandos disponibles. |

---
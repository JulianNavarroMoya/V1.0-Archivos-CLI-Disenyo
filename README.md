# JobTrackr — Tracker de Candidaturas Laborales (v2.0)

Aplicación de escritorio con interfaz gráfica **JavaFX** para gestionar candidaturas
laborales: ofertas, empresas y entrevistas. Desarrollada en **Java 24** con **Maven**,
con persistencia en **MySQL** mediante **JDBC**.

Evoluciona la versión 1.0 (CLI con ficheros de texto), que se conserva en el historial
del repositorio con la etiqueta `v1.0`.

## Equipo

| Integrante | Responsabilidades |
|------------|-------------------|
| Julián Navarro Moya | Modelo de datos, esquema SQL, DAO JDBC, capa de servicio y validaciones, importación CSV, transacciones, tests con H2, configuración Maven |
| Chakir Hachmi | Interfaz JavaFX: pantallas FXML con Scene Builder, controladores, navegación, formularios, filtros visuales, hoja de estilos CSS |

Diseño de la interfaz en Figma: _enlace pendiente_

## Arquitectura

Estructura en capas. Cada capa solo depende de la inmediatamente inferior.

```text
src/main/java/org/example/
├── config/          ConfiguracionBD        Lectura de config.properties y conexión JDBC
├── model/           Empresa, Oferta,       Records y enums del dominio
│                    Entrevista, FiltroOferta, EstadoOferta, TipoEntrevista,
│                    ResultadoEntrevista
├── repository/jdbc/ EmpresaJdbcDAO,        Acceso a MySQL con PreparedStatement
│                    OfertaJdbcDAO,
│                    EntrevistaJdbcDAO
├── service/         EmpresaService,        Contratos de negocio (interfaces)
│                    OfertaService,
│                    EntrevistaService
├── service/impl/    *ServiceImpl,          Validaciones y reglas de negocio
│                    Validaciones
├── exception/       TrackerException       Error de dominio con mensaje para el usuario
└── ui/              Launcher, JobTrackrApp, Punto de entrada JavaFX
                     Navegador, Servicios,  y pantallas
                     controller/, fxml/
```

Flujo de una petición: **pantalla FXML → controlador → servicio → DAO → MySQL**.
La interfaz solo conoce las interfaces de `service`, de modo que la implementación
puede cambiarse sin tocar las pantallas.

## Requisitos

- JDK 24 o superior.
- Maven 3.9 o superior.
- Docker, con un contenedor MySQL 8 accesible en el puerto `3308`.

## Configuración

1. Crea la base de datos y el usuario una sola vez, con `root`:

   ```sql
   CREATE DATABASE tracker CHARACTER SET utf8mb4;
   CREATE USER 'user'@'%' IDENTIFIED BY 'TU_CONTRASEÑA';
   GRANT ALL PRIVILEGES ON tracker.* TO 'user'@'%';
   FLUSH PRIVILEGES;
   ```

2. Crea las tablas ejecutando `sql/schema.sql` contra la base `tracker`.

3. Copia `config.example.properties` como `config.properties` en la raíz del proyecto
   y rellena tus datos:

   ```properties
   db.url=jdbc:mysql://localhost:3308/tracker?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Madrid
   db.user=user
   db.password=TU_CONTRASEÑA
   ```

`config.properties` contiene credenciales y **no se sube al repositorio** (está en
`.gitignore`).

## Compilar y ejecutar

Desde la raíz del proyecto:

```bash
mvn clean package
```

Ejecutar la interfaz gráfica:

```bash
mvn javafx:run
```

En IntelliJ también puedes crear una configuración de tipo *Application* con
**Main class** `org.example.ui.Launcher`.

## Esquema de la base de datos

| Tabla | Descripción | Relación |
|-------|-------------|----------|
| `EMPRESA` | Empresas a las que se envían candidaturas. El nombre es único. | — |
| `OFERTA` | Candidatura a un puesto concreto con su estado y salario. | N:1 con `EMPRESA`, `ON DELETE RESTRICT` |
| `ENTREVISTA` | Entrevistas asociadas a una oferta. | N:1 con `OFERTA`, `ON DELETE CASCADE` |

Estados de una oferta: `pendiente`, `entrevista`, `rechazado`, `aceptado`.

Borrar una oferta borra sus entrevistas. Borrar una empresa que tiene ofertas está
prohibido, y la aplicación lo indica con un mensaje.

## Importación CSV

Desde la pantalla principal se puede importar un fichero con este formato, con
cabecera y separador `;`:

```text
empresa;titulo;estado;fechaPostulacion;salario;enlace;notas
Indra;Desarrollador Junior Java;pendiente;2026-11-20;24000;https://indra.example/oferta1;primera prueba
NTT Data;Backend Spring Boot;entrevista;2026-11-21;;https://nttdata.example/oferta2;
```

- Las empresas que no existen se crean automáticamente. La búsqueda por nombre no
  distingue mayúsculas.
- Si una sola fila es incorrecta, **no se importa ninguna**, y se muestra el número de
  línea y el motivo.

## Pruebas

Las pruebas usan JUnit 5 con una base de datos H2 en memoria, así que no necesitan
MySQL:

```bash
mvn test
```

_Estado: pruebas de DAO, filtros y transacciones en desarrollo._

## Uso de inteligencia artificial

Se ha utilizado un asistente de IA para explicar conceptos, revisar código y preparar
ejemplos. Todo el código ha sido revisado, comprendido y probado por sus autores.

## Versiones

- **v1.0**: CLI con persistencia en ficheros de texto. Etiqueta `v1.0`.
- **v2.0**: interfaz JavaFX y MySQL con JDBC. Etiqueta `v2.0`.
- **v3.0**: migración a Hibernate. Fuera del alcance de esta entrega.

@echo off
rem ---------------------------------------------------------------------------
rem  Lanzador del Tracker de Candidaturas Laborales (Windows)
rem
rem  Uso:  tracker.cmd <comando> [opciones]
rem  Ej.:  tracker.cmd listar --estado pendiente --empresa Indra
rem
rem  Evita el error "Unable to access jarfile tracker.jar": resuelve la ruta
rem  real del JAR (target\tracker.jar o tracker.jar en la raiz), lo genera si
rem  falta y ejecuta siempre desde la raiz del proyecto para que candidaturas.txt
rem  sea localizable.
rem ---------------------------------------------------------------------------
setlocal EnableExtensions

set "PROJECT_DIR=%~dp0"
set "JAR=%PROJECT_DIR%target\tracker.jar"
set "ROOT_JAR=%PROJECT_DIR%tracker.jar"

rem Si no hay JAR en target pero si en la raiz, se usa el de la raiz.
if not exist "%JAR%" if exist "%ROOT_JAR%" set "JAR=%ROOT_JAR%"

if not exist "%JAR%" (
    echo [INFO] No se encontro el JAR. Generando el paquete...
    if exist "%PROJECT_DIR%mvnw.cmd" (
        call "%PROJECT_DIR%mvnw.cmd" -q -f "%PROJECT_DIR%pom.xml" clean package
    ) else (
        where mvn >nul 2>&1
        if not errorlevel 1 (
            call mvn -q -f "%PROJECT_DIR%pom.xml" clean package
        ) else (
            echo [INFO] Maven no esta disponible. Compilando con javac...
            call :compilarConJavac
            if errorlevel 1 exit /b 1
        )
    )

    if not exist "%JAR%" if exist "%ROOT_JAR%" set "JAR=%ROOT_JAR%"
    if not exist "%JAR%" (
        echo [ERROR] La compilacion ha fallado. Revisa la salida anterior.
        exit /b 1
    )
)

rem Directorio de trabajo = raiz del proyecto (datos en candidaturas.txt).
cd /d "%PROJECT_DIR%"
java -jar "%JAR%" %*
exit /b %ERRORLEVEL%

rem ---------------------------------------------------------------------------
rem  Compilacion de respaldo usando solo el JDK (sin Maven).
rem ---------------------------------------------------------------------------
:compilarConJavac
set "CLASSES=%PROJECT_DIR%target\classes"
set "SOURCES=%TEMP%\tracker-fuentes.txt"

if exist "%CLASSES%" rmdir /s /q "%CLASSES%"
mkdir "%CLASSES%" 2>nul

dir /s /b "%PROJECT_DIR%src\main\java\*.java" > "%SOURCES%"
javac -encoding UTF-8 -d "%CLASSES%" @"%SOURCES%"
if errorlevel 1 exit /b 1

jar --create --file "%JAR%" --main-class org.example.Main -C "%CLASSES%" .
if errorlevel 1 exit /b 1

copy /y "%JAR%" "%ROOT_JAR%" >nul
exit /b 0

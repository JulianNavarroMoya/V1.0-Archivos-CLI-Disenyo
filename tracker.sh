#!/usr/bin/env bash
# ---------------------------------------------------------------------------
#  Lanzador del Tracker de Candidaturas Laborales (Linux/macOS)
#
#  Uso:  ./tracker.sh <comando> [opciones]
#  Ej.:  ./tracker.sh listar --estado pendiente --empresa Indra
#
#  Evita el error "Unable to access jarfile tracker.jar": resuelve la ruta
#  real del JAR (target/tracker.jar o tracker.jar en la raiz), lo genera si
#  falta y ejecuta siempre desde la raiz del proyecto para que candidaturas.txt
#  sea localizable.
# ---------------------------------------------------------------------------
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR="$PROJECT_DIR/target/tracker.jar"
ROOT_JAR="$PROJECT_DIR/tracker.jar"

# Si no hay JAR en target pero si en la raiz, se usa el de la raiz.
if [[ ! -f "$JAR" && -f "$ROOT_JAR" ]]; then
    JAR="$ROOT_JAR"
fi

compilar_con_javac() {
    local classes="$PROJECT_DIR/target/classes"

    rm -rf "$classes"
    mkdir -p "$classes" "$PROJECT_DIR/target"

    find "$PROJECT_DIR/src/main/java" -name '*.java' -print0 \
        | xargs -0 javac -encoding UTF-8 -d "$classes"

    jar --create --file "$JAR" --main-class org.example.Main -C "$classes" .
    cp -f "$JAR" "$ROOT_JAR"
}

if [[ ! -f "$JAR" ]]; then
    echo "[INFO] No se encontro el JAR. Generando el paquete..."
    if [[ -x "$PROJECT_DIR/mvnw" ]]; then
        "$PROJECT_DIR/mvnw" -q -f "$PROJECT_DIR/pom.xml" clean package
    elif command -v mvn >/dev/null 2>&1; then
        mvn -q -f "$PROJECT_DIR/pom.xml" clean package
    elif command -v javac >/dev/null 2>&1 && command -v jar >/dev/null 2>&1; then
        echo "[INFO] Maven no esta disponible. Compilando con javac..."
        compilar_con_javac
    else
        echo "[ERROR] No hay JAR y no se encontro Maven ni un JDK para generarlo." >&2
        echo "        Compila primero con:  mvn clean package" >&2
        exit 1
    fi

    if [[ ! -f "$JAR" && -f "$ROOT_JAR" ]]; then
        JAR="$ROOT_JAR"
    fi
    if [[ ! -f "$JAR" ]]; then
        echo "[ERROR] La compilacion ha fallado. Revisa la salida anterior." >&2
        exit 1
    fi
fi

cd "$PROJECT_DIR"
exec java -jar "$JAR" "$@"

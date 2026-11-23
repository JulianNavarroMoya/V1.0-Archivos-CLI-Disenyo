package org.example.model;

/**
 * Resultado de una entrevista.
 */
public enum ResultadoEntrevista {
    PENDIENTE("pendiente"),
    POSITIVO("positivo"),
    NEGATIVO("negativo");

    private final String valorBd;

    ResultadoEntrevista(String valorBd) {
        this.valorBd = valorBd;
    }

    /**
     * @return valor almacenado en la columna {@code resultado} de la tabla ENTREVISTA
     */
    public String valorBd() {
        return valorBd;
    }

    /**
     * @param valor valor leído de la base de datos
     * @return resultado correspondiente
     * @throws IllegalArgumentException si el valor no existe
     */
    public static ResultadoEntrevista desdeBd(String valor) {
        for (ResultadoEntrevista resultado : values()) {
            if (resultado.valorBd.equals(valor)) {
                return resultado;
            }
        }
        throw new IllegalArgumentException("Resultado desconocido: " + valor);
    }
}

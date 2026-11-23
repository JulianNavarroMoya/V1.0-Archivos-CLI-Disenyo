package org.example.model;

/**
 * Tipo de entrevista asociada a una oferta.
 */
public enum TipoEntrevista {
    TELEFONICA("telefonica"),
    TECNICA("tecnica"),
    PRESENCIAL("presencial"),
    ONLINE("online");

    private final String valorBd;

    TipoEntrevista(String valorBd) {
        this.valorBd = valorBd;
    }

    /**
     * @return valor almacenado en la columna {@code tipo} de la tabla ENTREVISTA
     */
    public String valorBd() {
        return valorBd;
    }

    /**
     * @param valor valor leído de la base de datos
     * @return tipo correspondiente
     * @throws IllegalArgumentException si el valor no existe
     */
    public static TipoEntrevista desdeBd(String valor) {
        for (TipoEntrevista tipo : values()) {
            if (tipo.valorBd.equals(valor)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo desconocido: " + valor);
    }
}
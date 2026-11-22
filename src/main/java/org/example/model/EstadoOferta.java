package org.example.model;

/**
 * Estado de una oferta en el proceso de selección.
 */
public enum EstadoOferta {
    PENDIENTE("pendiente"),
    ENTREVISTA("entrevista"),
    RECHAZADO("rechazado"),
    ACEPTADO("aceptado");

    private final String valorBd;

    EstadoOferta(String valorBd) {
        this.valorBd = valorBd;
    }

    /**
     * @return valor almacenado en la columna {@code estado} de la tabla OFERTA
     */
    public String valorBd() {
        return valorBd;
    }

    /**
     * @param valor valor leído de la base de datos
     * @return estado correspondiente
     * @throws IllegalArgumentException si el valor no existe
     */
    public static EstadoOferta desdeBd(String valor) {
        for (EstadoOferta estado : values()) {
            if (estado.valorBd.equals(valor)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado desconocido: " + valor);
    }
}

package com.cipolflo.server.reservas.domain.enums;

import java.util.Arrays;
import java.util.List;

public enum EstadoReserva {
    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    EN_CURSO("En curso"),
    VENCIDA_SIN_PAGO("Vencida sin pago"),
    FINALIZADA("Finalizada"),
    CANCELADA("Cancelada");

    /**
     * Estados que cuentan como "ocupando" un servicio en un rango de fechas (para el
     * chequeo de solapamiento al crear/modificar reservas y para calcular ocupación).
     * Se deriva de {@link #esOcupante()} para que un estado nuevo quede clasificado en
     * un solo lugar, sin tener que acordarse de sumarlo a mano en cada usuario de la lista.
     */
    public static final List<EstadoReserva> ESTADOS_OCUPANTES = Arrays.stream(values())
            .filter(EstadoReserva::esOcupante)
            .toList();

    private final String label;

    EstadoReserva(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean esFinalizable() {
        return this == EN_CURSO || this == VENCIDA_SIN_PAGO;
    }

    public boolean esOcupante() {
        return this == PENDIENTE || this == CONFIRMADA || this == EN_CURSO;
    }

    @Override
    public String toString() {
        return label;
    }
}

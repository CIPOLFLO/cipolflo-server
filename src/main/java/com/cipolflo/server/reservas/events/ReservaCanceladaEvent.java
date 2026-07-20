package com.cipolflo.server.reservas.events;

import java.util.List;

public record ReservaCanceladaEvent(List<Long> reservaIds, MotivoCancelacionReserva motivo) {

    public static ReservaCanceladaEvent manual(Long reservaId) {
        return new ReservaCanceladaEvent(List.of(reservaId), MotivoCancelacionReserva.MANUAL);
    }
}

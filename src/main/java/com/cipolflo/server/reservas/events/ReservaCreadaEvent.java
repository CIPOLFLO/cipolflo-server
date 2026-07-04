package com.cipolflo.server.reservas.events;

/**
 * Se publica dentro de {@code ReservaService.registrar} cuando se crea una reserva.
 * Lleva solo el id; el listener resuelve el resto (email, nombre) recién tras el commit,
 * para no cargar la transacción de creación con consultas de notificación.
 */
public record ReservaCreadaEvent(Long reservaId) {
}

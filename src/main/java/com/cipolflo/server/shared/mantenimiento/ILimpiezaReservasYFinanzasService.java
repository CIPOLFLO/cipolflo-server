package com.cipolflo.server.shared.mantenimiento;

/**
 * Limpieza por retención de reservas viejas (con sus finanzas asociadas) y de finanzas
 * sueltas vencidas. Ver {@link LimpiezaReservasYFinanzasService}.
 */
public interface ILimpiezaReservasYFinanzasService {

    /**
     * Borra las reservas vencidas junto con sus finanzas asociadas, y las finanzas sin
     * reserva asociada que ya vencieron. Idempotente: si no hay nada vencido, no borra nada.
     *
     * @return resumen legible de lo que hizo (para el log de tareas programadas)
     */
    String limpiarVencidos();
}

package com.cipolflo.server.reservas.service;

/**
 * Mueve las reservas de estado según la fecha del día en que corre la tarea. Ver
 * {@code TransicionEstadoReservasPorFechaScheduler}.
 */
public interface ITransicionEstadoReservasPorFechaService {

    /**
     * Corre dos transiciones, ambas basadas en la fecha de hoy (hora de Uruguay):
     * <ul>
     *   <li>CONFIRMADA → EN_CURSO: reservas cuya {@code fechaEntrada} es hoy.</li>
     *   <li>EN_CURSO → FINALIZADA (si {@code estaPaga()}) o VENCIDA_SIN_PAGO (si no):
     *       reservas cuya {@code fechaSalida} fue ayer, es decir que terminaron el día
     *       anterior a que corre la tarea (que corre a las 00:00).</li>
     * </ul>
     *
     * @return resumen legible de lo que hizo (para el log de tareas programadas)
     */
    String transicionarEstadosPorFecha();
}

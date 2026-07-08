package com.cipolflo.server.reservas.service;

/**
 * Genera y envía por mail el reporte semanal de reservas (las de la semana que
 * arranca el lunes en que corre la tarea). Ver {@code ReporteSemanalReservasScheduler}.
 */
public interface IReporteSemanalReservasService {

    /**
     * Arma el reporte de la semana actual (lunes a domingo, hora de Uruguay) y lo
     * envía al destinatario configurado. Si no hay destinatario configurado, no envía.
     *
     * @return resumen legible de lo que hizo (para el log de tareas programadas)
     */
    String enviarReporteSemanal();
}

package com.cipolflo.server.shared.scheduling;

/**
 * Identifica cada tarea programada de la aplicación. Se persiste en
 * {@code log_tareas_programadas} para rastrear qué corrió y cuándo.
 */
public enum TipoTareaProgramada {
    REPORTE_SEMANAL_RESERVAS,
    LIMPIEZA_LOGS_EMAIL,
    CANCELACION_AUTOMATICA_RESERVAS,
    TRANSICION_ESTADO_RESERVAS_POR_FECHA,
    INACTIVACION_SOCIOS
}

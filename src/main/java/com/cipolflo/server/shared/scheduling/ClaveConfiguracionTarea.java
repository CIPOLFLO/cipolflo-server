package com.cipolflo.server.shared.scheduling;

/**
 * Claves de parámetros configurables de tareas programadas, persistidos en
 * {@code configuracion_tarea} para que se puedan ajustar sin necesidad de un deploy.
 * Cada tarea que necesite un parámetro así agrega su propia clave acá.
 */
public enum ClaveConfiguracionTarea {

    /** Años de antigüedad de fechaSalida a partir de los cuales se borra una reserva (y sus finanzas). */
    LIMPIEZA_RESERVAS_RETENCION_ANIOS,

    /** Años de antigüedad de fecha a partir de los cuales se borra una finanza sin reserva asociada. */
    LIMPIEZA_FINANZAS_SUELTAS_RETENCION_ANIOS
}

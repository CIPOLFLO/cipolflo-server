package com.cipolflo.server.shared.mantenimiento;

/**
 * Purga de los registros de envío de emails ({@code envio_emails_logs}) más antiguos
 * que la ventana de retención configurada. Ver {@link LimpiezaLogsEmailService}.
 */
public interface ILimpiezaLogsEmailService {

    /**
     * Elimina los logs de email vencidos. Idempotente: si no hay vencidos, no borra nada.
     *
     * @return resumen legible de lo que hizo (para el log de tareas programadas)
     */
    String purgarLogsVencidos();
}

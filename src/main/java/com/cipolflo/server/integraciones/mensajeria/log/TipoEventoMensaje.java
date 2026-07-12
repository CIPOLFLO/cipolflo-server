package com.cipolflo.server.integraciones.mensajeria.log;

/** Qué originó el envío de un mensaje, para poder auditar después qué se mandó. */
public enum TipoEventoMensaje {
    RESPUESTA_CONSULTA,
    REPORTE_SEMANAL,
    CUOTAS_ATRASADAS,
    RESERVAS_POR_VENCER,
    RECHAZO_NO_AUTORIZADO
}

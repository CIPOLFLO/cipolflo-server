package com.cipolflo.server.integraciones.mensajeria.log;

/** Qué originó el envío de un mensaje, para poder auditar después qué se mandó. */
public enum TipoEventoMensaje {
    RESPUESTA_CONSULTA,
    NOTIFICACION_INTERNA,
    RECHAZO_NO_AUTORIZADO
}

package com.cipolflo.server.integraciones.mensajeria.ai;

/**
 * Envuelve una falla del proveedor de IA (timeout, error del modelo) al resolver una
 * consulta. La lanza {@code AsistenteConsultas}; la consume {@code ProcesadorMensajeTelegram}
 * para mandarle al usuario el mensaje genérico de error, sin propagar el detalle técnico.
 * No lleva handler en {@code GlobalExceptionHandler}: nunca viaja por un hilo de request HTTP.
 */
public class AsistenteException extends RuntimeException {

    public AsistenteException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

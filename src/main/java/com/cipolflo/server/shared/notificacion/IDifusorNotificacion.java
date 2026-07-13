package com.cipolflo.server.shared.notificacion;

/**
 * Puerto para difundir una notificación interna por un canal (email, Telegram, o el que
 * se agregue después). Genérico a propósito: no sabe qué disparó la notificación, así que
 * el mismo mecanismo sirve para cualquier aviso futuro sin agregar un puerto por caso.
 */
public interface IDifusorNotificacion {

    /**
     * Difunde {@code cuerpo} por este canal. No lanza: cada implementación contiene sus
     * propios errores y devuelve un resumen legible (éxito, fallo u omitido) para que el
     * llamador pueda agregar el resultado de todos los difusores en un solo log.
     */
    String difundir(String asunto, String cuerpo);
}

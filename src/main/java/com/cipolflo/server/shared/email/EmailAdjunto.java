package com.cipolflo.server.shared.email;

/**
 * Representa un adjunto de un email (ej. un comprobante PDF ya generado).
 *
 * @param nombre      nombre del archivo tal como lo verá el destinatario (ej. "comprobante.pdf")
 * @param contenido   bytes del archivo
 * @param contentType tipo MIME (ej. "application/pdf")
 */
public record EmailAdjunto(
        String nombre,
        byte[] contenido,
        String contentType
) {
}

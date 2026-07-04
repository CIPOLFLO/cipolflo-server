package com.cipolflo.server.shared.email;

import java.util.Arrays;
import java.util.Objects;

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

    // equals/hashCode/toString overrideados para comparar el contenido del array (byte[]) y no su
    // referencia: los métodos autogenerados de un record usan identidad para campos array (java:S6218).

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EmailAdjunto that)) {
            return false;
        }
        return Objects.equals(nombre, that.nombre)
                && Arrays.equals(contenido, that.contenido)
                && Objects.equals(contentType, that.contentType);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hash(nombre, contentType) + Arrays.hashCode(contenido);
    }

    @Override
    public String toString() {
        return "EmailAdjunto{nombre=" + nombre
                + ", contentType=" + contentType
                + ", contenido=" + (contenido != null ? contenido.length + " bytes" : "null")
                + '}';
    }
}

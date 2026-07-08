package com.cipolflo.server.shared.email;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailAdjuntoTest {

    private EmailAdjunto adjunto(String nombre, byte[] contenido, String contentType) {
        return new EmailAdjunto(nombre, contenido, contentType);
    }

    @Test
    void equals_true_cuandoElContenidoDelArrayEsIgualAunqueSeaOtraReferencia() {
        EmailAdjunto a = adjunto("comprobante.pdf", new byte[]{1, 2, 3}, "application/pdf");
        EmailAdjunto b = adjunto("comprobante.pdf", new byte[]{1, 2, 3}, "application/pdf");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_true_consigoMismo() {
        EmailAdjunto a = adjunto("comprobante.pdf", new byte[]{1, 2, 3}, "application/pdf");

        assertEquals(a, a);
    }

    @Test
    void equals_false_cuandoDifiereElContenido() {
        EmailAdjunto a = adjunto("comprobante.pdf", new byte[]{1, 2, 3}, "application/pdf");
        EmailAdjunto b = adjunto("comprobante.pdf", new byte[]{9, 9, 9}, "application/pdf");

        assertNotEquals(a, b);
    }

    @Test
    void equals_false_cuandoDifiereElNombre() {
        EmailAdjunto a = adjunto("comprobante.pdf", new byte[]{1, 2, 3}, "application/pdf");
        EmailAdjunto b = adjunto("otro.pdf", new byte[]{1, 2, 3}, "application/pdf");

        assertNotEquals(a, b);
    }

    @Test
    void equals_false_cuandoDifiereElContentType() {
        EmailAdjunto a = adjunto("comprobante.pdf", new byte[]{1, 2, 3}, "application/pdf");
        EmailAdjunto b = adjunto("comprobante.pdf", new byte[]{1, 2, 3}, "text/plain");

        assertNotEquals(a, b);
    }

    @Test
    void equals_false_conNullYConOtroTipo() {
        EmailAdjunto a = adjunto("comprobante.pdf", new byte[]{1, 2, 3}, "application/pdf");

        assertNotEquals(a, null);
        assertNotEquals(a, "no soy un adjunto");
    }

    @Test
    void toString_muestraElTamanioYNoLosBytesCrudos() {
        EmailAdjunto a = adjunto("comprobante.pdf", new byte[]{1, 2, 3}, "application/pdf");

        String texto = a.toString();

        assertTrue(texto.contains("comprobante.pdf"));
        assertTrue(texto.contains("application/pdf"));
        assertTrue(texto.contains("3 bytes"));
    }

    @Test
    void toString_manejaContenidoNull() {
        EmailAdjunto a = adjunto("vacio.pdf", null, "application/pdf");

        assertTrue(a.toString().contains("null"));
    }
}

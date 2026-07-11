package com.cipolflo.server.shared.pdf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NombreArchivoPdfTest {

    @Test
    void deberiaGenerarNombreConFormatoEsperado() {
        String nombre = NombreArchivoPdf.generar("comprobante-reserva-1");

        assertTrue(nombre.matches("comprobante-reserva-1_\\d{4}-\\d{2}-\\d{2}_\\d{4}\\.pdf"));
    }
}

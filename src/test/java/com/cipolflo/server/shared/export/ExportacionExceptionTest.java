package com.cipolflo.server.shared.export;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExportacionExceptionTest {

    @Test
    void deberiaCrearExceptionConMensaje() {
        ExportacionException exception =
                new ExportacionException("Error de exportación");

        assertEquals("Error de exportación", exception.getMessage());
    }
}
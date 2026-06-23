package com.cipolflo.server.shared.export;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FormulaSanitizerTest {

    @Test
    void deberiaPrefijarApostrofoCuandoEmpiezaConCaracterRiesgoso() {
        assertEquals("'=SUMA(A1:A2)", FormulaSanitizer.sanitizar("=SUMA(A1:A2)"));
        assertEquals("'+100", FormulaSanitizer.sanitizar("+100"));
        assertEquals("'-100", FormulaSanitizer.sanitizar("-100"));
        assertEquals("'@valor", FormulaSanitizer.sanitizar("@valor"));
        assertEquals("'\tvalor", FormulaSanitizer.sanitizar("\tvalor"));
        assertEquals("'\rvalor", FormulaSanitizer.sanitizar("\rvalor"));
    }

    @Test
    void noDeberiaModificarValoresNormales() {
        assertEquals("Texto normal", FormulaSanitizer.sanitizar("Texto normal"));
        assertEquals("100", FormulaSanitizer.sanitizar("100"));
    }

    @Test
    void deberiaManejarNullYVacio() {
        assertNull(FormulaSanitizer.sanitizar(null));
        assertEquals("", FormulaSanitizer.sanitizar(""));
    }
}
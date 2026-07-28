package com.cipolflo.server.clientes.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetodoCobroTest {

    @Test
    void deberiaRetornarLabelLegibleParaCadaMetodo() {
        assertEquals("Descuento salarial",  MetodoCobro.DESCUENTO_SALARIAL.getLabel());
        assertEquals("Transferencia",       MetodoCobro.TRANSFERENCIA.getLabel());
        assertEquals("En sede",             MetodoCobro.EN_SEDE.getLabel());
        assertEquals("Efectivo",            MetodoCobro.EFECTIVO.getLabel());
        assertEquals("Débito",              MetodoCobro.DEBITO.getLabel());
    }

    @Test
    void toStringShouldReturnLabel() {
        for (MetodoCobro metodo : MetodoCobro.values()) {
            assertEquals(metodo.getLabel(), metodo.toString());
        }
    }
}

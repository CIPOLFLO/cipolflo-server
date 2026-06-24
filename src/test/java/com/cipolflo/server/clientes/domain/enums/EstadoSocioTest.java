package com.cipolflo.server.clientes.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EstadoSocioTest {

    @Test
    void deberiaRetornarLabelLegibleParaCadaEstado() {
        assertEquals("Activo",   EstadoSocio.ACTIVO.getLabel());
        assertEquals("Inactivo", EstadoSocio.INACTIVO.getLabel());
        assertEquals("De baja",  EstadoSocio.DE_BAJA.getLabel());
    }

    @Test
    void toStringShouldReturnLabel() {
        assertEquals(EstadoSocio.ACTIVO.getLabel(),   EstadoSocio.ACTIVO.toString());
        assertEquals(EstadoSocio.INACTIVO.getLabel(), EstadoSocio.INACTIVO.toString());
        assertEquals(EstadoSocio.DE_BAJA.getLabel(),  EstadoSocio.DE_BAJA.toString());
    }
}

package com.cipolflo.server.ajustes.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CostoCuotaSocioTest {

    @Test
    void constructor_conMontoValido_creaLaInstanciaConIdFijo() {
        CostoCuotaSocio costoCuota = new CostoCuotaSocio(new BigDecimal("200.00"));

        assertEquals(CostoCuotaSocio.ID_FIJO, costoCuota.getId());
        assertEquals(new BigDecimal("200.00"), costoCuota.getMonto());
    }

    @Test
    void constructor_conMontoCero_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new CostoCuotaSocio(BigDecimal.ZERO));
    }

    @Test
    void constructor_conMontoNegativo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new CostoCuotaSocio(new BigDecimal("-10")));
    }

    @Test
    void constructor_conMontoNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new CostoCuotaSocio(null));
    }

    @Test
    void actualizarMonto_conValorPositivo_actualizaElMonto() {
        CostoCuotaSocio costoCuota = new CostoCuotaSocio(new BigDecimal("200.00"));

        costoCuota.actualizarMonto(new BigDecimal("250.00"));

        assertEquals(new BigDecimal("250.00"), costoCuota.getMonto());
    }

    @Test
    void actualizarMonto_conValorCero_lanzaExcepcionYNoModificaElMonto() {
        CostoCuotaSocio costoCuota = new CostoCuotaSocio(new BigDecimal("200.00"));

        assertThrows(IllegalArgumentException.class, () -> costoCuota.actualizarMonto(BigDecimal.ZERO));

        assertEquals(new BigDecimal("200.00"), costoCuota.getMonto());
    }

    @Test
    void actualizarMonto_conValorNegativo_lanzaExcepcion() {
        CostoCuotaSocio costoCuota = new CostoCuotaSocio(new BigDecimal("200.00"));

        assertThrows(IllegalArgumentException.class, () -> costoCuota.actualizarMonto(new BigDecimal("-1")));
    }

    @Test
    void actualizarMonto_conValorNulo_lanzaExcepcion() {
        CostoCuotaSocio costoCuota = new CostoCuotaSocio(new BigDecimal("200.00"));

        assertThrows(IllegalArgumentException.class, () -> costoCuota.actualizarMonto(null));
    }
}

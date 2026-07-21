package com.cipolflo.server.shared.scheduling;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfiguracionTareaTest {

    private ConfiguracionTarea configuracion(String valor) {
        return new ConfiguracionTarea(ClaveConfiguracionTarea.LIMPIEZA_RESERVAS_RETENCION_ANIOS, valor);
    }

    @Test
    void actualizarValorEntero_conValorPositivo_actualizaElValor() {
        ConfiguracionTarea configuracion = configuracion("2");

        configuracion.actualizarValorEntero(5);

        assertEquals(5, configuracion.valorComoEntero());
    }

    @Test
    void actualizarValorEntero_conCero_lanzaExcepcionYNoModificaElValor() {
        ConfiguracionTarea configuracion = configuracion("2");

        assertThrows(IllegalArgumentException.class, () -> configuracion.actualizarValorEntero(0));

        assertEquals(2, configuracion.valorComoEntero());
    }

    @Test
    void actualizarValorEntero_conNegativo_lanzaExcepcion() {
        ConfiguracionTarea configuracion = configuracion("2");

        assertThrows(IllegalArgumentException.class, () -> configuracion.actualizarValorEntero(-1));
    }
}

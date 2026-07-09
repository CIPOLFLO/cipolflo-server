package com.cipolflo.server.clientes.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RutNormalizadorTest {

    @Test
    void normalizar_deberiaEliminarCaracteresNoNumericos() {
        assertEquals("210001230018", RutNormalizador.normalizar("21.000.123-0018"));
    }

    @Test
    void normalizar_deberiaDejarIgualUnValorSoloNumerico() {
        assertEquals("210001230018", RutNormalizador.normalizar("210001230018"));
    }

    @Test
    void normalizar_deberiaRetornarVacioParaNull() {
        assertEquals("", RutNormalizador.normalizar(null));
    }

    @Test
    void normalizar_deberiaRetornarVacioCuandoNoHayDigitos() {
        assertEquals("", RutNormalizador.normalizar("sin-digitos"));
    }
}

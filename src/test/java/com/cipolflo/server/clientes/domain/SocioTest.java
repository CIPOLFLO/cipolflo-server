package com.cipolflo.server.clientes.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class SocioTest {

    @Test
    void deberiaCalcularCeroAniosCuandoLaFechaDeIngresoCoincideConLaReferencia() {
        Socio socio = new Socio();
        LocalDate fecha = LocalDate.of(2026, 7, 15);
        socio.setFechaIngreso(fecha);

        int antiguedad = socio.calcularAntiguedadEnAnios(fecha);

        assertEquals(0, antiguedad);
    }

    @Test
    void deberiaCalcularAniosCompletosDeAntiguedad() {
        Socio socio = new Socio();
        socio.setFechaIngreso(LocalDate.of(2020, 7, 15));

        int antiguedad = socio.calcularAntiguedadEnAnios(LocalDate.of(2026, 7, 15));

        assertEquals(6, antiguedad);
    }

    @Test
    void noDeberiaContarUnAnioQueTodaviaNoSeCumplio() {
        Socio socio = new Socio();
        socio.setFechaIngreso(LocalDate.of(2020, 7, 16));

        int antiguedad = socio.calcularAntiguedadEnAnios(LocalDate.of(2026, 7, 15));

        assertEquals(5, antiguedad);
    }

    @Test
    void deberiaCalcularCorrectamenteEntreFinEInicioDeAnio() {
        Socio socio = new Socio();
        socio.setFechaIngreso(LocalDate.of(2020, 12, 31));

        int antiguedad = socio.calcularAntiguedadEnAnios(LocalDate.of(2026, 1, 1));

        assertEquals(5, antiguedad);
    }

    @Test
    void deberiaCalcularCorrectamenteConFechaBisiesta() {
        Socio socio = new Socio();
        socio.setFechaIngreso(LocalDate.of(2020, 2, 29));

        int antiguedad = socio.calcularAntiguedadEnAnios(LocalDate.of(2024, 2, 29));

        assertEquals(4, antiguedad);
    }

    @Test
    void deberiaMantenerUnResultadoConsistenteSiLaFechaReferenciaEsAnterior() {
        Socio socio = new Socio();
        socio.setFechaIngreso(LocalDate.of(2026, 7, 15));

        int antiguedad = socio.calcularAntiguedadEnAnios(LocalDate.of(2025, 7, 15));

        assertEquals(-1, antiguedad);
    }
}

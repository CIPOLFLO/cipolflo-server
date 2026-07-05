package com.cipolflo.server.shared.pdf;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PdfFormatoTest {

    @Test
    void textoDeberiaUsarGuionCuandoEsNull() {
        assertEquals("-", PdfFormato.texto(null));
        assertEquals("42", PdfFormato.texto(42));
    }

    @Test
    void fechaDeberiaFormatearseDdMmYyyy() {
        assertEquals("-", PdfFormato.fecha(null));
        assertEquals("10/08/2026", PdfFormato.fecha(LocalDate.of(2026, 8, 10)));
    }

    @Test
    void horaDeberiaFormatearseHhMm() {
        assertEquals("-", PdfFormato.hora(null));
        assertEquals("09:05", PdfFormato.hora(LocalTime.of(9, 5)));
    }

    @Test
    void importeDeberiaLlevarPrefijoPeso() {
        assertEquals("-", PdfFormato.importe(null));
        assertEquals("$15000", PdfFormato.importe(BigDecimal.valueOf(15000)));
    }

    @Test
    void booleanoDeberiaMostrarSiNoOGuion() {
        assertEquals("-", PdfFormato.booleano(null));
        assertEquals("Sí", PdfFormato.booleano(true));
        assertEquals("No", PdfFormato.booleano(false));
    }
}

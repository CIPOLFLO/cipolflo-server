package com.cipolflo.server.servicios.costo;

import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.domain.TarifaServicio;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EstrategiasTest {

    private Servicio servicio(Integer capacidad, BigDecimal costoPersonaExtra) {
        Servicio s = new Servicio();
        s.setNombre("test");
        s.setProcedencia(Procedencia.CAMPING);
        s.setCapacidad(capacidad);
        s.setCostoPersonaExtra(costoPersonaExtra);
        s.setHabilitado(true);
        return s;
    }

    private TarifaServicio tarifa(BigDecimal precio) {
        TarifaServicio tarifa = mock(TarifaServicio.class);
        when(tarifa.getPrecio()).thenReturn(precio);
        return tarifa;
    }

    private CalculoCostoParams params(
            Servicio servicio,
            BigDecimal precio,
            Integer cantidadTotal,
            Integer cantidad,
            Integer cantidadMenores,
            long dias,
            long horas
    ) {
        return new CalculoCostoParams(
                servicio,
                precio,
                cantidadTotal,
                cantidad,
                cantidadMenores,
                dias,
                horas
        );
    }

    // --- POR_DIA ---

    @Test
    void porDia_multiplicaPorDias() {
        Servicio s = servicio(null, null);
        BigDecimal resultado = new EstrategiaCostoPorDia().calcular(
                params(s, new BigDecimal("1000"), null, null, null, 3, 0));
        assertEquals(new BigDecimal("3000"), resultado);
    }

    @Test
    void porDia_usaPrecioDeLaTarifa() {
        Servicio s = servicio(null, null);
        BigDecimal resultado = new EstrategiaCostoPorDia().calcular(
                params(s, new BigDecimal("700"), null, null, null, 3, 0));
        assertEquals(new BigDecimal("2100"), resultado);
    }

    @Test
    void porDia_unDia_retornaPrecioBase() {
        Servicio s = servicio(null, null);
        BigDecimal resultado = new EstrategiaCostoPorDia().calcular(
                params(s, new BigDecimal("500"), null, null, null, 1, 0));
        assertEquals(new BigDecimal("500"), resultado);
    }

    @Test
    void porDia_conExcedente_sumaCostoExtraMultiplicadoPorDias() {
        Servicio s = servicio(4, new BigDecimal("200"));
        // 7 total, 1 menor → adultosEquivalentes=6, excedente=2 → (1000 + 2×200) × 3 = 4200
        BigDecimal resultado = new EstrategiaCostoPorDia().calcular(
                params(s, new BigDecimal("1000"), 7, null, 1, 3, 0));
        assertEquals(new BigDecimal("4200"), resultado);
    }

// --- POR_HORA ---

    @Test
    void porHora_multiplicaPorHoras() {
        Servicio s = servicio(null, null);
        BigDecimal resultado = new EstrategiaCostoPorHora().calcular(
                params(s, new BigDecimal("200"), null, null, null, 1, 4));
        assertEquals(new BigDecimal("800"), resultado);
    }

    @Test
    void porHora_usaPrecioDeLaTarifa() {
        Servicio s = servicio(null, null);
        BigDecimal resultado = new EstrategiaCostoPorHora().calcular(
                params(s, new BigDecimal("150"), null, null, null, 1, 2));
        assertEquals(new BigDecimal("300"), resultado);
    }

// --- POR_UNIDAD ---

    @Test
    void porUnidad_cantidadNula_asume1() {
        Servicio s = servicio(null, null);
        BigDecimal resultado = new EstrategiaCostoPorUnidad().calcular(
                params(s, new BigDecimal("500"), null, null, null, 1, 0));
        assertEquals(new BigDecimal("500"), resultado);
    }

    @Test
    void porUnidad_multiplicaPorCantidad() {
        Servicio s = servicio(null, null);
        BigDecimal resultado = new EstrategiaCostoPorUnidad().calcular(
                params(s, new BigDecimal("500"), null, 5, null, 1, 0));
        assertEquals(new BigDecimal("2500"), resultado);
    }

    @Test
    void porUnidad_usaPrecioDeLaTarifa() {
        Servicio s = servicio(null, null);
        BigDecimal resultado = new EstrategiaCostoPorUnidad().calcular(
                params(s, new BigDecimal("350"), null, 3, null, 1, 0));
        assertEquals(new BigDecimal("1050"), resultado);
    }

// --- POR_PERSONA ---

    @Test
    void porPersona_sinExcedente_retornaPrecioBase() {
        Servicio s = servicio(4, new BigDecimal("300"));
        // 3 personas, 0 menores → adultosEquivalentes=3 ≤ capacidad=4 → excedente=0
        BigDecimal resultado = new EstrategiaCostoPorPersona().calcular(
                params(s, new BigDecimal("2000"), 3, null, 0, 1, 0));
        assertEquals(new BigDecimal("2000"), resultado);
    }

    @Test
    void porPersona_conExcedente_sumaCostoExtra() {
        Servicio s = servicio(4, new BigDecimal("300"));
        // 7 personas, 1 menor → adultosEquivalentes=6, excedente=2 → 2000 + 2*300 = 2600
        BigDecimal resultado = new EstrategiaCostoPorPersona().calcular(
                params(s, new BigDecimal("2000"), 7, null, 1, 1, 0));
        assertEquals(new BigDecimal("2600"), resultado);
    }

    @Test
    void porPersona_menoresDescuentanDelTotal() {
        Servicio s = servicio(4, new BigDecimal("300"));
        // 4 personas, 3 menores → adultosEquivalentes=1, excedente=0
        BigDecimal resultado = new EstrategiaCostoPorPersona().calcular(
                params(s, new BigDecimal("2000"), 4, null, 3, 1, 0));
        assertEquals(new BigDecimal("2000"), resultado);
    }

    @Test
    void porPersona_excedenteCero_cuandoAdultosIgualCapacidad() {
        Servicio s = servicio(4, new BigDecimal("300"));
        // 4 personas, 0 menores → adultosEquivalentes=4, excedente=0
        BigDecimal resultado = new EstrategiaCostoPorPersona().calcular(
                params(s, new BigDecimal("2000"), 4, null, 0, 1, 0));
        assertEquals(new BigDecimal("2000"), resultado);
    }

    @Test
    void porPersona_costoExtraNull_trataComoZero() {
        Servicio s = servicio(2, null);
        // 5 personas, excedente=3, pero costoExtra=null → solo precioBase
        BigDecimal resultado = new EstrategiaCostoPorPersona().calcular(
                params(s, new BigDecimal("2000"), 5, null, 0, 1, 0));
        assertEquals(new BigDecimal("2000"), resultado);
    }

    @Test
    void porPersona_cantidadTotalNull_trataComoZero() {
        Servicio s = servicio(4, new BigDecimal("300"));
        BigDecimal resultado = new EstrategiaCostoPorPersona().calcular(
                params(s, new BigDecimal("2000"), null, null, null, 1, 0));
        assertEquals(new BigDecimal("2000"), resultado);
    }

// --- POR_DIA_POR_PERSONA ---

    @Test
    void porDiaPorPersona_sinExcedente_multiplicaPorDias() {
        Servicio s = servicio(4, new BigDecimal("300"));
        // 3 personas, 0 menores, excedente=0 → 2000 × 3 días = 6000
        BigDecimal resultado = new EstrategiaCostoPorDiaPorPersona().calcular(
                params(s, new BigDecimal("2000"), 3, null, 0, 3, 0));
        assertEquals(new BigDecimal("6000"), resultado);
    }

    @Test
    void porDiaPorPersona_conExcedente_multiplicaPorDias() {
        Servicio s = servicio(4, new BigDecimal("300"));
        // 6 personas, 0 menores, excedente=2 → (2000 + 600) × 2 = 5200
        BigDecimal resultado = new EstrategiaCostoPorDiaPorPersona().calcular(
                params(s, new BigDecimal("2000"), 6, null, 0, 2, 0));
        assertEquals(new BigDecimal("5200"), resultado);
    }
}

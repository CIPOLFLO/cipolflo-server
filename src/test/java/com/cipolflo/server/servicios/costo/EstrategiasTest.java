package com.cipolflo.server.servicios.costo;

import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EstrategiasTest {

    private Servicio servicio(BigDecimal precioParticular, BigDecimal precioSocio,
                              Integer capacidad, BigDecimal costoPersonaExtra) {
        Servicio s = new Servicio();
        s.setNombre("test");
        s.setProcedencia(Procedencia.CAMPING);
        s.setPrecioParticular(precioParticular);
        s.setPrecioSocio(precioSocio);
        s.setCapacidad(capacidad);
        s.setCostoPersonaExtra(costoPersonaExtra);
        s.setModalidadPrecio(ModalidadPrecio.POR_DIA);
        s.setHabilitado(true);
        return s;
    }

    private CalculoCostoParams params(Servicio servicio, TipoCliente tipoCliente,
                                      Integer cantidadTotal, Integer cantidad,
                                      Integer cantidadMenores, long dias, long horas) {
        return new CalculoCostoParams(servicio, tipoCliente, cantidadTotal, cantidad, cantidadMenores, dias, horas);
    }

    // --- POR_DIA ---

    @Test
    void porDia_particular_multiplicaPorDias() {
        Servicio s = servicio(new BigDecimal("1000"), new BigDecimal("700"), null, null);
        BigDecimal resultado = new EstrategiaCostoPorDia().calcular(params(s, TipoCliente.PARTICULAR, null, null, null, 3, 0));
        assertEquals(new BigDecimal("3000"), resultado);
    }

    @Test
    void porDia_socio_usaPrecioSocio() {
        Servicio s = servicio(new BigDecimal("1000"), new BigDecimal("700"), null, null);
        BigDecimal resultado = new EstrategiaCostoPorDia().calcular(params(s, TipoCliente.SOCIO, null, null, null, 3, 0));
        assertEquals(new BigDecimal("2100"), resultado);
    }

    @Test
    void porDia_unDia_retornaPrecioBase() {
        Servicio s = servicio(new BigDecimal("500"), new BigDecimal("300"), null, null);
        BigDecimal resultado = new EstrategiaCostoPorDia().calcular(params(s, TipoCliente.PARTICULAR, null, null, null, 1, 0));
        assertEquals(new BigDecimal("500"), resultado);
    }

    @Test
    void porDia_conExcedente_sumaCostoExtraMultiplicadoPorDias() {
        Servicio s = servicio(new BigDecimal("1000"), new BigDecimal("700"), 4, new BigDecimal("200"));
        // 7 total, 1 menor → adultosEquivalentes=6, excedente=2 → (1000 + 2×200) × 3 = 4200
        BigDecimal resultado = new EstrategiaCostoPorDia().calcular(params(s, TipoCliente.PARTICULAR, 7, null, 1, 3, 0));
        assertEquals(new BigDecimal("4200"), resultado);
    }

    // --- POR_HORA ---

    @Test
    void porHora_particular_multiplicaPorHoras() {
        Servicio s = servicio(new BigDecimal("200"), new BigDecimal("150"), null, null);
        BigDecimal resultado = new EstrategiaCostoPorHora().calcular(params(s, TipoCliente.PARTICULAR, null, null, null, 1, 4));
        assertEquals(new BigDecimal("800"), resultado);
    }

    @Test
    void porHora_socio_usaPrecioSocio() {
        Servicio s = servicio(new BigDecimal("200"), new BigDecimal("150"), null, null);
        BigDecimal resultado = new EstrategiaCostoPorHora().calcular(params(s, TipoCliente.SOCIO, null, null, null, 1, 2));
        assertEquals(new BigDecimal("300"), resultado);
    }

    // --- POR_UNIDAD ---

    @Test
    void porUnidad_cantidadNula_asume1() {
        Servicio s = servicio(new BigDecimal("500"), new BigDecimal("350"), null, null);
        BigDecimal resultado = new EstrategiaCostoPorUnidad().calcular(params(s, TipoCliente.PARTICULAR, null, null, null, 1, 0));
        assertEquals(new BigDecimal("500"), resultado);
    }

    @Test
    void porUnidad_multiplicaPorCantidad() {
        Servicio s = servicio(new BigDecimal("500"), new BigDecimal("350"), null, null);
        BigDecimal resultado = new EstrategiaCostoPorUnidad().calcular(params(s, TipoCliente.PARTICULAR, null, 5, null, 1, 0));
        assertEquals(new BigDecimal("2500"), resultado);
    }

    @Test
    void porUnidad_socio_usaPrecioSocio() {
        Servicio s = servicio(new BigDecimal("500"), new BigDecimal("350"), null, null);
        BigDecimal resultado = new EstrategiaCostoPorUnidad().calcular(params(s, TipoCliente.SOCIO, null, 3, null, 1, 0));
        assertEquals(new BigDecimal("1050"), resultado);
    }

    // --- POR_PERSONA ---

    @Test
    void porPersona_sinExcedente_retornaPrecioBase() {
        Servicio s = servicio(new BigDecimal("2000"), new BigDecimal("1400"), 4, new BigDecimal("300"));
        // 3 personas, 0 menores → adultosEquivalentes=3 ≤ capacidad=4 → excedente=0
        BigDecimal resultado = new EstrategiaCostoPorPersona().calcular(params(s, TipoCliente.PARTICULAR, 3, null, 0, 1, 0));
        assertEquals(new BigDecimal("2000"), resultado);
    }

    @Test
    void porPersona_conExcedente_sumaCostoExtra() {
        Servicio s = servicio(new BigDecimal("2000"), new BigDecimal("1400"), 4, new BigDecimal("300"));
        // 7 personas, 1 menor → adultosEquivalentes=6, excedente=2 → 2000 + 2*300 = 2600
        BigDecimal resultado = new EstrategiaCostoPorPersona().calcular(params(s, TipoCliente.PARTICULAR, 7, null, 1, 1, 0));
        assertEquals(new BigDecimal("2600"), resultado);
    }

    @Test
    void porPersona_menoresDescuentanDelTotal() {
        Servicio s = servicio(new BigDecimal("2000"), new BigDecimal("1400"), 4, new BigDecimal("300"));
        // 4 personas, 3 menores → adultosEquivalentes=1, excedente=0
        BigDecimal resultado = new EstrategiaCostoPorPersona().calcular(params(s, TipoCliente.PARTICULAR, 4, null, 3, 1, 0));
        assertEquals(new BigDecimal("2000"), resultado);
    }

    @Test
    void porPersona_excedenteCero_cuandoAdultosIgualCapacidad() {
        Servicio s = servicio(new BigDecimal("2000"), new BigDecimal("1400"), 4, new BigDecimal("300"));
        // 4 personas, 0 menores → adultosEquivalentes=4, excedente=0
        BigDecimal resultado = new EstrategiaCostoPorPersona().calcular(params(s, TipoCliente.PARTICULAR, 4, null, 0, 1, 0));
        assertEquals(new BigDecimal("2000"), resultado);
    }

    @Test
    void porPersona_costoExtraNull_trataComoZero() {
        Servicio s = servicio(new BigDecimal("2000"), new BigDecimal("1400"), 2, null);
        // 5 personas, excedente=3, pero costoExtra=null → solo precioBase
        BigDecimal resultado = new EstrategiaCostoPorPersona().calcular(params(s, TipoCliente.PARTICULAR, 5, null, 0, 1, 0));
        assertEquals(new BigDecimal("2000"), resultado);
    }

    @Test
    void porPersona_cantidadTotalNull_trataComoZero() {
        Servicio s = servicio(new BigDecimal("2000"), new BigDecimal("1400"), 4, new BigDecimal("300"));
        BigDecimal resultado = new EstrategiaCostoPorPersona().calcular(params(s, TipoCliente.PARTICULAR, null, null, null, 1, 0));
        assertEquals(new BigDecimal("2000"), resultado);
    }

    // --- POR_DIA_POR_PERSONA ---

    @Test
    void porDiaPorPersona_sinExcedente_multiplicaPorDias() {
        Servicio s = servicio(new BigDecimal("2000"), new BigDecimal("1400"), 4, new BigDecimal("300"));
        // 3 personas, 0 menores, excedente=0 → 2000 × 3 días = 6000
        BigDecimal resultado = new EstrategiaCostoPorDiaPorPersona().calcular(params(s, TipoCliente.PARTICULAR, 3, null, 0, 3, 0));
        assertEquals(new BigDecimal("6000"), resultado);
    }

    @Test
    void porDiaPorPersona_conExcedente_multiplicaPorDias() {
        Servicio s = servicio(new BigDecimal("2000"), new BigDecimal("1400"), 4, new BigDecimal("300"));
        // 6 personas, 0 menores, excedente=2 → (2000 + 600) × 2 = 5200
        BigDecimal resultado = new EstrategiaCostoPorDiaPorPersona().calcular(params(s, TipoCliente.PARTICULAR, 6, null, 0, 2, 0));
        assertEquals(new BigDecimal("5200"), resultado);
    }
}

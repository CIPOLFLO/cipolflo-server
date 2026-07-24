package com.cipolflo.server.servicios.costo;

import com.cipolflo.server.servicios.domain.TarifaServicio;
import com.cipolflo.server.servicios.domain.enums.TipoClienteTarifa;
import com.cipolflo.server.servicios.exception.ServicioValidacionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResolutorTarifaServicioTest {

    private ResolutorTarifaServicio resolutor;

    @BeforeEach
    void setUp() {
        resolutor = new ResolutorTarifaServicio();
    }

    @Test
    void deberiaResolverTarifaParticular() {
        TarifaServicio tarifaParticular = tarifa(
                TipoClienteTarifa.PARTICULAR,
                null,
                null
        );

        TarifaServicio resultado = resolutor.resolver(
                List.of(tarifaParticular),
                TipoClienteTarifa.PARTICULAR,
                null
        );

        assertSame(tarifaParticular, resultado);
    }

    @Test
    void deberiaResolverTarifaDeSocioComunDentroDelRangoDeAntiguedad() {
        TarifaServicio tarifaSocioComun = tarifa(
                TipoClienteTarifa.SOCIO_COMUN,
                2,
                10
        );

        TarifaServicio resultado = resolutor.resolver(
                List.of(tarifaSocioComun),
                TipoClienteTarifa.SOCIO_COMUN,
                5
        );

        assertSame(tarifaSocioComun, resultado);
    }

    @Test
    void deberiaAceptarElLimiteMinimoDeAntiguedad() {
        TarifaServicio tarifaSocioComun = tarifa(
                TipoClienteTarifa.SOCIO_COMUN,
                2,
                10
        );

        TarifaServicio resultado = resolutor.resolver(
                List.of(tarifaSocioComun),
                TipoClienteTarifa.SOCIO_COMUN,
                2
        );

        assertSame(tarifaSocioComun, resultado);
    }

    @Test
    void deberiaAceptarElLimiteMaximoDeAntiguedad() {
        TarifaServicio tarifaSocioComun = tarifa(
                TipoClienteTarifa.SOCIO_COMUN,
                2,
                10
        );

        TarifaServicio resultado = resolutor.resolver(
                List.of(tarifaSocioComun),
                TipoClienteTarifa.SOCIO_COMUN,
                10
        );

        assertSame(tarifaSocioComun, resultado);
    }

    @Test
    void deberiaResolverTarifaSinLimiteMaximo() {
        TarifaServicio tarifaSocioComun = tarifa(
                TipoClienteTarifa.SOCIO_COMUN,
                5,
                null
        );

        TarifaServicio resultado = resolutor.resolver(
                List.of(tarifaSocioComun),
                TipoClienteTarifa.SOCIO_COMUN,
                12
        );

        assertSame(tarifaSocioComun, resultado);
    }

    @Test
    void deberiaResolverTarifaSinLimiteMinimo() {
        TarifaServicio tarifaSocioComun = tarifa(
                TipoClienteTarifa.SOCIO_COMUN,
                null,
                5
        );

        TarifaServicio resultado = resolutor.resolver(
                List.of(tarifaSocioComun),
                TipoClienteTarifa.SOCIO_COMUN,
                3
        );

        assertSame(tarifaSocioComun, resultado);
    }

    @Test
    void deberiaUsarTarifaDeSocioComunComoFallbackParaSocioPolicia() {
        TarifaServicio tarifaSocioComun = tarifa(
                TipoClienteTarifa.SOCIO_COMUN,
                0,
                20
        );

        TarifaServicio resultado = resolutor.resolver(
                List.of(tarifaSocioComun),
                TipoClienteTarifa.SOCIO_POLICIA,
                7
        );

        assertSame(tarifaSocioComun, resultado);
    }

    @Test
    void deberiaUsarTarifaDeSocioComunComoFallbackParaPoliciaRetirado() {
        TarifaServicio tarifaSocioComun = tarifa(
                TipoClienteTarifa.SOCIO_COMUN,
                0,
                null
        );

        TarifaServicio resultado = resolutor.resolver(
                List.of(tarifaSocioComun),
                TipoClienteTarifa.SOCIO_POLICIA_RETIRADO,
                25
        );

        assertSame(tarifaSocioComun, resultado);
    }

    @Test
    void deberiaPriorizarLaTarifaEspecificaSobreElFallback() {
        TarifaServicio tarifaSocioComun = tarifa(
                TipoClienteTarifa.SOCIO_COMUN,
                0,
                null
        );

        TarifaServicio tarifaSocioPolicia = tarifa(
                TipoClienteTarifa.SOCIO_POLICIA,
                0,
                null
        );

        TarifaServicio resultado = resolutor.resolver(
                List.of(tarifaSocioComun, tarifaSocioPolicia),
                TipoClienteTarifa.SOCIO_POLICIA,
                8
        );

        assertSame(tarifaSocioPolicia, resultado);
    }

    @Test
    void deberiaLanzarExcepcionCuandoLaAntiguedadEstaFueraDelRango() {
        TarifaServicio tarifaSocioComun = tarifa(
                TipoClienteTarifa.SOCIO_COMUN,
                5,
                10
        );

        assertThrows(
                ServicioValidacionException.class,
                () -> resolutor.resolver(
                        List.of(tarifaSocioComun),
                        TipoClienteTarifa.SOCIO_COMUN,
                        3
                )
        );
    }

    @Test
    void deberiaLanzarExcepcionCuandoNoExisteTarifaAplicable() {
        assertThrows(
                ServicioValidacionException.class,
                () -> resolutor.resolver(
                        List.of(),
                        TipoClienteTarifa.PARTICULAR,
                        null
                )
        );
    }

    @Test
    void deberiaLanzarExcepcionCuandoNoExisteTarifaEspecificaNiFallback() {
        TarifaServicio tarifaParticular = tarifa(
                TipoClienteTarifa.PARTICULAR,
                null,
                null
        );

        assertThrows(
                ServicioValidacionException.class,
                () -> resolutor.resolver(
                        List.of(tarifaParticular),
                        TipoClienteTarifa.SOCIO_POLICIA,
                        5
                )
        );
    }

    private TarifaServicio tarifa(
            TipoClienteTarifa tipoCliente,
            Integer antiguedadMinima,
            Integer antiguedadMaxima
    ) {
        TarifaServicio tarifa = mock(TarifaServicio.class);

        when(tarifa.getTipoCliente()).thenReturn(tipoCliente);
        when(tarifa.getAntiguedadMinima()).thenReturn(antiguedadMinima);
        when(tarifa.getAntiguedadMaxima()).thenReturn(antiguedadMaxima);

        return tarifa;
    }
}
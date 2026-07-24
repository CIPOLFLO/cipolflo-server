package com.cipolflo.server.servicios.costo;

import com.cipolflo.server.clientes.domain.Particular;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.CategoriaSocio;
import com.cipolflo.server.clientes.service.IConsultaClienteParaCosto;
import com.cipolflo.server.servicios.domain.TarifaServicio;
import com.cipolflo.server.servicios.domain.enums.TipoClienteTarifa;
import com.cipolflo.server.servicios.service.IConsultaServicioParaCosto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResolutorTarifaAplicableTest {

    private static final Long SERVICIO_ID = 1L;
    private static final Long CLIENTE_ID = 10L;

    @Mock
    private IConsultaServicioParaCosto consultaServicio;

    @Mock
    private IConsultaClienteParaCosto consultaCliente;

    @Mock
    private ResolutorTarifaServicio resolutorTarifaServicio;

    @Mock
    private Particular particular;

    @Mock
    private Socio socio;

    @Mock
    private TarifaServicio tarifa;

    private ResolutorTarifaAplicable resolutorTarifaAplicable;

    @BeforeEach
    void setUp() {
        resolutorTarifaAplicable = new ResolutorTarifaAplicable(
                consultaServicio,
                consultaCliente,
                resolutorTarifaServicio
        );
    }

    @Test
    void clienteIdNulo_resuelveComoParticularSinAntiguedad() {
        List<TarifaServicio> tarifas = List.of(tarifa);
        when(consultaServicio.obtenerTarifas(SERVICIO_ID)).thenReturn(tarifas);
        when(resolutorTarifaServicio.resolver(tarifas, TipoClienteTarifa.PARTICULAR, null))
                .thenReturn(tarifa);

        TarifaServicio resultado = resolutorTarifaAplicable.resolver(SERVICIO_ID, null);

        assertEquals(tarifa, resultado);
        verifyNoInteractions(consultaCliente);
    }

    @Test
    void clienteParticular_resuelveComoParticularSinAntiguedad() {
        List<TarifaServicio> tarifas = List.of(tarifa);
        when(consultaCliente.obtenerCliente(CLIENTE_ID)).thenReturn(particular);
        when(consultaServicio.obtenerTarifas(SERVICIO_ID)).thenReturn(tarifas);
        when(resolutorTarifaServicio.resolver(tarifas, TipoClienteTarifa.PARTICULAR, null))
                .thenReturn(tarifa);

        TarifaServicio resultado = resolutorTarifaAplicable.resolver(SERVICIO_ID, CLIENTE_ID);

        assertEquals(tarifa, resultado);
    }

    @Test
    void clienteSocio_resuelveTipoDeTarifaYAntiguedadDesdeElSocio() {
        List<TarifaServicio> tarifas = List.of(tarifa);
        when(socio.getCategoriaSocio()).thenReturn(CategoriaSocio.SOCIO_COMUN);
        when(socio.calcularAntiguedadEnAnios(any(LocalDate.class))).thenReturn(5);
        when(consultaCliente.obtenerCliente(CLIENTE_ID)).thenReturn(socio);
        when(consultaServicio.obtenerTarifas(SERVICIO_ID)).thenReturn(tarifas);
        when(resolutorTarifaServicio.resolver(tarifas, TipoClienteTarifa.SOCIO_COMUN, 5))
                .thenReturn(tarifa);

        TarifaServicio resultado = resolutorTarifaAplicable.resolver(SERVICIO_ID, CLIENTE_ID);

        assertEquals(tarifa, resultado);
        verify(resolutorTarifaServicio).resolver(tarifas, TipoClienteTarifa.SOCIO_COMUN, 5);
    }
}

package com.cipolflo.server.integraciones.mensajeria.ai.tools;

import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.clientes.dto.BusquedaCedulaResponseDto;
import com.cipolflo.server.clientes.dto.BusquedaRutResponseDto;
import com.cipolflo.server.clientes.dto.EstadoSocioResponseDto;
import com.cipolflo.server.clientes.dto.UltimaCuotaDto;
import com.cipolflo.server.clientes.exception.ClienteNotFoundException;
import com.cipolflo.server.clientes.service.IClienteService;
import com.cipolflo.server.clientes.service.IConsultaClienteDetalle;
import com.cipolflo.server.clientes.service.IPagoCuotaService;
import com.cipolflo.server.reservas.dto.ClienteDetalleReservaDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuotaToolsTest {

    @Mock
    private IConsultaClienteDetalle consultaClienteDetalle;

    @Mock
    private IClienteService clienteService;

    @Mock
    private IPagoCuotaService pagoCuotaService;

    @InjectMocks
    private CuotaTools cuotaTools;

    private ClienteDetalleReservaDto socio(Long id, String nombre, String cedula) {
        return new ClienteDetalleReservaDto(id, nombre, cedula, null, "099000000", nombre + "@mail.com", TipoCliente.SOCIO);
    }

    @Test
    void sinNombreNiIdentificador_deberiaPedirUno() {
        String resultado = cuotaTools.consultarEstadoCuota(null, null);

        assertTrue(resultado.toLowerCase().contains("nombre") || resultado.toLowerCase().contains("cédula")
                || resultado.toLowerCase().contains("cedula"));
        verify(consultaClienteDetalle, never()).getIdsByNombre(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void busquedaPorCedula_unSoloMatch() {
        when(clienteService.buscarPorCedula("12345678"))
                .thenReturn(new BusquedaCedulaResponseDto(1L, "Juan Pérez", "12345678", "099111111",
                        "juan@mail.com", null, TipoCliente.SOCIO));
        when(clienteService.consultarEstadoSocio(1L))
                .thenReturn(new EstadoSocioResponseDto(1L, EstadoSocio.ACTIVO, 5, 0));
        when(pagoCuotaService.calcularUltimaCuotaPaga(1L))
                .thenReturn(new UltimaCuotaDto(2026, 6, "junio", "Junio 2026"));

        String resultado = cuotaTools.consultarEstadoCuota(null, "12345678");

        assertTrue(resultado.contains("Juan Pérez"));
        assertTrue(resultado.contains("ACTIVO"));
        assertTrue(resultado.contains("Junio 2026"));
    }

    @Test
    void busquedaPorRut_unSoloMatch() {
        String rut = "212345670019";
        when(clienteService.buscarPorRut(rut))
                .thenReturn(new BusquedaRutResponseDto(3L, "Empresa SA", rut, "099333333",
                        "empresa@mail.com", null, TipoCliente.EMPRESA));

        String resultado = cuotaTools.consultarEstadoCuota(null, rut);

        assertTrue(resultado.contains("Empresa SA"));
        assertTrue(resultado.toLowerCase().contains("no es socio"));
        verify(clienteService, never()).consultarEstadoSocio(anyLong());
    }

    @Test
    void busquedaPorNombre_unSoloMatch() {
        when(consultaClienteDetalle.getIdsByNombre("Juan Pérez")).thenReturn(List.of(1L));
        when(consultaClienteDetalle.getDetallClienteSimple(1L)).thenReturn(socio(1L, "Juan Pérez", "12345678"));
        when(clienteService.consultarEstadoSocio(1L))
                .thenReturn(new EstadoSocioResponseDto(1L, EstadoSocio.ACTIVO, 5, 2));
        when(pagoCuotaService.calcularUltimaCuotaPaga(1L)).thenReturn(null);

        String resultado = cuotaTools.consultarEstadoCuota("Juan Pérez", null);

        assertTrue(resultado.contains("Juan Pérez"));
        assertTrue(resultado.toLowerCase().contains("nunca pagó") || resultado.toLowerCase().contains("nunca pago"));
    }

    @Test
    void busquedaPorNombre_homonimos_deberiaResponderPorTodosRotuladosPorCedula() {
        when(consultaClienteDetalle.getIdsByNombre("Juan Pérez")).thenReturn(List.of(1L, 2L));
        when(consultaClienteDetalle.getDetallClienteSimple(1L)).thenReturn(socio(1L, "Juan Pérez", "11111111"));
        when(consultaClienteDetalle.getDetallClienteSimple(2L)).thenReturn(socio(2L, "Juan Pérez", "22222222"));
        when(clienteService.consultarEstadoSocio(1L))
                .thenReturn(new EstadoSocioResponseDto(1L, EstadoSocio.ACTIVO, 1, 0));
        when(clienteService.consultarEstadoSocio(2L))
                .thenReturn(new EstadoSocioResponseDto(2L, EstadoSocio.INACTIVO, 2, 4));
        when(pagoCuotaService.calcularUltimaCuotaPaga(anyLong())).thenReturn(null);

        String resultado = cuotaTools.consultarEstadoCuota("Juan Pérez", null);

        assertTrue(resultado.contains("11111111"));
        assertTrue(resultado.contains("22222222"));
        assertTrue(resultado.contains("ACTIVO"));
        assertTrue(resultado.contains("INACTIVO"));
    }

    @Test
    void homonimos_unoEsSocioOtroNo_elNoSocioDeberiaAclararloEnVezDeOmitirse() {
        ClienteDetalleReservaDto particular =
                new ClienteDetalleReservaDto(2L, "Juan Pérez", "22222222", null, "099", "mail", TipoCliente.PARTICULAR);
        when(consultaClienteDetalle.getIdsByNombre("Juan Pérez")).thenReturn(List.of(1L, 2L));
        when(consultaClienteDetalle.getDetallClienteSimple(1L)).thenReturn(socio(1L, "Juan Pérez", "11111111"));
        when(consultaClienteDetalle.getDetallClienteSimple(2L)).thenReturn(particular);
        when(clienteService.consultarEstadoSocio(1L))
                .thenReturn(new EstadoSocioResponseDto(1L, EstadoSocio.ACTIVO, 1, 0));
        when(pagoCuotaService.calcularUltimaCuotaPaga(1L)).thenReturn(null);

        String resultado = cuotaTools.consultarEstadoCuota("Juan Pérez", null);

        assertTrue(resultado.contains("11111111"));
        assertTrue(resultado.contains("22222222"));
        assertTrue(resultado.toLowerCase().contains("no es socio"));
        verify(clienteService, never()).consultarEstadoSocio(2L);
    }

    @Test
    void busquedaPorNombre_sinCoincidencias() {
        when(consultaClienteDetalle.getIdsByNombre("Nadie")).thenReturn(List.of());

        String resultado = cuotaTools.consultarEstadoCuota("Nadie", null);

        assertTrue(resultado.toLowerCase().contains("no encontré"));
    }

    @Test
    void socioSinNingunaCuotaPaga_deberiaDecirloExplicitamente() {
        when(consultaClienteDetalle.getIdsByNombre("Juan Pérez")).thenReturn(List.of(1L));
        when(consultaClienteDetalle.getDetallClienteSimple(1L)).thenReturn(socio(1L, "Juan Pérez", "12345678"));
        when(clienteService.consultarEstadoSocio(1L))
                .thenReturn(new EstadoSocioResponseDto(1L, EstadoSocio.ACTIVO, 5, 3));
        when(pagoCuotaService.calcularUltimaCuotaPaga(1L)).thenReturn(null);

        String resultado = cuotaTools.consultarEstadoCuota("Juan Pérez", null);

        assertTrue(resultado.toLowerCase().contains("nunca pagó") || resultado.toLowerCase().contains("nunca pago"));
    }

    @Test
    void socioConMesesDeAtraso_deberiaInformarlos() {
        when(consultaClienteDetalle.getIdsByNombre("Juan Pérez")).thenReturn(List.of(1L));
        when(consultaClienteDetalle.getDetallClienteSimple(1L)).thenReturn(socio(1L, "Juan Pérez", "12345678"));
        when(clienteService.consultarEstadoSocio(1L))
                .thenReturn(new EstadoSocioResponseDto(1L, EstadoSocio.ACTIVO, 5, 2));
        when(pagoCuotaService.calcularUltimaCuotaPaga(1L))
                .thenReturn(new UltimaCuotaDto(2026, 1, "enero", "Enero 2026"));

        String resultado = cuotaTools.consultarEstadoCuota("Juan Pérez", null);

        assertTrue(resultado.contains("2"));
    }

    @Test
    void identificadorConPuntosYGuion_deberiaNormalizarseAntesDeBuscar() {
        when(clienteService.buscarPorCedula("1.234.567-8"))
                .thenReturn(new BusquedaCedulaResponseDto(1L, "Juan Pérez", "12345678", "099", "mail", null,
                        TipoCliente.SOCIO));
        when(clienteService.consultarEstadoSocio(1L))
                .thenReturn(new EstadoSocioResponseDto(1L, EstadoSocio.ACTIVO, 5, 0));
        when(pagoCuotaService.calcularUltimaCuotaPaga(1L)).thenReturn(null);

        String resultado = cuotaTools.consultarEstadoCuota(null, "1.234.567-8");

        assertTrue(resultado.contains("Juan Pérez"));
    }

    @Test
    void identificadorInexistente_deberiaResponderSinLanzarExcepcion() {
        when(clienteService.buscarPorCedula("12345678")).thenThrow(new ClienteNotFoundException("No existe"));

        String resultado = cuotaTools.consultarEstadoCuota(null, "12345678");

        assertTrue(resultado.toLowerCase().contains("no encontré"));
    }

    @Test
    void masDeDiezCoincidenciasPorNombre_deberiaDevolverDiezYAvisarQueHayMas() {
        List<Long> ids = new ArrayList<>();
        for (long i = 1; i <= 12; i++) {
            ids.add(i);
        }
        for (long i = 1; i <= 10; i++) {
            when(consultaClienteDetalle.getDetallClienteSimple(i)).thenReturn(socio(i, "Homónimo", "1000000" + i));
            when(clienteService.consultarEstadoSocio(i)).thenReturn(new EstadoSocioResponseDto(i, EstadoSocio.ACTIVO, null, 0));
        }
        when(consultaClienteDetalle.getIdsByNombre("Homónimo")).thenReturn(ids);
        when(pagoCuotaService.calcularUltimaCuotaPaga(anyLong())).thenReturn(null);

        String resultado = cuotaTools.consultarEstadoCuota("Homónimo", null);

        verify(clienteService, times(10)).consultarEstadoSocio(anyLong());
        assertTrue(resultado.toLowerCase().contains("más resultados") || resultado.toLowerCase().contains("hay más"));
    }
}

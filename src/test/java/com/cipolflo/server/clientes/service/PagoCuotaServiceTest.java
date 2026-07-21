package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.PagoCuota;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import com.cipolflo.server.clientes.dto.PagoCuotaResponseDto;
import com.cipolflo.server.clientes.dto.PeriodoCuotaDto;
import com.cipolflo.server.clientes.dto.RegistroPagoCuotaRequestDto;
import com.cipolflo.server.clientes.dto.UltimaCuotaDto;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import com.cipolflo.server.clientes.exception.PagoCuotaNotFoundException;
import com.cipolflo.server.clientes.exception.SocioNotFoundException;
import com.cipolflo.server.clientes.pdf.ComprobantePagoCuotaContenidoPdf;
import com.cipolflo.server.clientes.repository.ClienteRepository;
import com.cipolflo.server.clientes.repository.PagoCuotaRepository;
import com.cipolflo.server.finanzas.dto.FinanzaCrearRequestDto;
import com.cipolflo.server.finanzas.service.IFinanzaService;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.export.ArchivoExportado;
import com.cipolflo.server.shared.pdf.IPdfGeneratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoCuotaServiceTest {

    @Mock
    private PagoCuotaRepository pagoCuotaRepository;

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private IFinanzaService finanzaService;
    @Mock
    private IPdfGeneratorService pdfGeneratorService;

    @InjectMocks
    private PagoCuotaService pagoCuotaService;

    private Socio crearSocio() {
        Socio socio = new Socio();
        socio.setId(1L);
        socio.setNombreCompleto("Juan Pérez");
        socio.setCedula("12345678");
        socio.setTelefono("099000000");
        socio.setMail("juan@mail.com");
        socio.setNumeroSocio(5);
        socio.setEstado(EstadoSocio.ACTIVO);
        socio.setFechaNacimiento(LocalDate.of(1990, Month.JANUARY, 1));
        socio.setPais("Uruguay");
        socio.setDepartamento("Montevideo");
        socio.setCiudad("Montevideo");
        socio.setFechaIngreso(LocalDate.of(2026, Month.JANUARY, 1));
        socio.setMetodoCobro(MetodoCobro.EFECTIVO);
        return socio;
    }

    private PagoCuota pago(Integer anio, Integer mes) {
        return PagoCuota.crear(
                1L,
                anio,
                mes,
                Instant.parse("2026-06-15T03:00:00Z"),
                BigDecimal.valueOf(5000),
                MetodoCobro.EFECTIVO,
                "Pago en caja"
        );
    }
    private void mockRegistroPago() {
        Socio socio = crearSocio();

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(pagoCuotaRepository.findTopBySocioIdOrderByAnioDescMesDesc(1L))
                .thenReturn(Optional.empty());
        when(pagoCuotaRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void deberiaCalcularUltimaCuotaPaga() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(crearSocio()));
        when(pagoCuotaRepository.findTopBySocioIdOrderByAnioDescMesDesc(1L))
                .thenReturn(Optional.of(pago(2026, 6)));

        UltimaCuotaDto dto = pagoCuotaService.calcularUltimaCuotaPaga(1L);

        assertNotNull(dto);
        assertEquals(2026, dto.anio());
        assertEquals(6, dto.mes());
        assertEquals("junio", dto.nombreMes());
        assertEquals("Junio 2026", dto.descripcion());

        verify(clienteRepository).findById(1L);
        verify(pagoCuotaRepository).findTopBySocioIdOrderByAnioDescMesDesc(1L);
    }

    @Test
    void deberiaRetornarNullCuandoSocioNoTieneCuotasPagas() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(crearSocio()));
        when(pagoCuotaRepository.findTopBySocioIdOrderByAnioDescMesDesc(1L))
                .thenReturn(Optional.empty());

        UltimaCuotaDto dto = pagoCuotaService.calcularUltimaCuotaPaga(1L);

        assertNull(dto);

        verify(clienteRepository).findById(1L);
        verify(pagoCuotaRepository).findTopBySocioIdOrderByAnioDescMesDesc(1L);
    }

    @Test
    void deberiaCalcularPeriodosCubiertosCuandoNoHayPagosPrevios() {
        Socio socio = crearSocio();
        socio.setFechaIngreso(LocalDate.of(2026, Month.JANUARY, 1));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(pagoCuotaRepository.findTopBySocioIdOrderByAnioDescMesDesc(1L)).thenReturn(Optional.empty());

        List<PeriodoCuotaDto> periodos =
                pagoCuotaService.calcularPeriodosCubiertos(1L, 3);

        assertEquals(3, periodos.size());

        assertEquals(2026, periodos.get(0).anio());
        assertEquals(1, periodos.get(0).mes());
        assertEquals("enero", periodos.get(0).nombreMes());
        assertEquals("Enero 2026", periodos.get(0).descripcion());

        assertEquals(2026, periodos.get(1).anio());
        assertEquals(2, periodos.get(1).mes());
        assertEquals("febrero", periodos.get(1).nombreMes());
        assertEquals("Febrero 2026", periodos.get(1).descripcion());

        assertEquals(2026, periodos.get(2).anio());
        assertEquals(3, periodos.get(2).mes());
        assertEquals("marzo", periodos.get(2).nombreMes());
        assertEquals("Marzo 2026", periodos.get(2).descripcion());

        verify(clienteRepository).findById(1L);
        verify(pagoCuotaRepository).findTopBySocioIdOrderByAnioDescMesDesc(1L);
    }

    @Test
    void deberiaCalcularPeriodosCubiertosCuandoYaHayPagosRegistrados() {
        Socio socio = crearSocio();
        socio.setFechaIngreso(LocalDate.of(2026, Month.JANUARY, 1));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(pagoCuotaRepository.findTopBySocioIdOrderByAnioDescMesDesc(1L))
                .thenReturn(Optional.of(pago(2026, 3)));

        List<PeriodoCuotaDto> periodos =
                pagoCuotaService.calcularPeriodosCubiertos(1L, 2);

        assertEquals(2, periodos.size());

        assertEquals(2026, periodos.get(0).anio());
        assertEquals(4, periodos.get(0).mes());
        assertEquals("abril", periodos.get(0).nombreMes());
        assertEquals("Abril 2026", periodos.get(0).descripcion());

        assertEquals(2026, periodos.get(1).anio());
        assertEquals(5, periodos.get(1).mes());
        assertEquals("mayo", periodos.get(1).nombreMes());
        assertEquals("Mayo 2026", periodos.get(1).descripcion());

        verify(clienteRepository).findById(1L);
        verify(pagoCuotaRepository).findTopBySocioIdOrderByAnioDescMesDesc(1L);
    }
    @Test
    void deberiaLanzarSocioNotFoundExceptionCuandoSocioNoExiste() {
        when(clienteRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                SocioNotFoundException.class,
                () -> pagoCuotaService.calcularUltimaCuotaPaga(99L)
        );

        verify(clienteRepository).findById(99L);
    }

    @Test
    void deberiaRegistrarPagoGenerandoUnaLineaPorMes() {
        Socio socio = crearSocio();
        socio.setFechaIngreso(LocalDate.of(2026, Month.JANUARY, 1));

        RegistroPagoCuotaRequestDto request = new RegistroPagoCuotaRequestDto(
                3,
                BigDecimal.valueOf(15000),
                MetodoCobro.EFECTIVO,
                LocalDate.of(2026, Month.JUNE, 15),
                "Pago en caja"
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(pagoCuotaRepository.findTopBySocioIdOrderByAnioDescMesDesc(1L)).thenReturn(Optional.empty());
        when(pagoCuotaRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<PagoCuotaResponseDto> response =
                pagoCuotaService.registrarPago(1L, request);

        assertEquals(3, response.size());

        assertEquals(2026, response.get(0).anio());
        assertEquals(1, response.get(0).mes());
        assertEquals("Enero 2026", response.get(0).descripcion());

        assertEquals(2026, response.get(1).anio());
        assertEquals(2, response.get(1).mes());
        assertEquals("Febrero 2026", response.get(1).descripcion());

        assertEquals(2026, response.get(2).anio());
        assertEquals(3, response.get(2).mes());
        assertEquals("Marzo 2026", response.get(2).descripcion());

        verify(clienteRepository).findById(1L);
        verify(pagoCuotaRepository).findTopBySocioIdOrderByAnioDescMesDesc(1L);
        verify(pagoCuotaRepository).saveAll(anyList());
        verify(finanzaService, times(request.cantidadCuotas()))
                .registrarPagoCuota(any(FinanzaCrearRequestDto.class));
    }
    @Test
    void deberiaRegistrarFinanzaConFormaPagoEfectivoCuandoMetodoEsCobradora() {
        RegistroPagoCuotaRequestDto request = crearRequest(MetodoCobro.COBRADORA);
        mockRegistroPago();

        pagoCuotaService.registrarPago(1L, request);

        ArgumentCaptor<FinanzaCrearRequestDto> captor =
                ArgumentCaptor.forClass(FinanzaCrearRequestDto.class);

        verify(finanzaService).registrarPagoCuota(captor.capture());

        assertEquals(FormaPago.EFECTIVO, captor.getValue().getFormaPago());
    }

    private RegistroPagoCuotaRequestDto crearRequest(MetodoCobro metodoCobro) {
        return new RegistroPagoCuotaRequestDto(
                1,
                BigDecimal.valueOf(1000),
                metodoCobro,
                LocalDate.now(),
                "Pago de prueba"
        );
    }
    @Test
    void deberiaRegistrarFinanzaConFormaPagoTransferenciaCuandoMetodoEsTransferencia() {
        RegistroPagoCuotaRequestDto request = crearRequest(MetodoCobro.TRANSFERENCIA);
        mockRegistroPago();

        pagoCuotaService.registrarPago(1L, request);

        ArgumentCaptor<FinanzaCrearRequestDto> captor =
                ArgumentCaptor.forClass(FinanzaCrearRequestDto.class);

        verify(finanzaService).registrarPagoCuota(captor.capture());

        assertEquals(FormaPago.TRANSFERENCIA, captor.getValue().getFormaPago());
    }

    @Test
    void deberiaRegistrarFinanzaConFormaPagoDebitoCuandoMetodoEsDebito() {
        RegistroPagoCuotaRequestDto request = crearRequest(MetodoCobro.DEBITO);
        mockRegistroPago();

        pagoCuotaService.registrarPago(1L, request);

        ArgumentCaptor<FinanzaCrearRequestDto> captor =
                ArgumentCaptor.forClass(FinanzaCrearRequestDto.class);

        verify(finanzaService).registrarPagoCuota(captor.capture());

        assertEquals(FormaPago.DEBITO, captor.getValue().getFormaPago());
    }

    @Test
    void deberiaRegistrarFinanzaConFormaPagoTransferenciaCuandoMetodoEsDescuentoSalarial() {
        RegistroPagoCuotaRequestDto request = crearRequest(MetodoCobro.DESCUENTO_SALARIAL);
        mockRegistroPago();

        pagoCuotaService.registrarPago(1L, request);

        ArgumentCaptor<FinanzaCrearRequestDto> captor =
                ArgumentCaptor.forClass(FinanzaCrearRequestDto.class);

        verify(finanzaService).registrarPagoCuota(captor.capture());

        assertEquals(FormaPago.TRANSFERENCIA, captor.getValue().getFormaPago());
    }

    // --- generarComprobantePago ---

    @Test
    void deberiaGenerarComprobantePagoConUnMes() {
        Socio socio = crearSocio();
        PagoCuota pago = pago(2026, 6);
        pago.setId(10L);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(pagoCuotaRepository.findBySocioIdAndIdIn(1L, List.of(10L))).thenReturn(List.of(pago));
        when(pdfGeneratorService.generar(any())).thenReturn("pdf".getBytes());

        ArchivoExportado archivo = pagoCuotaService.generarComprobantePago(1L, List.of(10L));

        assertNotNull(archivo);
        assertTrue(archivo.getNombre().contains("comprobante-pago-cuota-1"));
        assertArrayEquals("pdf".getBytes(), archivo.getContenido());
        verify(pagoCuotaRepository).findBySocioIdAndIdIn(1L, List.of(10L));
    }

    @Test
    void deberiaGenerarComprobantePagoCuandoIdsVieneConDuplicados() {
        Socio socio = crearSocio();
        PagoCuota pago = pago(2026, 6);
        pago.setId(10L);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(pagoCuotaRepository.findBySocioIdAndIdIn(1L, List.of(10L, 10L))).thenReturn(List.of(pago));
        when(pdfGeneratorService.generar(any())).thenReturn("pdf".getBytes());

        ArchivoExportado archivo = pagoCuotaService.generarComprobantePago(1L, List.of(10L, 10L));

        assertNotNull(archivo);
        verify(pdfGeneratorService).generar(any());
    }

    @Test
    void deberiaGenerarComprobantePagoConVariosMesesYSumarElImporte() {
        Socio socio = crearSocio();
        PagoCuota pagoEnero = pago(2026, 1);
        pagoEnero.setId(10L);
        pagoEnero.setImporte(BigDecimal.valueOf(1000));
        PagoCuota pagoFebrero = pago(2026, 2);
        pagoFebrero.setId(11L);
        pagoFebrero.setImporte(BigDecimal.valueOf(1000));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(pagoCuotaRepository.findBySocioIdAndIdIn(1L, List.of(10L, 11L))).thenReturn(List.of(pagoEnero, pagoFebrero));

        ArgumentCaptor<ComprobantePagoCuotaContenidoPdf> captor =
                ArgumentCaptor.forClass(ComprobantePagoCuotaContenidoPdf.class);
        when(pdfGeneratorService.generar(captor.capture())).thenReturn("pdf".getBytes());

        pagoCuotaService.generarComprobantePago(1L, List.of(10L, 11L));

        verify(pdfGeneratorService).generar(any());
    }

    @Test
    void deberiaOrdenarPeriodosCronologicamenteAunqueVenganDesordenados() {
        Socio socio = crearSocio();
        PagoCuota pagoFebrero = pago(2026, 2);
        pagoFebrero.setId(11L);
        PagoCuota pagoEnero = pago(2026, 1);
        pagoEnero.setId(10L);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(pagoCuotaRepository.findBySocioIdAndIdIn(1L, List.of(11L, 10L))).thenReturn(List.of(pagoFebrero, pagoEnero));
        when(pdfGeneratorService.generar(any())).thenReturn("pdf".getBytes());

        ArchivoExportado archivo = pagoCuotaService.generarComprobantePago(1L, List.of(11L, 10L));

        assertNotNull(archivo);
        verify(pdfGeneratorService).generar(any());
    }

    @Test
    void deberiaLanzarPagoCuotaNotFoundExceptionCuandoUnIdNoExiste() {
        Socio socio = crearSocio();
        PagoCuota pago = pago(2026, 6);
        pago.setId(10L);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(pagoCuotaRepository.findBySocioIdAndIdIn(1L, List.of(10L, 99L))).thenReturn(List.of(pago));

        assertThrows(PagoCuotaNotFoundException.class,
                () -> pagoCuotaService.generarComprobantePago(1L, List.of(10L, 99L)));

        verify(pdfGeneratorService, never()).generar(any());
    }

    @Test
    void deberiaLanzarPagoCuotaNotFoundExceptionCuandoUnPagoEsDeOtroSocio() {
        Socio socio = crearSocio();

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(pagoCuotaRepository.findBySocioIdAndIdIn(1L, List.of(10L))).thenReturn(List.of());

        assertThrows(PagoCuotaNotFoundException.class,
                () -> pagoCuotaService.generarComprobantePago(1L, List.of(10L)));

        verify(pdfGeneratorService, never()).generar(any());
    }

    @Test
    void deberiaLanzarSocioNotFoundExceptionCuandoSocioNoExisteEnComprobantePago() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(SocioNotFoundException.class,
                () -> pagoCuotaService.generarComprobantePago(99L, List.of(1L)));

        verify(pagoCuotaRepository, never()).findBySocioIdAndIdIn(any(), any());
        verify(pdfGeneratorService, never()).generar(any());
    }

    @Test
    void deberiaLanzarClienteValidacionExceptionCuandoIdsEsVacio() {
        assertThrows(ClienteValidacionException.class,
                () -> pagoCuotaService.generarComprobantePago(1L, List.of()));

        verify(clienteRepository, never()).findById(any());
        verify(pdfGeneratorService, never()).generar(any());
    }

    @Test
    void deberiaLanzarClienteValidacionExceptionCuandoLosPagosTienenMetodosDeCobroDistintos() {
        Socio socio = crearSocio();
        PagoCuota pagoEfectivo = pago(2026, 1);
        pagoEfectivo.setId(10L);
        PagoCuota pagoTransferencia = PagoCuota.crear(
                1L, 2026, 2, Instant.parse("2026-06-15T03:00:00Z"),
                BigDecimal.valueOf(5000), MetodoCobro.TRANSFERENCIA, null);
        pagoTransferencia.setId(11L);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(pagoCuotaRepository.findBySocioIdAndIdIn(1L, List.of(10L, 11L)))
                .thenReturn(List.of(pagoEfectivo, pagoTransferencia));

        assertThrows(ClienteValidacionException.class,
                () -> pagoCuotaService.generarComprobantePago(1L, List.of(10L, 11L)));

        verify(pdfGeneratorService, never()).generar(any());
    }
}
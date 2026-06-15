package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.clientes.domain.PagoCuota;
import com.cipolflo.server.clientes.domain.Particular;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import com.cipolflo.server.clientes.dto.CuotaPendienteDto;
import com.cipolflo.server.clientes.dto.RegistroPagoCuotaRequestDto;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import com.cipolflo.server.clientes.exception.SocioNotFoundException;
import com.cipolflo.server.clientes.repository.ClienteRepository;
import com.cipolflo.server.clientes.repository.PagoCuotaRepository;
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
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoCuotaServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Montevideo");

    @Mock
    private PagoCuotaRepository pagoCuotaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private PagoCuotaService pagoCuotaService;

    @Test
    void deberiaRegistrarPagoDeUnaCuota() {
        YearMonth periodo = YearMonth.now(ZONA);
        Socio socio = crearSocio(1L, periodo.minusMonths(1), EstadoSocio.ACTIVO);

        RegistroPagoCuotaRequestDto request = new RegistroPagoCuotaRequestDto(
                periodo.getYear(),
                periodo.getMonthValue(),
                1,
                BigDecimal.valueOf(5000),
                MetodoCobro.EFECTIVO,
                LocalDate.of(2026,6,10),
                "Pago mensual"
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(pagoCuotaRepository.existsBySocioIdAndAnioAndMes(eq(1L), anyInt(), anyInt()))
                .thenReturn(false);

        when(pagoCuotaRepository.findBySocioId(1L))
                .thenReturn(List.of(PagoCuota.crear(
                        1L,
                        periodo.getYear(),
                        periodo.getMonthValue(),
                        Instant.now(),
                        BigDecimal.valueOf(5000),
                        MetodoCobro.EFECTIVO,
                        "Pago mensual"
                )));

        pagoCuotaService.registrarPago(1L, request);

        ArgumentCaptor<List<PagoCuota>> captor = ArgumentCaptor.forClass(List.class);
        verify(pagoCuotaRepository).saveAll(captor.capture());

        List<PagoCuota> pagos = captor.getValue();

        assertEquals(1, pagos.size());
        assertEquals(periodo.getYear(), pagos.get(0).getAnio());
        assertEquals(periodo.getMonthValue(), pagos.get(0).getMes());
        assertEquals(BigDecimal.valueOf(5000).setScale(2), pagos.get(0).getImporte());
        assertEquals(MetodoCobro.EFECTIVO, pagos.get(0).getMetodoCobro());

        verify(clienteRepository).save(socio);
    }

    @Test
    void deberiaRegistrarPagoDeVariasCuotas() {
        YearMonth periodo = YearMonth.now(ZONA).minusMonths(2);
        Socio socio = crearSocio(1L, periodo, EstadoSocio.ACTIVO);

        RegistroPagoCuotaRequestDto request = new RegistroPagoCuotaRequestDto(
                periodo.getYear(),
                periodo.getMonthValue(),
                3,
                BigDecimal.valueOf(15000),
                MetodoCobro.TRANSFERENCIA,
                LocalDate.of(2026, 6, 10),
                "Pago de tres meses"
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(pagoCuotaRepository.existsBySocioIdAndAnioAndMes(eq(1L), anyInt(), anyInt()))
                .thenReturn(false);

        when(pagoCuotaRepository.findBySocioId(1L))
                .thenReturn(List.of(
                        pago(periodo),
                        pago(periodo.plusMonths(1)),
                        pago(periodo.plusMonths(2))
                ));

        pagoCuotaService.registrarPago(1L, request);

        ArgumentCaptor<List<PagoCuota>> captor = ArgumentCaptor.forClass(List.class);
        verify(pagoCuotaRepository).saveAll(captor.capture());

        List<PagoCuota> pagos = captor.getValue();

        assertEquals(3, pagos.size());
        assertEquals(periodo.getMonthValue(), pagos.get(0).getMes());
        assertEquals(periodo.plusMonths(1).getMonthValue(), pagos.get(1).getMes());
        assertEquals(periodo.plusMonths(2).getMonthValue(), pagos.get(2).getMes());
        assertTrue(pagos.stream().allMatch(p -> BigDecimal.valueOf(5000).setScale(2).equals(p.getImporte())));
    }

    @Test
    void deberiaLanzarErrorCuandoLaCuotaYaFueRegistrada() {
        YearMonth periodo = YearMonth.now(ZONA);
        Socio socio = crearSocio(1L, periodo, EstadoSocio.ACTIVO);

        RegistroPagoCuotaRequestDto request = new RegistroPagoCuotaRequestDto(
                periodo.getYear(),
                periodo.getMonthValue(),
                1,
                BigDecimal.valueOf(5000),
                MetodoCobro.EFECTIVO,
                LocalDate.of(2026, 6, 10),
                null
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(pagoCuotaRepository.existsBySocioIdAndAnioAndMes(
                1L,
                periodo.getYear(),
                periodo.getMonthValue()
        )).thenReturn(true);

        ClienteValidacionException exception = assertThrows(
                ClienteValidacionException.class,
                () -> pagoCuotaService.registrarPago(1L, request)
        );

        assertEquals("CUOTA_YA_REGISTRADA", exception.getCodigo());
        verify(pagoCuotaRepository, never()).saveAll(anyList());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deberiaRetornarCuotasPendientes() {
        YearMonth inicio = YearMonth.now(ZONA).minusMonths(2);
        YearMonth cuotaPaga = inicio;

        Socio socio = crearSocio(1L, inicio, EstadoSocio.ACTIVO);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(pagoCuotaRepository.findBySocioId(1L))
                .thenReturn(List.of(pago(cuotaPaga)));

        List<CuotaPendienteDto> pendientes = pagoCuotaService.obtenerCuotasPendientes(1L);

        assertEquals(2, pendientes.size());
        assertEquals(inicio.plusMonths(1).getYear(), pendientes.get(0).anio());
        assertEquals(inicio.plusMonths(1).getMonthValue(), pendientes.get(0).mes());
        assertEquals(inicio.plusMonths(2).getYear(), pendientes.get(1).anio());
        assertEquals(inicio.plusMonths(2).getMonthValue(), pendientes.get(1).mes());
    }

    @Test
    void deberiaActualizarSocioAInactivoCuandoDebeTresOMasMeses() {
        YearMonth inicio = YearMonth.now(ZONA).minusMonths(4);
        YearMonth periodoPago = YearMonth.now(ZONA);

        Socio socio = crearSocio(1L, inicio, EstadoSocio.ACTIVO);

        RegistroPagoCuotaRequestDto request = new RegistroPagoCuotaRequestDto(
                periodoPago.getYear(),
                periodoPago.getMonthValue(),
                1,
                BigDecimal.valueOf(5000),
                MetodoCobro.EFECTIVO,
                LocalDate.of(2026, 6, 10),
                null
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(pagoCuotaRepository.existsBySocioIdAndAnioAndMes(eq(1L), anyInt(), anyInt()))
                .thenReturn(false);
        when(pagoCuotaRepository.findBySocioId(1L))
                .thenReturn(List.of(pago(periodoPago)));

        pagoCuotaService.registrarPago(1L, request);

        assertEquals(EstadoSocio.INACTIVO, socio.getEstado());
        assertTrue(socio.getMesesSinPagar() >= 3);
    }

    @Test
    void deberiaActualizarSocioAActivoCuandoDebeMenosDeTresMeses() {
        YearMonth inicio = YearMonth.now(ZONA);
        Socio socio = crearSocio(1L, inicio, EstadoSocio.INACTIVO);

        RegistroPagoCuotaRequestDto request = new RegistroPagoCuotaRequestDto(
                inicio.getYear(),
                inicio.getMonthValue(),
                1,
                BigDecimal.valueOf(5000),
                MetodoCobro.EFECTIVO,
                LocalDate.of(2026, 6, 10),
                null
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(pagoCuotaRepository.existsBySocioIdAndAnioAndMes(eq(1L), anyInt(), anyInt()))
                .thenReturn(false);
        when(pagoCuotaRepository.findBySocioId(1L))
                .thenReturn(List.of(pago(inicio)));

        pagoCuotaService.registrarPago(1L, request);

        assertEquals(EstadoSocio.ACTIVO, socio.getEstado());
        assertEquals(0, socio.getMesesSinPagar());
    }

    @Test
    void noDeberiaReactivarSocioDadoDeBaja() {
        YearMonth inicio = YearMonth.now(ZONA);
        Socio socio = crearSocio(1L, inicio, EstadoSocio.DE_BAJA);

        RegistroPagoCuotaRequestDto request = new RegistroPagoCuotaRequestDto(
                inicio.getYear(),
                inicio.getMonthValue(),
                1,
                BigDecimal.valueOf(5000),
                MetodoCobro.EFECTIVO,
                LocalDate.of(2026, 6, 10),
                null
        );

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(pagoCuotaRepository.existsBySocioIdAndAnioAndMes(eq(1L), anyInt(), anyInt()))
                .thenReturn(false);
        when(pagoCuotaRepository.findBySocioId(1L))
                .thenReturn(List.of(pago(inicio)));

        pagoCuotaService.registrarPago(1L, request);

        assertEquals(EstadoSocio.DE_BAJA, socio.getEstado());
        assertEquals(0, socio.getMesesSinPagar());
    }

    @Test
    void deberiaLanzarErrorCuandoNoExisteSocio() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        RegistroPagoCuotaRequestDto request = new RegistroPagoCuotaRequestDto(
                2026,
                1,
                1,
                BigDecimal.valueOf(5000),
                MetodoCobro.EFECTIVO,
                LocalDate.of(2026, 6, 10),
                null
        );

        assertThrows(SocioNotFoundException.class, () -> pagoCuotaService.registrarPago(99L, request));

        verify(pagoCuotaRepository, never()).saveAll(anyList());
    }

    @Test
    void deberiaLanzarErrorCuandoClienteNoEsSocio() {
        Particular particular = new Particular();
        particular.setId(1L);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(particular));

        RegistroPagoCuotaRequestDto request = new RegistroPagoCuotaRequestDto(
                2026,
                1,
                1,
                BigDecimal.valueOf(5000),
                MetodoCobro.EFECTIVO,
                LocalDate.of(2026, 6, 10),
                null
        );

        assertThrows(SocioNotFoundException.class, () -> pagoCuotaService.registrarPago(1L, request));

        verify(pagoCuotaRepository, never()).saveAll(anyList());
    }

    private Socio crearSocio(Long id, YearMonth fechaIngreso, EstadoSocio estado) {
        Socio socio = new Socio();
        socio.setId(id);
        socio.setNombreCompleto("Juan Pérez");
        socio.setCedula("12345678");
        socio.setTelefono("099111111");
        socio.setEstado(estado);
        socio.setFechaNacimiento(LocalDate.of(1990, Month.JANUARY, 1));
        socio.setPais("Uruguay");
        socio.setDepartamento("Montevideo");
        socio.setCiudad("Montevideo");
        socio.setFechaIngreso(fechaIngreso.atDay(1));
        socio.setMetodoCobro(MetodoCobro.EFECTIVO);
        socio.setMesesSinPagar(0);
        return socio;
    }

    private PagoCuota pago(YearMonth periodo) {
        return PagoCuota.crear(
                1L,
                periodo.getYear(),
                periodo.getMonthValue(),
                Instant.now(),
                BigDecimal.valueOf(5000),
                MetodoCobro.EFECTIVO,
                null
        );
    }
}

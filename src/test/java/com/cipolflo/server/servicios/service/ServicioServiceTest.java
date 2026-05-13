package com.cipolflo.server.servicios.service;

import com.cipolflo.server.reservas.service.IReservaService;
import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.servicios.dto.ServicioRequestDto;
import com.cipolflo.server.servicios.dto.ServicioResponseDto;
import com.cipolflo.server.servicios.exception.ConfirmacionDevolucionRequeridaException;
import com.cipolflo.server.servicios.exception.ReservaNoCancelableException;
import com.cipolflo.server.servicios.repository.ServicioRepository;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.servicios.exception.ServicioNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.time.Instant;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ServicioServiceTest {

    @Mock
    private ServicioRepository servicioRepository;
    @Mock
    private IReservaService reservaService;

    @InjectMocks
    private ServicioService servicioService;

    private Servicio crearServicio(Long id, boolean habilitado) {
        Servicio servicio = new Servicio();
        servicio.setId(id);
        servicio.setNombre("Cabaña");
        servicio.setProcedencia(Procedencia.CAMPING);
        servicio.setCapacidad(4);
        servicio.setCantidad(2);
        servicio.setPrecioSocio(BigDecimal.valueOf(1500));
        servicio.setPrecioParticular(BigDecimal.valueOf(2500));
        servicio.setHabilitado(habilitado);
        servicio.setModalidadPrecio(ModalidadPrecio.POR_DIA);
        return servicio;
    }

    @Test
    void deberiaRetornarDetalleServicioCuandoExiste() {
        Long servicioId = 1L;

        Servicio servicio = crearServicio(servicioId, true);

        when(servicioRepository.findById(servicioId))
                .thenReturn(Optional.of(servicio));

        ServicioResponseDto resultado = servicioService.getDetalleServicio(servicioId);

        assertNotNull(resultado);
        assertEquals(servicioId, resultado.getId());
        assertEquals("Cabaña", resultado.getNombre());
        assertEquals(Procedencia.CAMPING, resultado.getProcedencia());
        assertEquals(4, resultado.getCapacidad());
        assertEquals(2, resultado.getCantidad());
        assertEquals(BigDecimal.valueOf(1500), resultado.getPrecioSocio());
        assertEquals(BigDecimal.valueOf(2500), resultado.getPrecioParticular());
        assertTrue(resultado.getHabilitado());
        assertEquals(ModalidadPrecio.POR_DIA, resultado.getModalidadPrecio());
        verify(servicioRepository).findById(servicioId);
    }

    @Test
    void deberiaLanzarErrorCuandoElServicioNoExiste() {
        Long servicioId = 99L;
        when(servicioRepository.findById(servicioId))
                .thenReturn(Optional.empty());

        ServicioNotFoundException exception =
                assertThrows(ServicioNotFoundException.class, () -> {
                    servicioService.getDetalleServicio(servicioId);
                });

        assertEquals(
                "Servicio no encontrado con id: " + servicioId,
                exception.getMessage()
        );

        verify(servicioRepository).findById(servicioId);
    }

    @Test
    void deberiaDeshabilitarServicioCuandoEstaHabilitado() {
        Long servicioId = 1L;

        Servicio servicio = crearServicio(servicioId, true);

        ServicioRequestDto request = new ServicioRequestDto();
        request.setHabilitado(false);

        when(servicioRepository.findById(servicioId))
                .thenReturn(Optional.of(servicio));
        when(servicioRepository.save(servicio))
                .thenReturn(servicio);

        ServicioResponseDto resultado =
                servicioService.cambiarHabilitacionServicio(servicioId, request);

        assertNotNull(resultado);
        assertFalse(resultado.getHabilitado());

        verify(servicioRepository).findById(servicioId);
        verify(servicioRepository).save(servicio);
    }

    @Test
    void deberiaHabilitarServicioCuandoEstaDeshabilitado() {
        Long servicioId = 1L;

        Servicio servicio = crearServicio(servicioId, false);

        ServicioRequestDto request = new ServicioRequestDto();
        request.setHabilitado(true);

        when(servicioRepository.findById(servicioId))
                .thenReturn(Optional.of(servicio));
        when(servicioRepository.save(servicio))
                .thenReturn(servicio);

        ServicioResponseDto resultado =
                servicioService.cambiarHabilitacionServicio(servicioId, request);

        assertNotNull(resultado);
        assertTrue(resultado.getHabilitado());

        verify(servicioRepository).findById(servicioId);
        verify(servicioRepository).save(servicio);
    }

    @Test
    void deberiaLanzarErrorCuandoElServicioNoExisteAlCambiarHabilitacion() {
        Long servicioId = 99L;

        ServicioRequestDto request = new ServicioRequestDto();
        request.setHabilitado(false);

        when(servicioRepository.findById(servicioId))
                .thenReturn(Optional.empty());

        ServicioNotFoundException exception =
                assertThrows(ServicioNotFoundException.class, () -> {
                    servicioService.cambiarHabilitacionServicio(servicioId, request);
                });

        assertEquals(
                "Servicio no encontrado con id: " + servicioId,
                exception.getMessage()
        );

        verify(servicioRepository).findById(servicioId);
        verify(servicioRepository, never()).save(any());
    }

    @Test
    void deberiaCancelarReservasSeleccionadasNoPagas() {
        Long servicioId = 1L;

        Servicio servicio = new Servicio();
        servicio.setId(servicioId);
        servicio.setHabilitado(true);

        Reserva reserva = Reserva.crear(
                1L,
                servicioId,
                Procedencia.CAMPING,
                Instant.now().plusSeconds(86400),
                Instant.now().plusSeconds(172800),
                false
        );
        ReflectionTestUtils.setField(reserva, "id", 1L);

        ServicioRequestDto request = new ServicioRequestDto();
        request.setHabilitado(false);
        request.setReservasACancelar(List.of(1L));

        when(servicioRepository.findById(servicioId))
                .thenReturn(Optional.of(servicio));
        when(reservaService.obtenerProximasPorServicioEnRango(servicioId))
                .thenReturn(List.of(reserva));
        when(servicioRepository.save(any(Servicio.class)))
                .thenReturn(servicio);

        servicioService.cambiarHabilitacionServicio(servicioId, request);

        verify(reservaService).cancelarTodas(List.of(reserva));
        verify(servicioRepository).save(servicio);
    }

    @Test
    void deberiaLanzarErrorCuandoReservaEstaPagaYSinConfirmarDevolucion() {
        Long servicioId = 1L;

        Servicio servicio = new Servicio();
        servicio.setId(servicioId);
        servicio.setHabilitado(true);

        Reserva reserva = Reserva.crear(
                1L,
                servicioId,
                Procedencia.CAMPING,
                Instant.now().plusSeconds(86400),
                Instant.now().plusSeconds(172800),
                false
        );
        ReflectionTestUtils.setField(reserva, "id", 1L);

        reserva.confirmarPago(
                BigDecimal.valueOf(1500),
                FormaPago.EFECTIVO
        );

        ServicioRequestDto request = new ServicioRequestDto();
        request.setHabilitado(false);
        request.setReservasACancelar(List.of(1L));
        request.setConfirmarDevolucion(false);

        when(servicioRepository.findById(servicioId))
                .thenReturn(Optional.of(servicio));
        when(reservaService.obtenerProximasPorServicioEnRango(servicioId))
                .thenReturn(List.of(reserva));

        ConfirmacionDevolucionRequeridaException exception =
                assertThrows(ConfirmacionDevolucionRequeridaException.class, () -> {
                    servicioService.cambiarHabilitacionServicio(servicioId, request);
                });

        assertEquals(
                "Existen reservas pagas. Debe confirmar la devolución para cancelarlas",
                exception.getMessage()
        );

        verify(reservaService, never()).cancelarTodas(anyList());
    }

    @Test
    void deberiaCancelarReservaPagaCuandoConfirmaDevolucion() {
        Long servicioId = 1L;

        Servicio servicio = new Servicio();
        servicio.setId(servicioId);

        Reserva reserva = Reserva.crear(
                1L,
                servicioId,
                Procedencia.CAMPING,
                Instant.now().plusSeconds(86400),
                Instant.now().plusSeconds(172800),
                false
        );
        ReflectionTestUtils.setField(reserva, "id", 1L);

        reserva.confirmarPago(
                BigDecimal.valueOf(1500),
                FormaPago.EFECTIVO
        );

        ServicioRequestDto request = new ServicioRequestDto();
        request.setHabilitado(false);
        request.setReservasACancelar(List.of(1L));
        request.setConfirmarDevolucion(true);

        when(servicioRepository.findById(servicioId))
                .thenReturn(Optional.of(servicio));
        when(reservaService.obtenerProximasPorServicioEnRango(servicioId))
                .thenReturn(List.of(reserva));
        when(servicioRepository.save(any(Servicio.class)))
                .thenReturn(servicio);

        servicioService.cambiarHabilitacionServicio(servicioId, request);

        verify(reservaService).cancelarTodas(List.of(reserva));
    }

    @Test
    void deberiaLanzarErrorCuandoLasReservasNoSonProximas() {
        Long servicioId = 1L;

        Servicio servicio = new Servicio();
        servicio.setId(servicioId);
        servicio.setHabilitado(true);

        Reserva reservaProxima = Reserva.crear(
                2L,
                servicioId,
                Procedencia.CAMPING,
                Instant.now().plusSeconds(86400),
                Instant.now().plusSeconds(172800),
                false
        );
        ReflectionTestUtils.setField(reservaProxima, "id", 2L);

        ServicioRequestDto request = new ServicioRequestDto();
        request.setHabilitado(false);
        request.setReservasACancelar(List.of(99L));

        when(servicioRepository.findById(servicioId))
                .thenReturn(Optional.of(servicio));
        when(reservaService.obtenerProximasPorServicioEnRango(servicioId))
                .thenReturn(List.of(reservaProxima));

        assertThrows(ReservaNoCancelableException.class, () -> {
            servicioService.cambiarHabilitacionServicio(servicioId, request);
        });

        verify(reservaService, never()).cancelarTodas(anyList());
    }
}

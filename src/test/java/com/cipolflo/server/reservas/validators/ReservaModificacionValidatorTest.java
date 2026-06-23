package com.cipolflo.server.reservas.validators;

import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.reservas.dto.ReservaModificacionRequestDto;
import com.cipolflo.server.reservas.exception.ReservaCodigoError;
import com.cipolflo.server.reservas.exception.ReservaValidacionException;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.repository.ServicioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaModificacionValidatorTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ServicioRepository servicioRepository;

    @InjectMocks
    private ReservaModificacionValidator validator;

    private ReservaModificacionRequestDto mockDto(
            Long servicioId,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String rut
    ) {
        ReservaModificacionRequestDto dto = mock(ReservaModificacionRequestDto.class);
        lenient().when(dto.getServicioId()).thenReturn(servicioId);
        lenient().when(dto.getFechaInicio()).thenReturn(fechaInicio);
        lenient().when(dto.getFechaFin()).thenReturn(fechaFin);
        lenient().when(dto.getRut()).thenReturn(rut);
        return dto;
    }

    private Servicio servicioHabilitado(Long id) {
        Servicio s = new Servicio();
        s.setId(id);
        s.setHabilitado(true);
        return s;
    }

    // ── validarFechas ──────────────────────────────────────────────────────────

    @Test
    void deberiaLanzarExcepcionCuandoFechaInicioEsAnteriorAHoy() {
        ReservaModificacionRequestDto dto = mockDto(
                1L,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(2),
                null
        );

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(1L, TipoReserva.COMUN, dto)
        );
        assertEquals(ReservaCodigoError.FECHA_PASADA.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExcepcionCuandoFechaFinEsAnteriorAFechaInicio() {
        ReservaModificacionRequestDto dto = mockDto(
                1L,
                LocalDate.now().plusDays(3), LocalDate.now().plusDays(1),
                null
        );

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(1L, TipoReserva.COMUN, dto)
        );
        assertEquals(ReservaCodigoError.FECHA_FIN_ANTERIOR_A_INICIO.name(), ex.getCodigo());
    }

    // ── validarServicio ────────────────────────────────────────────────────────

    @Test
    void deberiaLanzarExcepcionCuandoServicioNoExiste() {
        ReservaModificacionRequestDto dto = mockDto(
                99L,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                null
        );
        when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(1L, TipoReserva.COMUN, dto)
        );
        assertEquals(ReservaCodigoError.SERVICIO_NO_DISPONIBLE.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExcepcionCuandoServicioEstaDeshabilitado() {
        Servicio deshabilitado = servicioHabilitado(1L);
        deshabilitado.setHabilitado(false);

        ReservaModificacionRequestDto dto = mockDto(
                1L,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                null
        );
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(deshabilitado));

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(1L, TipoReserva.COMUN, dto)
        );
        assertEquals(ReservaCodigoError.SERVICIO_NO_DISPONIBLE.name(), ex.getCodigo());
    }

    // ── validarSolapamiento ────────────────────────────────────────────────────

    @Test
    void deberiaLanzarExcepcionCuandoExisteSolapamientoConOtraReserva() {
        Long reservaId = 1L;
        ReservaModificacionRequestDto dto = mockDto(
                1L,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5),
                null
        );
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqualAndIdNot(
                eq(1L),
                eq(List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA, EstadoReserva.EN_CURSO)),
                eq(LocalDate.now().plusDays(5)),
                eq(LocalDate.now().plusDays(1)),
                eq(reservaId)
        )).thenReturn(true);

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(reservaId, TipoReserva.COMUN, dto)
        );
        assertEquals(ReservaCodigoError.FECHAS_SOLAPADAS.name(), ex.getCodigo());
    }

    @Test
    void noDeberiaLanzarExcepcionCuandoSoloHaySolapamientoConLaMismaReserva() {
        Long reservaId = 1L;
        ReservaModificacionRequestDto dto = mockDto(
                1L,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5),
                null
        );
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqualAndIdNot(
                any(), any(), any(), any(), eq(reservaId)
        )).thenReturn(false);

        assertDoesNotThrow(() -> validator.validar(reservaId, TipoReserva.COMUN, dto));
    }

    // ── validarRut ─────────────────────────────────────────────────────────────

    @Test
    void deberiaLanzarExcepcionCuandoRutPresenteEnReservaNoColaboracion() {
        ReservaModificacionRequestDto dto = mockDto(
                1L,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                "20123456-7"
        );
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqualAndIdNot(
                any(), any(), any(), any(), any()
        )).thenReturn(false);

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(1L, TipoReserva.COMUN, dto)
        );
        assertEquals(ReservaCodigoError.RUT_SOLO_VALIDO_EN_COLABORACION.name(), ex.getCodigo());
    }

    @Test
    void deberiaPasarValidacionColaboracionConRut() {
        ReservaModificacionRequestDto dto = mockDto(
                1L,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                "20123456-7"
        );
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqualAndIdNot(
                any(), any(), any(), any(), any()
        )).thenReturn(false);

        assertDoesNotThrow(() -> validator.validar(1L, TipoReserva.COLABORACION_SIN_FINES_DE_LUCRO, dto));
    }

    @Test
    void deberiaPasarValidacionSinRut() {
        ReservaModificacionRequestDto dto = mockDto(
                1L,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                null
        );
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqualAndIdNot(
                any(), any(), any(), any(), any()
        )).thenReturn(false);

        assertDoesNotThrow(() -> validator.validar(1L, TipoReserva.COMUN, dto));
    }
}

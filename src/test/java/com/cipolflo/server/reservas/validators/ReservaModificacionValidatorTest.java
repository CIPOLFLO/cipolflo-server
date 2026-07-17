package com.cipolflo.server.reservas.validators;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.PlazoConfirmacion;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.reservas.dto.ReservaModificacionRequestDto;
import com.cipolflo.server.reservas.exception.ReservaCodigoError;
import com.cipolflo.server.reservas.exception.ReservaValidacionException;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.repository.ServicioRepository;
import com.cipolflo.server.shared.ZonaHoraria;
import com.cipolflo.server.shared.enums.Procedencia;
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
            LocalDate fechaFin
    ) {
        ReservaModificacionRequestDto dto = mock(ReservaModificacionRequestDto.class);
        lenient().when(dto.getServicioId()).thenReturn(servicioId);
        lenient().when(dto.getFechaInicio()).thenReturn(fechaInicio);
        lenient().when(dto.getFechaFin()).thenReturn(fechaFin);
        return dto;
    }

    private Reserva mockReserva(Long id, TipoReserva tipoReserva, LocalDate fechaEntrada) {
        Reserva reserva = mock(Reserva.class);
        lenient().when(reserva.getId()).thenReturn(id);
        lenient().when(reserva.getTipoReserva()).thenReturn(tipoReserva);
        lenient().when(reserva.getFechaEntrada()).thenReturn(fechaEntrada);
        return reserva;
    }

    private Reserva mockReservaConPlazo(
            Long id, TipoReserva tipoReserva, LocalDate fechaEntrada, PlazoConfirmacion plazoConfirmacion
    ) {
        Reserva reserva = mockReserva(id, tipoReserva, fechaEntrada);
        lenient().when(reserva.getPlazoConfirmacion()).thenReturn(plazoConfirmacion);
        return reserva;
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
        LocalDate fechaInicioNueva = LocalDate.now(ZonaHoraria.URUGUAY).minusDays(1);
        ReservaModificacionRequestDto dto = mockDto(
                1L,
                fechaInicioNueva, LocalDate.now(ZonaHoraria.URUGUAY).plusDays(2)
        );
        // fechaEntrada actual distinta de la nueva para que se active la validación
        Reserva reserva = mockReserva(1L, TipoReserva.COMUN, LocalDate.now(ZonaHoraria.URUGUAY).plusDays(3));

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(reserva, dto)
        );
        assertEquals(ReservaCodigoError.FECHA_PASADA.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExcepcionCuandoFechaFinEsAnteriorAFechaInicio() {
        LocalDate fechaInicio = LocalDate.now(ZonaHoraria.URUGUAY).plusDays(3);
        ReservaModificacionRequestDto dto = mockDto(
                1L,
                fechaInicio, LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1)
        );
        // fechaEntrada igual a la nueva para saltear la validación de fecha pasada
        Reserva reserva = mockReserva(1L, TipoReserva.COMUN, fechaInicio);

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(reserva, dto)
        );
        assertEquals(ReservaCodigoError.FECHA_FIN_ANTERIOR_A_INICIO.name(), ex.getCodigo());
    }

    // ── validarServicio ────────────────────────────────────────────────────────

    @Test
    void deberiaLanzarExcepcionCuandoServicioNoExiste() {
        LocalDate fechaInicio = LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1);
        ReservaModificacionRequestDto dto = mockDto(
                99L,
                fechaInicio, LocalDate.now(ZonaHoraria.URUGUAY).plusDays(3)
        );
        Reserva reserva = mockReserva(1L, TipoReserva.COMUN, fechaInicio);
        when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(reserva, dto)
        );
        assertEquals(ReservaCodigoError.SERVICIO_NO_DISPONIBLE.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExcepcionCuandoServicioEstaDeshabilitado() {
        Servicio deshabilitado = servicioHabilitado(1L);
        deshabilitado.setHabilitado(false);

        LocalDate fechaInicio = LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1);
        ReservaModificacionRequestDto dto = mockDto(
                1L,
                fechaInicio, LocalDate.now(ZonaHoraria.URUGUAY).plusDays(3)
        );
        Reserva reserva = mockReserva(1L, TipoReserva.COMUN, fechaInicio);
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(deshabilitado));

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(reserva, dto)
        );
        assertEquals(ReservaCodigoError.SERVICIO_NO_DISPONIBLE.name(), ex.getCodigo());
    }

    // ── validarSolapamiento ────────────────────────────────────────────────────

    @Test
    void deberiaLanzarExcepcionCuandoExisteSolapamientoConOtraReserva() {
        Long reservaId = 1L;
        LocalDate fechaInicio = LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1);
        ReservaModificacionRequestDto dto = mockDto(
                1L,
                fechaInicio, LocalDate.now(ZonaHoraria.URUGUAY).plusDays(5)
        );
        Reserva reserva = mockReserva(reservaId, TipoReserva.COMUN, fechaInicio);
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqualAndIdNot(
                eq(1L),
                eq(List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA, EstadoReserva.EN_CURSO)),
                eq(LocalDate.now(ZonaHoraria.URUGUAY).plusDays(5)),
                eq(LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1)),
                eq(reservaId)
        )).thenReturn(true);

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(reserva, dto)
        );
        assertEquals(ReservaCodigoError.FECHAS_SOLAPADAS.name(), ex.getCodigo());
    }

    @Test
    void noDeberiaLanzarExcepcionCuandoSoloHaySolapamientoConLaMismaReserva() {
        Long reservaId = 1L;
        LocalDate fechaInicio = LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1);
        ReservaModificacionRequestDto dto = mockDto(
                1L,
                fechaInicio, LocalDate.now(ZonaHoraria.URUGUAY).plusDays(5)
        );
        Reserva reserva = mockReserva(reservaId, TipoReserva.COMUN, fechaInicio);
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqualAndIdNot(
                any(), any(), any(), any(), eq(reservaId)
        )).thenReturn(false);

        assertDoesNotThrow(() -> validator.validar(reserva, dto));
    }

    // ── validarPlazoConfirmacion ─────────────────────────────────────────────

    @Test
    void deberiaLanzarExcepcionCuandoLaNuevaFechaInicioDejaElPlazoDeConfirmacionVencido() {
        Long reservaId = 1L;
        // Reserva con plazo de 3 meses: mover fechaInicio a mañana deja el límite (mañana - 3 meses) vencido.
        LocalDate nuevaFechaInicio = LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1);
        ReservaModificacionRequestDto dto = mockDto(
                1L,
                nuevaFechaInicio, nuevaFechaInicio.plusDays(2)
        );
        Reserva reserva = mockReservaConPlazo(
                reservaId, TipoReserva.COMUN,
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(90),
                PlazoConfirmacion.TRES_MESES
        );
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqualAndIdNot(
                any(), any(), any(), any(), eq(reservaId)
        )).thenReturn(false);

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(reserva, dto)
        );
        assertEquals(ReservaCodigoError.PLAZO_CONFIRMACION_VENCIDO.name(), ex.getCodigo());
    }

    @Test
    void deberiaPasarValidacionCuandoElPlazoDeConfirmacionSigueVigenteTrasModificar() {
        Long reservaId = 1L;
        LocalDate nuevaFechaInicio = LocalDate.now(ZonaHoraria.URUGUAY).plusDays(90);
        ReservaModificacionRequestDto dto = mockDto(
                1L,
                nuevaFechaInicio, nuevaFechaInicio.plusDays(2)
        );
        Reserva reserva = mockReservaConPlazo(
                reservaId, TipoReserva.COMUN,
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(2),
                PlazoConfirmacion.VEINTICUATRO_HORAS
        );
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqualAndIdNot(
                any(), any(), any(), any(), eq(reservaId)
        )).thenReturn(false);

        assertDoesNotThrow(() -> validator.validar(reserva, dto));
    }

    @Test
    void deberiaPasarValidacionCuandoLaReservaNoTienePlazoConfirmacion() {
        Long reservaId = 1L;
        LocalDate nuevaFechaInicio = LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1);
        ReservaModificacionRequestDto dto = mockDto(
                1L,
                nuevaFechaInicio, nuevaFechaInicio.plusDays(2)
        );
        Reserva reserva = mockReservaConPlazo(
                reservaId, TipoReserva.COMUN,
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(2),
                null
        );
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqualAndIdNot(
                any(), any(), any(), any(), eq(reservaId)
        )).thenReturn(false);

        assertDoesNotThrow(() -> validator.validar(reserva, dto));
    }
}
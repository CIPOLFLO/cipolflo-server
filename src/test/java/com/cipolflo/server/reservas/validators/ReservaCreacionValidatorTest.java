package com.cipolflo.server.reservas.validators;

import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.reservas.dto.ReservaCreacionRequestDto;
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

import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;

import com.cipolflo.server.shared.ZonaHoraria;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaCreacionValidatorTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ServicioRepository servicioRepository;

    @InjectMocks
    private ReservaCreacionValidator validator;

    private ReservaCreacionRequestDto mockDto(
            TipoReserva tipoReserva,
            Long servicioId,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Long clienteId,
            Boolean crearCliente,
            String nombre,
            String cedula,
            String celular,
            String rut
    ) {
        ReservaCreacionRequestDto dto = mock(ReservaCreacionRequestDto.class);
        lenient().when(dto.getTipoReserva()).thenReturn(tipoReserva);
        lenient().when(dto.getServicioId()).thenReturn(servicioId);
        lenient().when(dto.getFechaInicio()).thenReturn(fechaInicio);
        lenient().when(dto.getFechaFin()).thenReturn(fechaFin);
        lenient().when(dto.getClienteId()).thenReturn(clienteId);
        lenient().when(dto.getCrearCliente()).thenReturn(crearCliente);
        lenient().when(dto.getNombre()).thenReturn(nombre);
        lenient().when(dto.getCedula()).thenReturn(cedula);
        lenient().when(dto.getCelular()).thenReturn(celular);
        lenient().when(dto.getRut()).thenReturn(rut);
        return dto;
    }

    private Servicio servicioHabilitado(Long id) {
        Servicio s = new Servicio();
        s.setId(id);
        s.setHabilitado(true);
        return s;
    }

    private Servicio servicioHabilitado(Long id, ModalidadPrecio modalidadPrecio) {
        Servicio s = servicioHabilitado(id);
        s.setModalidadPrecio(modalidadPrecio);
        return s;
    }

    private ReservaCreacionRequestDto mockDtoConHoras(
            LocalDate fechaInicio, LocalDate fechaFin,
            LocalTime horaInicio, LocalTime horaFin,
            Long servicioId, Long clienteId
    ) {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COMUN, servicioId,
                fechaInicio, fechaFin,
                clienteId, false, null, null, null, null
        );
        lenient().when(dto.getHoraInicio()).thenReturn(horaInicio);
        lenient().when(dto.getHoraFin()).thenReturn(horaFin);
        return dto;
    }

    // ── validarFechas ──────────────────────────────────────────────────────────

    @Test
    void deberiaLanzarExcepcionCuandoFechaInicioEsAnteriorAHoy() {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COMUN, 1L,
                LocalDate.now(ZonaHoraria.URUGUAY).minusDays(1), LocalDate.now(ZonaHoraria.URUGUAY).plusDays(2),
                1L, false, null, null, null, null
        );

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(dto)
        );
        assertEquals(ReservaCodigoError.FECHA_PASADA.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExcepcionCuandoFechaFinEsAnteriorAFechaInicio() {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COMUN, 1L,
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(3), LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1),
                1L, false, null, null, null, null
        );

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(dto)
        );
        assertEquals(ReservaCodigoError.FECHA_FIN_ANTERIOR_A_INICIO.name(), ex.getCodigo());
    }

    // ── validarServicio ────────────────────────────────────────────────────────

    @Test
    void deberiaLanzarExcepcionCuandoServicioNoExiste() {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COMUN, 99L,
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1), LocalDate.now(ZonaHoraria.URUGUAY).plusDays(3),
                1L, false, null, null, null, null
        );
        when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(dto)
        );
        assertEquals(ReservaCodigoError.SERVICIO_NO_DISPONIBLE.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExcepcionCuandoServicioEstaDeshabilitado() {
        Servicio deshabilitado = servicioHabilitado(1L);
        deshabilitado.setHabilitado(false);

        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COMUN, 1L,
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1), LocalDate.now(ZonaHoraria.URUGUAY).plusDays(3),
                1L, false, null, null, null, null
        );
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(deshabilitado));

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(dto)
        );
        assertEquals(ReservaCodigoError.SERVICIO_NO_DISPONIBLE.name(), ex.getCodigo());
    }

    // ── validarSolapamiento ────────────────────────────────────────────────────

    @Test
    void deberiaLanzarExcepcionCuandoExisteSolapamiento() {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COMUN, 1L,
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1), LocalDate.now(ZonaHoraria.URUGUAY).plusDays(5),
                1L, false, null, null, null, null
        );
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                eq(1L),
                eq(List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA, EstadoReserva.EN_CURSO)),
                eq(LocalDate.now(ZonaHoraria.URUGUAY).plusDays(5)),
                eq(LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1))
        )).thenReturn(true);

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(dto)
        );
        assertEquals(ReservaCodigoError.FECHAS_SOLAPADAS.name(), ex.getCodigo());
    }

    // ── validarCliente con crearCliente=true ───────────────────────────────────

    @Test
    void deberiaLanzarExcepcionCuandoCrearClienteSinNombre() {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COMUN, 1L,
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1), LocalDate.now(ZonaHoraria.URUGUAY).plusDays(3),
                null, true, "", "1.234.567-8", "099111111", null
        );
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                any(), any(), any(), any()
        )).thenReturn(false);

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(dto)
        );
        assertEquals(ReservaCodigoError.NOMBRE_REQUERIDO_PARA_CREAR_CLIENTE.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExcepcionCuandoCrearClienteSinCedula() {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COMUN, 1L,
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1), LocalDate.now(ZonaHoraria.URUGUAY).plusDays(3),
                null, true, "Juan Pérez", "", "099111111", null
        );
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                any(), any(), any(), any()
        )).thenReturn(false);

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(dto)
        );
        assertEquals(ReservaCodigoError.CEDULA_REQUERIDA_PARA_CREAR_CLIENTE.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExcepcionCuandoCrearClienteSinCelular() {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COMUN, 1L,
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1), LocalDate.now(ZonaHoraria.URUGUAY).plusDays(3),
                null, true, "Juan Pérez", "1.234.567-8", "", null
        );
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                any(), any(), any(), any()
        )).thenReturn(false);

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(dto)
        );
        assertEquals(ReservaCodigoError.CELULAR_REQUERIDO_PARA_CREAR_CLIENTE.name(), ex.getCodigo());
    }

    // ── validarCliente sin clienteId ───────────────────────────────────────────

    @Test
    void deberiaLanzarExcepcionCuandoRutPresenteEnReservaNoColaboracion() {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COMUN, 1L,
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1), LocalDate.now(ZonaHoraria.URUGUAY).plusDays(3),
                null, false, null, null, null, "20123456-7"
        );
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                any(), any(), any(), any()
        )).thenReturn(false);

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(dto)
        );
        assertEquals(ReservaCodigoError.RUT_SOLO_VALIDO_EN_COLABORACION.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExcepcionCuandoNoHayClienteIdNiRut() {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COMUN, 1L,
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1), LocalDate.now(ZonaHoraria.URUGUAY).plusDays(3),
                null, false, null, null, null, null
        );
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                any(), any(), any(), any()
        )).thenReturn(false);

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(dto)
        );
        assertEquals(ReservaCodigoError.CLIENTE_REQUERIDO.name(), ex.getCodigo());
    }

    @Test
    void deberiaPasarValidacionColaboracionConRutYNombre() {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COLABORACION_SIN_FINES_DE_LUCRO, 1L,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                null, false, "Org Solidaria", null, null, "20123456-7"
        );
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                any(), any(), any(), any()
        )).thenReturn(false);

        assertDoesNotThrow(() -> validator.validar(dto));
    }

    @Test
    void deberiaLanzarExcepcionCuandoColaboracionSinNombre() {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COLABORACION_SIN_FINES_DE_LUCRO, 1L,
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1), LocalDate.now(ZonaHoraria.URUGUAY).plusDays(3),
                null, false, null, null, null, "20123456-7"
        );
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                any(), any(), any(), any()
        )).thenReturn(false);

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(dto)
        );
        assertEquals(ReservaCodigoError.NOMBRE_REQUERIDO_PARA_COLABORACION.name(), ex.getCodigo());
    }

    @Test
    void deberiaPasarValidacionConClienteIdExistente() {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COMUN, 1L,
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1), LocalDate.now(ZonaHoraria.URUGUAY).plusDays(3),
                42L, false, null, null, null, null
        );
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                any(), any(), any(), any()
        )).thenReturn(false);

        assertDoesNotThrow(() -> validator.validar(dto));
    }

    // ── validarHoras ───────────────────────────────────────────────────────────

    @Test
    void deberiaLanzarExcepcionCuandoServicioPorHoraSinHoraInicio() {
        LocalDate fecha = LocalDate.now().plusDays(1);
        ReservaCreacionRequestDto dto = mockDtoConHoras(fecha, fecha, null, LocalTime.of(12, 0), 1L, 42L);
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L, ModalidadPrecio.POR_HORA)));

        ReservaValidacionException ex = assertThrows(ReservaValidacionException.class, () -> validator.validar(dto));
        assertEquals(ReservaCodigoError.HORA_REQUERIDA_PARA_SERVICIO_POR_HORA.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExcepcionCuandoServicioPorHoraSinHoraFin() {
        LocalDate fecha = LocalDate.now().plusDays(1);
        ReservaCreacionRequestDto dto = mockDtoConHoras(fecha, fecha, LocalTime.of(10, 0), null, 1L, 42L);
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L, ModalidadPrecio.POR_HORA)));

        ReservaValidacionException ex = assertThrows(ReservaValidacionException.class, () -> validator.validar(dto));
        assertEquals(ReservaCodigoError.HORA_REQUERIDA_PARA_SERVICIO_POR_HORA.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExcepcionCuandoHoraFinIgualAHoraInicioEnMismoDia() {
        LocalDate fecha = LocalDate.now().plusDays(1);
        ReservaCreacionRequestDto dto = mockDtoConHoras(fecha, fecha, LocalTime.of(10, 0), LocalTime.of(10, 0), 1L, 42L);
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L, ModalidadPrecio.POR_HORA)));

        ReservaValidacionException ex = assertThrows(ReservaValidacionException.class, () -> validator.validar(dto));
        assertEquals(ReservaCodigoError.HORA_FIN_ANTERIOR_O_IGUAL_A_INICIO.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExcepcionCuandoHoraFinAnteriorAHoraInicioEnMismoDia() {
        LocalDate fecha = LocalDate.now().plusDays(1);
        ReservaCreacionRequestDto dto = mockDtoConHoras(fecha, fecha, LocalTime.of(14, 0), LocalTime.of(10, 0), 1L, 42L);
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L, ModalidadPrecio.POR_HORA)));

        ReservaValidacionException ex = assertThrows(ReservaValidacionException.class, () -> validator.validar(dto));
        assertEquals(ReservaCodigoError.HORA_FIN_ANTERIOR_O_IGUAL_A_INICIO.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExcepcionCuandoServicioNoPorHoraTieneHoras() {
        LocalDate fecha = LocalDate.now().plusDays(1);
        ReservaCreacionRequestDto dto = mockDtoConHoras(fecha, fecha, LocalTime.of(10, 0), LocalTime.of(12, 0), 1L, 42L);
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L, ModalidadPrecio.POR_DIA)));

        ReservaValidacionException ex = assertThrows(ReservaValidacionException.class, () -> validator.validar(dto));
        assertEquals(ReservaCodigoError.HORA_NO_PERMITIDA_PARA_MODALIDAD.name(), ex.getCodigo());
    }

    @Test
    void deberiaPasarValidacionHorasConMismoDiaYHoraFinPosterior() {
        LocalDate fecha = LocalDate.now().plusDays(1);
        ReservaCreacionRequestDto dto = mockDtoConHoras(fecha, fecha, LocalTime.of(10, 0), LocalTime.of(12, 0), 1L, 42L);
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L, ModalidadPrecio.POR_HORA)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                any(), any(), any(), any()
        )).thenReturn(false);

        assertDoesNotThrow(() -> validator.validar(dto));
    }

    @Test
    void deberiaPasarValidacionHorasConDiasDistintosAunqueHoraFinSeaAnterior() {
        LocalDate inicio = LocalDate.now().plusDays(1);
        LocalDate fin = LocalDate.now().plusDays(3);
        ReservaCreacionRequestDto dto = mockDtoConHoras(inicio, fin, LocalTime.of(14, 0), LocalTime.of(10, 0), 1L, 42L);
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L, ModalidadPrecio.POR_HORA)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                any(), any(), any(), any()
        )).thenReturn(false);

        assertDoesNotThrow(() -> validator.validar(dto));
    }
}

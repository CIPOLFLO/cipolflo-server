package com.cipolflo.server.reservas.validators;

import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.clientes.exception.ClienteNotFoundException;
import com.cipolflo.server.clientes.service.IConsultaClienteDetalle;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.PlazoConfirmacion;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.reservas.dto.ClienteDetalleReservaDto;
import com.cipolflo.server.reservas.dto.ReservaCreacionRequestDto;
import com.cipolflo.server.reservas.exception.ReservaCodigoError;
import com.cipolflo.server.reservas.exception.ReservaValidacionException;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.servicios.costo.ResolutorTarifaAplicable;
import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.domain.TarifaServicio;
import com.cipolflo.server.servicios.domain.enums.TipoClienteTarifa;
import com.cipolflo.server.servicios.repository.ServicioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;

import com.cipolflo.server.shared.ZonaHoraria;

import java.math.BigDecimal;
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

    @Mock
    private IConsultaClienteDetalle consultaClienteDetalle;

    @Mock
    private ResolutorTarifaAplicable resolutorTarifaAplicable;

    @InjectMocks
    private ReservaCreacionValidator validator;

    private ClienteDetalleReservaDto clienteDe(TipoCliente tipoCliente) {
        return new ClienteDetalleReservaDto(
                42L,
                "Cliente de prueba",
                null,
                null,
                "099111111",
                "cliente@test.com",
                tipoCliente
        );
    }

    private ReservaCreacionRequestDto mockDto(
            TipoReserva tipoReserva,
            Long servicioId,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Long clienteId,
            Boolean crearCliente,
            String nombre,
            String cedula,
            String celular
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

        return dto;
    }

    private Servicio servicioHabilitado(Long id) {
        Servicio s = new Servicio();
        s.setId(id);
        s.setHabilitado(true);
        return s;
    }

    private ReservaCreacionRequestDto mockDtoConHoras(
            LocalDate fechaInicio,
            LocalDate fechaFin,
            LocalTime horaInicio,
            LocalTime horaFin,
            Long servicioId,
            Long clienteId
    ) {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COMUN,
                servicioId,
                fechaInicio,
                fechaFin,
                clienteId,
                false,
                null,
                null,
                null
        );

        lenient().when(dto.getHoraInicio()).thenReturn(horaInicio);
        lenient().when(dto.getHoraFin()).thenReturn(horaFin);

        return dto;
    }

    /**
     * Helper específico para los tests de validarPlazoConfirmacion: agrega requiereSena,
     * requiereDocumentacion, plazoConfirmacion y (opcionalmente) horaInicio sobre el mockDto
     * base, sin tocar el helper genérico existente.
     */
    private ReservaCreacionRequestDto mockDtoConPlazo(
            LocalDate fechaInicio,
            LocalDate fechaFin,
            boolean requiereSena,
            boolean requiereDocumentacion,
            PlazoConfirmacion plazoConfirmacion,
            Long servicioId,
            Long clienteId
    ) {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COMUN,
                servicioId,
                fechaInicio,
                fechaFin,
                clienteId,
                false,
                null,
                null,
                null
        );

        lenient().when(dto.getRequiereSena()).thenReturn(requiereSena);
        lenient().when(dto.getRequiereDocumentacion()).thenReturn(requiereDocumentacion);
        lenient().when(dto.getPlazoConfirmacion()).thenReturn(plazoConfirmacion);

        return dto;
    }

    private void mockearServicioYSinSolapamiento(
            Long servicioId,
            Long clienteId,
            ModalidadPrecio modalidad
    ) {
        mockearTarifa(servicioId, clienteId, modalidad);

        when(servicioRepository.findById(servicioId))
                .thenReturn(Optional.of(servicioHabilitado(servicioId)));

        when(reservaRepository
                .existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                        any(),
                        any(),
                        any(),
                        any()
                ))
                .thenReturn(false);
    }

    private void mockearTarifa(
            Long servicioId,
            Long clienteId,
            ModalidadPrecio modalidad
    ) {
        TarifaServicio tarifa = TarifaServicio.registrar(
                servicioHabilitado(servicioId),
                TipoClienteTarifa.PARTICULAR,
                BigDecimal.TEN,
                modalidad,
                null,
                null
        );

        when(resolutorTarifaAplicable.resolver(servicioId, clienteId))
                .thenReturn(tarifa);
    }


    // ── validarFechas ──────────────────────────────────────────────────────────

    @Test
    void deberiaLanzarExcepcionCuandoFechaInicioEsAnteriorAHoy() {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COMUN, 1L,
                LocalDate.now(ZonaHoraria.URUGUAY).minusDays(1), LocalDate.now(ZonaHoraria.URUGUAY).plusDays(2),
                1L, false, null, null, null
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
                1L, false, null, null, null
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
                1L, false, null, null, null
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
                1L, false, null, null, null
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
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1),
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(5),
                1L, false, null, null, null
        );

        when(servicioRepository.findById(1L))
                .thenReturn(Optional.of(servicioHabilitado(1L)));

        when(reservaRepository
                .existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                        eq(1L),
                        eq(List.of(
                                EstadoReserva.PENDIENTE,
                                EstadoReserva.CONFIRMADA,
                                EstadoReserva.EN_CURSO
                        )),
                        eq(LocalDate.now(ZonaHoraria.URUGUAY).plusDays(5)),
                        eq(LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1))
                ))
                .thenReturn(true);

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(dto)
        );

        assertEquals(
                ReservaCodigoError.FECHAS_SOLAPADAS.name(),
                ex.getCodigo()
        );
    }

    // ── validarCliente con crearCliente=true ───────────────────────────────────

    @Test
    void deberiaLanzarExcepcionCuandoCrearClienteConClienteIdInformado() {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COMUN,
                1L,
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1),
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(3),
                123L,
                true,
                "Juan Pérez",
                "1.234.567-8",
                "099111111"
        );

        when(servicioRepository.findById(1L))
                .thenReturn(Optional.of(servicioHabilitado(1L)));

        when(reservaRepository
                .existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                        any(),
                        any(),
                        any(),
                        any()
                ))
                .thenReturn(false);

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(dto)
        );

        assertEquals(
                ReservaCodigoError.CLIENTE_ID_NO_PERMITIDO_CON_CREAR_CLIENTE.name(),
                ex.getCodigo()
        );

        verifyNoInteractions(resolutorTarifaAplicable);
    }

    @Test
    void deberiaLanzarExcepcionCuandoCrearClienteSinNombre() {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COMUN,
                1L,
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1),
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(3),
                null,
                true,
                "",
                "1.234.567-8",
                "099111111"
        );

        when(servicioRepository.findById(1L))
                .thenReturn(Optional.of(servicioHabilitado(1L)));

        when(reservaRepository
                .existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                        any(),
                        any(),
                        any(),
                        any()
                ))
                .thenReturn(false);

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(dto)
        );

        assertEquals(
                ReservaCodigoError.NOMBRE_REQUERIDO_PARA_CREAR_CLIENTE.name(),
                ex.getCodigo()
        );
    }

    @Test
    void deberiaLanzarExcepcionCuandoCrearClienteSinCedula() {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COMUN,
                1L,
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1),
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(3),
                null,
                true,
                "Juan Pérez",
                "",
                "099111111"
        );

        when(servicioRepository.findById(1L))
                .thenReturn(Optional.of(servicioHabilitado(1L)));

        when(reservaRepository
                .existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                        any(),
                        any(),
                        any(),
                        any()
                ))
                .thenReturn(false);

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(dto)
        );

        assertEquals(
                ReservaCodigoError.CEDULA_REQUERIDA_PARA_CREAR_CLIENTE.name(),
                ex.getCodigo()
        );
    }

    @Test
    void deberiaLanzarExcepcionCuandoCrearClienteSinCelular() {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COMUN,
                1L,
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1),
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(3),
                null,
                true,
                "Juan Pérez",
                "1.234.567-8",
                ""
        );

        when(servicioRepository.findById(1L))
                .thenReturn(Optional.of(servicioHabilitado(1L)));

        when(reservaRepository
                .existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                        any(),
                        any(),
                        any(),
                        any()
                ))
                .thenReturn(false);

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(dto)
        );

        assertEquals(
                ReservaCodigoError.CELULAR_REQUERIDO_PARA_CREAR_CLIENTE.name(),
                ex.getCodigo()
        );
    }


    // ── validarCliente sin clienteId ───────────────────────────────────────────

    @Test
    void deberiaLanzarExcepcionCuandoNoHayClienteId() {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COMUN, 1L,
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1), LocalDate.now(ZonaHoraria.URUGUAY).plusDays(3),
                null, false, null, null, null
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
    void deberiaLanzarExcepcionCuandoColaboracionSinClienteId() {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COLABORACION_SIN_FINES_DE_LUCRO, 1L,
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1), LocalDate.now(ZonaHoraria.URUGUAY).plusDays(3),
                null, false, null, null, null
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
    void deberiaPasarValidacionColaboracionConClienteEmpresa() {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COLABORACION_SIN_FINES_DE_LUCRO, 1L,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                20L, false, null, null, null
        );
        mockearTarifa(1L, 20L, ModalidadPrecio.POR_DIA);
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                any(), any(), any(), any()
        )).thenReturn(false);
        when(consultaClienteDetalle.getDetallClienteSimple(20L)).thenReturn(clienteDe(TipoCliente.EMPRESA));

        assertDoesNotThrow(() -> validator.validar(dto));
    }

    @Test
    void deberiaLanzarExcepcionCuandoColaboracionConClienteQueNoEsEmpresa() {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COLABORACION_SIN_FINES_DE_LUCRO, 1L,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                20L, false, null, null, null
        );
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                any(), any(), any(), any()
        )).thenReturn(false);
        when(consultaClienteDetalle.getDetallClienteSimple(20L)).thenReturn(clienteDe(TipoCliente.PARTICULAR));

        ReservaValidacionException ex = assertThrows(
                ReservaValidacionException.class,
                () -> validator.validar(dto)
        );
        assertEquals(ReservaCodigoError.CLIENTE_EMPRESA_REQUERIDO_PARA_COLABORACION.name(), ex.getCodigo());
    }

    @Test
    void deberiaPropagarExcepcionCuandoClienteIdNoExiste() {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COMUN, 1L,
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1), LocalDate.now(ZonaHoraria.URUGUAY).plusDays(3),
                99L, false, null, null, null
        );
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                any(), any(), any(), any()
        )).thenReturn(false);
        when(consultaClienteDetalle.getDetallClienteSimple(99L))
                .thenThrow(new ClienteNotFoundException(99L));

        assertThrows(ClienteNotFoundException.class, () -> validator.validar(dto));
    }

    @Test
    void deberiaPasarValidacionConClienteIdExistente() {
        ReservaCreacionRequestDto dto = mockDto(
                TipoReserva.COMUN, 1L,
                LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1), LocalDate.now(ZonaHoraria.URUGUAY).plusDays(3),
                42L, false, null, null, null
        );
        mockearTarifa(1L, 42L, ModalidadPrecio.POR_DIA);
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicioHabilitado(1L)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                any(), any(), any(), any()
        )).thenReturn(false);
        when(consultaClienteDetalle.getDetallClienteSimple(42L)).thenReturn(clienteDe(TipoCliente.PARTICULAR));

        assertDoesNotThrow(() -> validator.validar(dto));
    }

    // ── validarHoras ───────────────────────────────────────────────────────────

    @Test
    void deberiaLanzarExcepcionCuandoServicioPorHoraSinHoraInicio() {
        LocalDate fecha = LocalDate.now().plusDays(1);
        ReservaCreacionRequestDto dto = mockDtoConHoras(fecha, fecha, null, LocalTime.of(12, 0), 1L, 42L);
        mockearTarifa(1L, 42L, ModalidadPrecio.POR_HORA);

        when(servicioRepository.findById(1L))
                .thenReturn(Optional.of(servicioHabilitado(1L)));
        ReservaValidacionException ex = assertThrows(ReservaValidacionException.class, () -> validator.validar(dto));
        assertEquals(ReservaCodigoError.HORA_REQUERIDA_PARA_SERVICIO_POR_HORA.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExcepcionCuandoServicioPorHoraSinHoraFin() {
        LocalDate fecha = LocalDate.now().plusDays(1);
        ReservaCreacionRequestDto dto = mockDtoConHoras(fecha, fecha, LocalTime.of(10, 0), null, 1L, 42L);
        mockearTarifa(1L, 42L, ModalidadPrecio.POR_HORA);

        when(servicioRepository.findById(1L))
                .thenReturn(Optional.of(servicioHabilitado(1L)));
        ReservaValidacionException ex = assertThrows(ReservaValidacionException.class, () -> validator.validar(dto));
        assertEquals(ReservaCodigoError.HORA_REQUERIDA_PARA_SERVICIO_POR_HORA.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExcepcionCuandoHoraFinIgualAHoraInicioEnMismoDia() {
        LocalDate fecha = LocalDate.now().plusDays(1);
        ReservaCreacionRequestDto dto = mockDtoConHoras(fecha, fecha, LocalTime.of(10, 0), LocalTime.of(10, 0), 1L, 42L);
        mockearTarifa(1L, 42L, ModalidadPrecio.POR_HORA);

        when(servicioRepository.findById(1L))
                .thenReturn(Optional.of(servicioHabilitado(1L)));
        ReservaValidacionException ex = assertThrows(ReservaValidacionException.class, () -> validator.validar(dto));
        assertEquals(ReservaCodigoError.HORA_FIN_ANTERIOR_O_IGUAL_A_INICIO.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExcepcionCuandoHoraFinAnteriorAHoraInicioEnMismoDia() {
        LocalDate fecha = LocalDate.now().plusDays(1);
        ReservaCreacionRequestDto dto = mockDtoConHoras(fecha, fecha, LocalTime.of(14, 0), LocalTime.of(10, 0), 1L, 42L);
        mockearTarifa(1L, 42L, ModalidadPrecio.POR_HORA);

        when(servicioRepository.findById(1L))
                .thenReturn(Optional.of(servicioHabilitado(1L)));
        ReservaValidacionException ex = assertThrows(ReservaValidacionException.class, () -> validator.validar(dto));
        assertEquals(ReservaCodigoError.HORA_FIN_ANTERIOR_O_IGUAL_A_INICIO.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExcepcionCuandoServicioNoPorHoraTieneHoras() {
        LocalDate fecha = LocalDate.now().plusDays(1);
        ReservaCreacionRequestDto dto = mockDtoConHoras(fecha, fecha, LocalTime.of(10, 0), LocalTime.of(12, 0), 1L, 42L);
        mockearTarifa(1L, 42L, ModalidadPrecio.POR_DIA);
        when(servicioRepository.findById(1L))
                .thenReturn(Optional.of(servicioHabilitado(1L)));
        ReservaValidacionException ex = assertThrows(ReservaValidacionException.class, () -> validator.validar(dto));
        assertEquals(ReservaCodigoError.HORA_NO_PERMITIDA_PARA_MODALIDAD.name(), ex.getCodigo());
    }

    @Test
    void deberiaPasarValidacionHorasConMismoDiaYHoraFinPosterior() {
        LocalDate fecha = LocalDate.now().plusDays(1);
        ReservaCreacionRequestDto dto = mockDtoConHoras(fecha, fecha, LocalTime.of(10, 0), LocalTime.of(12, 0), 1L, 42L);
        mockearTarifa(1L, 42L, ModalidadPrecio.POR_HORA);
        when(servicioRepository.findById(1L))
                .thenReturn(Optional.of(servicioHabilitado(1L)));
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
        mockearTarifa(1L, 42L, ModalidadPrecio.POR_HORA);

        when(servicioRepository.findById(1L))
                .thenReturn(Optional.of(servicioHabilitado(1L)));
        when(reservaRepository.existsByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                any(), any(), any(), any()
        )).thenReturn(false);

        assertDoesNotThrow(() -> validator.validar(dto));
    }

    // ── validarPlazoConfirmacion ─────────────────────────────────────────────

    @Test
    void deberiaLanzarExcepcionCuandoRequiereSenaSinPlazoConfirmacion() {
        LocalDate inicio = LocalDate.now(ZonaHoraria.URUGUAY).plusDays(10);
        ReservaCreacionRequestDto dto = mockDtoConPlazo(
                inicio, inicio.plusDays(2),
                true, false, null,
                1L, 42L
        );
        mockearServicioYSinSolapamiento(
                1L,
                42L,
                ModalidadPrecio.POR_DIA
        );
        ReservaValidacionException ex = assertThrows(ReservaValidacionException.class, () -> validator.validar(dto));
        assertEquals(ReservaCodigoError.PLAZO_CONFIRMACION_REQUERIDO.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExcepcionCuandoRequiereDocumentacionSinPlazoConfirmacion() {
        LocalDate inicio = LocalDate.now(ZonaHoraria.URUGUAY).plusDays(10);
        ReservaCreacionRequestDto dto = mockDtoConPlazo(
                inicio, inicio.plusDays(2),
                false, true, null,
                1L, 42L
        );
        mockearServicioYSinSolapamiento(
                1L,
                42L,
                ModalidadPrecio.POR_DIA
        );
        ReservaValidacionException ex = assertThrows(ReservaValidacionException.class, () -> validator.validar(dto));
        assertEquals(ReservaCodigoError.PLAZO_CONFIRMACION_REQUERIDO.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExcepcionCuandoNoRequiereNadaYEnviaPlazoConfirmacion() {
        LocalDate inicio = LocalDate.now(ZonaHoraria.URUGUAY).plusDays(10);
        ReservaCreacionRequestDto dto = mockDtoConPlazo(
                inicio, inicio.plusDays(2),
                false, false, PlazoConfirmacion.TRES_MESES,
                1L, 42L
        );
        mockearServicioYSinSolapamiento(1L,42L,ModalidadPrecio.POR_DIA);
        ReservaValidacionException ex = assertThrows(ReservaValidacionException.class, () -> validator.validar(dto));
        assertEquals(ReservaCodigoError.PLAZO_CONFIRMACION_NO_APLICA.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExcepcionCuandoLaFechaLimiteConfirmacionYaVencio() {
        // Reserva para mañana con plazo de 3 meses: el límite (mañana - 3 meses) ya pasó.
        LocalDate inicio = LocalDate.now(ZonaHoraria.URUGUAY).plusDays(1);
        ReservaCreacionRequestDto dto = mockDtoConPlazo(
                inicio, inicio.plusDays(2),
                true, false, PlazoConfirmacion.TRES_MESES,
                1L, 42L
        );
        mockearServicioYSinSolapamiento(
                1L,
                42L,
                ModalidadPrecio.POR_DIA
        );
        ReservaValidacionException ex = assertThrows(ReservaValidacionException.class, () -> validator.validar(dto));
        assertEquals(ReservaCodigoError.PLAZO_CONFIRMACION_VENCIDO.name(), ex.getCodigo());
    }

    @Test
    void deberiaPasarValidacionConPlazoConfirmacionVigente() {
        LocalDate inicio = LocalDate.now(ZonaHoraria.URUGUAY).plusDays(90);
        ReservaCreacionRequestDto dto = mockDtoConPlazo(
                inicio, inicio.plusDays(2),
                true, false, PlazoConfirmacion.VEINTICUATRO_HORAS,
                1L, 42L
        );
        mockearServicioYSinSolapamiento(
                1L,
                42L,
                ModalidadPrecio.POR_DIA
        );
        assertDoesNotThrow(() -> validator.validar(dto));
    }

    @Test
    void deberiaPasarValidacionSinRequerirNadaYSinPlazoConfirmacion() {
        LocalDate inicio = LocalDate.now(ZonaHoraria.URUGUAY).plusDays(10);
        ReservaCreacionRequestDto dto = mockDtoConPlazo(
                inicio, inicio.plusDays(2),
                false, false, null,
                1L, 42L
        );
        mockearServicioYSinSolapamiento(
                1L,
                42L,
                ModalidadPrecio.POR_DIA
        );
        assertDoesNotThrow(() -> validator.validar(dto));
    }
}

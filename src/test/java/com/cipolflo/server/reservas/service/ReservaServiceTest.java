package com.cipolflo.server.reservas.service;

import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.clientes.dto.ClienteResponseDto;
import com.cipolflo.server.clientes.service.IConsultaClienteDetalle;
import com.cipolflo.server.clientes.service.IRegistroParticularService;
import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.reservas.dto.ClienteDetalleReservaDto;
import com.cipolflo.server.reservas.dto.ListadoReservasRequestDto;
import com.cipolflo.server.reservas.dto.ListadoReservasResponseDto;
import com.cipolflo.server.reservas.dto.ReservaCreacionRequestDto;
import com.cipolflo.server.reservas.dto.ReservaCreacionResponseDto;
import com.cipolflo.server.reservas.dto.ReservaDetalleResponseDto;
import com.cipolflo.server.reservas.dto.ServicioDetalleReservaDto;
import com.cipolflo.server.reservas.exception.ReservaNotFoundException;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.reservas.validators.ReservaCreacionValidator;
import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.servicios.service.IConsultaServicioSimple;
import com.cipolflo.server.servicios.service.IServicioRequiereDocumentacion;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private IRegistroParticularService registroParticularService;

    @Mock
    private ReservaCreacionValidator reservaCreacionValidator;

    @Mock
    private IServicioRequiereDocumentacion servicioRequiereDocumentacion;

    @Mock
    private IConsultaClienteDetalle consultaClienteDetalle;

    @Mock
    private IConsultaServicioSimple consultaServicioSimple;

    @InjectMocks
    private ReservaService reservaService;

    @Test
    void deberiaBuscarYCancelarReservasFuturasDelCliente() {
        Long clienteId = 1L;

        Reserva reservaFutura = Reserva.crear(
                TipoReserva.COMUN,
                clienteId,
                10L,
                Procedencia.CAMPING,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                null, null, null, null, null, null, null, null,
                false
        );

        when(reservaRepository.findByClienteIdAndFechaEntradaAfterAndEstadoIn(
                eq(clienteId),
                any(LocalDate.class),
                eq(List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA))
        )).thenReturn(List.of(reservaFutura));

        reservaService.cancelarReservasFuturasPorCliente(clienteId);

        assertEquals(EstadoReserva.CANCELADA, reservaFutura.getEstado());

        verify(reservaRepository).findByClienteIdAndFechaEntradaAfterAndEstadoIn(
                eq(clienteId),
                any(LocalDate.class),
                eq(List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA))
        );

        verify(reservaRepository).saveAll(List.of(reservaFutura));
    }

    @Test
    void noDeberiaCancelarReservasPasadasDelCliente() {
        Long clienteId = 1L;

        when(reservaRepository.findByClienteIdAndFechaEntradaAfterAndEstadoIn(
                eq(clienteId),
                any(LocalDate.class),
                eq(List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA))
        )).thenReturn(List.of());

        reservaService.cancelarReservasFuturasPorCliente(clienteId);

        verify(reservaRepository).findByClienteIdAndFechaEntradaAfterAndEstadoIn(
                eq(clienteId),
                any(LocalDate.class),
                eq(List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA))
        );

        verify(reservaRepository).saveAll(List.of());
    }

    @Test
    void deberiaCancelarReservaPagaSinModificarPago() {
        Long clienteId = 1L;

        Reserva reservaPaga = Reserva.crear(
                TipoReserva.COMUN,
                clienteId,
                10L,
                Procedencia.CAMPING,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                null, null, null, null, null, null, null, null,
                false
        );

        reservaPaga.confirmarPago(
                BigDecimal.valueOf(1500),
                FormaPago.EFECTIVO
        );

        assertTrue(reservaPaga.getPago());

        when(reservaRepository.findByClienteIdAndFechaEntradaAfterAndEstadoIn(
                eq(clienteId),
                any(LocalDate.class),
                eq(List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA))
        )).thenReturn(List.of(reservaPaga));

        reservaService.cancelarReservasFuturasPorCliente(clienteId);

        assertEquals(EstadoReserva.CANCELADA, reservaPaga.getEstado());
        assertTrue(reservaPaga.getPago());

        verify(reservaRepository).saveAll(List.of(reservaPaga));
    }

    @Test
    void deberiaObtenerOcupacionPorServicioEnRangoConSolapamientoInclusivo() {
        Long servicioId = 10L;
        LocalDate desde = LocalDate.of(2026, 6, 16);
        LocalDate hasta = LocalDate.of(2026, 6, 20);

        Reserva reserva = Reserva.crear(
                TipoReserva.COMUN,
                1L,
                servicioId,
                Procedencia.CAMPING,
                LocalDate.of(2026, 6, 18),
                LocalDate.of(2026, 6, 19),
                null, null, null, null, null, null, null, null,
                false
        );

        when(reservaRepository
                .findByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                        eq(servicioId),
                        eq(List.of(
                                EstadoReserva.PENDIENTE,
                                EstadoReserva.CONFIRMADA,
                                EstadoReserva.EN_CURSO
                        )),
                        eq(hasta),
                        eq(desde)
                )).thenReturn(List.of(reserva));

        List<Reserva> resultado =
                reservaService.obtenerOcupacionPorServicioEnRango(servicioId, desde, hasta);

        assertEquals(1, resultado.size());
        assertEquals(reserva, resultado.get(0));

        verify(reservaRepository)
                .findByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                        eq(servicioId),
                        eq(List.of(
                                EstadoReserva.PENDIENTE,
                                EstadoReserva.CONFIRMADA,
                                EstadoReserva.EN_CURSO
                        )),
                        eq(hasta),
                        eq(desde)
                );
    }

    // ── registrar ──────────────────────────────────────────────────────────────

    @Test
    void deberiaRegistrarReservaConClienteIdExistente() {
        ReservaCreacionRequestDto dto = mock(ReservaCreacionRequestDto.class);
        when(dto.getCrearCliente()).thenReturn(false);
        when(dto.getClienteId()).thenReturn(5L);
        when(dto.getTipoReserva()).thenReturn(TipoReserva.COMUN);
        when(dto.getServicioId()).thenReturn(10L);
        when(dto.getProcedencia()).thenReturn(Procedencia.CAMPING);
        when(dto.getFechaInicio()).thenReturn(LocalDate.now().plusDays(1));
        when(dto.getFechaFin()).thenReturn(LocalDate.now().plusDays(3));

        when(servicioRequiereDocumentacion.requiereDocumentacion(10L)).thenReturn(false);

        Reserva reservaMock = mock(Reserva.class);
        when(reservaMock.getId()).thenReturn(1L);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaMock);

        ReservaCreacionResponseDto response = reservaService.registrar(dto);

        assertEquals(1L, response.getId());
        verify(reservaCreacionValidator).validar(dto);
        verify(registroParticularService, never()).registrarParticular(any());
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    void deberiaRegistrarReservaCreadoNuevoCliente() {
        ReservaCreacionRequestDto dto = mock(ReservaCreacionRequestDto.class);
        when(dto.getCrearCliente()).thenReturn(true);
        when(dto.getNombre()).thenReturn("Ana López");
        when(dto.getCedula()).thenReturn("1.234.567-8");
        when(dto.getCelular()).thenReturn("099000111");
        when(dto.getEmail()).thenReturn("ana@mail.com");
        when(dto.getTipoReserva()).thenReturn(TipoReserva.COMUN);
        when(dto.getServicioId()).thenReturn(10L);
        when(dto.getProcedencia()).thenReturn(Procedencia.CAMPING);
        when(dto.getFechaInicio()).thenReturn(LocalDate.now().plusDays(1));
        when(dto.getFechaFin()).thenReturn(LocalDate.now().plusDays(3));

        ClienteResponseDto clienteCreado = mock(ClienteResponseDto.class);
        when(clienteCreado.getId()).thenReturn(7L);
        when(registroParticularService.registrarParticular(any())).thenReturn(clienteCreado);
        when(servicioRequiereDocumentacion.requiereDocumentacion(10L)).thenReturn(false);

        Reserva reservaMock = mock(Reserva.class);
        when(reservaMock.getId()).thenReturn(2L);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaMock);

        ReservaCreacionResponseDto response = reservaService.registrar(dto);

        assertEquals(2L, response.getId());
        verify(registroParticularService).registrarParticular(argThat(r ->
                "Ana López".equals(r.getNombre()) &&
                "1.234.567-8".equals(r.getCedula()) &&
                "099000111".equals(r.getCelular()) &&
                "ana@mail.com".equals(r.getMail())
        ));
        verify(reservaRepository).save(argThat(r -> Long.valueOf(7L).equals(r.getClienteId())));
    }

    @Test
    void deberiaRegistrarReservaColaboracionConRutSinClienteId() {
        ReservaCreacionRequestDto dto = mock(ReservaCreacionRequestDto.class);
        when(dto.getCrearCliente()).thenReturn(false);
        when(dto.getClienteId()).thenReturn(null);
        when(dto.getTipoReserva()).thenReturn(TipoReserva.COLABORACION_SIN_FINES_DE_LUCRO);
        when(dto.getServicioId()).thenReturn(10L);
        when(dto.getProcedencia()).thenReturn(Procedencia.CAMPING);
        when(dto.getFechaInicio()).thenReturn(LocalDate.now().plusDays(1));
        when(dto.getFechaFin()).thenReturn(LocalDate.now().plusDays(3));
        when(dto.getRut()).thenReturn("20123456-7");
        when(dto.getNombre()).thenReturn("Org Solidaria");

        when(servicioRequiereDocumentacion.requiereDocumentacion(10L)).thenReturn(false);

        Reserva reservaMock = mock(Reserva.class);
        when(reservaMock.getId()).thenReturn(3L);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaMock);

        ReservaCreacionResponseDto response = reservaService.registrar(dto);

        assertEquals(3L, response.getId());
        verify(registroParticularService, never()).registrarParticular(any());
        verify(reservaRepository).save(argThat(r ->
                r.getClienteId() == null &&
                TipoReserva.COLABORACION_SIN_FINES_DE_LUCRO.equals(r.getTipoReserva())
        ));
    }

    // ── getDetalle ─────────────────────────────────────────────────────────────

    @Test
    void deberiaLanzarExcepcionCuandoReservaNoExiste() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ReservaNotFoundException.class, () -> reservaService.getDetalle(99L));

        verify(consultaClienteDetalle, never()).getDetallClienteSimple(any());
        verify(consultaServicioSimple, never()).getDetalleServicioSimple(any());
    }

    @Test
    void deberiaLlamarConsultaClienteConClienteIdDeLaReserva() {
        Long clienteId = 5L;
        Reserva reserva = Reserva.crear(
                TipoReserva.COMUN, clienteId, 10L, Procedencia.CAMPING,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                null, null, null, null, null, null, null, null, false
        );
        ClienteDetalleReservaDto clienteDto = new ClienteDetalleReservaDto(
                clienteId, "Juan", "12345678", "099", null, TipoCliente.SOCIO
        );
        ServicioDetalleReservaDto servicioDto = new ServicioDetalleReservaDto(
                10L, "Servicio", Procedencia.CAMPING, ModalidadPrecio.POR_DIA
        );

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(consultaClienteDetalle.getDetallClienteSimple(clienteId)).thenReturn(clienteDto);
        when(consultaServicioSimple.getDetalleServicioSimple(10L)).thenReturn(servicioDto);

        reservaService.getDetalle(1L);

        verify(consultaClienteDetalle).getDetallClienteSimple(clienteId);
    }

    @Test
    void deberiaRetornarClienteNullCuandoClienteIdEsNull() {
        Reserva reserva = Reserva.crear(
                TipoReserva.COLABORACION_SIN_FINES_DE_LUCRO, null, 10L, Procedencia.CAMPING,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                null, null, null, null, null, "20123456-7", "Org Test", null, false
        );
        ServicioDetalleReservaDto servicioDto = new ServicioDetalleReservaDto(
                10L, "Servicio", Procedencia.CAMPING, ModalidadPrecio.POR_DIA
        );

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(consultaServicioSimple.getDetalleServicioSimple(10L)).thenReturn(servicioDto);

        ReservaDetalleResponseDto resultado = reservaService.getDetalle(1L);

        assertNull(resultado.getCliente());
        verify(consultaClienteDetalle, never()).getDetallClienteSimple(any());
    }

    @Test
    void deberiaLlamarConsultaServicioConServicioIdDeLaReserva() {
        Long servicioId = 10L;
        Reserva reserva = Reserva.crear(
                TipoReserva.COMUN, 5L, servicioId, Procedencia.CAMPING,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                null, null, null, null, null, null, null, null, false
        );
        ClienteDetalleReservaDto clienteDto = new ClienteDetalleReservaDto(
                5L, "Juan", "12345678", "099", null, TipoCliente.SOCIO
        );
        ServicioDetalleReservaDto servicioDto = new ServicioDetalleReservaDto(
                servicioId, "Servicio", Procedencia.CAMPING, ModalidadPrecio.POR_DIA
        );

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(consultaClienteDetalle.getDetallClienteSimple(5L)).thenReturn(clienteDto);
        when(consultaServicioSimple.getDetalleServicioSimple(servicioId)).thenReturn(servicioDto);

        reservaService.getDetalle(1L);

        verify(consultaServicioSimple).getDetalleServicioSimple(servicioId);
    }

    @Test
    void deberiaRetornarDtoConClienteYServicioMapeados() {
        Reserva reserva = Reserva.crear(
                TipoReserva.COMUN, 5L, 10L, Procedencia.CAMPING,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                null, null, 4, 1, null, null, null, "Nota", false
        );
        ClienteDetalleReservaDto clienteDto = new ClienteDetalleReservaDto(
                5L, "Juan", "12345678", "099", "j@mail.com", TipoCliente.SOCIO
        );
        ServicioDetalleReservaDto servicioDto = new ServicioDetalleReservaDto(
                10L, "Cabaña", Procedencia.CAMPING, ModalidadPrecio.POR_DIA
        );

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));
        when(consultaClienteDetalle.getDetallClienteSimple(5L)).thenReturn(clienteDto);
        when(consultaServicioSimple.getDetalleServicioSimple(10L)).thenReturn(servicioDto);

        ReservaDetalleResponseDto resultado = reservaService.getDetalle(1L);

        assertNotNull(resultado);
        assertEquals(clienteDto, resultado.getCliente());
        assertEquals(servicioDto, resultado.getServicio());
        assertEquals(TipoReserva.COMUN, resultado.getTipoReserva());
        assertEquals(EstadoReserva.PENDIENTE, resultado.getEstado());
        assertEquals("Nota", resultado.getNotas());
    }

    // ── getListadoReservas ─────────────────────────────────────────────────────

    private Reserva crearReservaComun(Long clienteId, Long servicioId) {
        return Reserva.crear(
                TipoReserva.COMUN, clienteId, servicioId, Procedencia.CAMPING,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5),
                null, null, null, null, null, null, null, null, false
        );
    }

    @Test
    void deberiaRetornarPaginaConReservasMapeadasCorrectamente() {
        ListadoReservasRequestDto filtros = new ListadoReservasRequestDto(null, null, null, null, null, null);
        PageRequestDto pageRequest = new PageRequestDto(0, 10, null, null);

        Reserva reserva = crearReservaComun(1L, 5L);
        when(reservaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(reserva)));
        when(consultaClienteDetalle.getNombresByIds(Set.of(1L))).thenReturn(Map.of(1L, "Juan Pérez"));
        when(consultaServicioSimple.getNombresByIds(Set.of(5L))).thenReturn(Map.of(5L, "Cabaña"));

        PageResponse<ListadoReservasResponseDto> resultado =
                reservaService.getListadoReservas(filtros, pageRequest);

        assertEquals(1, resultado.content().size());
        ListadoReservasResponseDto dto = resultado.content().get(0);
        assertEquals(1L, dto.getClienteId());
        assertEquals("Juan Pérez", dto.getNombreCliente());
        assertEquals(5L, dto.getServicioId());
        assertEquals("Cabaña", dto.getServicioNombre());
        assertEquals(LocalDate.of(2026, 7, 1), dto.getFechaEntrada());
        assertEquals(LocalDate.of(2026, 7, 5), dto.getFechaSalida());
        assertEquals(EstadoReserva.PENDIENTE, dto.getEstadoReserva());
    }

    @Test
    void deberiaLlamarGetIdsByNombreCuandoFiltroNombreClienteTieneValor() {
        ListadoReservasRequestDto filtros = new ListadoReservasRequestDto(null, null, "Juan", null, null, null);
        PageRequestDto pageRequest = new PageRequestDto(0, 10, null, null);

        when(consultaClienteDetalle.getIdsByNombre("Juan")).thenReturn(List.of(1L, 2L));
        when(reservaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(consultaClienteDetalle.getNombresByIds(any())).thenReturn(Map.of());
        when(consultaServicioSimple.getNombresByIds(any())).thenReturn(Map.of());

        reservaService.getListadoReservas(filtros, pageRequest);

        verify(consultaClienteDetalle).getIdsByNombre("Juan");
        verify(reservaRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void deberiaRetornarPaginaVaciaSinConsultarRepositorioCuandoNombreNoTieneCoincidencias() {
        ListadoReservasRequestDto filtros = new ListadoReservasRequestDto(null, null, "NoExiste", null, null, null);
        PageRequestDto pageRequest = new PageRequestDto(0, 10, null, null);

        when(consultaClienteDetalle.getIdsByNombre("NoExiste")).thenReturn(List.of());

        PageResponse<ListadoReservasResponseDto> resultado =
                reservaService.getListadoReservas(filtros, pageRequest);

        assertTrue(resultado.content().isEmpty());
        assertEquals(0, resultado.totalElements());
        verify(reservaRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void deberiaRetornarNombreClienteNullCuandoReservaTieneClienteIdNull() {
        Reserva reserva = Reserva.crear(
                TipoReserva.COLABORACION_SIN_FINES_DE_LUCRO, null, 5L, Procedencia.CAMPING,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                null, null, null, null, null, "20123456-7", "Org Test", null, false
        );

        when(reservaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(reserva)));
        when(consultaClienteDetalle.getNombresByIds(any())).thenReturn(Map.of());
        when(consultaServicioSimple.getNombresByIds(any())).thenReturn(Map.of(5L, "Cabaña"));

        PageResponse<ListadoReservasResponseDto> resultado = reservaService.getListadoReservas(
                new ListadoReservasRequestDto(null, null, null, null, null, null),
                new PageRequestDto(0, 10, null, null)
        );

        assertNull(resultado.content().get(0).getClienteId());
        assertEquals("Org Test", resultado.content().get(0).getNombreCliente());
    }

    @Test
    void deberiaBuscarNombresDeClientesYServiciosDeTodasLasReservasDeLaPagina() {
        Reserva reserva1 = crearReservaComun(1L, 5L);
        Reserva reserva2 = crearReservaComun(2L, 6L);

        when(reservaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(reserva1, reserva2)));
        when(consultaClienteDetalle.getNombresByIds(Set.of(1L, 2L)))
                .thenReturn(Map.of(1L, "Juan", 2L, "Ana"));
        when(consultaServicioSimple.getNombresByIds(Set.of(5L, 6L)))
                .thenReturn(Map.of(5L, "Cabaña", 6L, "Parcela"));

        PageResponse<ListadoReservasResponseDto> resultado = reservaService.getListadoReservas(
                new ListadoReservasRequestDto(null, null, null, null, null, null),
                new PageRequestDto(0, 10, null, null)
        );

        assertEquals(2, resultado.content().size());
        verify(consultaClienteDetalle).getNombresByIds(Set.of(1L, 2L));
        verify(consultaServicioSimple).getNombresByIds(Set.of(5L, 6L));
    }

    @Test
    void deberiaRetornarMetadatosDePaginacionCorrectamente() {
        when(reservaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), Pageable.ofSize(10), 0));
        when(consultaClienteDetalle.getNombresByIds(any())).thenReturn(Map.of());
        when(consultaServicioSimple.getNombresByIds(any())).thenReturn(Map.of());

        PageResponse<ListadoReservasResponseDto> resultado = reservaService.getListadoReservas(
                new ListadoReservasRequestDto(null, null, null, null, null, null),
                new PageRequestDto(0, 10, null, null)
        );

        assertEquals(0, resultado.totalElements());
        assertEquals(0, resultado.totalPages());
        assertTrue(resultado.first());
        assertTrue(resultado.last());
    }

    @Test
    void deberiaLlamarValidadorAntesDeCrearReserva() {
        ReservaCreacionRequestDto dto = mock(ReservaCreacionRequestDto.class);
        when(dto.getCrearCliente()).thenReturn(false);
        when(dto.getClienteId()).thenReturn(1L);
        when(dto.getTipoReserva()).thenReturn(TipoReserva.COMUN);
        when(dto.getServicioId()).thenReturn(10L);
        when(dto.getProcedencia()).thenReturn(Procedencia.CAMPING);
        when(dto.getFechaInicio()).thenReturn(LocalDate.now().plusDays(1));
        when(dto.getFechaFin()).thenReturn(LocalDate.now().plusDays(3));

        when(servicioRequiereDocumentacion.requiereDocumentacion(10L)).thenReturn(false);

        Reserva reservaMock = mock(Reserva.class);
        when(reservaMock.getId()).thenReturn(1L);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaMock);

        reservaService.registrar(dto);

        var inOrder = inOrder(reservaCreacionValidator, reservaRepository);
        inOrder.verify(reservaCreacionValidator).validar(dto);
        inOrder.verify(reservaRepository).save(any(Reserva.class));
    }
}

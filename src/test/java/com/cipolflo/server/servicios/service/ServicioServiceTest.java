package com.cipolflo.server.servicios.service;

import com.cipolflo.server.clientes.service.IClienteService;
import com.cipolflo.server.reservas.service.IReservaService;
import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.servicios.dto.ModificacionServicioDto;
import com.cipolflo.server.servicios.dto.ServicioRegistroRequestDto;
import com.cipolflo.server.servicios.dto.ServicioRequestDto;
import com.cipolflo.server.servicios.dto.ServicioResponseDto;
import com.cipolflo.server.servicios.exception.ConfirmacionDevolucionRequeridaException;
import com.cipolflo.server.servicios.exception.ReservaNoCancelableException;
import com.cipolflo.server.servicios.exception.ServicioValidacionException;
import com.cipolflo.server.servicios.repository.ServicioRepository;
import com.cipolflo.server.servicios.validator.ModificacionServicioValidator;
import com.cipolflo.server.servicios.validator.ModificacionValidationContext;
import com.cipolflo.server.servicios.validator.ServicioRegistroValidator;
import com.cipolflo.server.shared.enums.FormaPago;
import com.cipolflo.server.shared.enums.Procedencia;
import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.servicios.exception.ServicioNotFoundException;

import com.cipolflo.server.shared.exception.ServicioCodigoError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.cipolflo.server.servicios.domain.enums.EstadoServicio;
import com.cipolflo.server.servicios.dto.ListadoServiciosRequestDto;
import com.cipolflo.server.servicios.dto.ListadoServiciosResponseDto;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.cipolflo.server.servicios.dto.ReservaProximaResponseDto;
import com.cipolflo.server.servicios.dto.ServicioReservaOcupacionDto;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ServicioServiceTest {

    @Mock
    private ServicioRepository servicioRepository;
    @Mock
    private IReservaService reservaService;
    @Mock
    private IClienteService clienteService;
    @Mock
    private ModificacionServicioValidator modificacionServicioValidator;
    @Mock
    private ServicioRegistroValidator servicioRegistroValidator;

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
        assertEquals(EstadoServicio.HABILITADO, resultado.getEstado());
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
        assertEquals(EstadoServicio.DESHABILITADO, resultado.getEstado());

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
        assertEquals(EstadoServicio.HABILITADO, resultado.getEstado());

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
                TipoReserva.COMUN,
                1L,
                servicioId,
                Procedencia.CAMPING,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                null, null, null, null, null, null, null, null,
                false,false
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
                TipoReserva.COMUN,
                1L,
                servicioId,
                Procedencia.CAMPING,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                null, null, null, null, null, null, null, null,
                false,false
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
                TipoReserva.COMUN,
                1L,
                servicioId,
                Procedencia.CAMPING,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                null, null, null, null, null, null, null, null,
                false,false
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

    private Servicio crearServicio(Long id, String nombre, Procedencia procedencia, Boolean habilitado) {
        Servicio s = new Servicio();
        s.setId(id);
        s.setNombre(nombre);
        s.setProcedencia(procedencia);
        s.setHabilitado(habilitado);
        s.setPrecioParticular(BigDecimal.valueOf(2500));
        s.setPrecioSocio(BigDecimal.valueOf(1500));
        s.setModalidadPrecio(ModalidadPrecio.POR_DIA);
        return s;
    }

    private PageRequestDto pageRequest() {
        return new PageRequestDto(0, 10, null, null);
    }

    @Test
    void deberiaRetornarTodosLosServiciosSinFiltros() {
        List<Servicio> servicios = List.of(
                crearServicio(1L, "Cabaña", Procedencia.CAMPING, true),
                crearServicio(2L, "Cancha", Procedencia.SEDE, false)
        );
        Page<Servicio> page = new PageImpl<>(servicios, pageRequest().toPageable(), servicios.size());
        when(servicioRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ListadoServiciosRequestDto filtros = new ListadoServiciosRequestDto(null, null, null);
        PageResponse<ListadoServiciosResponseDto> resultado = servicioService.getListadoServicios(filtros, pageRequest());

        assertNotNull(resultado);
        assertEquals(2, resultado.totalElements());
        assertEquals(2, resultado.content().size());
        verify(servicioRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void deberiaFiltrarServiciosPorNombre() {
        Servicio servicio = crearServicio(1L, "Cabaña", Procedencia.CAMPING, true);
        Page<Servicio> page = new PageImpl<>(List.of(servicio));
        when(servicioRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ListadoServiciosRequestDto filtros = new ListadoServiciosRequestDto("Cabaña", null, null);
        PageResponse<ListadoServiciosResponseDto> resultado = servicioService.getListadoServicios(filtros, pageRequest());

        assertEquals(1, resultado.content().size());
        assertEquals("Cabaña", resultado.content().get(0).getNombre());
        verify(servicioRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void deberiaFiltrarServiciosPorNombreParcial() {
        Servicio servicio = crearServicio(1L, "Cabaña Grande", Procedencia.CAMPING, true);
        Page<Servicio> page = new PageImpl<>(List.of(servicio));
        when(servicioRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ListadoServiciosRequestDto filtros = new ListadoServiciosRequestDto("caba", null, null);
        PageResponse<ListadoServiciosResponseDto> resultado = servicioService.getListadoServicios(filtros, pageRequest());

        assertEquals(1, resultado.content().size());
        verify(servicioRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void deberiaFiltrarServiciosPorProcedencia() {
        Servicio servicio = crearServicio(1L, "Cabaña", Procedencia.CAMPING, true);
        Page<Servicio> page = new PageImpl<>(List.of(servicio));
        when(servicioRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ListadoServiciosRequestDto filtros = new ListadoServiciosRequestDto(null, Procedencia.CAMPING, null);
        PageResponse<ListadoServiciosResponseDto> resultado = servicioService.getListadoServicios(filtros, pageRequest());

        assertEquals(1, resultado.content().size());
        assertEquals(Procedencia.CAMPING, resultado.content().get(0).getProcedencia());
        verify(servicioRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void deberiaFiltrarServiciosPorEstadoHabilitado() {
        Servicio servicio = crearServicio(1L, "Cabaña", Procedencia.CAMPING, true);
        Page<Servicio> page = new PageImpl<>(List.of(servicio));
        when(servicioRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ListadoServiciosRequestDto filtros = new ListadoServiciosRequestDto(null, null, EstadoServicio.HABILITADO);
        PageResponse<ListadoServiciosResponseDto> resultado = servicioService.getListadoServicios(filtros, pageRequest());

        assertEquals(1, resultado.content().size());
        assertEquals(EstadoServicio.HABILITADO, resultado.content().get(0).getEstado());
        verify(servicioRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void deberiaFiltrarServiciosPorEstadoDeshabilitado() {
        Servicio servicio = crearServicio(1L, "Cancha", Procedencia.SEDE, false);
        Page<Servicio> page = new PageImpl<>(List.of(servicio));
        when(servicioRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ListadoServiciosRequestDto filtros = new ListadoServiciosRequestDto(null, null, EstadoServicio.DESHABILITADO);
        PageResponse<ListadoServiciosResponseDto> resultado = servicioService.getListadoServicios(filtros, pageRequest());

        assertEquals(1, resultado.content().size());
        assertEquals(EstadoServicio.DESHABILITADO, resultado.content().get(0).getEstado());
        verify(servicioRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void deberiaFiltrarConTodosLosParametrosCombinados() {
        Servicio servicio = crearServicio(1L, "Cabaña", Procedencia.CAMPING, true);
        Page<Servicio> page = new PageImpl<>(List.of(servicio));
        when(servicioRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ListadoServiciosRequestDto filtros = new ListadoServiciosRequestDto("Cabaña", Procedencia.CAMPING, EstadoServicio.HABILITADO);
        PageResponse<ListadoServiciosResponseDto> resultado = servicioService.getListadoServicios(filtros, pageRequest());

        assertEquals(1, resultado.content().size());
        verify(servicioRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void deberiaRetornarPaginaVaciaCuandoNoHayCoincidencias() {
        Page<Servicio> page = Page.empty(pageRequest().toPageable());
        when(servicioRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ListadoServiciosRequestDto filtros = new ListadoServiciosRequestDto("nombreQueNoExiste", null, null);
        PageResponse<ListadoServiciosResponseDto> resultado = servicioService.getListadoServicios(filtros, pageRequest());

        assertNotNull(resultado);
        assertEquals(0, resultado.totalElements());
        assertTrue(resultado.content().isEmpty());
        verify(servicioRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void deberiaNormalizarNombreConEspacios() {
        Page<Servicio> page = new PageImpl<>(List.of());
        when(servicioRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ListadoServiciosRequestDto filtros = new ListadoServiciosRequestDto("   ", null, null);
        PageResponse<ListadoServiciosResponseDto> resultado = servicioService.getListadoServicios(filtros, pageRequest());

        assertNotNull(resultado);
        verify(servicioRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void deberiaLanzarExcepcionCuandoServicioTieneHabilitadoNull() {
        Servicio servicio = crearServicio(1L, "Cabaña", Procedencia.CAMPING, null);
        Page<Servicio> page = new PageImpl<>(List.of(servicio));
        when(servicioRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ListadoServiciosRequestDto filtros = new ListadoServiciosRequestDto(null, null, null);

        assertThrows(IllegalStateException.class,
                () -> servicioService.getListadoServicios(filtros, pageRequest()));
    }

    private ModificacionServicioDto crearDto(String nombre, BigDecimal precioParticular, BigDecimal precioSocio) {
        ModificacionServicioDto dto = new ModificacionServicioDto();
        dto.setNombre(nombre);
        dto.setPrecioParticular(precioParticular);
        dto.setPrecioSocio(precioSocio);
        dto.setModalidadPrecio(ModalidadPrecio.POR_DIA);
        dto.setCapacidad(4);
        dto.setCantidad(2);
        return dto;
    }

    @Test
    void deberiaModificarServicioExitosamente() {
        Long servicioId = 1L;
        Servicio servicio = crearServicio(servicioId, true);
        ModificacionServicioDto dto = crearDto("Cabaña Premium", BigDecimal.valueOf(3000), BigDecimal.valueOf(2000));

        when(servicioRepository.findById(servicioId)).thenReturn(Optional.of(servicio));
        when(servicioRepository.save(servicio)).thenReturn(servicio);

        ServicioResponseDto resultado = servicioService.modificarServicio(servicioId, dto);

        assertNotNull(resultado);
        assertEquals("Cabaña Premium", resultado.getNombre());
        assertEquals(BigDecimal.valueOf(3000), resultado.getPrecioParticular());
        assertEquals(BigDecimal.valueOf(2000), resultado.getPrecioSocio());
        verify(modificacionServicioValidator).validar(any(ModificacionValidationContext.class));
        verify(servicioRepository).save(servicio);
    }

    @Test
    void deberiaLanzarErrorCuandoServicioNoExisteAlModificar() {
        Long servicioId = 99L;
        ModificacionServicioDto dto = crearDto("Cabaña", BigDecimal.valueOf(3000), BigDecimal.valueOf(2000));

        when(servicioRepository.findById(servicioId)).thenReturn(Optional.empty());

        assertThrows(ServicioNotFoundException.class,
                () -> servicioService.modificarServicio(servicioId, dto));

        verify(servicioRepository, never()).save(any());
    }

    @Test
    void deberiaLanzarErrorCuandoNombreEsDuplicadoAlModificar() {
        Long servicioId = 1L;
        Servicio servicio = crearServicio(servicioId, true);
        ModificacionServicioDto dto = crearDto("Cabaña Existente", BigDecimal.valueOf(3000), BigDecimal.valueOf(2000));

        when(servicioRepository.findById(servicioId)).thenReturn(Optional.of(servicio));
        doThrow(new ServicioValidacionException(
                ServicioCodigoError.NOMBRE_DUPLICADO.name(),
                "Ya existe un servicio con ese nombre"))
                .when(modificacionServicioValidator).validar(any(ModificacionValidationContext.class));

        ServicioValidacionException exception = assertThrows(ServicioValidacionException.class,
                () -> servicioService.modificarServicio(servicioId, dto));

        assertEquals(ServicioCodigoError.NOMBRE_DUPLICADO.name(), exception.getCodigo());
        verify(servicioRepository, never()).save(any());
    }

    @Test
    void deberiaLanzarErrorCuandoPrecioSocioEsMayorOIgualAlParticular() {
        Long servicioId = 1L;
        Servicio servicio = crearServicio(servicioId, true);
        ModificacionServicioDto dto = crearDto("Cabaña", BigDecimal.valueOf(2000), BigDecimal.valueOf(2000));

        when(servicioRepository.findById(servicioId)).thenReturn(Optional.of(servicio));

        ServicioValidacionException exception = assertThrows(ServicioValidacionException.class,
                () -> servicioService.modificarServicio(servicioId, dto));

        assertEquals(ServicioCodigoError.PRECIO_SOCIO_MAYOR_O_IGUAL_PARTICULAR.name(), exception.getCodigo());
        verify(servicioRepository, never()).save(any());
    }

    @Test
    void deberiaLanzarErrorCuandoLasReservasNoSonProximas() {
        Long servicioId = 1L;

        Servicio servicio = new Servicio();
        servicio.setId(servicioId);
        servicio.setHabilitado(true);

        Reserva reservaProxima = Reserva.crear(
                TipoReserva.COMUN,
                2L,
                servicioId,
                Procedencia.CAMPING,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                null, null, null, null, null, null, null, null,
                false,true
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


   private ServicioRegistroRequestDto crearDtoRegistro(
        String nombre,
        BigDecimal precioParticular,
        BigDecimal precioSocio,
        Integer capacidad,
        Integer cantidad
) {
    ServicioRegistroRequestDto dto = new ServicioRegistroRequestDto();

    dto.setNombre(nombre);
    dto.setProcedencia(Procedencia.CAMPING);
    dto.setPrecioParticular(precioParticular);
    dto.setPrecioSocio(precioSocio);
    dto.setModalidadPrecio(ModalidadPrecio.POR_DIA);
    dto.setCapacidad(capacidad);
    dto.setCantidad(cantidad);

    return dto;
}


@Test
void deberiaRegistrarServicioExitosamente() {
    ServicioRegistroRequestDto dto = crearDtoRegistro("Cabaña Nueva", BigDecimal.valueOf(2500), BigDecimal.valueOf(1500), 4, 2);

    Servicio servicioGuardado = new Servicio();
    servicioGuardado.setId(1L);
    servicioGuardado.setNombre("Cabaña Nueva");
    servicioGuardado.setProcedencia(Procedencia.CAMPING);
    servicioGuardado.setPrecioParticular(BigDecimal.valueOf(2500));
    servicioGuardado.setPrecioSocio(BigDecimal.valueOf(1500));
    servicioGuardado.setModalidadPrecio(ModalidadPrecio.POR_DIA);
    servicioGuardado.setCapacidad(4);
    servicioGuardado.setCantidad(2);
    servicioGuardado.setHabilitado(true);

    when(servicioRepository.save(any(Servicio.class))).thenReturn(servicioGuardado);

    ServicioResponseDto resultado = servicioService.registrarServicio(dto);

    assertNotNull(resultado);
    assertEquals(1L, resultado.getId());
    assertEquals("Cabaña Nueva", resultado.getNombre());
    assertEquals(Procedencia.CAMPING, resultado.getProcedencia());
    assertEquals(BigDecimal.valueOf(2500), resultado.getPrecioParticular());
    assertEquals(BigDecimal.valueOf(1500), resultado.getPrecioSocio());
    assertEquals(EstadoServicio.HABILITADO, resultado.getEstado());

    verify(servicioRegistroValidator).validar(dto);
    verify(servicioRepository).save(any(Servicio.class));
}

@Test
void deberiaCrearServicioConEstadoHabilitadoPorDefecto() {
    ServicioRegistroRequestDto dto = crearDtoRegistro("Cabaña", BigDecimal.valueOf(2500), BigDecimal.valueOf(1500), null, null);
    
    Servicio servicioGuardado = new Servicio();
    servicioGuardado.setId(1L);
    servicioGuardado.setHabilitado(true);

    when(servicioRepository.save(any(Servicio.class))).thenReturn(servicioGuardado);

    ServicioResponseDto resultado = servicioService.registrarServicio(dto);

    assertEquals(EstadoServicio.HABILITADO, resultado.getEstado());
    verify(servicioRepository).save(argThat(servicio -> servicio.getHabilitado() == true));
}

@Test
void deberiaLanzarErrorCuandoNombreDuplicadoAlRegistrar() {
    ServicioRegistroRequestDto dto = crearDtoRegistro("Cabaña Existente", BigDecimal.valueOf(2500), BigDecimal.valueOf(1500), null, null);

    doThrow(new ServicioValidacionException(
            ServicioCodigoError.NOMBRE_DUPLICADO.name(),
            "Ya existe un servicio con ese nombre"))
            .when(servicioRegistroValidator).validar(dto);

    ServicioValidacionException exception = assertThrows(ServicioValidacionException.class,
            () -> servicioService.registrarServicio(dto));

    assertEquals(ServicioCodigoError.NOMBRE_DUPLICADO.name(), exception.getCodigo());
    verify(servicioRepository, never()).save(any());
}

@Test
void deberiaLanzarErrorCuandoPrecioParticularMenorQuePrecioSocioAlRegistrar() {
    ServicioRegistroRequestDto dto = crearDtoRegistro(
            "Cabaña",
            BigDecimal.valueOf(1000),
            BigDecimal.valueOf(2000),
            null,
            null
    );

    ServicioValidacionException exception =
            assertThrows(ServicioValidacionException.class,
                    () -> servicioService.registrarServicio(dto));

    assertEquals(
            ServicioCodigoError.PRECIO_SOCIO_MAYOR_O_IGUAL_PARTICULAR.name(),
            exception.getCodigo()
    );

    verify(servicioRepository, never()).save(any());
}
@Test
void deberiaGuardarTodosLosCamposCorrectamenteAlRegistrar() {
    ServicioRegistroRequestDto dto = crearDtoRegistro(
            "Cabaña Premium",
            BigDecimal.valueOf(3500),
            BigDecimal.valueOf(2000),
            null,
            null
    );

    dto.setCapacidad(6);
    dto.setCantidad(3);

    Servicio servicioGuardado = new Servicio();
    servicioGuardado.setId(1L);

    when(servicioRepository.save(any(Servicio.class)))
            .thenReturn(servicioGuardado);

    servicioService.registrarServicio(dto);

    verify(servicioRepository).save(argThat(servicio ->
        servicio.getNombre().equals("Cabaña Premium") &&
        servicio.getProcedencia().equals(Procedencia.CAMPING) &&
        servicio.getPrecioParticular().equals(BigDecimal.valueOf(3500)) &&
        servicio.getPrecioSocio().equals(BigDecimal.valueOf(2000)) &&
        servicio.getModalidadPrecio().equals(ModalidadPrecio.POR_DIA) &&
        servicio.getCapacidad().equals(6) &&
        servicio.getCantidad().equals(3) &&
        servicio.getHabilitado().equals(true)
));
}
@Test
void deberiaMapearHabilitadoFalseAEstadoDeshabilitadoEnDetalle() {
    Long servicioId = 1L;
    Servicio servicio = crearServicio(servicioId, false);

    when(servicioRepository.findById(servicioId))
            .thenReturn(Optional.of(servicio));

    ServicioResponseDto resultado = servicioService.getDetalleServicio(servicioId);

    assertEquals(EstadoServicio.DESHABILITADO, resultado.getEstado());
}

@Test
void deberiaIncluirCamposAuditoriaEnLaRespuestaDeDetalle() {
    Long servicioId = 1L;
    Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");
    Instant updatedAt = Instant.parse("2024-06-01T00:00:00Z");

    Servicio servicio = crearServicio(servicioId, true);
    ReflectionTestUtils.setField(servicio, "createdAt", createdAt);
    ReflectionTestUtils.setField(servicio, "updatedAt", updatedAt);
    ReflectionTestUtils.setField(servicio, "createdBy", "admin");
    ReflectionTestUtils.setField(servicio, "updatedBy", "editor");

    when(servicioRepository.findById(servicioId))
            .thenReturn(Optional.of(servicio));

    ServicioResponseDto resultado = servicioService.getDetalleServicio(servicioId);

    assertEquals(createdAt, resultado.getCreatedAt());
    assertEquals(updatedAt, resultado.getUpdatedAt());
    assertEquals("admin", resultado.getCreatedBy());
    assertEquals("editor", resultado.getUpdatedBy());
}

@Test
void deberiaPermitirCamposOpcionalesNulosAlRegistrar() {
    ServicioRegistroRequestDto dto = crearDtoRegistro("Cabaña", BigDecimal.valueOf(2500), BigDecimal.valueOf(1500), null, null);
    dto.setCapacidad(null);
    dto.setCantidad(null);
    
    Servicio servicioGuardado = new Servicio();
    servicioGuardado.setId(1L);

    when(servicioRepository.save(any(Servicio.class))).thenReturn(servicioGuardado);

    servicioService.registrarServicio(dto);

    verify(servicioRepository).save(argThat(servicio ->
            servicio.getCapacidad() == null &&
            servicio.getCantidad() == null
    ));
}

    @Test
    void deberiaRetornarReservasProximasConNombreCliente() {
        Long servicioId = 1L;
        Long clienteId = 10L;

        Servicio servicio = crearServicio(servicioId, true);

        Reserva reserva = Reserva.crear(
                TipoReserva.COMUN,
                clienteId,
                servicioId,
                Procedencia.CAMPING,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                null, null, null, null, null, null, null, null,
                false,false
        );
        ReflectionTestUtils.setField(reserva, "id", 1L);

        when(servicioRepository.findById(servicioId)).thenReturn(Optional.of(servicio));
        when(reservaService.obtenerProximasPorServicioEnRango(servicioId)).thenReturn(List.of(reserva));
        when(clienteService.getNombresByIds(any())).thenReturn(Map.of(clienteId, "Juan Pérez"));

        List<ReservaProximaResponseDto> resultado = servicioService.getReservasProximas(servicioId);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals(clienteId, resultado.get(0).getClienteId());
        assertEquals("Juan Pérez", resultado.get(0).getNombreCliente());
        verify(clienteService).getNombresByIds(any());
    }

    @Test
    void deberiaRetornarListaVaciaSiNoHayReservasProximas() {
        Long servicioId = 1L;

        Servicio servicio = crearServicio(servicioId, true);

        when(servicioRepository.findById(servicioId)).thenReturn(Optional.of(servicio));
        when(reservaService.obtenerProximasPorServicioEnRango(servicioId)).thenReturn(List.of());
        when(clienteService.getNombresByIds(any())).thenReturn(Map.of());

        List<ReservaProximaResponseDto> resultado = servicioService.getReservasProximas(servicioId);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(clienteService).getNombresByIds(any());
    }

    @Test
    void deberiaLanzarServicioNotFoundEnGetReservasProximas() {
        Long servicioId = 99L;

        when(servicioRepository.findById(servicioId)).thenReturn(Optional.empty());

        assertThrows(ServicioNotFoundException.class,
                () -> servicioService.getReservasProximas(servicioId));

        verify(reservaService, never()).obtenerProximasPorServicioEnRango(any());
        verify(clienteService, never()).getNombresByIds(any());
    }

    @Test
    void deberiaLanzarIllegalStateExceptionCuandoClienteNoExisteParaReserva() {
        Long servicioId = 1L;
        Long clienteId = 10L;

        Servicio servicio = crearServicio(servicioId, true);

        Reserva reserva = Reserva.crear(
                TipoReserva.COMUN,
                clienteId,
                servicioId,
                Procedencia.CAMPING,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                null, null, null, null, null, null, null, null,
                false,false
        );
        ReflectionTestUtils.setField(reserva, "id", 1L);

        when(servicioRepository.findById(servicioId)).thenReturn(Optional.of(servicio));
        when(reservaService.obtenerProximasPorServicioEnRango(servicioId)).thenReturn(List.of(reserva));
        when(clienteService.getNombresByIds(any())).thenReturn(Map.of());

        assertThrows(IllegalStateException.class,
                () -> servicioService.getReservasProximas(servicioId));
    }

    @Test
    void deberiaMapearTodosLosCamposDelDtoCorrectamente() {
        Long servicioId = 1L;
        Long clienteId = 10L;
        LocalDate fechaEntrada = LocalDate.of(2026, 6, 1);
        LocalDate fechaSalida = LocalDate.of(2026, 6, 5);

        Servicio servicio = crearServicio(servicioId, true);

        Reserva reserva = Reserva.crear(
                TipoReserva.COMUN,
                clienteId,
                servicioId,
                Procedencia.CAMPING,
                fechaEntrada,
                fechaSalida,
                null, null, null, null, null, null, null, null,
                false,false
        );
        ReflectionTestUtils.setField(reserva, "id", 5L);

        when(servicioRepository.findById(servicioId)).thenReturn(Optional.of(servicio));
        when(reservaService.obtenerProximasPorServicioEnRango(servicioId)).thenReturn(List.of(reserva));
        when(clienteService.getNombresByIds(any())).thenReturn(Map.of(clienteId, "Ana García"));

        List<ReservaProximaResponseDto> resultado = servicioService.getReservasProximas(servicioId);

        assertEquals(1, resultado.size());
        ReservaProximaResponseDto dto = resultado.get(0);
        assertEquals(5L, dto.getId());
        assertEquals(clienteId, dto.getClienteId());
        assertEquals("Ana García", dto.getNombreCliente());
        assertEquals(fechaEntrada, dto.getFechaEntrada());
        assertEquals(fechaSalida, dto.getFechaSalida());
        assertFalse(dto.getPago());
        assertEquals(EstadoReserva.CONFIRMADA, dto.getEstado());
    }

    @Test
    void deberiaResolverNombreCorrectamenteParaMultiplesReservasYClientes() {
        Long servicioId = 1L;
        Long clienteId1 = 10L;
        Long clienteId2 = 20L;

        Servicio servicio = crearServicio(servicioId, true);

        Reserva reserva1 = Reserva.crear(
                TipoReserva.COMUN,
                clienteId1,
                servicioId,
                Procedencia.CAMPING,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                null, null, null, null, null, null, null, null,
                false,false
        );
        ReflectionTestUtils.setField(reserva1, "id", 1L);

        Reserva reserva2 = Reserva.crear(
                TipoReserva.COMUN,
                clienteId2,
                servicioId,
                Procedencia.CAMPING,
                LocalDate.now().plusDays(3),
                LocalDate.now().plusDays(4),
                null, null, null, null, null, null, null, null,
                false,false
        );
        ReflectionTestUtils.setField(reserva2, "id", 2L);

        when(servicioRepository.findById(servicioId)).thenReturn(Optional.of(servicio));
        when(reservaService.obtenerProximasPorServicioEnRango(servicioId)).thenReturn(List.of(reserva1, reserva2));
        when(clienteService.getNombresByIds(any())).thenReturn(Map.of(
                clienteId1, "Juan Pérez",
                clienteId2, "Ana García"
        ));

        List<ReservaProximaResponseDto> resultado = servicioService.getReservasProximas(servicioId);

        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals(clienteId1, resultado.get(0).getClienteId());
        assertEquals("Juan Pérez", resultado.get(0).getNombreCliente());
        assertEquals(2L, resultado.get(1).getId());
        assertEquals(clienteId2, resultado.get(1).getClienteId());
        assertEquals("Ana García", resultado.get(1).getNombreCliente());
    }

    @Test
    void deberiaRetornarFechasOcupadasDelServicio() {
        Long servicioId = 1L;
        LocalDate desde = LocalDate.of(2026, 6, 16);
        LocalDate hasta = LocalDate.of(2026, 6, 20);

        Servicio servicio = crearServicio(servicioId, true);

        Reserva reserva = Reserva.crear(
                TipoReserva.COMUN,
                10L,
                servicioId,
                Procedencia.CAMPING,
                LocalDate.of(2026, 6, 17),
                LocalDate.of(2026, 6, 19),
                null, null, null, null, null, null, null, null,
                false,false
        );
        ReflectionTestUtils.setField(reserva, "id", 5L);

        when(servicioRepository.findById(servicioId)).thenReturn(Optional.of(servicio));
        when(reservaService.obtenerOcupacionPorServicioEnRango(servicioId, desde, hasta))
                .thenReturn(List.of(reserva));

        List<ServicioReservaOcupacionDto> resultado =
                servicioService.getFechasOcupadas(servicioId, desde, hasta);

        assertEquals(1, resultado.size());
        ServicioReservaOcupacionDto dto = resultado.get(0);
        assertEquals(5L, dto.reservaId());
        assertEquals(EstadoReserva.CONFIRMADA, dto.estado());
        assertEquals(LocalDate.of(2026, 6, 17), dto.fechaInicio());
        assertEquals(LocalDate.of(2026, 6, 19), dto.fechaFin());
        verify(reservaService).obtenerOcupacionPorServicioEnRango(servicioId, desde, hasta);
    }

    @Test
    void deberiaRetornarListaVaciaCuandoNoHayFechasOcupadas() {
        Long servicioId = 1L;
        LocalDate desde = LocalDate.of(2026, 6, 16);
        LocalDate hasta = LocalDate.of(2026, 6, 20);

        Servicio servicio = crearServicio(servicioId, true);

        when(servicioRepository.findById(servicioId)).thenReturn(Optional.of(servicio));
        when(reservaService.obtenerOcupacionPorServicioEnRango(servicioId, desde, hasta))
                .thenReturn(List.of());

        List<ServicioReservaOcupacionDto> resultado =
                servicioService.getFechasOcupadas(servicioId, desde, hasta);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void deberiaPermitirRangoDeUnSoloDiaEnFechasOcupadas() {
        Long servicioId = 1L;
        LocalDate dia = LocalDate.of(2026, 6, 16);

        Servicio servicio = crearServicio(servicioId, true);

        when(servicioRepository.findById(servicioId)).thenReturn(Optional.of(servicio));
        when(reservaService.obtenerOcupacionPorServicioEnRango(servicioId, dia, dia))
                .thenReturn(List.of());

        List<ServicioReservaOcupacionDto> resultado =
                servicioService.getFechasOcupadas(servicioId, dia, dia);

        assertNotNull(resultado);
        verify(reservaService).obtenerOcupacionPorServicioEnRango(servicioId, dia, dia);
    }

    @Test
    void deberiaLanzarErrorCuandoElRangoDeFechasEsInvalido() {
        Long servicioId = 1L;
        LocalDate desde = LocalDate.of(2026, 6, 20);
        LocalDate hasta = LocalDate.of(2026, 6, 16);

        ServicioValidacionException exception = assertThrows(ServicioValidacionException.class,
                () -> servicioService.getFechasOcupadas(servicioId, desde, hasta));

        assertEquals(ServicioCodigoError.RANGO_FECHAS_INVALIDO.name(), exception.getCodigo());
        verify(servicioRepository, never()).findById(any());
        verify(reservaService, never()).obtenerOcupacionPorServicioEnRango(any(), any(), any());
    }

    @Test
    void deberiaLanzarServicioNotFoundEnFechasOcupadas() {
        Long servicioId = 99L;
        LocalDate desde = LocalDate.of(2026, 6, 16);
        LocalDate hasta = LocalDate.of(2026, 6, 20);

        when(servicioRepository.findById(servicioId)).thenReturn(Optional.empty());

        assertThrows(ServicioNotFoundException.class,
                () -> servicioService.getFechasOcupadas(servicioId, desde, hasta));

        verify(reservaService, never()).obtenerOcupacionPorServicioEnRango(any(), any(), any());
    }
}

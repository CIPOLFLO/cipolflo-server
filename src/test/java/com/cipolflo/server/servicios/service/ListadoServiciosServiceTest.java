package com.cipolflo.server.servicios.service;

import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.domain.enums.EstadoServicio;
import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.servicios.dto.ListadoServiciosRequestDto;
import com.cipolflo.server.servicios.dto.ListadoServiciosResponseDto;
import com.cipolflo.server.servicios.repository.ServicioRepository;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListadoServiciosServiceTest {

    @Mock
    private ServicioRepository servicioRepository;

    @InjectMocks
    private ServicioService servicioService;

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
        return new PageRequestDto(0, 10);
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
}

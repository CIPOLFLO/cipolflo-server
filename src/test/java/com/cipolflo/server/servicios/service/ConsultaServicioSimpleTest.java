package com.cipolflo.server.servicios.service;

import com.cipolflo.server.reservas.dto.ServicioDetalleReservaDto;
import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.dto.ServicioReferenciaDto;
import com.cipolflo.server.servicios.exception.ServicioNotFoundException;
import com.cipolflo.server.servicios.repository.ServicioRepository;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultaServicioSimpleTest {

    @Mock
    private ServicioRepository servicioRepository;

    @InjectMocks
    private ConsultaServicioSimple consultaServicioSimple;

    @Test
    void deberiaRetornarDtoConTodosLosCamposCuandoServicioExiste() {
        Servicio servicio = new Servicio();
        servicio.setId(1L);
        servicio.setNombre("Cabaña");
        servicio.setProcedencia(Procedencia.CAMPING);

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));

        ServicioDetalleReservaDto resultado = consultaServicioSimple.getDetalleServicioSimple(1L);

        assertEquals(1L, resultado.id());
        assertEquals("Cabaña", resultado.nombre());
        assertEquals(Procedencia.CAMPING, resultado.procedencia());
        verify(servicioRepository).findById(1L);
    }

    @Test
    void deberiaLanzarServicioNotFoundExceptionCuandoServicioNoExiste() {
        when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ServicioNotFoundException.class,
                () -> consultaServicioSimple.getDetalleServicioSimple(99L));

        verify(servicioRepository).findById(99L);
    }

    @Test
    void buscarPorNombre_deberiaMapearTodasLasCoincidenciasIncluyendoDeshabilitados() {
        Servicio servicioSede = new Servicio();
        servicioSede.setId(1L);
        servicioSede.setNombre("Cabaña 1");
        servicioSede.setProcedencia(Procedencia.SEDE);
        servicioSede.setHabilitado(true);

        Servicio servicioCampingDeshabilitado = new Servicio();
        servicioCampingDeshabilitado.setId(2L);
        servicioCampingDeshabilitado.setNombre("Cabaña 1");
        servicioCampingDeshabilitado.setProcedencia(Procedencia.CAMPING);
        servicioCampingDeshabilitado.setHabilitado(false);

        when(servicioRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(servicioSede, servicioCampingDeshabilitado));

        List<ServicioReferenciaDto> resultado = consultaServicioSimple.buscarPorNombre("cabaña 1", null);

        assertEquals(2, resultado.size());
        assertTrue(resultado.contains(new ServicioReferenciaDto(1L, "Cabaña 1", Procedencia.SEDE, true)));
        assertTrue(resultado.contains(new ServicioReferenciaDto(2L, "Cabaña 1", Procedencia.CAMPING, false)));
    }
}

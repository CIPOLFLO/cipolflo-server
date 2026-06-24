package com.cipolflo.server.servicios.service;

import com.cipolflo.server.reservas.dto.ServicioDetalleReservaDto;
import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.servicios.exception.ServicioNotFoundException;
import com.cipolflo.server.servicios.repository.ServicioRepository;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
        servicio.setModalidadPrecio(ModalidadPrecio.POR_DIA);

        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));

        ServicioDetalleReservaDto resultado = consultaServicioSimple.getDetalleServicioSimple(1L);

        assertEquals(1L, resultado.id());
        assertEquals("Cabaña", resultado.nombre());
        assertEquals(Procedencia.CAMPING, resultado.procedencia());
        assertEquals(ModalidadPrecio.POR_DIA, resultado.modalidadPrecio());
        verify(servicioRepository).findById(1L);
    }

    @Test
    void deberiaLanzarServicioNotFoundExceptionCuandoServicioNoExiste() {
        when(servicioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ServicioNotFoundException.class,
                () -> consultaServicioSimple.getDetalleServicioSimple(99L));

        verify(servicioRepository).findById(99L);
    }
}

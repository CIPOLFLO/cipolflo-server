package com.cipolflo.server.servicios.mapper;

import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.domain.enums.EstadoServicio;
import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.servicios.dto.ListadoServiciosResponseDto;
import com.cipolflo.server.shared.enums.Procedencia;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServicioMapperTest {

    private Servicio crearServicio(Boolean habilitado) {
        Servicio s = new Servicio();
        s.setId(1L);
        s.setNombre("Cabaña");
        s.setProcedencia(Procedencia.CAMPING);
        s.setPrecioParticular(BigDecimal.valueOf(2500));
        s.setPrecioSocio(BigDecimal.valueOf(1500));
        s.setModalidadPrecio(ModalidadPrecio.POR_DIA);
        s.setHabilitado(habilitado);
        return s;
    }

    @Test
    void deberiaMapearServicioHabilitado() {
        ListadoServiciosResponseDto dto = ServicioMapper.toListadoResponseDto(crearServicio(true));

        assertEquals(EstadoServicio.HABILITADO, dto.getEstado());
    }

    @Test
    void deberiaMapearServicioDeshabilitado() {
        ListadoServiciosResponseDto dto = ServicioMapper.toListadoResponseDto(crearServicio(false));

        assertEquals(EstadoServicio.DESHABILITADO, dto.getEstado());
    }

    @Test
    void deberiaLanzarExcepcionCuandoHabilitadoEsNull() {
        Servicio servicio = crearServicio(null);

        assertThrows(IllegalStateException.class, () -> ServicioMapper.toListadoResponseDto(servicio));
    }

    @Test
    void deberiaMapearTodosLosCamposCorrecatamente() {
        ListadoServiciosResponseDto dto = ServicioMapper.toListadoResponseDto(crearServicio(true));

        assertEquals(1L, dto.getId());
        assertEquals("Cabaña", dto.getNombre());
        assertEquals(Procedencia.CAMPING, dto.getProcedencia());
        assertEquals(BigDecimal.valueOf(2500), dto.getPrecioParticular());
        assertEquals(BigDecimal.valueOf(1500), dto.getPrecioSocio());
        assertEquals(ModalidadPrecio.POR_DIA, dto.getModalidadPrecio());
    }

    // ── toServicioDetalleSimple ─────────────────────────────────────────────────

    @Test
    void deberiaMapearTodosLosCamposEnServicioDetalleSimple() {
        com.cipolflo.server.reservas.dto.ServicioDetalleReservaDto dto =
                ServicioMapper.toServicioDetalleSimple(crearServicio(true));

        assertEquals(1L, dto.id());
        assertEquals("Cabaña", dto.nombre());
        assertEquals(Procedencia.CAMPING, dto.procedencia());
        assertEquals(ModalidadPrecio.POR_DIA, dto.modalidadPrecio());
    }

    @Test
    void deberiaMapearServicioDeshabilitadoEnServicioDetalleSimple() {
        com.cipolflo.server.reservas.dto.ServicioDetalleReservaDto dto =
                ServicioMapper.toServicioDetalleSimple(crearServicio(false));

        assertEquals(1L, dto.id());
        assertEquals("Cabaña", dto.nombre());
    }
}

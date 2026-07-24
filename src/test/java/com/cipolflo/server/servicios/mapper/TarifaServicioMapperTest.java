package com.cipolflo.server.servicios.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.servicios.domain.enums.TipoClienteTarifa;
import org.junit.jupiter.api.Test;

import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.domain.TarifaServicio;
import com.cipolflo.server.servicios.dto.TarifaServicioRequestDto;
import com.cipolflo.server.servicios.dto.TarifaServicioResponseDto;

class TarifaServicioMapperTest {

    @Test
    void deberiaMapearRequestDtoAEntidad() {
        Servicio servicio = new Servicio();

        TarifaServicioRequestDto dto = new TarifaServicioRequestDto();
        dto.setTipoCliente(TipoClienteTarifa.PARTICULAR);
        dto.setPrecio(BigDecimal.valueOf(2500));
        dto.setModalidadPrecio(ModalidadPrecio.POR_DIA);
        dto.setAntiguedadMinima(null);
        dto.setAntiguedadMaxima(null);

        TarifaServicio tarifa = TarifaServicioMapper.toEntity(servicio, dto);

        assertEquals(servicio, tarifa.getServicio());
        assertEquals(TipoClienteTarifa.PARTICULAR, tarifa.getTipoCliente());
        assertEquals(BigDecimal.valueOf(2500), tarifa.getPrecio());
        assertEquals(ModalidadPrecio.POR_DIA, tarifa.getModalidadPrecio());
        assertNull(tarifa.getAntiguedadMinima());
        assertNull(tarifa.getAntiguedadMaxima());
    }

    @Test
    void deberiaMapearEntidadAResponseDto() {
        Servicio servicio = new Servicio();

        TarifaServicio tarifa = TarifaServicio.registrar(
                servicio,
                TipoClienteTarifa.SOCIO_COMUN,
                BigDecimal.valueOf(1500),
                ModalidadPrecio.POR_DIA,
                0,
                5
        );

        TarifaServicioResponseDto dto = TarifaServicioMapper.toResponseDto(tarifa);

        assertEquals(tarifa.getId(), dto.getId());
        assertEquals(TipoClienteTarifa.SOCIO_COMUN, dto.getTipoCliente());
        assertEquals(BigDecimal.valueOf(1500), dto.getPrecio());
        assertEquals(ModalidadPrecio.POR_DIA, dto.getModalidadPrecio());
        assertEquals(0, dto.getAntiguedadMinima());
        assertEquals(5, dto.getAntiguedadMaxima());
    }
}
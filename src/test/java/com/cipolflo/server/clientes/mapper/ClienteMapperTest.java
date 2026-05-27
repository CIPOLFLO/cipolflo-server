package com.cipolflo.server.clientes.mapper;

import com.cipolflo.server.clientes.domain.Particular;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.clientes.dto.ListadoClientesResponseDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ClienteMapperTest {

    private Socio crearSocio() {
        Socio socio = new Socio();
        socio.setId(1L);
        socio.setNombreCompleto("Juan Pérez");
        socio.setCedula("12345678");
        socio.setTelefono("099111111");
        socio.setNumeroSocio(5);
        socio.setEstado(EstadoSocio.AL_DIA);
        socio.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        socio.setDepartamento("Montevideo");
        socio.setDireccion("Av. 18 de Julio 100");
        socio.setFechaIngreso(LocalDate.of(2022, 1, 1));
        socio.setMetodoCobro(MetodoCobro.EN_SEDE);
        return socio;
    }

    private Particular crearParticular() {
        Particular particular = new Particular();
        particular.setId(2L);
        particular.setNombreCompleto("Laura Fernández");
        particular.setCedula("67890123");
        particular.setTelefono("099666666");
        return particular;
    }

    @Test
    void deberiaMapearTodosLosCamposDeUnSocio() {
        ListadoClientesResponseDto dto = ClienteMapper.toListadoResponseDto(crearSocio());

        assertEquals(1L, dto.getId());
        assertEquals("Juan Pérez", dto.getNombreCompleto());
        assertEquals("12345678", dto.getCedula());
        assertEquals(TipoCliente.SOCIO, dto.getTipoCliente());
        assertEquals(5, dto.getNumeroSocio());
        assertEquals(EstadoSocio.AL_DIA, dto.getEstado());
    }

    @Test
    void deberiaMapearTipoComoSocio() {
        ListadoClientesResponseDto dto = ClienteMapper.toListadoResponseDto(crearSocio());

        assertEquals(TipoCliente.SOCIO, dto.getTipoCliente());
    }

    @Test
    void deberiaMapearParticularConNumeroSocioYEstadoNulos() {
        ListadoClientesResponseDto dto = ClienteMapper.toListadoResponseDto(crearParticular());

        assertNull(dto.getNumeroSocio());
        assertNull(dto.getEstado());
    }

    @Test
    void deberiaMapearTipoComoParticular() {
        ListadoClientesResponseDto dto = ClienteMapper.toListadoResponseDto(crearParticular());

        assertEquals(TipoCliente.PARTICULAR, dto.getTipoCliente());
    }

    @Test
    void deberiaMapearSocioConNumeroSocioNulo() {
        Socio socio = crearSocio();
        socio.setNumeroSocio(null);

        ListadoClientesResponseDto dto = ClienteMapper.toListadoResponseDto(socio);

        assertEquals(TipoCliente.SOCIO, dto.getTipoCliente());
        assertNull(dto.getNumeroSocio());
        assertEquals(EstadoSocio.AL_DIA, dto.getEstado());
    }

    @Test
    void deberiaMapearCamposBaseDeParticular() {
        ListadoClientesResponseDto dto = ClienteMapper.toListadoResponseDto(crearParticular());

        assertEquals(2L, dto.getId());
        assertEquals("Laura Fernández", dto.getNombreCompleto());
        assertEquals("67890123", dto.getCedula());
    }
}

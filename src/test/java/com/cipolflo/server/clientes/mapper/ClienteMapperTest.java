package com.cipolflo.server.clientes.mapper;

import com.cipolflo.server.clientes.domain.PagoCuota;
import com.cipolflo.server.clientes.domain.Particular;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.clientes.dto.ClienteResponseDto;
import com.cipolflo.server.clientes.dto.ListadoClientesResponseDto;
import com.cipolflo.server.shared.enums.FormaPago;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ClienteMapperTest {

    private Socio crearSocio() {
        Socio socio = new Socio();
        socio.setId(1L);
        socio.setNombreCompleto("Juan Pérez");
        socio.setCedula("12345678");
        socio.setTelefono("099111111");
        socio.setMail("juan@mail.com");
        socio.setNumeroSocio(5);
        socio.setEstado(EstadoSocio.ACTIVO);
        socio.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        socio.setPais("Uruguay");
        socio.setDepartamento("Montevideo");
        socio.setCiudad("Montevideo");
        socio.setDireccion("Av. 18 de Julio 100");
        socio.setFechaIngreso(LocalDate.of(2022, 1, 1));
        socio.setMetodoCobro(MetodoCobro.EFECTIVO);
        return socio;
    }

    private Particular crearParticular() {
        Particular particular = new Particular();
        particular.setId(2L);
        particular.setNombreCompleto("Laura Fernández");
        particular.setCedula("67890123");
        particular.setTelefono("099666666");
        particular.setMail("laura@mail.com");
        return particular;
    }

    @Test
    void deberiaMapearTodosLosCamposDeUnSocio() {
        ListadoClientesResponseDto dto = ClienteMapper.toListadoResponseDto(crearSocio());

        assertEquals(1L, dto.getId());
        assertEquals("Juan Pérez", dto.getNombreCompleto());
        assertEquals("12345678", dto.getCedula());
        assertEquals("juan@mail.com", dto.getEmail());
        assertEquals(TipoCliente.SOCIO, dto.getTipoCliente());
        assertEquals(5, dto.getNumeroSocio());
        assertEquals(EstadoSocio.ACTIVO, dto.getEstado());
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
    void deberiaMapearEmailNuloCuandoClienteNoTieneMail() {
        Socio socio = crearSocio();
        socio.setMail(null);

        ListadoClientesResponseDto dto = ClienteMapper.toListadoResponseDto(socio);

        assertNull(dto.getEmail());
    }

    @Test
    void deberiaMapearSocioConNumeroSocioNulo() {
        Socio socio = crearSocio();
        socio.setNumeroSocio(null);

        ListadoClientesResponseDto dto = ClienteMapper.toListadoResponseDto(socio);

        assertEquals(TipoCliente.SOCIO, dto.getTipoCliente());
        assertNull(dto.getNumeroSocio());
        assertEquals(EstadoSocio.ACTIVO, dto.getEstado());
    }

    @Test
    void deberiaMapearCamposBaseDeParticular() {
        ListadoClientesResponseDto dto = ClienteMapper.toListadoResponseDto(crearParticular());

        assertEquals(2L, dto.getId());
        assertEquals("Laura Fernández", dto.getNombreCompleto());
        assertEquals("67890123", dto.getCedula());
        assertEquals("laura@mail.com", dto.getEmail());
    }

    @Test
    void deberiaMapearTodosLosCamposDeUnSocioEnDetalle() {
        ClienteResponseDto dto = ClienteMapper.toDetalleResponseDto(crearSocio(), null);

        assertEquals(1L, dto.getId());
        assertEquals("Juan Pérez", dto.getNombre());
        assertEquals("12345678", dto.getCedula());
        assertEquals(LocalDate.of(1990, 1, 1), dto.getFechaNacimiento());
        assertEquals("099111111", dto.getTelefono());
        assertEquals("juan@mail.com", dto.getEmail());
        assertEquals(MetodoCobro.EFECTIVO, dto.getMetodoCobro());
        assertEquals("Uruguay", dto.getPais());
        assertEquals("Montevideo", dto.getDepartamento());
        assertEquals("Montevideo", dto.getCiudad());
        assertEquals("Av. 18 de Julio 100", dto.getDireccion());
        assertEquals(5, dto.getNumeroSocio());
        assertEquals(TipoCliente.SOCIO, dto.getTipoCliente());
        assertEquals(EstadoSocio.ACTIVO, dto.getEstado());
    }

    @Test
    void deberiaMapearCamposDeAuditoriaEnDetalle() {
        Socio socio = crearSocio();
        Instant ahora = Instant.now();
        ReflectionTestUtils.setField(socio, "createdAt", ahora);
        ReflectionTestUtils.setField(socio, "createdBy", "admin@test.com");

        ClienteResponseDto dto = ClienteMapper.toDetalleResponseDto(socio, null);

        assertEquals(ahora, dto.getCreatedAt());
        assertEquals("admin@test.com", dto.getCreatedBy());
    }

    @Test
    void deberiaMapearTipoComoSocioEnDetalle() {
        ClienteResponseDto dto = ClienteMapper.toDetalleResponseDto(crearSocio(), null);

        assertEquals(TipoCliente.SOCIO, dto.getTipoCliente());
    }

    @Test
    void deberiaMapearParticularConCamposSocioNulosEnDetalle() {
        ClienteResponseDto dto = ClienteMapper.toDetalleResponseDto(crearParticular(), null);

        assertNull(dto.getFechaNacimiento());
        assertNull(dto.getMetodoCobro());
        assertNull(dto.getPais());
        assertNull(dto.getDepartamento());
        assertNull(dto.getCiudad());
        assertNull(dto.getDireccion());
        assertNull(dto.getNumeroSocio());
        assertNull(dto.getEstado());
    }

    @Test
    void deberiaMapearCamposBaseDeParticularEnDetalle() {
        ClienteResponseDto dto = ClienteMapper.toDetalleResponseDto(crearParticular(), null);

        assertEquals(2L, dto.getId());
        assertEquals("Laura Fernández", dto.getNombre());
        assertEquals("67890123", dto.getCedula());
        assertEquals("099666666", dto.getTelefono());
        assertEquals("laura@mail.com", dto.getEmail());
        assertEquals(TipoCliente.PARTICULAR, dto.getTipoCliente());
    }

    @Test
    void deberiaMapearUltimaCuotaPaga() {
        Socio socio = crearSocio();

        PagoCuota pago = new PagoCuota();
        pago.setFecha(Instant.parse("2026-06-10T10:00:00Z"));
        pago.setFormaPago(FormaPago.TRANSFERENCIA);

        ClienteResponseDto dto =
                ClienteMapper.toDetalleResponseDto(socio, pago);

        assertNotNull(dto.getUltimaCuotaPaga());
        assertEquals(FormaPago.TRANSFERENCIA,
                dto.getUltimaCuotaPaga().formaPago());
        assertEquals(
                Instant.parse("2026-06-10T10:00:00Z"),
                dto.getUltimaCuotaPaga().fechaPago()
        );
        assertNotNull(dto.getUltimaCuotaPaga().mesCorrespondiente());
    }


}

package com.cipolflo.server.clientes.mapper;

import com.cipolflo.server.clientes.domain.Empresa;
import com.cipolflo.server.clientes.domain.Particular;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.clientes.dto.*;
import com.cipolflo.server.reservas.dto.ClienteDetalleReservaDto;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.cipolflo.server.clientes.dto.UltimaCuotaDto;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    private Empresa crearEmpresa() {
        return Empresa.registrar(
                "210001230018",
                "Cipolatti S.A.",
                "099777777",
                "empresa@mail.com",
                "Uruguay",
                "Montevideo",
                "Montevideo",
                "Av. Libertador 500",
                null
        );
    }

    @Test
    void deberiaMapearTodosLosCamposDeUnSocio() {
        ListadoClientesResponseDto dto = ClienteMapper.toListadoResponseDto(crearSocio(), null);

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
        ListadoClientesResponseDto dto = ClienteMapper.toListadoResponseDto(crearSocio(), null);

        assertEquals(TipoCliente.SOCIO, dto.getTipoCliente());
    }

    @Test
    void deberiaMapearParticularConNumeroSocioYEstadoNulos() {
        ListadoClientesResponseDto dto = ClienteMapper.toListadoResponseDto(crearParticular(), null);

        assertNull(dto.getNumeroSocio());
        assertNull(dto.getEstado());
    }

    @Test
    void deberiaMapearTipoComoParticular() {
        ListadoClientesResponseDto dto = ClienteMapper.toListadoResponseDto(crearParticular(), null);

        assertEquals(TipoCliente.PARTICULAR, dto.getTipoCliente());
    }

    @Test
    void deberiaMapearEmailNuloCuandoClienteNoTieneMail() {
        Socio socio = crearSocio();
        socio.setMail(null);

        ListadoClientesResponseDto dto = ClienteMapper.toListadoResponseDto(socio, null);

        assertNull(dto.getEmail());
    }

    @Test
    void deberiaMapearSocioConNumeroSocioNulo() {
        Socio socio = crearSocio();
        socio.setNumeroSocio(null);

        ListadoClientesResponseDto dto = ClienteMapper.toListadoResponseDto(socio, null);

        assertEquals(TipoCliente.SOCIO, dto.getTipoCliente());
        assertNull(dto.getNumeroSocio());
        assertEquals(EstadoSocio.ACTIVO, dto.getEstado());
    }

    @Test
    void deberiaMapearCamposBaseDeParticular() {
        ListadoClientesResponseDto dto = ClienteMapper.toListadoResponseDto(crearParticular(),null);

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
    void deberiaMapearEstadoSocioResponseDto() {
        EstadoSocioResponseDto dto = ClienteMapper.toEstadoSocioResponseDto(crearSocio());

        assertEquals(1L, dto.getId());
        assertEquals(EstadoSocio.ACTIVO, dto.getEstado());
        assertEquals(5, dto.getNumeroSocio());
    }

    @Test
    void deberiaMapearUltimaCuotaPagaEnListadoCuandoEsSocio() {
        UltimaCuotaDto ultimaCuota = new UltimaCuotaDto(
                2026,
                6,
                "junio",
                "Junio 2026"
        );

        ListadoClientesResponseDto dto =
                ClienteMapper.toListadoResponseDto(crearSocio(), ultimaCuota);

        assertNotNull(dto.getUltimaCuotaDto());
        assertEquals(2026, dto.getUltimaCuotaDto().anio());
        assertEquals(6, dto.getUltimaCuotaDto().mes());
        assertEquals("junio", dto.getUltimaCuotaDto().nombreMes());
        assertEquals("Junio 2026", dto.getUltimaCuotaDto().descripcion());
    }

    @Test
    void deberiaMapearUltimaCuotaPagaNulaEnListadoCuandoEsParticular() {
        UltimaCuotaDto ultimaCuota = new UltimaCuotaDto(
                2026,
                6,
                "junio",
                "Junio 2026"
        );

        ListadoClientesResponseDto dto =
                ClienteMapper.toListadoResponseDto(crearParticular(), ultimaCuota);

        assertNull(dto.getUltimaCuotaDto());
    }

    // ── toBusquedaCedulaResponseDto ─────────────────────────────────────────────

    @Test
    void deberiaMapearTodosLosCamposEnBusquedaCedulaParaSocio() {
        Socio socio = crearSocio();
        socio.setNotas("Observación de prueba");

        BusquedaCedulaResponseDto dto =
                ClienteMapper.toBusquedaCedulaResponseDto(socio);

        assertEquals(1L, dto.getId());
        assertEquals("Juan Pérez", dto.getNombre());
        assertEquals("12345678", dto.getCedula());
        assertEquals("099111111", dto.getTelefono());
        assertEquals("juan@mail.com", dto.getMail());
        assertEquals("Observación de prueba", dto.getObservaciones());
        assertEquals(TipoCliente.SOCIO, dto.getTipoCliente());
    }

    @Test
    void deberiaMapearTipoParticularEnBusquedaCedula() {
        BusquedaCedulaResponseDto dto =
                ClienteMapper.toBusquedaCedulaResponseDto(crearParticular());

        assertEquals(TipoCliente.PARTICULAR, dto.getTipoCliente());
        assertEquals(2L, dto.getId());
        assertEquals("Laura Fernández", dto.getNombre());
        assertEquals("67890123", dto.getCedula());
    }

    @Test
    void deberiaMapearObservacionesNulaEnBusquedaCedulaCuandoClienteNoTieneNotas() {
        BusquedaCedulaResponseDto dto =
                ClienteMapper.toBusquedaCedulaResponseDto(crearParticular());

        assertNull(dto.getObservaciones());
    }

    // ── toClienteDetalleReservaDto ──────────────────────────────────────────────

    @Test
    void deberiaMapearTodosLosCamposDeClienteDetalleReservaDtoParaSocio() {
        Socio socio = crearSocio();

        ClienteDetalleReservaDto dto =
                ClienteMapper.toClienteDetalleReservaDto(socio);

        assertEquals(1L, dto.id());
        assertEquals("Juan Pérez", dto.nombre());
        assertEquals("12345678", dto.cedula());
        assertEquals("099111111", dto.telefono());
        assertEquals("juan@mail.com", dto.email());
        assertEquals(TipoCliente.SOCIO, dto.tipoCliente());
    }

    // ── toExportFila ──────────────────────────────────────────────────────────

    @Test
    void toExportFila_socioCompleto_devuelveFilaConTodosLosCampos() {
        Socio socio = crearSocio();
        socio.setFechaUltimoPago(LocalDate.of(2026, 5, 10));

        List<String> fila = ClienteMapper.toExportFila(socio);

        assertEquals(14, fila.size());
        assertEquals("Juan Pérez",           fila.get(0));
        assertEquals("5",                    fila.get(1));
        assertEquals("12345678",             fila.get(2));
        assertEquals("",                     fila.get(3));   // rut null (socio) → ""
        assertEquals("juan@mail.com",        fila.get(4));
        assertEquals("Activo",               fila.get(5));
        assertEquals("099111111",            fila.get(6));
        assertEquals("",                     fila.get(7));   // notas null → ""
        assertEquals("Efectivo",             fila.get(8));
        assertEquals("Uruguay",              fila.get(9));
        assertEquals("Montevideo",           fila.get(10));
        assertEquals("Av. 18 de Julio 100", fila.get(11));
        assertEquals("2022-01-01",           fila.get(12));
        assertEquals("2026-05-10",           fila.get(13));
    }

    @Test
    void toExportFila_estadoDeBaja_devuelveLabelEnLugarDeNombreEnum() {
        Socio socio = crearSocio();
        socio.setEstado(EstadoSocio.DE_BAJA);

        List<String> fila = ClienteMapper.toExportFila(socio);

        assertEquals("De baja", fila.get(5));
    }

    @Test
    void toExportFila_estadoInactivo_devuelveLabelEnLugarDeNombreEnum() {
        Socio socio = crearSocio();
        socio.setEstado(EstadoSocio.INACTIVO);

        List<String> fila = ClienteMapper.toExportFila(socio);

        assertEquals("Inactivo", fila.get(5));
    }

    @Test
    void toExportFila_socioConCamposNulos_devuelveVaciosEnLugarDeNull() {
        Socio socio = crearSocio();
        socio.setMail(null);
        socio.setPais(null);
        socio.setDepartamento(null);
        socio.setDireccion(null);

        List<String> fila = ClienteMapper.toExportFila(socio);

        assertEquals("", fila.get(4));   // mail
        assertEquals("", fila.get(9));   // pais
        assertEquals("", fila.get(10));  // departamento
        assertEquals("", fila.get(11));  // direccion
    }

    @Test
    void toExportFila_socioConFechaUltimoPagoNula_devuelveVacio() {
        List<String> fila = ClienteMapper.toExportFila(crearSocio());

        assertEquals("", fila.get(13));
    }

    @Test
    void toExportFila_socioConNumeroSocioNulo_devuelveNA() {
        Socio socio = crearSocio();
        socio.setNumeroSocio(null);

        List<String> fila = ClienteMapper.toExportFila(socio);

        assertEquals("N/A", fila.get(1));
    }

    @Test
    void toExportFila_particular_devuelveNAParaCamposExclusivoDeSocio() {
        List<String> fila = ClienteMapper.toExportFila(crearParticular());

        assertEquals("Laura Fernández", fila.get(0));
        assertEquals("N/A", fila.get(1));    // numeroSocio
        assertEquals("67890123",  fila.get(2));
        assertEquals("", fila.get(3));       // rut (particular) → ""
        assertEquals("N/A", fila.get(5));    // estado
        assertEquals("N/A", fila.get(8));    // metodoCobro
        assertEquals("", fila.get(9));       // pais
        assertEquals("", fila.get(10));      // departamento
        assertEquals("", fila.get(11));      // direccion
        assertEquals("", fila.get(12));      // fechaIngreso
        assertEquals("", fila.get(13));      // fechaUltimoPago
    }

    @Test
    void deberiaMapearTipoSocioEnClienteDetalleReservaDto() {
        ClienteDetalleReservaDto dto =
                ClienteMapper.toClienteDetalleReservaDto(crearSocio());

        assertEquals(TipoCliente.SOCIO, dto.tipoCliente());
    }

    @Test
    void deberiaMapearTipoParticularEnClienteDetalleReservaDto() {
        ClienteDetalleReservaDto dto =
                ClienteMapper.toClienteDetalleReservaDto(crearParticular());

        assertEquals(TipoCliente.PARTICULAR, dto.tipoCliente());
        assertEquals(2L, dto.id());
        assertEquals("Laura Fernández", dto.nombre());
        assertEquals("67890123", dto.cedula());
        assertEquals("099666666", dto.telefono());
        assertEquals("laura@mail.com", dto.email());
    }

    // ── Empresa ─────────────────────────────────────────────────────────────────

    @Test
    void deberiaMapearRutNuloParaSocioYParticular() {
        assertNull(ClienteMapper.toDetalleResponseDto(crearSocio(), null).getRut());
        assertNull(ClienteMapper.toListadoResponseDto(crearSocio(), null).getRut());
        assertNull(ClienteMapper.toDetalleResponseDto(crearParticular(), null).getRut());
        assertNull(ClienteMapper.toListadoResponseDto(crearParticular(), null).getRut());
    }

    @Test
    void deberiaMapearEmpresaEnDetalleConTipoYRut() {
        ClienteResponseDto dto = ClienteMapper.toDetalleResponseDto(crearEmpresa(), null);

        assertEquals("Cipolatti S.A.", dto.getNombre());
        assertEquals(TipoCliente.EMPRESA, dto.getTipoCliente());
        assertEquals("210001230018", dto.getRut());
        assertNull(dto.getCedula());
        assertNull(dto.getNumeroSocio());
        assertNull(dto.getEstado());
    }

    @Test
    void deberiaMapearEmpresaEnListadoConTipoYRut() {
        ListadoClientesResponseDto dto = ClienteMapper.toListadoResponseDto(crearEmpresa(), null);

        assertEquals("Cipolatti S.A.", dto.getNombreCompleto());
        assertEquals(TipoCliente.EMPRESA, dto.getTipoCliente());
        assertEquals("210001230018", dto.getRut());
        assertNull(dto.getCedula());
        assertNull(dto.getNumeroSocio());
        assertNull(dto.getEstado());
    }

    @Test
    void deberiaMapearTipoEmpresaEnBusquedaCedula() {
        BusquedaCedulaResponseDto dto = ClienteMapper.toBusquedaCedulaResponseDto(crearEmpresa());

        assertEquals(TipoCliente.EMPRESA, dto.getTipoCliente());
    }

    @Test
    void deberiaMapearTipoEmpresaEnClienteDetalleReservaDto() {
        ClienteDetalleReservaDto dto = ClienteMapper.toClienteDetalleReservaDto(crearEmpresa());

        assertEquals(TipoCliente.EMPRESA, dto.tipoCliente());
        assertEquals("Cipolatti S.A.", dto.nombre());
    }

    @Test
    void toExportFila_empresa_incluyeRutYNAParaCamposDeSocio() {
        List<String> fila = ClienteMapper.toExportFila(crearEmpresa());

        assertEquals(14, fila.size());
        assertEquals("Cipolatti S.A.", fila.get(0));
        assertEquals("N/A", fila.get(1));         // numeroSocio
        assertEquals("", fila.get(2));            // cedula (empresa) → ""
        assertEquals("210001230018", fila.get(3)); // rut
        assertEquals("N/A", fila.get(5));         // estado
        assertEquals("N/A", fila.get(8));         // metodoCobro
    }
}

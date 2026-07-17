package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.clientes.domain.Empresa;
import com.cipolflo.server.clientes.domain.Particular;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.CategoriaSocio;
import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.clientes.dto.*;
import com.cipolflo.server.clientes.exception.ClienteCodigoError;
import com.cipolflo.server.clientes.exception.ClienteNotFoundException;
import com.cipolflo.server.clientes.exception.SocioNotFoundException;
import com.cipolflo.server.clientes.repository.ClienteRepository;
import com.cipolflo.server.clientes.validator.*;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import com.cipolflo.server.reservas.service.IReservaService;
import com.cipolflo.server.shared.export.ArchivoExportado;
import com.cipolflo.server.shared.export.ExportacionException;
import com.cipolflo.server.shared.export.ExportProperties;
import com.cipolflo.server.shared.export.IExportService;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.dao.DataIntegrityViolationException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import com.cipolflo.server.clientes.validator.RutFormatoValidator;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;
import java.time.LocalDate;
import java.time.Month;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
private ClienteRepository clienteRepository;

@Mock
private IReservaService reservaService;

@Mock
private ModificacionParticularValidator modificacionParticularValidator;

@Mock
private ModificacionSocioValidator modificacionSocioValidator;

@Mock
private RegistroSocioValidator registroSocioValidator;

@Mock
private CedulaUnicaValidator cedulaUnicaValidator;

@Mock
private CedulaFormatoValidator cedulaFormatoValidator;

@Mock
private RegistroParticularValidator registroParticularValidator;

@Mock
private RegistroEmpresaValidator registroEmpresaValidator;

@Mock
private IPagoCuotaService pagoCuotaService;

@Mock
private ExportProperties exportProperties;

@Mock
private IExportService exportService;

@Mock
private RutFormatoValidator rutFormatoValidator;
    @InjectMocks
    private ClienteService clienteService;

    private PageRequestDto pageRequest() {
        return new PageRequestDto(0, 10, null, null);
    }

    private ListadoClientesRequestDto sinFiltros() {
        return new ListadoClientesRequestDto(null, null, null, null);
    }

    private Socio crearSocio(Long id, String nombre, String cedula, Integer nroSocio, EstadoSocio estado) {
        Socio socio = new Socio();
        socio.setId(id);
        socio.setNombreCompleto(nombre);
        socio.setCedula(cedula);
        socio.setTelefono("099000000");
        socio.setMail("socio@mail.com");
        socio.setNumeroSocio(nroSocio);
        socio.setEstado(estado);
        socio.setFechaNacimiento(LocalDate.of(1990, Month.JANUARY, 1));
        socio.setPais("Uruguay");
        socio.setDepartamento("Montevideo");
        socio.setCiudad("Montevideo");
        socio.setDireccion("Calle 1");
        socio.setFechaIngreso(LocalDate.of(2022, Month.JANUARY, 1));
        socio.setMetodoCobro(MetodoCobro.EFECTIVO);
        return socio;
    }

    private Particular crearParticular(Long id, String nombre, String cedula) {
        Particular particular = new Particular();
        particular.setId(id);
        particular.setNombreCompleto(nombre);
        particular.setCedula(cedula);
        particular.setTelefono("099000000");
        return particular;
    }

    private Empresa crearEmpresa(Long id, String razonSocial, String rut) {
        Empresa empresa = Empresa.registrar(
                rut, razonSocial, "099000000", "empresa@mail.com",
                "Uruguay", "Montevideo", "Montevideo", "Av. Libertador 500", null);
        empresa.setId(id);
        return empresa;
    }

    private ModificacionParticularRequestDto dtoParticular(String nombre, String telefono) {
        ModificacionParticularRequestDto dto = new ModificacionParticularRequestDto();
        dto.setCedula("12345672");
        dto.setNombreCompleto(nombre);
        dto.setTelefono(telefono);
        return dto;
    }

    private ModificacionSocioRequestDto dtoSocio(String nombre, String telefono) {
        ModificacionSocioRequestDto dto = new ModificacionSocioRequestDto();
        dto.setCedula("12345672");
        dto.setNombreCompleto(nombre);
        dto.setTelefono(telefono);
        dto.setFechaNacimiento(LocalDate.of(1990, Month.JANUARY, 1));
        dto.setPais("Uruguay");
        dto.setDepartamento("Montevideo");
        dto.setCiudad("Montevideo");
        dto.setDireccion("Calle 1");
        dto.setMetodoCobro(MetodoCobro.TRANSFERENCIA);
        dto.setCategoriaSocio(CategoriaSocio.SOCIO_COMUN);
        dto.setFechaIngreso(LocalDate.of(2020, Month.JANUARY, 1));
        return dto;
    }

    private RegistroSocioRequestDto crearRegistroSocioRequest() {
        RegistroSocioRequestDto dto = new RegistroSocioRequestDto();
        dto.setCedula("1.234.567-8");
        dto.setNombreCompleto("Juan Pérez");
        dto.setFechaNacimiento(LocalDate.of(1990, Month.MAY, 10));
        dto.setTelefono("099123456");
        dto.setEmail("juan@mail.com");
        dto.setMetodoCobro(MetodoCobro.EFECTIVO);
        dto.setPais("Uruguay");
        dto.setDepartamento("Montevideo");
        dto.setCiudad("Montevideo");
        dto.setDireccion("Av. Italia 1234");
        dto.setObservaciones("Sin observaciones");
        dto.setCategoriaSocio(CategoriaSocio.SOCIO_COMUN);
        dto.setFechaIngreso(LocalDate.of(2020, Month.JANUARY, 1));
        return dto;
    }

    private RegistroEmpresaRequestDto crearRegistroEmpresaRequest() {
        RegistroEmpresaRequestDto dto = new RegistroEmpresaRequestDto();
        dto.setRazonSocial("Antel S.A.");
        dto.setRut("21.100342.001-7");
        dto.setPais("Uruguay");
        dto.setDepartamento("Montevideo");
        dto.setCiudad("Montevideo");
        dto.setDireccion("Guatemala 1075");
        dto.setTelefono("099123456");
        dto.setMail("empresa@mail.com");
        dto.setObservaciones("Sin observaciones");
        return dto;
    }

    @Test
    void deberiaRetornarTodosLosClientesSinFiltros() {
        List<Cliente> clientes = List.of(
                crearSocio(1L, "Juan Pérez", "12345678", 1, EstadoSocio.ACTIVO),
                crearSocio(2L, "María García", "23456789", 2, EstadoSocio.INACTIVO)
        );
        Page<Cliente> page = new PageImpl<>(clientes, pageRequest().toPageable(), clientes.size());
        when(clienteRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<ListadoClientesResponseDto> resultado = clienteService.getListadoClientes(sinFiltros(), pageRequest());

        assertNotNull(resultado);
        assertEquals(2, resultado.totalElements());
        assertEquals(2, resultado.content().size());
        verify(clienteRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void deberiaRetornarPaginaVaciaCuandoNoHayCoincidencias() {
        Page<Cliente> page = Page.empty(pageRequest().toPageable());
        when(clienteRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ListadoClientesRequestDto filtros = new ListadoClientesRequestDto(null, "nombreQueNoExiste", null, null);
        PageResponse<ListadoClientesResponseDto> resultado = clienteService.getListadoClientes(filtros, pageRequest());

        assertNotNull(resultado);
        assertEquals(0, resultado.totalElements());
        assertTrue(resultado.content().isEmpty());
    }

    @Test
    void deberiaMapearSocioCorrectamente() {
        Socio socio = crearSocio(1L, "Juan Pérez", "12345678", 3, EstadoSocio.ACTIVO);
        Page<Cliente> page = new PageImpl<>(List.of(socio));
        when(clienteRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<ListadoClientesResponseDto> resultado = clienteService.getListadoClientes(sinFiltros(), pageRequest());

        ListadoClientesResponseDto dto = resultado.content().get(0);
        assertEquals(1L, dto.getId());
        assertEquals("Juan Pérez", dto.getNombreCompleto());
        assertEquals("12345678", dto.getCedula());
        assertEquals("socio@mail.com", dto.getEmail());
        assertEquals(TipoCliente.SOCIO, dto.getTipoCliente());
        assertEquals(3, dto.getNumeroSocio());
        assertEquals(EstadoSocio.ACTIVO, dto.getEstado());
    }

    @Test
    void deberiaMapearParticularConCamposNulos() {
        Particular particular = crearParticular(2L, "Laura Fernández", "67890123");
        Page<Cliente> page = new PageImpl<>(List.of(particular));
        when(clienteRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<ListadoClientesResponseDto> resultado = clienteService.getListadoClientes(sinFiltros(), pageRequest());

        ListadoClientesResponseDto dto = resultado.content().get(0);
        assertEquals(TipoCliente.PARTICULAR, dto.getTipoCliente());
        assertNull(dto.getNumeroSocio());
        assertNull(dto.getEstado());
    }

    @Test
    void deberiaRetornarMapaNombresParaIdsExistentes() {
        Socio socio1 = crearSocio(1L, "Juan Pérez", "12345678", 1, EstadoSocio.ACTIVO);
        Socio socio2 = crearSocio(2L, "María García", "23456789", 2, EstadoSocio.ACTIVO);

        when(clienteRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(socio1, socio2));

        Map<Long, String> resultado = clienteService.getNombresByIds(List.of(1L, 2L));

        assertEquals(2, resultado.size());
        assertEquals("Juan Pérez", resultado.get(1L));
        assertEquals("María García", resultado.get(2L));
    }

    @Test
    void deberiaRetornarMapaVacioCuandoColeccionEsVacia() {
        when(clienteRepository.findAllById(List.of()))
                .thenReturn(List.of());

        Map<Long, String> resultado = clienteService.getNombresByIds(List.of());

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void deberiaRetornarSoloClientesEncontradosCuandoAlgunosIdsNoExisten() {
        Socio socio = crearSocio(1L, "Juan Pérez", "12345678", 1, EstadoSocio.ACTIVO);

        when(clienteRepository.findAllById(List.of(1L, 99L)))
                .thenReturn(List.of(socio));

        Map<Long, String> resultado = clienteService.getNombresByIds(List.of(1L, 99L));

        assertEquals(1, resultado.size());
        assertEquals("Juan Pérez", resultado.get(1L));
        assertFalse(resultado.containsKey(99L));
    }

    @Test
    void deberiaRetornarDetalleDeUnSocio() {
        Socio socio = crearSocio(1L, "Juan Pérez", "12345678", 3, EstadoSocio.ACTIVO);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));

        ClienteResponseDto dto = clienteService.getDetalleCliente(1L);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Juan Pérez", dto.getNombre());
        assertEquals("12345678", dto.getCedula());
        assertEquals(TipoCliente.SOCIO, dto.getTipoCliente());
        assertEquals(3, dto.getNumeroSocio());
        assertEquals(EstadoSocio.ACTIVO, dto.getEstado());
        assertEquals("Uruguay", dto.getPais());
        verify(clienteRepository).findById(1L);
    }

    @Test
    void deberiaRetornarDetalleDeUnaEmpresaConTipoYRut() {
        Empresa empresa = crearEmpresa(3L, "Cipolatti S.A.", "210001230018");
        when(clienteRepository.findById(3L)).thenReturn(Optional.of(empresa));

        ClienteResponseDto dto = clienteService.getDetalleCliente(3L);

        assertEquals(3L, dto.getId());
        assertEquals("Cipolatti S.A.", dto.getNombre());
        assertEquals(TipoCliente.EMPRESA, dto.getTipoCliente());
        assertEquals("210001230018", dto.getRut());
        assertNull(dto.getCedula());
        verify(pagoCuotaService, never()).calcularUltimaCuotaPaga(anyLong());
    }

    @Test
    void deberiaMapearEmpresaEnListadoConTipoYRut() {
        Empresa empresa = crearEmpresa(3L, "Cipolatti S.A.", "210001230018");
        Page<Cliente> page = new PageImpl<>(List.of(empresa));
        when(clienteRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResponse<ListadoClientesResponseDto> resultado = clienteService.getListadoClientes(sinFiltros(), pageRequest());

        ListadoClientesResponseDto dto = resultado.content().get(0);
        assertEquals(TipoCliente.EMPRESA, dto.getTipoCliente());
        assertEquals("210001230018", dto.getRut());
        assertNull(dto.getCedula());
        assertNull(dto.getUltimaCuotaDto());
        verify(pagoCuotaService, never()).calcularUltimaCuotaPaga(anyLong());
    }

    @Test
    void deberiaLanzarClienteNotFoundExceptionCuandoClienteNoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class, () -> clienteService.getDetalleCliente(99L));
        verify(clienteRepository).findById(99L);
    }

    @Test
    void deberiaLanzarErrorCuandoSocioNoExiste() {
        Long socioId = 99L;

        when(clienteRepository.findById(socioId)).thenReturn(Optional.empty());

        assertThrows(SocioNotFoundException.class, () -> clienteService.darDeBajaSocio(socioId));

        verify(clienteRepository).findById(socioId);
        verify(reservaService, never()).cancelarReservasFuturasPorCliente(anyLong());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deberiaDarDeBajaSocioSinReservas() {
        Long socioId = 1L;

        Socio socio = new Socio();
        socio.setId(socioId);
        socio.setEstado(EstadoSocio.ACTIVO);

        when(clienteRepository.findById(socioId)).thenReturn(Optional.of(socio));

        clienteService.darDeBajaSocio(socioId);

        assertEquals(EstadoSocio.DE_BAJA, socio.getEstado());
        verify(reservaService).cancelarReservasFuturasPorCliente(socioId);
        verify(clienteRepository).save(socio);
    }

    @Test
    void deberiaDarDeBajaSocioConReservasFuturas() {
        Long socioId = 1L;

        Socio socio = new Socio();
        socio.setId(socioId);
        socio.setEstado(EstadoSocio.ACTIVO);

        when(clienteRepository.findById(socioId)).thenReturn(Optional.of(socio));

        clienteService.darDeBajaSocio(socioId);

        assertEquals(EstadoSocio.DE_BAJA, socio.getEstado());
        verify(reservaService).cancelarReservasFuturasPorCliente(socioId);
        verify(clienteRepository).save(socio);
    }

    @Test
    void deberiaDarDeBajaSocioConReservasPasadasSinTocarlas() {
        Long socioId = 1L;

        Socio socio = new Socio();
        socio.setId(socioId);
        socio.setEstado(EstadoSocio.ACTIVO);

        when(clienteRepository.findById(socioId)).thenReturn(Optional.of(socio));

        clienteService.darDeBajaSocio(socioId);

        assertEquals(EstadoSocio.DE_BAJA, socio.getEstado());
        verify(reservaService).cancelarReservasFuturasPorCliente(socioId);
        verify(clienteRepository).save(socio);
    }

    // --- modificarParticular ---

    @Test
    void deberiaLanzarExceptionCuandoValidadorParticularFalla() {
        Particular particular = crearParticular(1L, "Juan Pérez", "12345678");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(particular));

        ModificacionParticularRequestDto dto = dtoParticular("Juan", "099000000");
        org.mockito.Mockito.doThrow(new ClienteValidacionException("EMAIL_DUPLICADO", "El email ingresado ya está en uso"))
                .when(modificacionParticularValidator).validar(anyLong(), any(ModificacionParticularRequestDto.class), anyString(), any());

        assertThrows(ClienteValidacionException.class, () -> clienteService.modificarParticular(1L, dto));
    }

    @Test
    void deberiaLanzarExceptionAlModificarParticularConIdInexistente() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class,
                () -> clienteService.modificarParticular(99L, dtoParticular("Juan", "099000000")));
        verify(clienteRepository).findById(99L);
    }

    @Test
    void deberiaLanzarExceptionAlModificarParticularConIdDeSocio() {
        Socio socio = crearSocio(1L, "Juan Pérez", "12345678", 1, EstadoSocio.ACTIVO);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));

        assertThrows(ClienteNotFoundException.class,
                () -> clienteService.modificarParticular(1L, dtoParticular("Juan", "099000000")));
    }

    @Test
    void deberiaModificarParticularCorrectamente() {
        Particular particular = crearParticular(1L, "Juan Pérez", "12345678");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(particular));
        when(clienteRepository.saveAndFlush(particular)).thenReturn(particular);

        ModificacionParticularRequestDto dto = dtoParticular("Juan Modificado", "099999999");
        dto.setCedula("1.234.567-2");
        dto.setMail("nuevo@mail.com");

        ClienteResponseDto resultado = clienteService.modificarParticular(1L, dto);

        assertNotNull(resultado);
        assertEquals("12345672", particular.getCedula());
        assertEquals("Juan Modificado", particular.getNombreCompleto());
        assertEquals("099999999", particular.getTelefono());
        assertEquals("nuevo@mail.com", particular.getMail());
        verify(clienteRepository).saveAndFlush(particular);
        verify(modificacionParticularValidator).validar(anyLong(), any(ModificacionParticularRequestDto.class), anyString(), any());
    }

    @Test
    void deberiaNormalizarMailConEspaciosAlModificarParticular() {
        Particular particular = crearParticular(1L, "Juan Pérez", "12345678");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(particular));
        when(clienteRepository.saveAndFlush(particular)).thenReturn(particular);

        ModificacionParticularRequestDto dto = dtoParticular("Juan Pérez", "099000000");
        dto.setMail("  espacios@mail.com  ");

        clienteService.modificarParticular(1L, dto);

        assertEquals("espacios@mail.com", particular.getMail());
    }

    // --- modificarSocio ---

    @Test
    void deberiaLanzarExceptionCuandoValidadorSocioFalla() {
        Socio socio = crearSocio(1L, "Juan Pérez", "12345672", 1, EstadoSocio.ACTIVO);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));

        ModificacionSocioRequestDto dto = dtoSocio("Juan", "099000000");
        org.mockito.Mockito.doThrow(new ClienteValidacionException("CEDULA_DUPLICADA", "Ya existe un cliente con esa cédula"))
                .when(modificacionSocioValidator).validar(anyLong(), any(ModificacionSocioRequestDto.class), anyString(), any());

        assertThrows(ClienteValidacionException.class, () -> clienteService.modificarSocio(1L, dto));
    }

    @Test
    void deberiaLanzarExceptionAlModificarSocioConIdInexistente() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class,
                () -> clienteService.modificarSocio(99L, dtoSocio("Juan", "099000000")));
        verify(clienteRepository).findById(99L);
    }

    @Test
    void deberiaLanzarExceptionAlModificarSocioConIdDeParticular() {
        Particular particular = crearParticular(1L, "Juan Pérez", "12345678");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(particular));

        assertThrows(ClienteNotFoundException.class,
                () -> clienteService.modificarSocio(1L, dtoSocio("Juan", "099000000")));
    }

    @Test
    void deberiaModificarSocioCorrectamente() {
        Socio socio = crearSocio(1L, "Juan Pérez", "12345678", 1, EstadoSocio.ACTIVO);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(clienteRepository.saveAndFlush(socio)).thenReturn(socio);

        ModificacionSocioRequestDto dto = dtoSocio("Juan Modificado", "099999999");
        dto.setCedula("9.999.999-9");
        dto.setMail("nuevo@mail.com");
        dto.setPais("Argentina");
        dto.setCiudad("Buenos Aires");
        dto.setCategoriaSocio(CategoriaSocio.POLICIA_ACTIVO);
        dto.setFechaIngreso(LocalDate.of(2019, Month.MARCH, 15));

        ClienteResponseDto resultado = clienteService.modificarSocio(1L, dto);

        assertNotNull(resultado);
        assertEquals("99999999", socio.getCedula());
        assertEquals("Juan Modificado", socio.getNombreCompleto());
        assertEquals("099999999", socio.getTelefono());
        assertEquals("nuevo@mail.com", socio.getMail());
        assertEquals("Argentina", socio.getPais());
        assertEquals("Buenos Aires", socio.getCiudad());
        assertEquals(MetodoCobro.TRANSFERENCIA, socio.getMetodoCobro());
        assertEquals(CategoriaSocio.POLICIA_ACTIVO, socio.getCategoriaSocio());
        assertEquals(LocalDate.of(2019, Month.MARCH, 15), socio.getFechaIngreso());
        verify(clienteRepository).saveAndFlush(socio);
        verify(modificacionSocioValidator).validar(anyLong(), any(ModificacionSocioRequestDto.class), anyString(), any());
    }

    @Test
    void deberiaLanzarFechaIngresoInvalidaAlModificarSocio() {
        Socio socio = crearSocio(1L, "Juan Pérez", "12345678", 1, EstadoSocio.ACTIVO);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));

        ModificacionSocioRequestDto dto = dtoSocio("Juan Pérez", "099000000");
        dto.setFechaIngreso(LocalDate.now().plusDays(1));

        doThrow(new ClienteValidacionException(
                ClienteCodigoError.FECHA_INGRESO_INVALIDA.name(),
                "La fecha de ingreso no puede ser posterior a la fecha actual"
        )).when(modificacionSocioValidator).validar(anyLong(), any(ModificacionSocioRequestDto.class), anyString(), any());

        ClienteValidacionException exception = assertThrows(ClienteValidacionException.class,
                () -> clienteService.modificarSocio(1L, dto));

        assertEquals(ClienteCodigoError.FECHA_INGRESO_INVALIDA.name(), exception.getCodigo());
        verify(clienteRepository, never()).saveAndFlush(any());
    }

    @Test
    void deberiaNormalizarMailConEspaciosAlModificarSocio() {
        Socio socio = crearSocio(1L, "Juan Pérez", "12345678", 1, EstadoSocio.ACTIVO);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(clienteRepository.saveAndFlush(socio)).thenReturn(socio);

        ModificacionSocioRequestDto dto = dtoSocio("Juan Pérez", "099000000");
        dto.setMail("  espacios@mail.com  ");

        clienteService.modificarSocio(1L, dto);

        assertEquals("espacios@mail.com", socio.getMail());
    }

    @Test
    void deberiaLanzarErrorCuandoCedulaEstaDuplicadaAlRegistrarSocio() {
        RegistroSocioRequestDto dto = crearRegistroSocioRequest();

        doThrow(new ClienteValidacionException(
                ClienteCodigoError.CEDULA_DUPLICADA.name(),
                "Ya existe un cliente con esa cédula"
        )).when(registroSocioValidator).validar(any(RegistroSocioRequestDto.class), anyString(), any());

        assertThrows(ClienteValidacionException.class, () -> clienteService.registrarSocio(dto));

        verify(registroSocioValidator).validar(any(RegistroSocioRequestDto.class), anyString(), any());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deberiaAsignarNumeroSocioSiguiente() {
        RegistroSocioRequestDto dto = crearRegistroSocioRequest();

        when(clienteRepository.findMaxNumeroSocio()).thenReturn(Optional.of(10));

        when(clienteRepository.saveAndFlush(any(Socio.class))).thenAnswer(invocation -> {
            Socio socio = invocation.getArgument(0);
            socio.setId(1L);
            return socio;
        });

        ClienteResponseDto response = clienteService.registrarSocio(dto);

        assertNotNull(response);
        assertEquals(11, response.getNumeroSocio());

        verify(registroSocioValidator).validar(any(RegistroSocioRequestDto.class), anyString(), any());
        verify(clienteRepository).findMaxNumeroSocio();
        verify(clienteRepository).saveAndFlush(any(Socio.class));
    }

    @Test
    void deberiaRegistrarSocioCorrectamente() {
        RegistroSocioRequestDto dto = crearRegistroSocioRequest();

        when(clienteRepository.findMaxNumeroSocio()).thenReturn(Optional.empty());

        when(clienteRepository.saveAndFlush(any(Socio.class))).thenAnswer(invocation -> {
            Socio socio = invocation.getArgument(0);
            socio.setId(1L);
            return socio;
        });

        ClienteResponseDto response = clienteService.registrarSocio(dto);

        assertNotNull(response);
        assertEquals("12345678", response.getCedula());
        assertEquals(1, response.getNumeroSocio());
        assertEquals(TipoCliente.SOCIO, response.getTipoCliente());
        assertEquals(EstadoSocio.ACTIVO, response.getEstado());
        assertEquals(CategoriaSocio.SOCIO_COMUN, response.getCategoriaSocio());
        assertEquals(LocalDate.of(2020, Month.JANUARY, 1), response.getFechaIngreso());

        verify(clienteRepository).findMaxNumeroSocio();
        verify(registroSocioValidator).validar(any(RegistroSocioRequestDto.class), anyString(), any());
        verify(clienteRepository).saveAndFlush(any(Socio.class));
    }

    @Test
    void deberiaLanzarFechaIngresoInvalidaAlRegistrarSocio() {
        RegistroSocioRequestDto dto = crearRegistroSocioRequest();
        dto.setFechaIngreso(LocalDate.now().plusDays(1));

        doThrow(new ClienteValidacionException(
                ClienteCodigoError.FECHA_INGRESO_INVALIDA.name(),
                "La fecha de ingreso no puede ser posterior a la fecha actual"
        )).when(registroSocioValidator).validar(any(RegistroSocioRequestDto.class), anyString(), any());

        ClienteValidacionException exception = assertThrows(ClienteValidacionException.class,
                () -> clienteService.registrarSocio(dto));

        assertEquals(ClienteCodigoError.FECHA_INGRESO_INVALIDA.name(), exception.getCodigo());
        verify(clienteRepository, never()).saveAndFlush(any());
    }

    @Test
    void deberiaRegistrarSocioSinEmail() {
        RegistroSocioRequestDto dto = crearRegistroSocioRequest();
        dto.setEmail(null);

        when(clienteRepository.findMaxNumeroSocio()).thenReturn(Optional.empty());
        when(clienteRepository.saveAndFlush(any(Socio.class))).thenAnswer(invocation -> {
            Socio socio = invocation.getArgument(0);
            socio.setId(1L);
            return socio;
        });

        ClienteResponseDto response = clienteService.registrarSocio(dto);

        assertNotNull(response);
        assertNull(response.getEmail());

        verify(registroSocioValidator).validar(any(RegistroSocioRequestDto.class), anyString(), isNull());
        verify(clienteRepository).saveAndFlush(any(Socio.class));
    }

    // --- registrarEmpresa ---

    @Test
    void deberiaRegistrarEmpresaCorrectamente() {
        RegistroEmpresaRequestDto dto = crearRegistroEmpresaRequest();

        when(clienteRepository.saveAndFlush(any(Empresa.class))).thenAnswer(invocation -> {
            Empresa empresa = invocation.getArgument(0);
            empresa.setId(1L);
            return empresa;
        });

        ClienteResponseDto response = clienteService.registrarEmpresa(dto);

        assertNotNull(response);
        assertEquals("Antel S.A.", response.getNombre());
        assertEquals("211003420017", response.getRut());
        assertEquals(TipoCliente.EMPRESA, response.getTipoCliente());
        assertNull(response.getCedula());

        verify(registroEmpresaValidator).validar(any(RegistroEmpresaRequestDto.class), anyString(), anyString());
        verify(clienteRepository).saveAndFlush(any(Empresa.class));
    }

    @Test
    void deberiaRegistrarEmpresaSinMail() {
        RegistroEmpresaRequestDto dto = crearRegistroEmpresaRequest();
        dto.setMail(null);

        when(clienteRepository.saveAndFlush(any(Empresa.class))).thenAnswer(invocation -> {
            Empresa empresa = invocation.getArgument(0);
            empresa.setId(1L);
            return empresa;
        });

        ClienteResponseDto response = clienteService.registrarEmpresa(dto);

        assertNotNull(response);
        assertNull(response.getEmail());

        verify(registroEmpresaValidator).validar(any(RegistroEmpresaRequestDto.class), anyString(), isNull());
        verify(clienteRepository).saveAndFlush(any(Empresa.class));
    }

    @Test
    void deberiaLanzarErrorCuandoRutEsInvalidoAlRegistrarEmpresa() {
        RegistroEmpresaRequestDto dto = crearRegistroEmpresaRequest();

        doThrow(new ClienteValidacionException(
                ClienteCodigoError.RUT_INVALIDO.name(),
                "El RUT ingresado no es válido"
        )).when(registroEmpresaValidator).validar(any(RegistroEmpresaRequestDto.class), anyString(), any());

        assertThrows(ClienteValidacionException.class, () -> clienteService.registrarEmpresa(dto));

        verify(registroEmpresaValidator).validar(any(RegistroEmpresaRequestDto.class), anyString(), any());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deberiaLanzarErrorCuandoRutEstaDuplicadoAlRegistrarEmpresa() {
        RegistroEmpresaRequestDto dto = crearRegistroEmpresaRequest();

        doThrow(new ClienteValidacionException(
                ClienteCodigoError.RUT_DUPLICADO.name(),
                "Ya existe un cliente con ese RUT"
        )).when(registroEmpresaValidator).validar(any(RegistroEmpresaRequestDto.class), anyString(), any());

        assertThrows(ClienteValidacionException.class, () -> clienteService.registrarEmpresa(dto));

        verify(registroEmpresaValidator).validar(any(RegistroEmpresaRequestDto.class), anyString(), any());
        verify(clienteRepository, never()).save(any());
    }

    // --- buscarPorCedula ---



@Test
void deberiaLanzarClienteNotFoundExceptionCuandoCedulaNoExiste() {
    when(clienteRepository.findByCedula("99999999"))
            .thenReturn(Optional.empty());

    assertThrows(
            ClienteNotFoundException.class,
            () -> clienteService.buscarPorCedula("99999999")
    );

    verify(clienteRepository).findByCedula("99999999");
}

@Test
void deberiaRetornarDtoCuandoCedulaCorrespondeAParticular() {
    Particular particular = crearParticular(1L, "Laura Fernández", "12345678");

    when(clienteRepository.findByCedula("12345678"))
            .thenReturn(Optional.of(particular));

    BusquedaCedulaResponseDto resultado =
            clienteService.buscarPorCedula("12345678");

    assertEquals(TipoCliente.PARTICULAR, resultado.getTipoCliente());
    assertEquals("12345678", resultado.getCedula());

    verify(clienteRepository).findByCedula("12345678");
}

@Test
void deberiaRetornarDtoCuandoCedulaCorrespondeASocio() {
    Socio socio = crearSocio(
            1L,
            "Juan Pérez",
            "12345678",
            1,
            EstadoSocio.ACTIVO
    );

    when(clienteRepository.findByCedula("12345678"))
            .thenReturn(Optional.of(socio));

    BusquedaCedulaResponseDto resultado =
            clienteService.buscarPorCedula("12345678");

    assertEquals(TipoCliente.SOCIO, resultado.getTipoCliente());
    assertEquals("12345678", resultado.getCedula());

    verify(clienteRepository).findByCedula("12345678");
}

    // --- consultarEstadoSocio ---

    @Test
    void deberiaRetornarEstadoDelSocioCuandoExiste() {
        Socio socio = crearSocio(1L, "Juan Pérez", "12345678", 5, EstadoSocio.INACTIVO);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));
        when(pagoCuotaService.calcularMesesAdeudados(socio)).thenReturn(4);

        EstadoSocioResponseDto resultado = clienteService.consultarEstadoSocio(1L);

        assertEquals(1L, resultado.getId());
        assertEquals(EstadoSocio.INACTIVO, resultado.getEstado());
        assertEquals(5, resultado.getNumeroSocio());
        assertEquals(4, resultado.getMesesSinPagar());
        verify(clienteRepository).findById(1L);
    }

    @Test
    void deberiaLanzarSocioNotFoundExceptionCuandoIdNoExisteEnConsultaEstado() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(SocioNotFoundException.class, () -> clienteService.consultarEstadoSocio(99L));
        verify(clienteRepository).findById(99L);
    }

    @Test
    void deberiaLanzarSocioNotFoundExceptionCuandoIdEsDeParticularEnConsultaEstado() {
        Particular particular = crearParticular(1L, "Laura Fernández", "12345678");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(particular));

        assertThrows(SocioNotFoundException.class, () -> clienteService.consultarEstadoSocio(1L));
        verify(clienteRepository).findById(1L);
    }

    @Test
    void deberiaLanzarCedulaDuplicadaCuandoSaveAndFlushFallaAlModificarParticular() {
        Particular particular = crearParticular(1L, "Juan Pérez", "12345678");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(particular));

        ModificacionParticularRequestDto dto = dtoParticular("Juan Modificado", "099999999");
        dto.setCedula("1.234.567-2");

        when(clienteRepository.saveAndFlush(particular))
                .thenThrow(new DataIntegrityViolationException("duplicada"));

        ClienteValidacionException ex = assertThrows(
                ClienteValidacionException.class,
                () -> clienteService.modificarParticular(1L, dto)
        );

        assertEquals(ClienteCodigoError.CEDULA_DUPLICADA.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarCedulaDuplicadaCuandoSaveAndFlushFallaAlModificarSocio() {
        Socio socio = crearSocio(1L, "Juan Pérez", "12345678", 1, EstadoSocio.ACTIVO);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));

        ModificacionSocioRequestDto dto = dtoSocio("Juan Modificado", "099999999");
        dto.setCedula("1.234.567-2");

        when(clienteRepository.saveAndFlush(socio))
                .thenThrow(new DataIntegrityViolationException("duplicada"));

        ClienteValidacionException ex = assertThrows(
                ClienteValidacionException.class,
                () -> clienteService.modificarSocio(1L, dto)
        );

        assertEquals(ClienteCodigoError.CEDULA_DUPLICADA.name(), ex.getCodigo());
    }
    @Test
    void deberiaRetornarListadoDeSociosConUltimaCuotaPaga() {
        Socio socio = crearSocio(1L, "Juan Pérez", "12345678", 5, EstadoSocio.ACTIVO);

        Page<Cliente> page = new PageImpl<>(List.of(socio));

        UltimaCuotaDto ultimaCuota = new UltimaCuotaDto(
                2026,
                6,
                "junio",
                "Junio 2026"
        );

        when(clienteRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        when(pagoCuotaService.calcularUltimaCuotaPaga(1L))
                .thenReturn(ultimaCuota);

        PageResponse<ListadoClientesResponseDto> response =
                clienteService.getListadoClientes(sinFiltros(), pageRequest());

        assertEquals(1, response.content().size());
        assertNotNull(response.content().get(0).getUltimaCuotaDto());
        assertEquals(2026, response.content().get(0).getUltimaCuotaDto().anio());
        assertEquals(6, response.content().get(0).getUltimaCuotaDto().mes());

        verify(pagoCuotaService).calcularUltimaCuotaPaga(1L);
    }
    @Test
    void deberiaRetornarListadoDeParticularesConUltimaCuotaNula() {
        Particular particular = crearParticular(2L, "Laura Fernández", "67890123");

        Page<Cliente> page = new PageImpl<>(List.of(particular));

        when(clienteRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        PageResponse<ListadoClientesResponseDto> response =
                clienteService.getListadoClientes(sinFiltros(), pageRequest());

        assertEquals(1, response.content().size());
        assertNull(response.content().get(0).getUltimaCuotaDto());

        verify(pagoCuotaService, never()).calcularUltimaCuotaPaga(anyLong());
    }

    @Test
void deberiaLanzarExportacionExceptionCuandoNoHayClientes() {
    when(clienteRepository.findAll(any(Specification.class)))
            .thenReturn(List.of());

    assertThrows(ExportacionException.class,
            () -> clienteService.exportarClientes(sinFiltros()));

    verify(clienteRepository).findAll(any(Specification.class));
    verify(exportService, never()).generarExcel(anyString(), anyList(), anyList(), any(int[].class));
}
@Test
@SuppressWarnings("unchecked")
void deberiaExportarFilasConLabelsLegiblesDeEstadoYMetodoCobro() {
    Socio socio = crearSocio(1L, "Juan Pérez", "12345678", 1, EstadoSocio.ACTIVO);
    // metodoCobro = EFECTIVO según crearSocio()

    when(clienteRepository.findAll(any(Specification.class)))
            .thenReturn(List.of(socio));
    when(exportProperties.maxFilas()).thenReturn(1000);
    when(exportService.generarExcel(anyString(), anyList(), anyList(), any(int[].class)))
            .thenReturn(new byte[0]);

    clienteService.exportarClientes(sinFiltros());

    ArgumentCaptor<List<List<String>>> filasCaptor =
            ArgumentCaptor.forClass((Class) List.class);
    verify(exportService).generarExcel(anyString(), anyList(), filasCaptor.capture(), any(int[].class));

    List<String> fila = filasCaptor.getValue().get(0);
    assertEquals("Activo",   fila.get(5));   // estado usa label, no "ACTIVO"
    assertEquals("Efectivo", fila.get(8));   // metodoCobro usa label, no "EFECTIVO"
}

@Test
void deberiaExportarClientesCorrectamente() {

    ListadoClientesRequestDto filtros = sinFiltros();

    Socio socio = crearSocio(
            1L,
            "Juan Pérez",
            "12345678",
            1,
            EstadoSocio.ACTIVO
    );

    byte[] excel = "excel".getBytes();

    when(clienteRepository.findAll(any(Specification.class)))
            .thenReturn(List.of(socio));
    when(exportProperties.maxFilas()).thenReturn(1000);
    when(exportService.generarExcel(
            anyString(),
            anyList(),
            anyList(),
            any(int[].class)
    )).thenReturn(excel);

    ArchivoExportado resultado = clienteService.exportarClientes(filtros);

    assertNotNull(resultado);
    assertNotNull(resultado.getContenido());
    assertTrue(resultado.getContenido().length > 0);
    assertTrue(resultado.getNombre().contains("clientes"));

    verify(clienteRepository).findAll(any(Specification.class));
    verify(exportService).generarExcel(
            anyString(),
            anyList(),
            anyList(),
            any(int[].class)
    );
}

@Test
@SuppressWarnings("unchecked")
void deberiaIncluirColumnaRutEnLaExportacion() {
    Empresa empresa = crearEmpresa(1L, "Cipolatti S.A.", "210001230018");

    when(clienteRepository.findAll(any(Specification.class))).thenReturn(List.of(empresa));
    when(exportProperties.maxFilas()).thenReturn(1000);
    when(exportService.generarExcel(anyString(), anyList(), anyList(), any(int[].class)))
            .thenReturn(new byte[0]);

    clienteService.exportarClientes(sinFiltros());

    ArgumentCaptor<List<String>> encabezadosCaptor = ArgumentCaptor.forClass((Class) List.class);
    ArgumentCaptor<List<List<String>>> filasCaptor = ArgumentCaptor.forClass((Class) List.class);
    verify(exportService).generarExcel(anyString(), encabezadosCaptor.capture(), filasCaptor.capture(), any(int[].class));

    assertTrue(encabezadosCaptor.getValue().contains("RUT"));
    assertEquals("210001230018", filasCaptor.getValue().get(0).get(3));
}

    // --- buscarPorRut ---

    @Test
    void deberiaLanzarClienteNotFoundExceptionCuandoRutNoExiste() {
        when(clienteRepository.findByRut("211003420017"))
                .thenReturn(Optional.empty());

        assertThrows(
                ClienteNotFoundException.class,
                () -> clienteService.buscarPorRut("211003420017")
        );

        verify(clienteRepository).findByRut("211003420017");
    }

    @Test
    void deberiaRetornarDtoCuandoRutCorrespondeAEmpresa() {
        Empresa empresa = crearEmpresa(1L, "Antel S.A.", "211003420017");

        when(clienteRepository.findByRut("211003420017"))
                .thenReturn(Optional.of(empresa));

        BusquedaRutResponseDto resultado =
                clienteService.buscarPorRut("211003420017");

        assertEquals(TipoCliente.EMPRESA, resultado.getTipoCliente());
        assertEquals("211003420017", resultado.getRut());

        verify(clienteRepository).findByRut("211003420017");
    }
}

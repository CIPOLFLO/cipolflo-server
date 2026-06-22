package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.clientes.domain.Particular;
import com.cipolflo.server.clientes.domain.Socio;
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
import com.cipolflo.server.shared.export.ExportProperties;
import com.cipolflo.server.shared.export.IExportService;
import com.cipolflo.server.shared.export.exception.ExportacionSinResultadosException;
import com.cipolflo.server.shared.export.exception.LimiteFilasExportacionException;
import com.cipolflo.server.shared.export.exception.LimiteTamanioExportacionException;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.dao.DataIntegrityViolationException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
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
private IExportService exportService;

@Mock
private ExportProperties exportProperties;
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

        ClienteResponseDto resultado = clienteService.modificarSocio(1L, dto);

        assertNotNull(resultado);
        assertEquals("99999999", socio.getCedula());
        assertEquals("Juan Modificado", socio.getNombreCompleto());
        assertEquals("099999999", socio.getTelefono());
        assertEquals("nuevo@mail.com", socio.getMail());
        assertEquals("Argentina", socio.getPais());
        assertEquals("Buenos Aires", socio.getCiudad());
        assertEquals(MetodoCobro.TRANSFERENCIA, socio.getMetodoCobro());
        verify(clienteRepository).saveAndFlush(socio);
        verify(modificacionSocioValidator).validar(anyLong(), any(ModificacionSocioRequestDto.class), anyString(), any());
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

        when(clienteRepository.save(any(Socio.class))).thenAnswer(invocation -> {
            Socio socio = invocation.getArgument(0);
            socio.setId(1L);
            return socio;
        });

        ClienteResponseDto response = clienteService.registrarSocio(dto);

        assertNotNull(response);
        assertEquals(11, response.getNumeroSocio());

        verify(registroSocioValidator).validar(any(RegistroSocioRequestDto.class), anyString(), any());
        verify(clienteRepository).findMaxNumeroSocio();
        verify(clienteRepository).save(any(Socio.class));
    }

    @Test
    void deberiaRegistrarSocioCorrectamente() {
        RegistroSocioRequestDto dto = crearRegistroSocioRequest();

        when(clienteRepository.findMaxNumeroSocio()).thenReturn(Optional.empty());

        when(clienteRepository.save(any(Socio.class))).thenAnswer(invocation -> {
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

        verify(clienteRepository).findMaxNumeroSocio();
        verify(registroSocioValidator).validar(any(RegistroSocioRequestDto.class), anyString(), any());
        verify(clienteRepository).save(any(Socio.class));
    }

    @Test
    void deberiaRegistrarSocioSinEmail() {
        RegistroSocioRequestDto dto = crearRegistroSocioRequest();
        dto.setEmail(null);

        when(clienteRepository.findMaxNumeroSocio()).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Socio.class))).thenAnswer(invocation -> {
            Socio socio = invocation.getArgument(0);
            socio.setId(1L);
            return socio;
        });

        ClienteResponseDto response = clienteService.registrarSocio(dto);

        assertNotNull(response);
        assertNull(response.getEmail());

        verify(registroSocioValidator).validar(any(RegistroSocioRequestDto.class), anyString(), isNull());
        verify(clienteRepository).save(any(Socio.class));
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

        EstadoSocioResponseDto resultado = clienteService.consultarEstadoSocio(1L);

        assertEquals(1L, resultado.getId());
        assertEquals(EstadoSocio.INACTIVO, resultado.getEstado());
        assertEquals(5, resultado.getNumeroSocio());
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
    void deberiaRegistrarParticularCorrectamente() {
        RegistroParticularRequestDto dto = new RegistroParticularRequestDto();
        dto.setCedula("1.234.567-8");
        dto.setNombre("Juan Pérez");
        dto.setCelular("099123456");
        dto.setMail("juan@mail.com");

        Particular particularGuardado = Particular.registrar(
                "12345678",
                "Juan Pérez",
                "099123456",
                "juan@mail.com"
        );
        particularGuardado.setId(1L);

        when(clienteRepository.saveAndFlush(any(Particular.class))).thenReturn(particularGuardado);

        ClienteResponseDto response = clienteService.registrarParticular(dto);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("12345678", response.getCedula());
        assertEquals("Juan Pérez", response.getNombre());
        assertEquals("099123456", response.getTelefono());
        assertEquals("juan@mail.com", response.getEmail());
        assertEquals(TipoCliente.PARTICULAR, response.getTipoCliente());
        assertNull(response.getNumeroSocio());
        assertNull(response.getEstado());

        verify(registroParticularValidator).validar(dto, "12345678");
        verify(clienteRepository).saveAndFlush(any(Particular.class));
    }

    @Test
    void deberiaLanzarErrorCuandoCedulaDuplicadaAlRegistrarParticular() {
        RegistroParticularRequestDto dto = new RegistroParticularRequestDto();
        dto.setCedula("1.234.567-8");
        dto.setNombre("Juan Pérez");
        dto.setCelular("099123456");

        doThrow(new ClienteValidacionException(
                ClienteCodigoError.CEDULA_DUPLICADA.name(),
                "Ya existe un cliente con esa cédula"
        )).when(registroParticularValidator).validar(dto, "12345678");

        assertThrows(
                ClienteValidacionException.class,
                () -> clienteService.registrarParticular(dto)
        );

        verify(registroParticularValidator).validar(dto, "12345678");
        verify(clienteRepository, never()).saveAndFlush(any());
    }
    @Test
    void deberiaLanzarErrorCuandoCedulaTieneFormatoInvalido() {
        RegistroParticularRequestDto dto =
                new RegistroParticularRequestDto();

        dto.setCedula("abc");
        dto.setNombre("Juan");
        dto.setCelular("099111111");

        doThrow(new ClienteValidacionException(
                ClienteCodigoError.CEDULA_INVALIDA.name(),
                "La cédula ingresada no es válida"
        )).when(registroParticularValidator).validar(any(), anyString());

        assertThrows(
                ClienteValidacionException.class,
                () -> clienteService.registrarParticular(dto)
        );
        verify(registroParticularValidator).validar(dto, "");

        verify(clienteRepository, never()).save(any());
        verify(clienteRepository, never()).saveAndFlush(any());
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
    void deberiaLanzarCedulaDuplicadaCuandoSaveAndFlushFallaAlRegistrarParticular() {
        RegistroParticularRequestDto dto = new RegistroParticularRequestDto();
        dto.setCedula("1.234.567-8");
        dto.setNombre("Juan Pérez");
        dto.setCelular("099123456");
        dto.setMail("juan@mail.com");

        when(clienteRepository.saveAndFlush(any(Particular.class)))
                .thenThrow(new DataIntegrityViolationException("duplicada"));

        ClienteValidacionException ex = assertThrows(
                ClienteValidacionException.class,
                () -> clienteService.registrarParticular(dto)
        );

        assertEquals(ClienteCodigoError.CEDULA_DUPLICADA.name(), ex.getCodigo());
        verify(registroParticularValidator).validar(dto, "12345678");
    }

    // --- exportarClientes ---

@Test
void deberiaExportarClientesExitosamente() {
    when(exportProperties.maxFilas()).thenReturn(50000);
    when(exportProperties.maxBytes()).thenReturn(10485760L);
    Socio socio = crearSocio(1L, "Ana García", "12345678", 1, EstadoSocio.ACTIVO);
    when(clienteRepository.findAll(any(Specification.class), any(Sort.class)))
            .thenReturn(List.of(socio));
    when(exportService.generarExcel(any(), any(), any(), any()))
            .thenReturn("contenido".getBytes());

    ArchivoExportado resultado = clienteService.exportarClientes(sinFiltros());

    assertNotNull(resultado);
    assertTrue(resultado.nombre().startsWith("clientes_"));
    assertTrue(resultado.nombre().endsWith(".xlsx"));
    assertNotNull(resultado.contenido());
}

@Test
void deberiaMapearSocioYParticularEnExportacion() {
    when(exportProperties.maxFilas()).thenReturn(50000);
    when(exportProperties.maxBytes()).thenReturn(10485760L);
    Socio socio = crearSocio(1L, "Ana García", "12345678", 5, EstadoSocio.ACTIVO);
    Particular particular = crearParticular(2L, "Beto Pérez", "23456789");
    when(clienteRepository.findAll(any(Specification.class), any(Sort.class)))
            .thenReturn(List.of(socio, particular));
    when(exportService.generarExcel(any(), any(), any(), any()))
            .thenReturn("contenido".getBytes());

    clienteService.exportarClientes(sinFiltros());

    verify(exportService).generarExcel(
            eq("Clientes"),
            eq(List.of("Nombre", "Número de socio", "Cédula", "Email", "Estado")),
            argThat(filas ->
                filas.get(0).get(1).equals("5")
                && filas.get(0).get(4).equals("ACTIVO")
                && filas.get(1).get(1).equals("-")
                && filas.get(1).get(4).equals("-")
            ),
            any()
    );
}

@Test
void deberiaLanzarExportacionSinResultadosCuandoListaEstaVacia() {
    when(clienteRepository.findAll(any(Specification.class), any(Sort.class)))
            .thenReturn(List.of());

    assertThrows(ExportacionSinResultadosException.class,
            () -> clienteService.exportarClientes(sinFiltros()));

    verify(exportService, never()).generarExcel(any(), any(), any(), any());
}

@Test
void deberiaLanzarLimiteFilasExcedidoAntesDeGenerarExcel() {
    when(exportProperties.maxFilas()).thenReturn(2);
    List<Cliente> clientes = List.of(
            crearSocio(1L, "Ana", "11111111", 1, EstadoSocio.ACTIVO),
            crearSocio(2L, "Beto", "22222222", 2, EstadoSocio.ACTIVO),
            crearSocio(3L, "Carlos", "33333333", 3, EstadoSocio.ACTIVO)
    );
    when(clienteRepository.findAll(any(Specification.class), any(Sort.class)))
            .thenReturn(clientes);

    assertThrows(LimiteFilasExportacionException.class,
            () -> clienteService.exportarClientes(sinFiltros()));

    verify(exportService, never()).generarExcel(any(), any(), any(), any());
}

@Test
void deberiaLanzarLimiteTamanioExcedidoDespuesDeGenerarExcel() {
    when(exportProperties.maxFilas()).thenReturn(50000);
    when(exportProperties.maxBytes()).thenReturn(5L);
    Socio socio = crearSocio(1L, "Ana García", "12345678", 1, EstadoSocio.ACTIVO);
    when(clienteRepository.findAll(any(Specification.class), any(Sort.class)))
            .thenReturn(List.of(socio));
    when(exportService.generarExcel(any(), any(), any(), any()))
            .thenReturn("contenido-que-supera-limite".getBytes());

    assertThrows(LimiteTamanioExportacionException.class,
            () -> clienteService.exportarClientes(sinFiltros()));
}

@Test
void deberiaExportarSinFiltrarPorTipoCuandoTipoClienteEsNulo() {
    when(exportProperties.maxFilas()).thenReturn(50000);
    when(exportProperties.maxBytes()).thenReturn(10485760L);
    Socio socio = crearSocio(1L, "Ana García", "12345678", 1, EstadoSocio.ACTIVO);
    Particular particular = crearParticular(2L, "Beto Pérez", "23456789");
    when(clienteRepository.findAll(any(Specification.class), any(Sort.class)))
            .thenReturn(List.of(socio, particular));
    when(exportService.generarExcel(any(), any(), any(), any()))
            .thenReturn("contenido".getBytes());

    ListadoClientesRequestDto filtrosSinTipo = new ListadoClientesRequestDto(null, null, null, null);
    ArchivoExportado resultado = clienteService.exportarClientes(filtrosSinTipo);

    assertNotNull(resultado);
    verify(clienteRepository).findAll(any(Specification.class), any(Sort.class));
}
}

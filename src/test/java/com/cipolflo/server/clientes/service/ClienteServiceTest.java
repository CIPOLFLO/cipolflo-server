package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.clientes.domain.Particular;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.domain.enums.MetodoCobro;
import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.clientes.dto.ClienteResponseDto;
import com.cipolflo.server.clientes.dto.ListadoClientesRequestDto;
import com.cipolflo.server.clientes.dto.ListadoClientesResponseDto;
import com.cipolflo.server.clientes.exception.ClienteNotFoundException;
import com.cipolflo.server.clientes.repository.ClienteRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private PageRequestDto pageRequest() {
        return new PageRequestDto(0, 10);
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
        socio.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        socio.setPais("Uruguay");
        socio.setDepartamento("Montevideo");
        socio.setCiudad("Montevideo");
        socio.setDireccion("Calle 1");
        socio.setFechaIngreso(LocalDate.of(2022, 1, 1));
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
}

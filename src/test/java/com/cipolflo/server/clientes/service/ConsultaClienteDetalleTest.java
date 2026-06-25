package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.Particular;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.clientes.exception.ClienteNotFoundException;
import com.cipolflo.server.clientes.repository.ClienteRepository;
import com.cipolflo.server.reservas.dto.ClienteDetalleReservaDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultaClienteDetalleTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ConsultaClienteDetalle consultaClienteDetalle;

    @Test
    void deberiaRetornarDtoConTodosLosCamposCuandoClienteEsSocio() {
        Socio socio = new Socio();
        socio.setId(1L);
        socio.setNombreCompleto("Juan Pérez");
        socio.setCedula("12345678");
        socio.setTelefono("099111111");
        socio.setMail("juan@mail.com");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(socio));

        ClienteDetalleReservaDto resultado = consultaClienteDetalle.getDetallClienteSimple(1L);

        assertEquals(1L, resultado.id());
        assertEquals("Juan Pérez", resultado.nombre());
        assertEquals("12345678", resultado.cedula());
        assertEquals("099111111", resultado.telefono());
        assertEquals("juan@mail.com", resultado.email());
        assertEquals(TipoCliente.SOCIO, resultado.tipoCliente());
        verify(clienteRepository).findById(1L);
    }

    @Test
    void deberiaRetornarTipoParticularCuandoClienteEsParticular() {
        Particular particular = new Particular();
        particular.setId(2L);
        particular.setNombreCompleto("Laura Fernández");
        particular.setCedula("87654321");
        particular.setTelefono("099222222");
        particular.setMail("laura@mail.com");

        when(clienteRepository.findById(2L)).thenReturn(Optional.of(particular));

        ClienteDetalleReservaDto resultado = consultaClienteDetalle.getDetallClienteSimple(2L);

        assertEquals(TipoCliente.PARTICULAR, resultado.tipoCliente());
        assertEquals(2L, resultado.id());
        assertEquals("Laura Fernández", resultado.nombre());
        assertEquals("87654321", resultado.cedula());
        assertEquals("099222222", resultado.telefono());
        assertEquals("laura@mail.com", resultado.email());
    }

    @Test
    void deberiaLanzarClienteNotFoundExceptionCuandoClienteNoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class,
                () -> consultaClienteDetalle.getDetallClienteSimple(99L));

        verify(clienteRepository).findById(99L);
    }
}

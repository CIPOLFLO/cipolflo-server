package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.Particular;
import com.cipolflo.server.clientes.dto.ClienteResponseDto;
import com.cipolflo.server.clientes.dto.RegistroParticularRequestDto;
import com.cipolflo.server.clientes.exception.ClienteCodigoError;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import com.cipolflo.server.clientes.repository.ClienteRepository;
import com.cipolflo.server.clientes.validator.RegistroParticularValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistroParticularServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private RegistroParticularValidator registroParticularValidator;

    @InjectMocks
    private RegistroParticularService registroParticularService;

    @Test
    void deberiaRegistrarParticularCorrectamente() {
        RegistroParticularRequestDto dto = new RegistroParticularRequestDto();
        dto.setCedula("1.234.567-8");
        dto.setNombre("Juan Pérez");
        dto.setCelular("099123456");
        dto.setMail("juan@mail.com");

        Particular particularGuardado = Particular.registrar(
                "12345678", "Juan Pérez", "099123456", "juan@mail.com", null
        );
        particularGuardado.setId(1L);

        when(clienteRepository.saveAndFlush(any(Particular.class))).thenReturn(particularGuardado);

        ClienteResponseDto response = registroParticularService.registrarParticular(dto);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("12345678", response.getCedula());
        assertEquals("Juan Pérez", response.getNombre());
        assertEquals("099123456", response.getTelefono());
        assertEquals("juan@mail.com", response.getEmail());

        verify(registroParticularValidator).validar(dto, "12345678");
        verify(clienteRepository).saveAndFlush(any(Particular.class));
    }

    @Test
    void deberiaLanzarExcepcionCuandoCedulaDuplicadaEnSave() {
        RegistroParticularRequestDto dto = new RegistroParticularRequestDto();
        dto.setCedula("1.234.567-8");
        dto.setNombre("Juan Pérez");
        dto.setCelular("099123456");
        dto.setMail("juan@mail.com");

        when(clienteRepository.saveAndFlush(any(Particular.class)))
                .thenThrow(new DataIntegrityViolationException("duplicada"));

        ClienteValidacionException ex = assertThrows(
                ClienteValidacionException.class,
                () -> registroParticularService.registrarParticular(dto)
        );

        assertEquals(ClienteCodigoError.CEDULA_DUPLICADA.name(), ex.getCodigo());
        verify(registroParticularValidator).validar(dto, "12345678");
    }

    @Test
    void deberiaNormalizarCedulaAntesDeValidarYGuardar() {
        RegistroParticularRequestDto dto = new RegistroParticularRequestDto();
        dto.setCedula("1.234.567-8");
        dto.setNombre("Juan");
        dto.setCelular("099000000");

        Particular particularGuardado = Particular.registrar(
                "12345678", "Juan", "099000000", null, null
        );
        particularGuardado.setId(2L);

        when(clienteRepository.saveAndFlush(any(Particular.class))).thenReturn(particularGuardado);

        registroParticularService.registrarParticular(dto);

        verify(registroParticularValidator).validar(dto, "12345678");
    }

    @Test
    void deberiaLanzarExcepcionPropagadaDesdeValidator() {
        RegistroParticularRequestDto dto = new RegistroParticularRequestDto();
        dto.setCedula("abc");
        dto.setNombre("Juan");
        dto.setCelular("099000000");

        doThrow(new ClienteValidacionException(
                ClienteCodigoError.CEDULA_INVALIDA.name(),
                "La cédula ingresada no es válida"
        )).when(registroParticularValidator).validar(any(), any());

        ClienteValidacionException ex = assertThrows(
                ClienteValidacionException.class,
                () -> registroParticularService.registrarParticular(dto)
        );

        assertEquals(ClienteCodigoError.CEDULA_INVALIDA.name(), ex.getCodigo());
        verify(clienteRepository, never()).saveAndFlush(any());
    }

    @Test
    void deberiaTrimearNombreYCelular() {
        RegistroParticularRequestDto dto = new RegistroParticularRequestDto();
        dto.setCedula("1.234.567-8");
        dto.setNombre("  Juan Pérez  ");
        dto.setCelular("  099123456  ");

        Particular particularGuardado = Particular.registrar(
                "12345678", "Juan Pérez", "099123456", null, null
        );
        particularGuardado.setId(3L);

        when(clienteRepository.saveAndFlush(any(Particular.class))).thenReturn(particularGuardado);

        registroParticularService.registrarParticular(dto);

        verify(clienteRepository).saveAndFlush(argThat(p ->
                p instanceof Particular particular &&
                "Juan Pérez".equals(particular.getNombreCompleto()) &&
                "099123456".equals(particular.getTelefono())
        ));
    }
}

package com.cipolflo.server.clientes.validator;

import com.cipolflo.server.clientes.exception.ClienteCodigoError;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import com.cipolflo.server.clientes.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CedulaUnicaValidatorTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private CedulaUnicaValidator validator;

    @Test
    void deberiaPermitirCedulaNoExistente() {
        when(clienteRepository.existsByCedulaAndIdNot("12345672", 1L)).thenReturn(false);

        assertDoesNotThrow(() -> validator.validar("12345672", 1L));
    }

    @Test
    void deberiaLanzarExceptionCuandoCedulaYaExiste() {
        when(clienteRepository.existsByCedulaAndIdNot("12345672", 1L)).thenReturn(true);

        ClienteValidacionException ex = assertThrows(ClienteValidacionException.class,
                () -> validator.validar("12345672", 1L));
        assertEquals(ClienteCodigoError.CEDULA_DUPLICADA.name(), ex.getCodigo());
    }

    @Test
    void deberiaNormalizarCedulaAntesDeConsultar() {
        when(clienteRepository.existsByCedulaAndIdNot("12345672", 1L)).thenReturn(false);

        validator.validar("1.234.567-2", 1L);

        verify(clienteRepository).existsByCedulaAndIdNot("12345672", 1L);
    }

    @Test
    void deberiaLanzarExceptionCuandoCedulaEsNula() {
        ClienteValidacionException ex = assertThrows(ClienteValidacionException.class,
                () -> validator.validar(null, 1L));
        assertEquals(ClienteCodigoError.CEDULA_INVALIDA.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExceptionCuandoCedulaEsBlank() {
        ClienteValidacionException ex = assertThrows(ClienteValidacionException.class,
                () -> validator.validar("   ", 1L));
        assertEquals(ClienteCodigoError.CEDULA_INVALIDA.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExceptionCuandoCedulaNormalizadaEsVacia() {
        ClienteValidacionException ex = assertThrows(ClienteValidacionException.class,
                () -> validator.validar("...-", 1L));
        assertEquals(ClienteCodigoError.CEDULA_INVALIDA.name(), ex.getCodigo());
    }
    @Test
    void deberiaLanzarErrorCuandoCedulaNormalizadaQuedaVacia() {
        ClienteValidacionException exception =
                assertThrows(ClienteValidacionException.class, () -> validator.validar("...---"));

        assertEquals(ClienteCodigoError.CEDULA_INVALIDA.name(), exception.getCodigo());
        verifyNoInteractions(clienteRepository);
    }
    @Test
    void deberiaPermitirCedulaNoExistenteEnRegistro() {
        when(clienteRepository.existsByCedula("12345672")).thenReturn(false);

        assertDoesNotThrow(() -> validator.validar("12345672"));

        verify(clienteRepository).existsByCedula("12345672");
    }

    @Test
    void deberiaLanzarExceptionCuandoCedulaYaExisteEnRegistro() {
        when(clienteRepository.existsByCedula("12345672")).thenReturn(true);

        ClienteValidacionException ex = assertThrows(
                ClienteValidacionException.class,
                () -> validator.validar("12345672")
        );

        assertEquals(ClienteCodigoError.CEDULA_YA_REGISTRADA.name(), ex.getCodigo());
    }

    @Test
    void deberiaNormalizarCedulaAntesDeConsultarEnRegistro() {
        when(clienteRepository.existsByCedula("12345672")).thenReturn(false);

        validator.validar("1.234.567-2");

        verify(clienteRepository).existsByCedula("12345672");
    }

}

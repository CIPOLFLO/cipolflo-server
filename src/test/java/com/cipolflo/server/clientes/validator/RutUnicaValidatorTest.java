package com.cipolflo.server.clientes.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cipolflo.server.clientes.exception.ClienteCodigoError;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import com.cipolflo.server.clientes.repository.ClienteRepository;

@ExtendWith(MockitoExtension.class)
class RutUnicaValidatorTest {
    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private RutUnicaValidator validator;

    @Test
    void deberiaPermitirRutNoExistente() {
        when(clienteRepository.existsByRut("211003420017")).thenReturn(false);

        assertDoesNotThrow(() -> validator.validar("211003420017"));

        verify(clienteRepository).existsByRut("211003420017");
    }

    @Test
    void deberiaLanzarExceptionCuandoRutYaExiste() {
        when(clienteRepository.existsByRut("211003420017")).thenReturn(true);

        ClienteValidacionException ex = assertThrows(
                ClienteValidacionException.class,
                () -> validator.validar("211003420017")
        );

        assertEquals(ClienteCodigoError.RUT_DUPLICADO.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExceptionCuandoRutEsNulo() {
        ClienteValidacionException ex = assertThrows(ClienteValidacionException.class,
                () -> validator.validar(null));

        assertEquals(ClienteCodigoError.RUT_INVALIDO.name(), ex.getCodigo());
        verifyNoInteractions(clienteRepository);
    }

    @Test
    void deberiaLanzarExceptionCuandoRutEsBlank() {
        ClienteValidacionException ex = assertThrows(ClienteValidacionException.class,
                () -> validator.validar("   "));

        assertEquals(ClienteCodigoError.RUT_INVALIDO.name(), ex.getCodigo());
        verifyNoInteractions(clienteRepository);
    }
}
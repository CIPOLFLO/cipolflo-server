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
class EmailUnicoValidatorTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private EmailUnicoValidator validator;

    @Test
    void deberiaPermitirEmailNoExistente() {
        when(clienteRepository.existsByMailIgnoreCaseAndIdNot("juan@mail.com", 1L)).thenReturn(false);

        assertDoesNotThrow(() -> validator.validar("juan@mail.com", 1L));
    }

    @Test
    void deberiaLanzarExceptionCuandoEmailYaExiste() {
        when(clienteRepository.existsByMailIgnoreCaseAndIdNot("juan@mail.com", 1L)).thenReturn(true);

        ClienteValidacionException ex = assertThrows(ClienteValidacionException.class,
                () -> validator.validar("juan@mail.com", 1L));
        assertEquals(ClienteCodigoError.EMAIL_DUPLICADO.name(), ex.getCodigo());
    }

    @Test
    void deberiaPermitirEmailNulo() {
        assertDoesNotThrow(() -> validator.validar(null, 1L));

        verify(clienteRepository, never()).existsByMailIgnoreCaseAndIdNot(null, 1L);
    }

    @Test
    void deberiaPermitirEmailBlank() {
        assertDoesNotThrow(() -> validator.validar("   ", 1L));

        verify(clienteRepository, never()).existsByMailIgnoreCaseAndIdNot("   ", 1L);
    }

    @Test
    void deberiaTrimearEmailAntesDeConsultar() {
        when(clienteRepository.existsByMailIgnoreCaseAndIdNot("juan@mail.com", 1L)).thenReturn(false);

        validator.validar("  juan@mail.com  ", 1L);

        verify(clienteRepository).existsByMailIgnoreCaseAndIdNot("juan@mail.com", 1L);
    }

    @Test
    void noDeberiaConsultarRepositorioCuandoEmailEsNull() {
        validator.validar(null);

        verifyNoInteractions(clienteRepository);
    }

    @Test
    void noDeberiaConsultarRepositorioCuandoEmailEstaEnBlanco() {
        validator.validar("   ");

        verifyNoInteractions(clienteRepository);
    }
}



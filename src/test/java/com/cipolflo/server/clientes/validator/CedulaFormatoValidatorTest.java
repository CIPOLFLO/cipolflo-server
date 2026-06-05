package com.cipolflo.server.clientes.validator;

import com.cipolflo.server.clientes.exception.ClienteCodigoError;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CedulaFormatoValidatorTest {

    private final CedulaFormatoValidator validator = new CedulaFormatoValidator();

    @Test
    void deberiaValidarCedulaValida8Digitos() {
        assertDoesNotThrow(() -> validator.validar("12345672"));
    }

    @Test
    void deberiaValidarCedulaValida7Digitos() {
        assertDoesNotThrow(() -> validator.validar("1234561"));
    }

    @Test
    void deberiaValidarCedulaConPuntosYGuiones() {
        assertDoesNotThrow(() -> validator.validar("1.234.567-2"));
    }

    @Test
    void deberiaLanzarExceptionCuandoCedulaEsNula() {
        ClienteValidacionException ex = assertThrows(ClienteValidacionException.class,
                () -> validator.validar(null));
        assertEquals(ClienteCodigoError.CEDULA_INVALIDA.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExceptionCuandoCedulaEsBlank() {
        ClienteValidacionException ex = assertThrows(ClienteValidacionException.class,
                () -> validator.validar("   "));
        assertEquals(ClienteCodigoError.CEDULA_INVALIDA.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExceptionCuandoCedulaTieneMenosDe7Digitos() {
        ClienteValidacionException ex = assertThrows(ClienteValidacionException.class,
                () -> validator.validar("123456"));
        assertEquals(ClienteCodigoError.CEDULA_INVALIDA.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExceptionCuandoCedulaTieneMasDe8Digitos() {
        ClienteValidacionException ex = assertThrows(ClienteValidacionException.class,
                () -> validator.validar("123456789"));
        assertEquals(ClienteCodigoError.CEDULA_INVALIDA.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExceptionCuandoDigitoVerificadorEsIncorrecto() {
        ClienteValidacionException ex = assertThrows(ClienteValidacionException.class,
                () -> validator.validar("12345678"));
        assertEquals(ClienteCodigoError.CEDULA_INVALIDA.name(), ex.getCodigo());
    }
}

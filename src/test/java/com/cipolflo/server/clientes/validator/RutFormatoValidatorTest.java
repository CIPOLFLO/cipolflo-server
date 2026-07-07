package com.cipolflo.server.clientes.validator;

import com.cipolflo.server.clientes.exception.ClienteCodigoError;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RutFormatoValidatorTest {
    private final RutFormatoValidator validator = new RutFormatoValidator();

    @Test
    void deberiaValidarRutValido() {
        assertDoesNotThrow(() -> validator.validar("211003420017"));
    }

    @Test
    void deberiaValidarRutConPuntosYGuiones() {
        assertDoesNotThrow(() -> validator.validar("21.100342.001-7"));
    }

    @Test
    void deberiaLanzarExceptionCuandoRutEsNulo() {
        ClienteValidacionException ex = assertThrows(ClienteValidacionException.class,
                () -> validator.validar(null));
        assertEquals(ClienteCodigoError.RUT_INVALIDO.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExceptionCuandoRutEsBlank() {
        ClienteValidacionException ex = assertThrows(ClienteValidacionException.class,
                () -> validator.validar("   "));
        assertEquals(ClienteCodigoError.RUT_INVALIDO.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExceptionCuandoRutTieneMenosDe12Digitos() {
        ClienteValidacionException ex = assertThrows(ClienteValidacionException.class,
                () -> validator.validar("2110034200"));
        assertEquals(ClienteCodigoError.RUT_INVALIDO.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExceptionCuandoRutTieneMasDe12Digitos() {
        ClienteValidacionException ex = assertThrows(ClienteValidacionException.class,
                () -> validator.validar("2110034200177"));
        assertEquals(ClienteCodigoError.RUT_INVALIDO.name(), ex.getCodigo());
    }

    @Test
    void deberiaLanzarExceptionCuandoDigitoVerificadorEsIncorrecto() {
        ClienteValidacionException ex = assertThrows(ClienteValidacionException.class,
                () -> validator.validar("211003420011"));
        assertEquals(ClienteCodigoError.RUT_INVALIDO.name(), ex.getCodigo());
    }
}

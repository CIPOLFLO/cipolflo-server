package com.cipolflo.server.shared.exception;

import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.clientes.exception.ClienteCodigoError;
import com.cipolflo.server.clientes.exception.ClienteNotFoundException;
import com.cipolflo.server.servicios.exception.ConfirmacionDevolucionRequeridaException;
import com.cipolflo.server.servicios.exception.ReservaNoCancelableException;
import com.cipolflo.server.servicios.exception.ServicioNotFoundException;
import com.cipolflo.server.servicios.exception.ServicioValidacionException;
import com.cipolflo.server.shared.dto.ErrorResponse;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.core.MethodParameter;
import org.springframework.validation.method.ParameterValidationResult;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleClienteNotFoundException_deberiaRetornar404() {
        ClienteNotFoundException ex = new ClienteNotFoundException(1L);

        ResponseEntity<ErrorResponse> response = handler.handleClienteNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(ClienteCodigoError.CLIENTE_NO_ENCONTRADO.name(), response.getBody().codigo());
        assertEquals("Cliente no encontrado con id: 1", response.getBody().descripcion());
    }

    @Test
    void handleServicioNotFoundException_deberiaRetornar404() {
        ServicioNotFoundException ex = new ServicioNotFoundException(1L);

        ResponseEntity<ErrorResponse> response = handler.handleServicioNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(ServicioCodigoError.SERVICIO_NO_ENCONTRADO.name(), response.getBody().codigo());
        assertEquals("Servicio no encontrado con id: 1", response.getBody().descripcion());
    }

    @Test
    void handleServicioValidacionException_deberiaRetornar400ConCodigoYMensaje() {
        ServicioValidacionException ex = new ServicioValidacionException("NOMBRE_DUPLICADO", "Ya existe un servicio con ese nombre");

        ResponseEntity<ErrorResponse> response = handler.handleServicioValidacionException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("NOMBRE_DUPLICADO", response.getBody().codigo());
        assertEquals("Ya existe un servicio con ese nombre", response.getBody().descripcion());
    }

    @Test
    void handleConfirmacionDevolucionRequerida_deberiaRetornar400() {
        ConfirmacionDevolucionRequeridaException ex = new ConfirmacionDevolucionRequeridaException();

        ResponseEntity<ErrorResponse> response = handler.handleConfirmacionDevolucionRequeridaException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ServicioCodigoError.CONFIRMACION_DEVOLUCION_REQUERIDA.name(), response.getBody().codigo());
    }

    @Test
    void handleReservaNoCancelable_deberiaRetornar400() {
        ReservaNoCancelableException ex = new ReservaNoCancelableException();

        ResponseEntity<ErrorResponse> response = handler.handleReservaNoCancelableException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ServicioCodigoError.RESERVA_NO_CANCELABLE.name(), response.getBody().codigo());
    }

    @Test
    void handleHandlerMethodValidationException_conPathVariable_deberiaRetornarIdInvalido() {
        HandlerMethodValidationException ex = mock(HandlerMethodValidationException.class);
        ParameterValidationResult result = mock(ParameterValidationResult.class);
        MethodParameter param = mock(MethodParameter.class);

        when(param.hasParameterAnnotation(PathVariable.class)).thenReturn(true);
        when(result.getMethodParameter()).thenReturn(param);
        when(result.getResolvableErrors()).thenReturn(List.of());
        when(ex.getValueResults()).thenReturn(List.of(result));

        ResponseEntity<ErrorResponse> response = handler.handleHandlerMethodValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ServicioCodigoError.ID_INVALIDO.name(), response.getBody().codigo());
    }

    @Test
    void handleHandlerMethodValidationException_sinPathVariable_deberiaRetornarSolicitudInvalida() {
        HandlerMethodValidationException ex = mock(HandlerMethodValidationException.class);
        ParameterValidationResult result = mock(ParameterValidationResult.class);
        MethodParameter param = mock(MethodParameter.class);
        MessageSourceResolvable error = mock(MessageSourceResolvable.class);

        when(param.hasParameterAnnotation(PathVariable.class)).thenReturn(false);
        when(result.getMethodParameter()).thenReturn(param);
        when(error.getDefaultMessage()).thenReturn("size debe ser mayor a 0");
        when(result.getResolvableErrors()).thenReturn(List.of(error));
        when(ex.getValueResults()).thenReturn(List.of(result));

        ResponseEntity<ErrorResponse> response = handler.handleHandlerMethodValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ServicioCodigoError.SOLICITUD_INVALIDA.name(), response.getBody().codigo());
        assertEquals("size debe ser mayor a 0", response.getBody().descripcion());
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleConstraintViolationException_deberiaRetornar400ConMensaje() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("El nombre no puede superar los 100 caracteres");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ServicioCodigoError.SOLICITUD_INVALIDA.name(), response.getBody().codigo());
        assertEquals("El nombre no puede superar los 100 caracteres", response.getBody().descripcion());
    }

    @Test
    void handleMethodArgumentNotValidException_deberiaRetornar400ConMensaje() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "nombre", "El nombre no puede superar los 100 caracteres");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValidException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("El nombre no puede superar los 100 caracteres", response.getBody().descripcion());
    }

    @Test
    void handleMethodArgumentTypeMismatchException_deberiaRetornar400() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("id");

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentTypeMismatchException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ServicioCodigoError.ID_INVALIDO.name(), response.getBody().codigo());
    }

    @Test
    void handleBindException_conTypeMismatch_deberiaMostrarValorRechazado() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "filtros");
        bindingResult.addError(new FieldError("filtros", "tipoCliente", "INVALIDO", true,
                new String[]{"typeMismatch.tipoCliente"}, null, "default"));
        BindException ex = new BindException(bindingResult);

        ResponseEntity<String> response = handler.handleBindException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("INVALIDO"));
        assertTrue(response.getBody().contains("tipoCliente"));
    }

    @Test
    void handleBindException_conValidacion_deberiaMostrarMensajePorDefecto() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "filtros");
        bindingResult.addError(new FieldError("filtros", "nombre", null, false,
                new String[]{"Size.nombre"}, null, "El nombre no puede superar los 100 caracteres"));
        BindException ex = new BindException(bindingResult);

        ResponseEntity<String> response = handler.handleBindException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("El nombre no puede superar los 100 caracteres", response.getBody());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void handleJsonErrors_conEnumInvalido_deberiaMostrarValoresAceptados() {
        InvalidFormatException cause = mock(InvalidFormatException.class);
        when(cause.getTargetType()).thenReturn((Class) TipoCliente.class);
        when(cause.getPath()).thenReturn(List.of(new JsonMappingException.Reference(null, "tipoCliente")));
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "msg", cause, new MockHttpInputMessage(new byte[0]));

        ResponseEntity<ErrorResponse> response = handler.handleJsonErrors(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().descripcion().contains("SOCIO"));
        assertTrue(response.getBody().descripcion().contains("PARTICULAR"));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void handleJsonErrors_conTipoNoEnum_deberiaRetornarMensajeGenerico() {
        InvalidFormatException cause = mock(InvalidFormatException.class);
        when(cause.getTargetType()).thenReturn((Class) String.class);
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "msg", cause, new MockHttpInputMessage(new byte[0]));

        ResponseEntity<ErrorResponse> response = handler.handleJsonErrors(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("JSON malformado o campo con valor inválido", response.getBody().descripcion());
    }

    @Test
    void handleJsonErrors_sinInvalidFormatException_deberiaRetornarMensajeGenerico() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "msg", new RuntimeException("parse error"), new MockHttpInputMessage(new byte[0]));

        ResponseEntity<ErrorResponse> response = handler.handleJsonErrors(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("JSON malformado o campo con valor inválido", response.getBody().descripcion());
    }
}

package com.cipolflo.server.shared.exception;

import com.cipolflo.server.servicios.exception.ConfirmacionDevolucionRequeridaException;
import com.cipolflo.server.servicios.exception.ReservaNoCancelableException;
import com.cipolflo.server.servicios.exception.ServicioNotFoundException;
import com.cipolflo.server.servicios.exception.ServicioPreciosException;
import com.cipolflo.server.servicios.exception.ServicioValidacionException;
import com.cipolflo.server.shared.dto.ErrorResponse;
import com.cipolflo.server.shared.exception.ServicioCodigoError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServicioNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleServicioNotFoundException(ServicioNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ServicioCodigoError.SERVICIO_NO_ENCONTRADO.name(), ex.getMessage()));
    }

    @ExceptionHandler(ServicioValidacionException.class)
    public ResponseEntity<ErrorResponse> handleServicioValidacionException(ServicioValidacionException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getCodigo(), ex.getMessage()));
    }

    @ExceptionHandler(ConfirmacionDevolucionRequeridaException.class)
    public ResponseEntity<ErrorResponse> handleConfirmacionDevolucionRequeridaException(ConfirmacionDevolucionRequeridaException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ServicioCodigoError.CONFIRMACION_DEVOLUCION_REQUERIDA.name(), ex.getMessage()));
    }

    @ExceptionHandler(ReservaNoCancelableException.class)
    public ResponseEntity<ErrorResponse> handleReservaNoCancelableException(ReservaNoCancelableException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ServicioCodigoError.RESERVA_NO_CANCELABLE.name(), ex.getMessage()));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(HandlerMethodValidationException ex) {
        boolean esPathVariable = ex.getValueResults().stream()
                .anyMatch(r -> r.getMethodParameter().hasParameterAnnotation(PathVariable.class));
        String descripcion = ex.getValueResults().stream()
                .flatMap(r -> r.getResolvableErrors().stream())
                .map(MessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("Parámetro de solicitud inválido");
        String codigo = esPathVariable
                ? ServicioCodigoError.ID_INVALIDO.name()
                : ServicioCodigoError.SOLICITUD_INVALIDA.name();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(codigo, descripcion));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        String descripcion = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("Parámetro de solicitud inválido");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ServicioCodigoError.SOLICITUD_INVALIDA.name(), descripcion));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String descripcion = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Solicitud inválida");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ServicioCodigoError.SOLICITUD_INVALIDA.name(), descripcion));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ServicioCodigoError.SOLICITUD_INVALIDA.name(), "JSON malformado o campo con valor inválido"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ServicioCodigoError.ID_INVALIDO.name(), "El id debe ser un número positivo"));
    }


    @ExceptionHandler(BindException.class)
    public ResponseEntity<String> handleBindException(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::buildBindErrorMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(message);
    }

    private String buildBindErrorMessage(FieldError error) {
        boolean esTypeMismatch = error.getCodes() != null &&
                Arrays.stream(error.getCodes()).anyMatch(c -> c.startsWith("typeMismatch"));
        if (esTypeMismatch) {
            return "El valor '" + error.getRejectedValue() +
                    "' no es válido para el parámetro '" + error.getField() + "'";
        }
        return error.getDefaultMessage();
    }
    @ExceptionHandler(ServicioPreciosException.class)
public ResponseEntity<ErrorResponse> handleServicioPreciosException(
        ServicioPreciosException ex
) {
    ErrorResponse error = new ErrorResponse(
            ServicioCodigoError.PRECIO_SOCIO_MAYOR_O_IGUAL_PARTICULAR.name(),
            ex.getMessage()
    );

    return ResponseEntity.badRequest().body(error);
}
}

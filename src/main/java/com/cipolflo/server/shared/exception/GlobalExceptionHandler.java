package com.cipolflo.server.shared.exception;

import com.cipolflo.server.clientes.exception.ClienteCodigoError;
import com.cipolflo.server.clientes.exception.ClienteNotFoundException;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import com.cipolflo.server.finanzas.exception.FinanzaCodigoError;
import com.cipolflo.server.finanzas.exception.FinanzaNotFoundException;
import com.cipolflo.server.servicios.exception.ConfirmacionDevolucionRequeridaException;
import com.cipolflo.server.servicios.exception.ReservaNoCancelableException;
import com.cipolflo.server.servicios.exception.ServicioNotFoundException;
import com.cipolflo.server.servicios.exception.ServicioValidacionException;
import com.cipolflo.server.shared.dto.ErrorResponse;
import com.cipolflo.server.shared.export.ExportacionException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
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
import com.cipolflo.server.clientes.exception.SocioNotFoundException;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String RECURSO_NO_ENCONTRADO =
            "Recurso no encontrado: {}";

    @ExceptionHandler(ClienteNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleClienteNotFoundException(ClienteNotFoundException ex) {
        log.warn(RECURSO_NO_ENCONTRADO, ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ClienteCodigoError.CLIENTE_NO_ENCONTRADO.name(), ex.getMessage()));
    }

    @ExceptionHandler(ClienteValidacionException.class)
    public ResponseEntity<ErrorResponse> handleClienteValidacionException(ClienteValidacionException ex) {
        log.warn("Validación de negocio fallida [{}]: {}", ex.getCodigo(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getCodigo(), ex.getMessage()));
    }

    @ExceptionHandler(ServicioNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleServicioNotFoundException(ServicioNotFoundException ex) {
        log.warn(RECURSO_NO_ENCONTRADO, ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ServicioCodigoError.SERVICIO_NO_ENCONTRADO.name(), ex.getMessage()));
    }

    @ExceptionHandler(ServicioValidacionException.class)
    public ResponseEntity<ErrorResponse> handleServicioValidacionException(ServicioValidacionException ex) {
        log.warn("Validación de negocio fallida [{}]: {}", ex.getCodigo(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getCodigo(), ex.getMessage()));
    }

    @ExceptionHandler(ConfirmacionDevolucionRequeridaException.class)
    public ResponseEntity<ErrorResponse> handleConfirmacionDevolucionRequeridaException(ConfirmacionDevolucionRequeridaException ex) {
        log.warn("Confirmación de devolución requerida: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ServicioCodigoError.CONFIRMACION_DEVOLUCION_REQUERIDA.name(), ex.getMessage()));
    }

    @ExceptionHandler(ReservaNoCancelableException.class)
    public ResponseEntity<ErrorResponse> handleReservaNoCancelableException(ReservaNoCancelableException ex) {
        log.warn("Reserva no cancelable: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ServicioCodigoError.RESERVA_NO_CANCELABLE.name(), ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Argumento inválido: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ServicioCodigoError.SOLICITUD_INVALIDA.name(), ex.getMessage()));
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
        log.warn("Parámetro inválido [{}]: {}", codigo, descripcion);
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
        log.warn("Constraint violation: {}", descripcion);
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
                .map(FieldError::getDefaultMessage)
                .orElse("Solicitud inválida");
        log.warn("Validación de request fallida: {}", descripcion);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ServicioCodigoError.SOLICITUD_INVALIDA.name(), descripcion));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.warn("Tipo de argumento inválido para '{}': {}", ex.getName(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ServicioCodigoError.ID_INVALIDO.name(), "El id debe ser un número positivo"));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<String> handleBindException(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::buildBindErrorMessage)
                .collect(Collectors.joining(", "));
        log.warn("Bind exception: {}", message);
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonErrors(HttpMessageNotReadableException ex) {

        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException invalidFormatException) {

            if (invalidFormatException.getTargetType().isEnum()) {

                String campo = invalidFormatException.getPath().get(0).getFieldName();

                Object[] valores = invalidFormatException.getTargetType().getEnumConstants();

                String valoresAceptados = Arrays.stream(valores)
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));

                return ResponseEntity.badRequest().body(
                        new ErrorResponse(
                                "SOLICITUD_INVALIDA",
                                campo + " inválido. Valores aceptados: " + valoresAceptados
                        )
                );
            }
        }
        return ResponseEntity.badRequest().body(
                new ErrorResponse(
                        "SOLICITUD_INVALIDA",
                        "JSON malformado o campo con valor inválido"
                )
        );
    }

    @ExceptionHandler(SocioNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSocioNotFoundException(SocioNotFoundException ex) {
        log.warn("Socio no encontrado: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ClienteCodigoError.SOCIO_NO_ENCONTRADO.name(), ex.getMessage()));
    }

    @ExceptionHandler(FinanzaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFinanzaNotFoundException(FinanzaNotFoundException ex) {
        log.warn(RECURSO_NO_ENCONTRADO, ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(FinanzaCodigoError.FINANZA_NO_ENCONTRADA.name(), ex.getMessage()));
    }
    @ExceptionHandler(ExportacionException.class)
    public ResponseEntity<ErrorResponse> handleExportacionException(ExportacionException ex) {
        log.warn("Error en exportación: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("LIMITE_TAMANIO_EXCEDIDO", ex.getMessage()));
    }

@ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
        org.springframework.web.bind.MissingServletRequestParameterException ex) {
    log.warn("Parámetro requerido faltante: {}", ex.getMessage());
    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(ServicioCodigoError.SOLICITUD_INVALIDA.name(),
                    "El parámetro '" + ex.getParameterName() + "' es requerido"));
}
}

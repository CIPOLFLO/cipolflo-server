package com.cipolflo.server.ajustes.controller;

import com.cipolflo.server.ajustes.dto.DestinatarioNotificacionEmailResponseDto;
import com.cipolflo.server.ajustes.dto.HabilitacionDestinatarioNotificacionEmailRequestDto;
import com.cipolflo.server.ajustes.dto.ListadoDestinatarioNotificacionEmailResponseDto;
import com.cipolflo.server.ajustes.dto.ListadoDestinatariosNotificacionEmailRequestDto;
import com.cipolflo.server.ajustes.dto.ModificacionDestinatarioNotificacionEmailRequestDto;
import com.cipolflo.server.ajustes.dto.RegistroDestinatarioNotificacionEmailRequestDto;
import com.cipolflo.server.ajustes.service.IDestinatarioNotificacionEmailService;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/ajustes/destinatarios-notificacion-email")
public class DestinatarioNotificacionEmailController {

    private final IDestinatarioNotificacionEmailService destinatarioNotificacionEmailService;

    public DestinatarioNotificacionEmailController(
            IDestinatarioNotificacionEmailService destinatarioNotificacionEmailService) {
        this.destinatarioNotificacionEmailService = destinatarioNotificacionEmailService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<PageResponse<ListadoDestinatarioNotificacionEmailResponseDto>> getListado(
            @Valid @ModelAttribute ListadoDestinatariosNotificacionEmailRequestDto filtros,
            @Valid @ModelAttribute PageRequestDto pageRequest) {
        return ResponseEntity.ok(destinatarioNotificacionEmailService.getListado(filtros, pageRequest));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<DestinatarioNotificacionEmailResponseDto> getDetalle(
            @PathVariable
            @Positive(message = "El id del destinatario debe ser un número positivo")
            Long id) {
        return ResponseEntity.ok(destinatarioNotificacionEmailService.getDetalle(id));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<DestinatarioNotificacionEmailResponseDto> registrar(
            @Valid @RequestBody RegistroDestinatarioNotificacionEmailRequestDto dto) {
        DestinatarioNotificacionEmailResponseDto response = destinatarioNotificacionEmailService.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<DestinatarioNotificacionEmailResponseDto> modificar(
            @PathVariable
            @Positive(message = "El id del destinatario debe ser un número positivo")
            Long id,
            @Valid @RequestBody ModificacionDestinatarioNotificacionEmailRequestDto dto) {
        return ResponseEntity.ok(destinatarioNotificacionEmailService.modificar(id, dto));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/habilitacion")
    public ResponseEntity<DestinatarioNotificacionEmailResponseDto> cambiarHabilitacion(
            @PathVariable
            @Positive(message = "El id del destinatario debe ser un número positivo")
            Long id,
            @Valid @RequestBody HabilitacionDestinatarioNotificacionEmailRequestDto dto) {
        return ResponseEntity.ok(destinatarioNotificacionEmailService.cambiarHabilitacion(id, dto));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable
            @Positive(message = "El id del destinatario debe ser un número positivo")
            Long id) {
        destinatarioNotificacionEmailService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

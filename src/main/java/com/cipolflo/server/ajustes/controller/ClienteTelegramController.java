package com.cipolflo.server.ajustes.controller;

import com.cipolflo.server.ajustes.dto.ClienteTelegramResponseDto;
import com.cipolflo.server.ajustes.dto.HabilitacionClienteTelegramRequestDto;
import com.cipolflo.server.ajustes.dto.ListadoClienteTelegramResponseDto;
import com.cipolflo.server.ajustes.dto.ListadoClientesTelegramRequestDto;
import com.cipolflo.server.ajustes.dto.ModificacionClienteTelegramRequestDto;
import com.cipolflo.server.ajustes.dto.RegistroClienteTelegramRequestDto;
import com.cipolflo.server.ajustes.service.IClienteTelegramService;
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
@RequestMapping("/api/v1/ajustes/clientes-telegram")
public class ClienteTelegramController {

    private final IClienteTelegramService clienteTelegramService;

    public ClienteTelegramController(IClienteTelegramService clienteTelegramService) {
        this.clienteTelegramService = clienteTelegramService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<PageResponse<ListadoClienteTelegramResponseDto>> getListado(
            @Valid @ModelAttribute ListadoClientesTelegramRequestDto filtros,
            @Valid @ModelAttribute PageRequestDto pageRequest) {
        return ResponseEntity.ok(clienteTelegramService.getListado(filtros, pageRequest));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ClienteTelegramResponseDto> getDetalle(
            @PathVariable
            @Positive(message = "El id del cliente autorizado de Telegram debe ser un número positivo")
            Long id) {
        return ResponseEntity.ok(clienteTelegramService.getDetalle(id));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<ClienteTelegramResponseDto> registrar(
            @Valid @RequestBody RegistroClienteTelegramRequestDto dto) {
        ClienteTelegramResponseDto response = clienteTelegramService.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<ClienteTelegramResponseDto> modificar(
            @PathVariable
            @Positive(message = "El id del cliente autorizado de Telegram debe ser un número positivo")
            Long id,
            @Valid @RequestBody ModificacionClienteTelegramRequestDto dto) {
        return ResponseEntity.ok(clienteTelegramService.modificar(id, dto));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/habilitacion")
    public ResponseEntity<ClienteTelegramResponseDto> cambiarHabilitacion(
            @PathVariable
            @Positive(message = "El id del cliente autorizado de Telegram debe ser un número positivo")
            Long id,
            @Valid @RequestBody HabilitacionClienteTelegramRequestDto dto) {
        return ResponseEntity.ok(clienteTelegramService.cambiarHabilitacion(id, dto));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable
            @Positive(message = "El id del cliente autorizado de Telegram debe ser un número positivo")
            Long id) {
        clienteTelegramService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

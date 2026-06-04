package com.cipolflo.server.clientes.controller;

import com.cipolflo.server.clientes.dto.ClienteResponseDto;
import com.cipolflo.server.clientes.dto.ListadoClientesRequestDto;
import com.cipolflo.server.clientes.dto.ListadoClientesResponseDto;
import com.cipolflo.server.clientes.dto.ModificacionParticularRequestDto;
import com.cipolflo.server.clientes.dto.ModificacionSocioRequestDto;
import com.cipolflo.server.clientes.service.IClienteService;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/clientes")
public class ClienteController {
    private final IClienteService clienteService;

    public ClienteController(IClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<PageResponse<ListadoClientesResponseDto>> getListadoClientes(
            @Valid @ModelAttribute ListadoClientesRequestDto filtros,
            @Valid @ModelAttribute PageRequestDto pageRequest) {
        return ResponseEntity.ok(clienteService.getListadoClientes(filtros, pageRequest));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDto> getDetalleCliente(
            @PathVariable @Positive(message = "El id del cliente debe ser un número positivo") Long id) {
        return ResponseEntity.ok(clienteService.getDetalleCliente(id));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/particulares/{id}")
    public ResponseEntity<ClienteResponseDto> modificarParticular(
            @PathVariable @Positive(message = "El id del cliente debe ser un número positivo") Long id,
            @Valid @RequestBody ModificacionParticularRequestDto dto) {
        return ResponseEntity.ok(clienteService.modificarParticular(id, dto));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/socios/{id}")
    public ResponseEntity<ClienteResponseDto> modificarSocio(
            @PathVariable @Positive(message = "El id del cliente debe ser un número positivo") Long id,
            @Valid @RequestBody ModificacionSocioRequestDto dto) {
        return ResponseEntity.ok(clienteService.modificarSocio(id, dto));
    }
}

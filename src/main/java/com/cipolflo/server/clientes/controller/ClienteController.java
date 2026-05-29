package com.cipolflo.server.clientes.controller;

import com.cipolflo.server.clientes.dto.ListadoClientesRequestDto;
import com.cipolflo.server.clientes.dto.ListadoClientesResponseDto;
import com.cipolflo.server.clientes.service.IClienteService;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

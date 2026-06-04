package com.cipolflo.server.clientes.controller;

import com.cipolflo.server.clientes.dto.ClienteResponseDto;
import com.cipolflo.server.clientes.dto.ListadoClientesRequestDto;
import com.cipolflo.server.clientes.dto.ListadoClientesResponseDto;
import com.cipolflo.server.clientes.service.IClienteService;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@Validated
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private static final Set<String> CAMPOS_ORDEN_PERMITIDOS = Set.of(
            "nombreCompleto", "cedula", "numeroSocio"
    );

    private final IClienteService clienteService;

    public ClienteController(IClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<PageResponse<ListadoClientesResponseDto>> getListadoClientes(
            @Valid @ModelAttribute ListadoClientesRequestDto filtros,
            @Valid @ModelAttribute PageRequestDto pageRequest) {
        if (pageRequest.sortField() != null
                && !CAMPOS_ORDEN_PERMITIDOS.contains(pageRequest.sortField())) {
            throw new IllegalArgumentException(
                    "sortField inválido. Valores permitidos: " + CAMPOS_ORDEN_PERMITIDOS);
        }
        return ResponseEntity.ok(clienteService.getListadoClientes(filtros, pageRequest));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDto> getDetalleCliente(
            @PathVariable @Positive(message = "El id del cliente debe ser un número positivo") Long id) {
        return ResponseEntity.ok(clienteService.getDetalleCliente(id));
    }
}

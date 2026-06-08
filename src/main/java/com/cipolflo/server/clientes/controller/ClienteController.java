package com.cipolflo.server.clientes.controller;

import com.cipolflo.server.clientes.dto.*;
import com.cipolflo.server.clientes.service.IClienteService;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import com.cipolflo.server.clientes.dto.BusquedaCedulaResponseDto;
import jakarta.validation.Valid;
import com.cipolflo.server.clientes.utils.CedulaNormalizador;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/socios/{id}/baja")
    public ResponseEntity<Void> darDeBajaSocio(
            @PathVariable
            @Positive(message = "El id del socio debe ser un número positivo")
            Long id) {

        clienteService.darDeBajaSocio(id);
        return ResponseEntity.noContent().build();
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

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/socios")
    public ResponseEntity<ClienteResponseDto> registrarSocio(
            @Valid @RequestBody RegistroSocioRequestDto dto) {

        ClienteResponseDto response = clienteService.registrarSocio(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/cedula/{cedula}")
    public ResponseEntity<BusquedaCedulaResponseDto> buscarPorCedula(@PathVariable String cedula) {
        if(!CedulaNormalizador.esFormatoValido(cedula)) {
            return ResponseEntity.badRequest().build();
        }
        return clienteService.buscarPorCedula(cedula)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
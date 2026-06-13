package com.cipolflo.server.clientes.controller;

import com.cipolflo.server.clientes.dto.CuotaPendienteDto;
import com.cipolflo.server.clientes.dto.RegistroPagoCuotaRequestDto;
import com.cipolflo.server.clientes.service.PagoCuotaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
@Validated
public class PagoCuotaController {

    private final PagoCuotaService pagoCuotaService;

    public PagoCuotaController(PagoCuotaService pagoCuotaService) {
        this.pagoCuotaService = pagoCuotaService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/socios/{id}/pagos")
    public ResponseEntity<Void> registrarPago(
            @PathVariable @Positive(message = "El id del socio debe ser un número positivo") Long id,
            @Valid @RequestBody RegistroPagoCuotaRequestDto request
    ) {
        pagoCuotaService.registrarPago(id, request);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/socios/{id}/cuotas-pendientes")
    public ResponseEntity<List<CuotaPendienteDto>> obtenerCuotasPendientes(
            @PathVariable @Positive(message = "El id del socio debe ser un número positivo") Long id
    ) {
        return ResponseEntity.ok(pagoCuotaService.obtenerCuotasPendientes(id));
    }
}

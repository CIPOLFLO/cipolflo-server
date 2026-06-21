package com.cipolflo.server.clientes.controller;

import com.cipolflo.server.clientes.dto.PagoCuotaResponseDto;
import com.cipolflo.server.clientes.dto.RegistroPagoCuotaRequestDto;
import com.cipolflo.server.clientes.service.IPagoCuotaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/clientes/socios")
public class PagoCuotaController {

    private final IPagoCuotaService pagoCuotaService;

    public PagoCuotaController(IPagoCuotaService pagoCuotaService) {
        this.pagoCuotaService = pagoCuotaService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/pago-cuota")
    public ResponseEntity<List<PagoCuotaResponseDto>> registrarPago(
            @PathVariable @Positive(message = "El id del socio debe ser un número positivo") Long id,
            @Valid @RequestBody RegistroPagoCuotaRequestDto request
    ) {
        List<PagoCuotaResponseDto> response = pagoCuotaService.registrarPago(id, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}

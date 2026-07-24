package com.cipolflo.server.ajustes.controller;

import com.cipolflo.server.ajustes.dto.CostoCuotaRequestDto;
import com.cipolflo.server.ajustes.dto.CostoCuotaResponseDto;
import com.cipolflo.server.ajustes.service.ICostoCuotaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/ajustes/costo-cuota")
public class CostoCuotaController {

    private final ICostoCuotaService costoCuotaService;

    public CostoCuotaController(ICostoCuotaService costoCuotaService) {
        this.costoCuotaService = costoCuotaService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<CostoCuotaResponseDto> obtenerCostoCuota() {
        return ResponseEntity.ok(costoCuotaService.obtenerCostoCuota());
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping
    public ResponseEntity<CostoCuotaResponseDto> actualizarCostoCuota(
            @Valid @RequestBody CostoCuotaRequestDto dto) {
        return ResponseEntity.ok(costoCuotaService.actualizarCostoCuota(dto));
    }
}

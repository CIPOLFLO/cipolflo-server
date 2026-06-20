package com.cipolflo.server.finanzas.controller;

import com.cipolflo.server.finanzas.dto.FinanzaCrearRequestDto;
import com.cipolflo.server.finanzas.dto.FinanzaDetalleResponseDto;
import com.cipolflo.server.finanzas.dto.FinanzaResponseDto;
import com.cipolflo.server.finanzas.service.IFinanzaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/finanzas")
public class FinanzaController {

    private final IFinanzaService finanzaService;

    public FinanzaController(IFinanzaService finanzaService) {
        this.finanzaService = finanzaService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping()
    public ResponseEntity<FinanzaResponseDto> registrarFinanza(
            @Valid @RequestBody FinanzaCrearRequestDto request
    ) {
        FinanzaResponseDto response = finanzaService.registrarFinanza(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<FinanzaDetalleResponseDto> getDetalleFinanza(
            @PathVariable @Positive(message = "El id de la finanza debe ser un número positivo") Long id
    ) {
        return ResponseEntity.ok(finanzaService.getDetalleFinanza(id));
    }
}

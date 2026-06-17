package com.cipolflo.server.finanzas.controller;

import com.cipolflo.server.finanzas.dto.FinanzaCrearRequestDto;
import com.cipolflo.server.finanzas.dto.FinanzaResponseDto;
import com.cipolflo.server.finanzas.service.IFinanzaService;
import jakarta.validation.Valid;
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
    @PostMapping
    public ResponseEntity<FinanzaResponseDto> registrarFinanza(
            @Valid @RequestBody FinanzaCrearRequestDto request
    ) {
        FinanzaResponseDto response = finanzaService.registrarFinanza(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}

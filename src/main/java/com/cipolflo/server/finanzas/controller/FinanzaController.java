package com.cipolflo.server.finanzas.controller;

import com.cipolflo.server.finanzas.dto.*;
import com.cipolflo.server.finanzas.service.IFinanzaService;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.cipolflo.server.shared.export.ArchivoExportado;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Set;

@RestController
@Validated
@RequestMapping("/api/v1/finanzas")
public class FinanzaController {

    private final IFinanzaService finanzaService;

    private static final Set<String> CAMPOS_ORDEN_PERMITIDOS = Set.of("importe", "fecha");

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

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/export")
    public ResponseEntity<byte[]> exportarFinanzas(
            @RequestBody FinanzaExportRequestDto filters )
    { ArchivoExportado archivo = finanzaService.exportarFinanzas(filters);
        return ResponseEntity.ok() .
                header( HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + archivo.getNombre() + "\"" )
                .contentType(MediaType.parseMediaType( "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ))
                .body(archivo.getContenido());
    }
}


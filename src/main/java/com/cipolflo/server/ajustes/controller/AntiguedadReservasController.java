package com.cipolflo.server.ajustes.controller;

import com.cipolflo.server.ajustes.dto.AntiguedadReservasRequestDto;
import com.cipolflo.server.ajustes.dto.AntiguedadReservasResponseDto;
import com.cipolflo.server.ajustes.service.IAntiguedadReservasService;
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
@RequestMapping("/api/v1/ajustes/antiguedad-reservas")
public class AntiguedadReservasController {

    private final IAntiguedadReservasService antiguedadReservasService;

    public AntiguedadReservasController(IAntiguedadReservasService antiguedadReservasService) {
        this.antiguedadReservasService = antiguedadReservasService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<AntiguedadReservasResponseDto> obtenerAntiguedad() {
        return ResponseEntity.ok(antiguedadReservasService.obtenerAntiguedad());
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping
    public ResponseEntity<AntiguedadReservasResponseDto> actualizarAntiguedad(
            @Valid @RequestBody AntiguedadReservasRequestDto dto) {
        return ResponseEntity.ok(antiguedadReservasService.actualizarAntiguedad(dto));
    }
}

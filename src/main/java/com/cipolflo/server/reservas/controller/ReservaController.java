package com.cipolflo.server.reservas.controller;

import com.cipolflo.server.reservas.dto.ReservaCreacionRequestDto;
import com.cipolflo.server.reservas.dto.ReservaCreacionResponseDto;
import com.cipolflo.server.reservas.service.IReservaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/reservas")
public class ReservaController {

    private final IReservaService reservaService;

    public ReservaController(IReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<ReservaCreacionResponseDto> registrar(
            @Valid @RequestBody ReservaCreacionRequestDto dto) {

        ReservaCreacionResponseDto response = reservaService.registrar(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}

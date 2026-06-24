package com.cipolflo.server.reservas.controller;

import com.cipolflo.server.reservas.dto.ReservaCreacionRequestDto;
import com.cipolflo.server.reservas.dto.ReservaCreacionResponseDto;
import com.cipolflo.server.reservas.dto.ReservaDetalleResponseDto;
import com.cipolflo.server.reservas.service.IReservaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ReservaDetalleResponseDto> getDetalle(
            @PathVariable @Positive(message = "El id de la reserva debe ser un número positivo") Long id
    ) {
        return ResponseEntity.ok(reservaService.getDetalle(id));
    }
}

package com.cipolflo.server.reservas.controller;

import com.cipolflo.server.reservas.dto.RegistroPagoReservaRequestDto;
import com.cipolflo.server.reservas.service.IPagoReservaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@Validated
@RequestMapping("/api/v1/pago_reserva")
public class PagoReservaController {

    private final IPagoReservaService pagoReservaService;

    public PagoReservaController(IPagoReservaService pagoReservaService) {
        this.pagoReservaService = pagoReservaService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}")
    public ResponseEntity<Void> registrarPago(
            @PathVariable @Positive(message = "El id de la reserva debe ser un número positivo") Long id,
            @Valid @RequestBody RegistroPagoReservaRequestDto dto) {
        pagoReservaService.registrarPago(id, dto);
        return ResponseEntity.noContent().build();
    }

}


package com.cipolflo.server.reservas.controller;

import com.cipolflo.server.reservas.dto.*;
import com.cipolflo.server.reservas.service.IReservaService;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@Validated
@RequestMapping("/api/v1/reservas")
public class ReservaController {


    private static final Set<String> CAMPOS_ORDEN_PERMITIDOS = Set.of(
            "fechaEntrada", "fechaSalida", "nombreCliente"
    );

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

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<PageResponse<ListadoReservasResponseDto>> getListadoReservas(
            @Valid @ModelAttribute ListadoReservasRequestDto filtros,
            @Valid @ModelAttribute PageRequestDto pageRequest) {
        if (pageRequest.sortField() != null
                && !CAMPOS_ORDEN_PERMITIDOS.contains(pageRequest.sortField())) {
            throw new IllegalArgumentException(
                    "sortField inválido. Valores permitidos: " + CAMPOS_ORDEN_PERMITIDOS);
        }
        return ResponseEntity.ok(reservaService.getListadoReservas(filtros, pageRequest));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<ReservaModificacionResponseDto> modificar(
            @PathVariable @Positive(message = "El id de la reserva debe ser un número positivo") Long id,
            @Valid @RequestBody ReservaModificacionRequestDto dto) {
        return ResponseEntity.ok(reservaService.modificar(id, dto));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/calcular-costo")
    public ResponseEntity<CalculoCostoResponseDto> calcularCosto(
            @Valid @RequestBody CalculoCostoRequestDto dto) {
        return ResponseEntity.ok(reservaService.calcularCosto(dto));
    }
    @PreAuthorize("isAuthenticated()")
@PatchMapping("/{id}/documentacion")
public ResponseEntity<Void> confirmarDocumentacion(
        @PathVariable @Positive(message = "El id de la reserva debe ser un número positivo") Long id) {
    reservaService.confirmarDocumentacion(id);
    return ResponseEntity.noContent().build();
}
}

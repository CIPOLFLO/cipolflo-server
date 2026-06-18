package com.cipolflo.server.servicios.controller;

import com.cipolflo.server.servicios.dto.ModificacionServicioDto;
import com.cipolflo.server.servicios.dto.ServicioReservaOcupacionDto;
import com.cipolflo.server.servicios.dto.ReservaProximaResponseDto;
import com.cipolflo.server.servicios.dto.ServicioRegistroRequestDto;
import com.cipolflo.server.servicios.dto.ServicioRequestDto;
import com.cipolflo.server.servicios.dto.ListadoServiciosRequestDto;
import com.cipolflo.server.servicios.dto.ListadoServiciosResponseDto;
import com.cipolflo.server.servicios.dto.ServicioResponseDto;
import com.cipolflo.server.servicios.service.IServicioService;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@Validated
@RequestMapping("/api/v1/servicios")
public class ServicioController {

    private static final Set<String> CAMPOS_ORDEN_PERMITIDOS = Set.of(
            "nombre", "precioParticular", "precioSocio"
    );

    private final IServicioService servicioService;

    public ServicioController(IServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<PageResponse<ListadoServiciosResponseDto>> getListadoServicios(
            @Valid @ModelAttribute ListadoServiciosRequestDto filtros,
            @Valid @ModelAttribute PageRequestDto pageRequest) {
        if (pageRequest.sortField() != null
                && !CAMPOS_ORDEN_PERMITIDOS.contains(pageRequest.sortField())) {
            throw new IllegalArgumentException(
                    "sortField inválido. Valores permitidos: " + CAMPOS_ORDEN_PERMITIDOS);
        }
        return ResponseEntity.ok(servicioService.getListadoServicios(filtros, pageRequest));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ServicioResponseDto> getDetalleServicio(
            @PathVariable @Positive(message = "El id del servicio debe ser un número positivo") Long id) {
        ServicioResponseDto response = servicioService.getDetalleServicio(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/habilitacion")
    public ResponseEntity<ServicioResponseDto> cambiarHabilitacionServicio(
            @PathVariable @Positive(message = "El id del servicio debe ser un número positivo") Long id,
            @Valid @RequestBody ServicioRequestDto request) {
        ServicioResponseDto response = servicioService.cambiarHabilitacionServicio(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<ServicioResponseDto> modificarServicio(
            @PathVariable @Positive(message = "El id del servicio debe ser un número positivo") Long id,
            @Valid @RequestBody ModificacionServicioDto dto) {
        ServicioResponseDto response = servicioService.modificarServicio(id, dto);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/reservas-proximas")
    public ResponseEntity<List<ReservaProximaResponseDto>> getReservasProximas(
            @PathVariable
            @Positive(message = "El id del servicio debe ser un número positivo")
            Long id) {
        List<ReservaProximaResponseDto> response =
                servicioService.getReservasProximas(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/fechas-ocupadas")
    public ResponseEntity<List<ServicioReservaOcupacionDto>> getFechasOcupadas(
            @PathVariable
            @Positive(message = "El id del servicio debe ser un número positivo")
            Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        List<ServicioReservaOcupacionDto> response =
                servicioService.getFechasOcupadas(id, desde, hasta);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<ServicioResponseDto> registrarServicio(
            @Valid @RequestBody ServicioRegistroRequestDto request) {
        ServicioResponseDto response = servicioService.registrarServicio(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

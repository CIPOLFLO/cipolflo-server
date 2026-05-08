package com.cipolflo.server.servicios.controller;

import com.cipolflo.server.servicios.dto.ServicioResponseDto;
import com.cipolflo.server.servicios.service.IServicioService;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/servicios")
public class ServicioController {

    private final IServicioService servicioService;

    public ServicioController(IServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ServicioResponseDto> getDetalleServicio(@PathVariable @Positive(message = "El id del servicio debe ser un número positivo") Long id){
        ServicioResponseDto response = servicioService.getDetalleServicio(id);
        return ResponseEntity.ok(response);
    }
}
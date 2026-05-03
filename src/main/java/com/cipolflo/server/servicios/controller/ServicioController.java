package com.cipolflo.server.servicios.controller;

import com.cipolflo.server.servicios.dto.ServicioResponseDto;
import com.cipolflo.server.servicios.service.IServicioService;
import com.cipolflo.server.servicios.service.ServicioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/servicios")
public class ServicioController {

    private final IServicioService servicioService;

    public ServicioController(IServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicioResponseDto> getDetalleServicio(@PathVariable Long id){
        ServicioResponseDto response = servicioService.getDetalleServicio(id);
        return ResponseEntity.ok(response);
}
}
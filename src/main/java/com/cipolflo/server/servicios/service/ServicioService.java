package com.cipolflo.server.servicios.service;

import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.dto.ServicioResponseDto;
import com.cipolflo.server.servicios.repository.ServicioRepository;
import org.springframework.stereotype.Service;

@Service
public class ServicioService implements IServicioService {
    private final ServicioRepository servicioRepository;

    public ServicioService(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    public ServicioResponseDto getDetalleServicio(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El id del servicio debe ser válido");
        }

        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        ServicioResponseDto servicioResponseDto = new ServicioResponseDto(
                servicio.getId(),
                servicio.getNombre(),
                servicio.getProcedencia(),
                servicio.getCantidad(),
                servicio.getPrecioSocio(),
                servicio.getPrecioParticular(),
                servicio.getCapacidad(),
                servicio.getHabilitado(),
                servicio.getModalidadPrecio()
        );
        return servicioResponseDto;

    }
}

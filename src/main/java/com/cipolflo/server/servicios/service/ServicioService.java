package com.cipolflo.server.servicios.service;

import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.dto.ServicioResponseDto;
import com.cipolflo.server.servicios.repository.ServicioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ServicioService implements IServicioService {
    private final ServicioRepository servicioRepository;

    public ServicioService(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    public ServicioResponseDto getDetalleServicio(Long id) {

        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado"));

        return new ServicioResponseDto(
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

    }
}

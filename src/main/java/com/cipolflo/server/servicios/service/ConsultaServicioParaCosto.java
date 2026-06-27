package com.cipolflo.server.servicios.service;

import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.exception.ServicioNotFoundException;
import com.cipolflo.server.servicios.repository.ServicioRepository;
import org.springframework.stereotype.Service;

@Service
public class ConsultaServicioParaCosto implements IConsultaServicioParaCosto {

    private final ServicioRepository servicioRepository;

    public ConsultaServicioParaCosto(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    @Override
    public Servicio obtenerServicio(Long id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new ServicioNotFoundException(id));
    }
}

package com.cipolflo.server.servicios.service;

import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.domain.TarifaServicio;
import com.cipolflo.server.servicios.exception.ServicioNotFoundException;
import com.cipolflo.server.servicios.repository.ServicioRepository;
import com.cipolflo.server.servicios.repository.TarifaServicioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsultaServicioParaCosto implements IConsultaServicioParaCosto {

    private final ServicioRepository servicioRepository;
    private final TarifaServicioRepository tarifaServicioRepository;

    public ConsultaServicioParaCosto(
            ServicioRepository servicioRepository,
            TarifaServicioRepository tarifaServicioRepository
    ) {
        this.servicioRepository = servicioRepository;
        this.tarifaServicioRepository = tarifaServicioRepository;
    }

    @Override
    public Servicio obtenerServicio(Long id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new ServicioNotFoundException(id));
    }

    @Override
    public List<TarifaServicio> obtenerTarifas(Long servicioId) {
        return tarifaServicioRepository.findByServicioId(servicioId);
    }
}

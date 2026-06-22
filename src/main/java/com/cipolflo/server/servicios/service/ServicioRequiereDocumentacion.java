package com.cipolflo.server.servicios.service;

import com.cipolflo.server.servicios.repository.ServicioRepository;
import org.springframework.stereotype.Service;

@Service
public class ServicioRequiereDocumentacion implements IServicioRequiereDocumentacion{
    private final ServicioRepository servicioRepository;

    public ServicioRequiereDocumentacion(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    @Override
    public Boolean requiereDocumentacion(Long id) {
        return servicioRepository.existsByIdAndRequiereDocumentacionTrue(id);
    }
}

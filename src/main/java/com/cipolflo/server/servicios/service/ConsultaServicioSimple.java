package com.cipolflo.server.servicios.service;

import com.cipolflo.server.reservas.dto.ServicioDetalleReservaDto;
import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.exception.ServicioNotFoundException;
import com.cipolflo.server.servicios.mapper.ServicioMapper;
import com.cipolflo.server.servicios.repository.ServicioRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConsultaServicioSimple implements IConsultaServicioSimple{

    ServicioRepository servicioRepository;

    public ConsultaServicioSimple(
            ServicioRepository servicioRepository
    ){
        this.servicioRepository = servicioRepository;
    }

    @Override
    public ServicioDetalleReservaDto getDetalleServicioSimple(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ServicioNotFoundException(id));
        return ServicioMapper.toServicioDetalleSimple(servicio);
    }

    @Override
    public Map<Long, String> getNombresByIds(Collection<Long> ids) {
        return servicioRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Servicio::getId, Servicio::getNombre));
    }
}

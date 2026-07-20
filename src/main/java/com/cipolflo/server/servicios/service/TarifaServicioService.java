package com.cipolflo.server.servicios.service;

import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.domain.TarifaServicio;
import com.cipolflo.server.servicios.dto.TarifaServicioRequestDto;
import com.cipolflo.server.servicios.dto.TarifaServicioResponseDto;
import com.cipolflo.server.servicios.exception.TarifaServicioNotFoundException;
import com.cipolflo.server.servicios.mapper.TarifaServicioMapper;
import com.cipolflo.server.servicios.repository.TarifaServicioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TarifaServicioService {

    private final TarifaServicioRepository tarifaServicioRepository;

    public TarifaServicioService(
            TarifaServicioRepository tarifaServicioRepository
    ) {
        this.tarifaServicioRepository = tarifaServicioRepository;
    }

    public List<TarifaServicio> registrarTarifas(
            Servicio servicio,
            List<TarifaServicioRequestDto> tarifasDto
    ) {
        List<TarifaServicio> tarifas = tarifasDto.stream()
                .map(dto -> TarifaServicioMapper.toEntity(servicio, dto))
                .toList();

        return tarifaServicioRepository.saveAll(tarifas);
    }

    public List<TarifaServicio> modificarTarifas(
            Servicio servicio,
            List<TarifaServicioRequestDto> tarifasDto
    ) {
        List<TarifaServicio> tarifasExistentes =
                tarifaServicioRepository.findByServicioId(servicio.getId());

        Map<Long, TarifaServicio> tarifasPorId = tarifasExistentes.stream()
                .collect(Collectors.toMap(
                        TarifaServicio::getId,
                        Function.identity()
                ));

        List<TarifaServicio> tarifasActualizadas = tarifasDto.stream()
                .map(dto -> obtenerOCrearTarifa(
                        servicio,
                        dto,
                        tarifasPorId
                ))
                .toList();

        return tarifaServicioRepository.saveAll(tarifasActualizadas);
    }

    public List<TarifaServicioResponseDto> obtenerTarifasPorServicio(
            Long servicioId
    ) {
        return tarifaServicioRepository.findByServicioId(servicioId)
                .stream()
                .map(TarifaServicioMapper::toResponseDto)
                .toList();
    }

    private TarifaServicio obtenerOCrearTarifa(
            Servicio servicio,
            TarifaServicioRequestDto dto,
            Map<Long, TarifaServicio> tarifasPorId
    ) {
        if (dto.getId() == null) {
            return TarifaServicioMapper.toEntity(servicio, dto);
        }

        TarifaServicio tarifa = tarifasPorId.get(dto.getId());

        if (tarifa == null) {
            throw new TarifaServicioNotFoundException(
                    dto.getId(),
                    servicio.getId()
            );
        }

        tarifa.modificar(
                dto.getTipoCliente(),
                dto.getPrecio(),
                dto.getModalidadPrecio(),
                dto.getAntiguedadMinima(),
                dto.getAntiguedadMaxima()
        );

        return tarifa;
    }
}
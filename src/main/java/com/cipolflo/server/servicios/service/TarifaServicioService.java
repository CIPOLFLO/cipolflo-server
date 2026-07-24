package com.cipolflo.server.servicios.service;

import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.domain.TarifaServicio;
import com.cipolflo.server.servicios.dto.TarifaServicioRequestDto;
import com.cipolflo.server.servicios.dto.TarifaServicioResponseDto;
import com.cipolflo.server.servicios.exception.ServicioValidacionException;
import com.cipolflo.server.servicios.exception.TarifaServicioNotFoundException;
import com.cipolflo.server.servicios.mapper.TarifaServicioMapper;
import com.cipolflo.server.servicios.repository.TarifaServicioRepository;
import com.cipolflo.server.servicios.validator.TarifaServicioReglasValidator;
import com.cipolflo.server.shared.exception.ServicioCodigoError;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TarifaServicioService {

    private final TarifaServicioRepository tarifaServicioRepository;
    private final TarifaServicioReglasValidator tarifaServicioReglasValidator;

    public TarifaServicioService(
            TarifaServicioRepository tarifaServicioRepository,
            TarifaServicioReglasValidator tarifaServicioReglasValidator
    ) {
        this.tarifaServicioRepository = tarifaServicioRepository;
        this.tarifaServicioReglasValidator = tarifaServicioReglasValidator;
    }

    public List<TarifaServicioResponseDto> registrarTarifas(
            Servicio servicio,
            List<TarifaServicioRequestDto> tarifasDto
    ) {
        List<TarifaServicio> tarifas = tarifasDto.stream()
                .map(dto -> TarifaServicioMapper.toEntity(servicio, dto))
                .toList();

        tarifaServicioReglasValidator.validar(tarifas);

        List<TarifaServicio> tarifasGuardadas = tarifaServicioRepository.saveAll(tarifas);

        return TarifaServicioMapper.toResponseDtoList(tarifasGuardadas);
    }

    public List<TarifaServicioResponseDto> modificarTarifas(
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

        List<Long> idsEnviados = tarifasDto.stream()
                .map(TarifaServicioRequestDto::getId)
                .filter(Objects::nonNull)
                .toList();

        List<TarifaServicio> tarifasActualizadas = tarifasDto.stream()
                .map(dto -> obtenerOCrearTarifa(
                        servicio,
                        dto,
                        tarifasPorId
                ))
                .toList();

        List<TarifaServicio> tarifasNoTocadas = tarifasExistentes.stream()
                .filter(tarifa -> !idsEnviados.contains(tarifa.getId()))
                .toList();

        List<TarifaServicio> estadoResultante = new ArrayList<>(tarifasNoTocadas);
        estadoResultante.addAll(tarifasActualizadas);

        tarifaServicioReglasValidator.validar(estadoResultante);

        tarifaServicioRepository.saveAll(tarifasActualizadas);

        return TarifaServicioMapper.toResponseDtoList(estadoResultante);
    }

    public List<TarifaServicioResponseDto> obtenerTarifasPorServicio(
            Long servicioId
    ) {
        return TarifaServicioMapper.toResponseDtoList(
                tarifaServicioRepository.findByServicioId(servicioId)
        );
    }

    public void eliminarTarifa(Long servicioId, Long tarifaId) {
        TarifaServicio tarifa = tarifaServicioRepository
                .findByIdAndServicioId(tarifaId, servicioId)
                .orElseThrow(() -> new TarifaServicioNotFoundException(tarifaId, servicioId));

        List<TarifaServicio> tarifasRestantes = tarifaServicioRepository.findByServicioId(servicioId)
                .stream()
                .filter(existente -> !existente.getId().equals(tarifaId))
                .toList();

        if (!tarifaServicioReglasValidator.cumpleTarifasObligatorias(tarifasRestantes)) {
            throw new ServicioValidacionException(
                    ServicioCodigoError.TARIFA_OBLIGATORIA_NO_ELIMINABLE.name(),
                    "No es posible eliminar la única tarifa de tipo " + tarifa.getTipoCliente()
                            + " del servicio"
            );
        }

        tarifaServicioRepository.delete(tarifa);
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

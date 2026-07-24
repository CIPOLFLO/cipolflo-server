package com.cipolflo.server.ajustes.service;

import com.cipolflo.server.ajustes.domain.CostoCuotaSocio;
import com.cipolflo.server.ajustes.dto.CostoCuotaRequestDto;
import com.cipolflo.server.ajustes.dto.CostoCuotaResponseDto;
import com.cipolflo.server.ajustes.mapper.CostoCuotaMapper;
import com.cipolflo.server.ajustes.repository.CostoCuotaSocioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CostoCuotaService implements ICostoCuotaService {

    private final CostoCuotaSocioRepository costoCuotaSocioRepository;

    public CostoCuotaService(CostoCuotaSocioRepository costoCuotaSocioRepository) {
        this.costoCuotaSocioRepository = costoCuotaSocioRepository;
    }

    @Override
    public CostoCuotaResponseDto obtenerCostoCuota() {
        return CostoCuotaMapper.toResponseDto(buscarFilaUnica());
    }

    @Override
    @Transactional
    public CostoCuotaResponseDto actualizarCostoCuota(CostoCuotaRequestDto dto) {
        CostoCuotaSocio costoCuota = buscarFilaUnica();
        costoCuota.actualizarMonto(dto.monto());
        return CostoCuotaMapper.toResponseDto(costoCuotaSocioRepository.save(costoCuota));
    }

    private CostoCuotaSocio buscarFilaUnica() {
        return costoCuotaSocioRepository.findById(CostoCuotaSocio.ID_FIJO)
                .orElseThrow(() -> new IllegalStateException(
                        "Falta la fila de costo_cuota_socio (id=" + CostoCuotaSocio.ID_FIJO
                                + "); revisar la migración de siembra."));
    }
}

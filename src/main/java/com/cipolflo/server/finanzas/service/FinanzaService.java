package com.cipolflo.server.finanzas.service;

import com.cipolflo.server.finanzas.domain.Egreso;
import com.cipolflo.server.finanzas.domain.Finanza;
import com.cipolflo.server.finanzas.domain.Ingreso;
import com.cipolflo.server.finanzas.domain.enums.TipoMovimiento;
import com.cipolflo.server.finanzas.dto.FinanzaCrearRequestDto;
import com.cipolflo.server.finanzas.dto.FinanzaDetalleResponseDto;
import com.cipolflo.server.finanzas.dto.FinanzaResponseDto;
import com.cipolflo.server.finanzas.exception.FinanzaNotFoundException;
import com.cipolflo.server.finanzas.mapper.FinanzaMapper;
import com.cipolflo.server.finanzas.repository.FinanzaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class FinanzaService implements IFinanzaService {

    private final FinanzaRepository finanzaRepository;

    public FinanzaService(FinanzaRepository finanzaRepository) {
        this.finanzaRepository = finanzaRepository;
    }

    @Override
    @Transactional
    public FinanzaResponseDto registrarFinanza(FinanzaCrearRequestDto dto) {
        LocalDate fecha = dto.getFecha() != null
                ? dto.getFecha()
                : LocalDate.now();

        Finanza finanza = dto.getTipoMovimiento() == TipoMovimiento.INGRESO
                ? Ingreso.crearManual(
                fecha,
                dto.getImporte(),
                dto.getConcepto(),
                dto.getFormaPago(),
                dto.getProcedencia(),
                dto.getNotas()
        )
                : Egreso.crearManual(
                fecha,
                dto.getImporte(),
                dto.getConcepto(),
                dto.getFormaPago(),
                dto.getProcedencia(),
                dto.getNotas()
        );

        return FinanzaMapper.toResponseDto(finanzaRepository.save(finanza));
    }
    @Override
    public FinanzaDetalleResponseDto getDetalleFinanza(Long id) {
        Finanza finanza = finanzaRepository.findById(id)
                .orElseThrow(() -> new FinanzaNotFoundException(id));

        return FinanzaMapper.toDetalleResponseDto(finanza);
    }
}
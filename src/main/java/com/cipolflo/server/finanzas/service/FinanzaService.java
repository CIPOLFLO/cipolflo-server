package com.cipolflo.server.finanzas.service;

import com.cipolflo.server.finanzas.domain.Egreso;
import com.cipolflo.server.finanzas.domain.Finanza;
import com.cipolflo.server.finanzas.domain.Ingreso;
import com.cipolflo.server.finanzas.domain.enums.TipoMovimiento;
import com.cipolflo.server.finanzas.dto.FinanzaCrearRequestDto;
import com.cipolflo.server.finanzas.dto.FinanzaDetalleResponseDto;
import com.cipolflo.server.finanzas.dto.FinanzaExportRequestDto;
import com.cipolflo.server.finanzas.dto.FinanzaResponseDto;
import com.cipolflo.server.finanzas.exception.FinanzaNotFoundException;
import com.cipolflo.server.finanzas.mapper.FinanzaMapper;
import com.cipolflo.server.finanzas.repository.FinanzaRepository;
import com.cipolflo.server.finanzas.repository.FinanzaSpecification;
import com.cipolflo.server.shared.export.ArchivoExportado;
import com.cipolflo.server.shared.export.IExportService;
import com.cipolflo.server.shared.export.NombreArchivoExport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class FinanzaService implements IFinanzaService {

    private final FinanzaRepository finanzaRepository;
    private final IExportService exportService;

    public FinanzaService(FinanzaRepository finanzaRepository,
                          IExportService exportService) {
        this.finanzaRepository = finanzaRepository;
        this.exportService = exportService;
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

    @Override
    public ArchivoExportado exportarFinanzas(FinanzaExportRequestDto filtros) {

        List<Finanza> finanzas = finanzaRepository.findAll(
                FinanzaSpecification.desdeFiltros(
                        filtros.getFechaDesde(),
                        filtros.getFechaHasta(),
                        filtros.getConcepto(),
                        filtros.getTipoMovimiento()
                )
        );

        List<String> encabezados = List.of(
                "Tipo Movimiento",
                "Procedencia",
                "Concepto",
                "Fecha",
                "Importe",
                "Forma Pago",
                "Notas"
        );

        List<List<String>> filas = finanzas.stream()
                .map(finanza -> List.of(
                        finanza.getTipoMovimiento().name(),
                        finanza.getProcedencia().name(),
                        finanza.getConcepto().name(),
                        finanza.getFecha().toString(),
                        finanza.getImporte().toString(),
                        finanza.getFormaPago().name(),
                        finanza.getNotas() != null ? finanza.getNotas() : ""
                ))
                .toList();

        byte[] contenido = exportService.generarExcel(
                "Finanzas",
                encabezados,
                filas,
                new int[]{
                        6000,
                        5000,
                        6000,
                        4000,
                        4000,
                        5000,
                        12000
                }
        );

        return new ArchivoExportado(
                NombreArchivoExport.generar("finanzas"),
                contenido
        );
    }
}
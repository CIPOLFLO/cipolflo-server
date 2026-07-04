package com.cipolflo.server.finanzas.service;

import com.cipolflo.server.finanzas.domain.Egreso;
import com.cipolflo.server.finanzas.domain.Finanza;
import com.cipolflo.server.finanzas.domain.Ingreso;
import com.cipolflo.server.finanzas.domain.enums.TipoMovimiento;
import com.cipolflo.server.finanzas.dto.*;
import com.cipolflo.server.finanzas.exception.FinanzaNotFoundException;
import com.cipolflo.server.finanzas.mapper.FinanzaMapper;
import com.cipolflo.server.finanzas.repository.FinanzaRepository;
import com.cipolflo.server.finanzas.repository.FinanzaSpecification;
import com.cipolflo.server.shared.export.*;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import com.cipolflo.server.shared.pagination.PaginationMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class FinanzaService implements IFinanzaService {

    private final FinanzaRepository finanzaRepository;
    private final IExportService exportService;
    private final ExportProperties exportProperties;

    public FinanzaService(FinanzaRepository finanzaRepository,
                          IExportService exportService,
                          ExportProperties exportProperties) {
        this.finanzaRepository = finanzaRepository;
        this.exportService = exportService;
        this.exportProperties = exportProperties;
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
    public ArchivoExportado exportarFinanzas(ListadoFinanzasRequestDto filtros) {

        List<Finanza> finanzas = finanzaRepository.findAll(
                FinanzaSpecification.desdeFiltros(
                        filtros.fechaDesde(),
                        filtros.fechaHasta(),
                        filtros.concepto(),
                        filtros.tipoMovimiento()
                )
        );
        if (finanzas.isEmpty()) {
            throw new ExportacionException("No hay registros que coincidan con los filtros aplicados");
        }

        if (finanzas.size() > exportProperties.maxFilas()) {
            throw new ExportacionException(
                    "La exportación supera el límite de " + exportProperties.maxFilas() + " filas"
            );
        }

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

    @Override
    public PageResponse<ListadoFinanzasResponseDto> getListadoFinanzas(
            ListadoFinanzasRequestDto filtros,
            PageRequestDto pageRequest
    ) {
        Specification<Finanza> spec = FinanzaSpecification.desdeFiltros(
                filtros.fechaDesde(),
                filtros.fechaHasta(),
                filtros.concepto(),
                filtros.tipoMovimiento()
        );

        Page<ListadoFinanzasResponseDto> page = finanzaRepository
                .findAll(spec, pageRequest.toPageable())
                .map(FinanzaMapper::toListadoResponseDto);

        return PaginationMapper.toPageResponse(page);
    }

    @Override
    public void registrarPagoReserva(FinanzaCrearRequestDto dto) {
        Finanza finanza = Ingreso.crearDesdeReserva(
                dto.getFecha(),
                dto.getImporte(),
                dto.getFormaPago(),
                dto.getProcedencia(),
                dto.getNotas(),
                dto.getReservaId()
        );
        FinanzaMapper.toResponseDto(finanzaRepository.save(finanza));
    }

    @Transactional
    public void eliminarFinanza(Long id) {
      Finanza finanza = finanzaRepository.findById(id)
      .orElseThrow(() -> new FinanzaNotFoundException(id));
      finanzaRepository.delete(finanza);
    }

    @Override
    public void registrarPagoCuota(FinanzaCrearRequestDto dto) {
        Finanza finanza = Ingreso.crearDesdePagoCuota(
                dto.getFecha(),
                dto.getImporte(),
                dto.getFormaPago(),
                dto.getProcedencia(),
                dto.getNotas(),
                dto.getPagoCuotaId()
        );

        FinanzaMapper.toResponseDto(finanzaRepository.save(finanza));
    }
}
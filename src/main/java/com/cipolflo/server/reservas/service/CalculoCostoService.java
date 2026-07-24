package com.cipolflo.server.reservas.service;

import com.cipolflo.server.servicios.domain.TarifaServicio;
import com.cipolflo.server.reservas.dto.CalculoCostoRequestDto;
import com.cipolflo.server.reservas.dto.CalculoCostoResponseDto;
import com.cipolflo.server.reservas.validators.CalculoCostoValidator;
import com.cipolflo.server.servicios.costo.CalculoCostoParams;
import com.cipolflo.server.servicios.costo.EstrategiaCosto;
import com.cipolflo.server.servicios.costo.FabricaEstrategia;
import com.cipolflo.server.servicios.costo.ResolutorTarifaAplicable;
import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.service.IConsultaServicioParaCosto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

@Service
public class CalculoCostoService implements ICalculoCostoService {

    private final IConsultaServicioParaCosto consultaServicio;
    private final FabricaEstrategia fabricaEstrategia;
    private final CalculoCostoValidator calculoCostoValidator;
    private final ResolutorTarifaAplicable resolutorTarifaAplicable;

    public CalculoCostoService(IConsultaServicioParaCosto consultaServicio,
                               ResolutorTarifaAplicable resolutorTarifaAplicable,
                               FabricaEstrategia fabricaEstrategia,
                               CalculoCostoValidator calculoCostoValidator) {
        this.consultaServicio = consultaServicio;
        this.fabricaEstrategia = fabricaEstrategia;
        this.calculoCostoValidator = calculoCostoValidator;
        this.resolutorTarifaAplicable = resolutorTarifaAplicable;
    }

    @Override
    public CalculoCostoResponseDto calcularCosto(CalculoCostoRequestDto request) {
        TarifaServicio tarifa = resolutorTarifaAplicable.resolver(
                request.getServicioId(),
                request.getClienteId()
        );

        return calcularCosto(request, tarifa);
    }

    @Override
    public CalculoCostoResponseDto calcularCosto(CalculoCostoRequestDto request, TarifaServicio tarifa) {
        Servicio servicio = consultaServicio.obtenerServicio(request.getServicioId());

        calculoCostoValidator.validar(
                request,
                tarifa.getModalidadPrecio()
        );

        long numeroDias =
                request.getFechaFin().toEpochDay()
                        - request.getFechaInicio().toEpochDay()
                        + 1;

        long numeroHoras =
                request.getHoraInicio() != null
                        && request.getHoraFin() != null
                        ? request.getHoraInicio().until(
                        request.getHoraFin(),
                        ChronoUnit.HOURS
                )
                        : 0;

        CalculoCostoParams params = new CalculoCostoParams(
                servicio,
                tarifa.getPrecio(),
                request.getCantidadTotal(),
                request.getCantidad(),
                request.getCantidadMenores(),
                numeroDias,
                numeroHoras
        );

        EstrategiaCosto estrategia =
                fabricaEstrategia.obtener(tarifa.getModalidadPrecio());

        BigDecimal costoTotal = estrategia.calcular(params);

        return new CalculoCostoResponseDto(costoTotal);
    }
}

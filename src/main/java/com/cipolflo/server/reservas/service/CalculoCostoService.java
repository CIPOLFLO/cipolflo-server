package com.cipolflo.server.reservas.service;

import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.reservas.dto.CalculoCostoRequestDto;
import com.cipolflo.server.reservas.dto.CalculoCostoResponseDto;
import com.cipolflo.server.reservas.validators.CalculoCostoValidator;
import com.cipolflo.server.servicios.costo.CalculoCostoParams;
import com.cipolflo.server.servicios.costo.EstrategiaCosto;
import com.cipolflo.server.servicios.costo.FabricaEstrategia;
import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.service.IConsultaServicioParaCosto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CalculoCostoService implements ICalculoCostoService {

    private final IConsultaServicioParaCosto consultaServicio;
    private final FabricaEstrategia fabricaEstrategia;
    private final CalculoCostoValidator calculoCostoValidator;

    public CalculoCostoService(IConsultaServicioParaCosto consultaServicio,
                               FabricaEstrategia fabricaEstrategia,
                               CalculoCostoValidator calculoCostoValidator) {
        this.consultaServicio = consultaServicio;
        this.fabricaEstrategia = fabricaEstrategia;
        this.calculoCostoValidator = calculoCostoValidator;
    }

    @Override
    public CalculoCostoResponseDto calcularCosto(CalculoCostoRequestDto request) {
        Servicio servicio = consultaServicio.obtenerServicio(request.getServicioId());

        calculoCostoValidator.validar(request, servicio.getModalidadPrecio());

        TipoCliente tipoCliente = request.getTipoCliente() != null
                ? request.getTipoCliente()
                : TipoCliente.PARTICULAR;

        long numeroDias = request.getFechaFin().toEpochDay() - request.getFechaInicio().toEpochDay() + 1;
        long numeroHoras = request.getHoraInicio() != null && request.getHoraFin() != null
                ? request.getHoraInicio().until(request.getHoraFin(), java.time.temporal.ChronoUnit.HOURS)
                : 0;

        CalculoCostoParams params = new CalculoCostoParams(
                servicio,
                tipoCliente,
                request.getCantidadTotal(),
                request.getCantidad(),
                request.getCantidadMenores(),
                numeroDias,
                numeroHoras
        );

        EstrategiaCosto estrategia = fabricaEstrategia.obtener(servicio.getModalidadPrecio());
        BigDecimal costoTotal = estrategia.calcular(params);

        return new CalculoCostoResponseDto(costoTotal);
    }
}

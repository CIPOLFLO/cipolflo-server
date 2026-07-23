package com.cipolflo.server.reservas.service;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.service.IConsultaClienteParaCosto;
import com.cipolflo.server.servicios.domain.TarifaServicio;
import com.cipolflo.server.reservas.dto.CalculoCostoRequestDto;
import com.cipolflo.server.reservas.dto.CalculoCostoResponseDto;
import com.cipolflo.server.reservas.validators.CalculoCostoValidator;
import com.cipolflo.server.servicios.costo.CalculoCostoParams;
import com.cipolflo.server.servicios.costo.EstrategiaCosto;
import com.cipolflo.server.servicios.costo.FabricaEstrategia;
import com.cipolflo.server.servicios.costo.ResolutorTarifaServicio;
import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.domain.enums.TipoClienteTarifa;
import com.cipolflo.server.servicios.mapper.TipoClienteTarifaMapper;
import com.cipolflo.server.servicios.service.IConsultaServicioParaCosto;
import com.cipolflo.server.shared.ZonaHoraria;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class CalculoCostoService implements ICalculoCostoService {

    private final IConsultaServicioParaCosto consultaServicio;
    private final FabricaEstrategia fabricaEstrategia;
    private final CalculoCostoValidator calculoCostoValidator;
    private final ResolutorTarifaServicio resolutorTarifaServicio;
    private final IConsultaClienteParaCosto consultaCliente;

    public CalculoCostoService(IConsultaServicioParaCosto consultaServicio,
                               IConsultaClienteParaCosto consultaCliente,
                               FabricaEstrategia fabricaEstrategia,
                               ResolutorTarifaServicio resolutorTarifaServicio,
                               CalculoCostoValidator calculoCostoValidator) {
        this.consultaServicio = consultaServicio;
        this.fabricaEstrategia = fabricaEstrategia;
        this.calculoCostoValidator = calculoCostoValidator;
        this.resolutorTarifaServicio = resolutorTarifaServicio;
        this.consultaCliente = consultaCliente;
    }

    @Override
    public CalculoCostoResponseDto calcularCosto(CalculoCostoRequestDto request) {
        Servicio servicio = consultaServicio.obtenerServicio(request.getServicioId());

        TipoClienteTarifa tipoClienteTarifa;
        Integer antiguedadEnAnios = null;

        if (request.getClienteId() == null) {
            tipoClienteTarifa = TipoClienteTarifa.PARTICULAR;
        } else {
            Cliente cliente = consultaCliente.obtenerCliente(request.getClienteId());

            tipoClienteTarifa = TipoClienteTarifaMapper.desdeCliente(cliente);

            if (cliente instanceof Socio socio) {
                antiguedadEnAnios = socio.calcularAntiguedadEnAnios(
                        LocalDate.now(ZonaHoraria.URUGUAY)
                );
            }
        }

        List<TarifaServicio> tarifas =
                consultaServicio.obtenerTarifas(servicio.getId());

        TarifaServicio tarifa = resolutorTarifaServicio.resolver(
                tarifas,
                tipoClienteTarifa,
                antiguedadEnAnios
        );

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

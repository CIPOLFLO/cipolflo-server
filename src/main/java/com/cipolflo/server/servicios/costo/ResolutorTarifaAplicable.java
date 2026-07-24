package com.cipolflo.server.servicios.costo;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.service.IConsultaClienteParaCosto;
import com.cipolflo.server.servicios.domain.TarifaServicio;
import com.cipolflo.server.servicios.domain.enums.TipoClienteTarifa;
import com.cipolflo.server.servicios.mapper.TipoClienteTarifaMapper;
import com.cipolflo.server.servicios.service.IConsultaServicioParaCosto;
import com.cipolflo.server.shared.ZonaHoraria;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Resuelve la TarifaServicio aplicable a un servicio + cliente, a partir del clienteId
 * (null se trata siempre como PARTICULAR sin antigüedad). Único punto donde se traduce
 * un clienteId en tipo de tarifa + antigüedad, para que CalculoCostoService y
 * ReservaCreacionValidator no reimplementen la misma resolución por separado.
 */
@Component
public class ResolutorTarifaAplicable {

    private final IConsultaServicioParaCosto consultaServicio;
    private final IConsultaClienteParaCosto consultaCliente;
    private final ResolutorTarifaServicio resolutorTarifaServicio;

    public ResolutorTarifaAplicable(
            IConsultaServicioParaCosto consultaServicio,
            IConsultaClienteParaCosto consultaCliente,
            ResolutorTarifaServicio resolutorTarifaServicio
    ) {
        this.consultaServicio = consultaServicio;
        this.consultaCliente = consultaCliente;
        this.resolutorTarifaServicio = resolutorTarifaServicio;
    }

    public TarifaServicio resolver(Long servicioId, Long clienteId) {
        TipoClienteTarifa tipoClienteTarifa;
        Integer antiguedadEnAnios = null;

        if (clienteId == null) {
            tipoClienteTarifa = TipoClienteTarifa.PARTICULAR;
        } else {
            Cliente cliente = consultaCliente.obtenerCliente(clienteId);

            tipoClienteTarifa = TipoClienteTarifaMapper.desdeCliente(cliente);

            if (cliente instanceof Socio socio) {
                antiguedadEnAnios = socio.calcularAntiguedadEnAnios(
                        LocalDate.now(ZonaHoraria.URUGUAY)
                );
            }
        }

        List<TarifaServicio> tarifas = consultaServicio.obtenerTarifas(servicioId);

        return resolutorTarifaServicio.resolver(tarifas, tipoClienteTarifa, antiguedadEnAnios);
    }
}

package com.cipolflo.server.servicios.costo;

import com.cipolflo.server.servicios.domain.TarifaServicio;
import com.cipolflo.server.servicios.domain.enums.TipoClienteTarifa;
import com.cipolflo.server.servicios.exception.ServicioValidacionException;
import com.cipolflo.server.shared.exception.ServicioCodigoError;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ResolutorTarifaServicio {

    public TarifaServicio resolver(
            List<TarifaServicio> tarifas,
            TipoClienteTarifa tipoCliente,
            Integer antiguedadEnAnios
    ) {

        Optional<TarifaServicio> tarifa = buscarTarifa(
                tarifas,
                tipoCliente,
                antiguedadEnAnios
        );

        if (tarifa.isPresent()) {
            return tarifa.get();
        }

        if (tipoCliente == TipoClienteTarifa.SOCIO_POLICIA
                || tipoCliente == TipoClienteTarifa.SOCIO_POLICIA_RETIRADO) {

            return buscarTarifa(
                    tarifas,
                    TipoClienteTarifa.SOCIO_COMUN,
                    antiguedadEnAnios
            ).orElseThrow(this::tarifaNoEncontrada);
        }

        throw tarifaNoEncontrada();
    }

    private Optional<TarifaServicio> buscarTarifa(
            List<TarifaServicio> tarifas,
            TipoClienteTarifa tipoCliente,
            Integer antiguedad
    ) {

        return tarifas.stream()
                .filter(t -> t.getTipoCliente() == tipoCliente)
                .filter(t -> aplicaAntiguedad(t, antiguedad))
                .findFirst();
    }

    private boolean aplicaAntiguedad(
            TarifaServicio tarifa,
            Integer antiguedad
    ) {

        if (tarifa.getTipoCliente() == TipoClienteTarifa.PARTICULAR) {
            return true;
        }

        if (antiguedad == null) {
            return false;
        }

        return antiguedad >= TarifaServicio.normalizarAntiguedadMinima(tarifa.getAntiguedadMinima())
                && antiguedad <= TarifaServicio.normalizarAntiguedadMaxima(tarifa.getAntiguedadMaxima());
    }

    private ServicioValidacionException tarifaNoEncontrada() {
        return new ServicioValidacionException(
                ServicioCodigoError.TARIFA_NO_ENCONTRADA_PARA_CLIENTE.name(),
                "No existe una tarifa configurada para el cliente."
        );
    }
}
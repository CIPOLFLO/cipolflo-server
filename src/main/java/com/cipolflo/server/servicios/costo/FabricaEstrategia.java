package com.cipolflo.server.servicios.costo;

import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FabricaEstrategia {

    private final Map<ModalidadPrecio, EstrategiaCosto> estrategias;

    public FabricaEstrategia(
            EstrategiaCostoPorDia porDia,
            EstrategiaCostoPorHora porHora,
            EstrategiaCostoPorUnidad porUnidad,
            EstrategiaCostoPorPersona porPersona,
            EstrategiaCostoPorDiaPorPersona porDiaPorPersona) {

        this.estrategias = Map.of(
                ModalidadPrecio.POR_DIA, porDia,
                ModalidadPrecio.POR_HORA, porHora,
                ModalidadPrecio.POR_UNIDAD, porUnidad,
                ModalidadPrecio.POR_PERSONA, porPersona,
                ModalidadPrecio.POR_DIA_POR_PERSONA, porDiaPorPersona
        );
    }

    public EstrategiaCosto obtener(ModalidadPrecio modalidad) {
        EstrategiaCosto estrategia = estrategias.get(modalidad);
        if (estrategia == null) {
            throw new IllegalStateException("Sin estrategia de costo para modalidad: " + modalidad);
        }
        return estrategia;
    }
}

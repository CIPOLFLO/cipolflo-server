package com.cipolflo.server.servicios.validator;

import com.cipolflo.server.servicios.domain.TarifaServicio;
import com.cipolflo.server.servicios.domain.enums.TipoClienteTarifa;
import com.cipolflo.server.servicios.exception.ServicioValidacionException;
import com.cipolflo.server.shared.exception.ServicioCodigoError;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TarifaServicioReglasValidator {

    public void validar(List<TarifaServicio> tarifas) {
        validarObligatorias(tarifas);
        validarSinSuperposicion(tarifas);
    }

    public void validarObligatorias(List<TarifaServicio> tarifas) {
        if (!cumpleTarifasObligatorias(tarifas)) {
            throw new ServicioValidacionException(
                    ServicioCodigoError.TARIFAS_OBLIGATORIAS_FALTANTES.name(),
                    "Debe existir al menos una tarifa particular y una tarifa de socio común"
            );
        }
    }

    public boolean cumpleTarifasObligatorias(List<TarifaServicio> tarifas) {
        boolean tieneParticular = tarifas.stream()
                .anyMatch(tarifa ->
                        tarifa.getTipoCliente() == TipoClienteTarifa.PARTICULAR
                );

        boolean tieneSocioComun = tarifas.stream()
                .anyMatch(tarifa ->
                        tarifa.getTipoCliente() == TipoClienteTarifa.SOCIO_COMUN
                );

        return tieneParticular && tieneSocioComun;
    }

    private void validarSinSuperposicion(List<TarifaServicio> tarifas) {
        Map<TipoClienteTarifa, List<TarifaServicio>> tarifasPorTipo =
                tarifas.stream()
                        .collect(Collectors.groupingBy(
                                TarifaServicio::getTipoCliente
                        ));

        for (List<TarifaServicio> tarifasDelTipo : tarifasPorTipo.values()) {
            validarSuperposicionesDelMismoTipo(tarifasDelTipo);
        }
    }

    private void validarSuperposicionesDelMismoTipo(
            List<TarifaServicio> tarifas
    ) {
        for (int i = 0; i < tarifas.size(); i++) {
            for (int j = i + 1; j < tarifas.size(); j++) {
                if (rangosSeSuperponen(tarifas.get(i), tarifas.get(j))) {
                    throw new ServicioValidacionException(
                            ServicioCodigoError.TARIFA_SUPERPUESTA.name(),
                            "Existen tarifas del mismo tipo de cliente con rangos superpuestos"
                    );
                }
            }
        }
    }

    private boolean rangosSeSuperponen(
            TarifaServicio primera,
            TarifaServicio segunda
    ) {
        int minimaPrimera = valorMinimo(primera.getAntiguedadMinima());
        int maximaPrimera = valorMaximo(primera.getAntiguedadMaxima());

        int minimaSegunda = valorMinimo(segunda.getAntiguedadMinima());
        int maximaSegunda = valorMaximo(segunda.getAntiguedadMaxima());

        return minimaPrimera <= maximaSegunda
                && minimaSegunda <= maximaPrimera;
    }

    private int valorMinimo(Integer antiguedadMinima) {
        return antiguedadMinima == null
                ? Integer.MIN_VALUE
                : antiguedadMinima;
    }

    private int valorMaximo(Integer antiguedadMaxima) {
        return antiguedadMaxima == null
                ? Integer.MAX_VALUE
                : antiguedadMaxima;
    }
}

package com.cipolflo.server.servicios.validator;

import com.cipolflo.server.servicios.domain.enums.TipoClienteTarifa;
import com.cipolflo.server.servicios.dto.TarifaServicioRequestDto;
import com.cipolflo.server.servicios.exception.ServicioValidacionException;
import com.cipolflo.server.shared.exception.ServicioCodigoError;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TarifaServicioReglasValidator {

    public void validar(List<TarifaServicioRequestDto> tarifas) {
        if (tarifas == null || tarifas.isEmpty()) {
            throw new ServicioValidacionException(
                    ServicioCodigoError.TARIFAS_OBLIGATORIAS_FALTANTES.name(),
                    "Debe ingresar al menos una tarifa particular y una tarifa de socio común"
            );
        }

        validarTarifasObligatorias(tarifas);
        validarAntiguedadParticular(tarifas);
        validarRangos(tarifas);
        validarSuperposiciones(tarifas);
    }

    private void validarTarifasObligatorias(List<TarifaServicioRequestDto> tarifas) {
        boolean tieneParticular = tarifas.stream()
                .anyMatch(tarifa ->
                        tarifa.getTipoCliente() == TipoClienteTarifa.PARTICULAR
                );

        boolean tieneSocioComun = tarifas.stream()
                .anyMatch(tarifa ->
                        tarifa.getTipoCliente() == TipoClienteTarifa.SOCIO_COMUN
                );

        if (!tieneParticular || !tieneSocioComun) {
            throw new ServicioValidacionException(
                    ServicioCodigoError.TARIFAS_OBLIGATORIAS_FALTANTES.name(),
                    "Debe existir al menos una tarifa particular y una tarifa de socio común"
            );
        }
    }

    private void validarAntiguedadParticular(List<TarifaServicioRequestDto> tarifas) {
        boolean particularConAntiguedad = tarifas.stream()
                .filter(tarifa ->
                        tarifa.getTipoCliente() == TipoClienteTarifa.PARTICULAR
                )
                .anyMatch(tarifa ->
                        tarifa.getAntiguedadMinima() != null
                                || tarifa.getAntiguedadMaxima() != null
                );

        if (particularConAntiguedad) {
            throw new ServicioValidacionException(
                    ServicioCodigoError.ANTIGUEDAD_NO_APLICABLE_A_PARTICULAR.name(),
                    "Las tarifas particulares no pueden tener rangos de antigüedad"
            );
        }
    }

    private void validarRangos(List<TarifaServicioRequestDto> tarifas) {
        boolean existeRangoInvalido = tarifas.stream()
                .anyMatch(this::esRangoInvalido);

        if (existeRangoInvalido) {
            throw new ServicioValidacionException(
                    ServicioCodigoError.RANGO_ANTIGUEDAD_INVALIDO.name(),
                    "La antigüedad mínima no puede ser mayor que la antigüedad máxima"
            );
        }
    }

    private boolean esRangoInvalido(TarifaServicioRequestDto tarifa) {
        Integer minima = tarifa.getAntiguedadMinima();
        Integer maxima = tarifa.getAntiguedadMaxima();

        if (minima != null && minima < 0) {
            return true;
        }

        if (maxima != null && maxima < 0) {
            return true;
        }

        return minima != null
                && maxima != null
                && minima > maxima;
    }

    private void validarSuperposiciones(List<TarifaServicioRequestDto> tarifas) {
        Map<TipoClienteTarifa, List<TarifaServicioRequestDto>> tarifasPorTipo =
                tarifas.stream()
                        .collect(Collectors.groupingBy(
                                TarifaServicioRequestDto::getTipoCliente
                        ));

        for (List<TarifaServicioRequestDto> tarifasDelTipo : tarifasPorTipo.values()) {
            validarSuperposicionesDelMismoTipo(tarifasDelTipo);
        }
    }

    private void validarSuperposicionesDelMismoTipo(
            List<TarifaServicioRequestDto> tarifas
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
            TarifaServicioRequestDto primera,
            TarifaServicioRequestDto segunda
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

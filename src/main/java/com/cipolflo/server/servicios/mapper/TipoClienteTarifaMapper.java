package com.cipolflo.server.servicios.mapper;

import com.cipolflo.server.clientes.domain.Cliente;
import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.CategoriaSocio;
import com.cipolflo.server.servicios.domain.enums.TipoClienteTarifa;

public final class TipoClienteTarifaMapper {

    private TipoClienteTarifaMapper() {
    }

    public static TipoClienteTarifa desdeCliente(Cliente cliente) {
        if (!(cliente instanceof Socio socio)) {
            return TipoClienteTarifa.PARTICULAR;
        }

        return mapearCategoriaSocio(socio.getCategoriaSocio());
    }

    private static TipoClienteTarifa mapearCategoriaSocio(CategoriaSocio categoriaSocio) {
        if (categoriaSocio == null) {
            throw new IllegalArgumentException("La categoría del socio no puede ser nula");
        }

        return switch (categoriaSocio) {
            case SOCIO_COMUN -> TipoClienteTarifa.SOCIO_COMUN;
            case POLICIA_ACTIVO -> TipoClienteTarifa.SOCIO_POLICIA;
            case POLICIA_RETIRADO -> TipoClienteTarifa.SOCIO_POLICIA_RETIRADO;
        };
    }
}
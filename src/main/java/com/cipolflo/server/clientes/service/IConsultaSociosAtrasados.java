package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.dto.SocioAtrasadoDto;

import java.util.List;

public interface IConsultaSociosAtrasados {

    /** Socios ACTIVO o INACTIVO con al menos un mes impago. Excluye DE_BAJA. */
    List<SocioAtrasadoDto> buscarAtrasados();
}

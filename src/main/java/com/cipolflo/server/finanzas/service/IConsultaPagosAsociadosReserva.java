package com.cipolflo.server.finanzas.service;


import com.cipolflo.server.reservas.dto.PagoAsociadoReservaDto;

import java.util.List;

public interface IConsultaPagosAsociadosReserva {

    List<PagoAsociadoReservaDto> getPagosAsociados(Long reservaId);
}
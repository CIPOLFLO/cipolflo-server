package com.cipolflo.server.reservas.service;

import java.math.BigDecimal;

public interface IReversionPagoReservaService {

    void revertirPorEliminacion(Long reservaId, BigDecimal importe, boolean confirmar);
}

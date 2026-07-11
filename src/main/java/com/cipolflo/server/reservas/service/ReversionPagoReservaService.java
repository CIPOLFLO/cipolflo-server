package com.cipolflo.server.reservas.service;

import com.cipolflo.server.finanzas.exception.ConfirmacionEliminacionReservaRequeridaException;
import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.exception.ReservaNotFoundException;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Revierte el efecto de un pago de reserva sobre la propia reserva cuando se elimina su ingreso
 * asociado en finanzas.
 * <p>
 * Depende únicamente de {@link ReservaRepository} para evitar el ciclo con {@code IFinanzaService}:
 * la cadena {@code PagoReservaService → FinanzaService → ReversionPagoReservaService → ReservaRepository}
 * no forma ciclo.
 */
@Service
public class ReversionPagoReservaService implements IReversionPagoReservaService {

    private final ReservaRepository reservaRepository;

    public ReversionPagoReservaService(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    @Override
    @Transactional
    public void revertirPorEliminacion(Long reservaId, BigDecimal importe, boolean confirmar) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));

        if (reserva.getEstado() == EstadoReserva.FINALIZADA
                || reserva.getEstado() == EstadoReserva.CANCELADA) {
            if (!confirmar) {
                throw new ConfirmacionEliminacionReservaRequeridaException();
            }
            return;
        }

        reserva.revertirPago(importe);
        reservaRepository.save(reserva);
    }
}

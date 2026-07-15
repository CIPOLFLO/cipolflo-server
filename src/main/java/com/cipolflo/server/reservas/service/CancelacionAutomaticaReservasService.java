

package com.cipolflo.server.reservas.service;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.shared.ZonaHoraria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class CancelacionAutomaticaReservasService implements ICancelacionAutomaticaReservasService {

    private final ReservaRepository reservaRepository;

    public CancelacionAutomaticaReservasService(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    @Override
    @Transactional
    public String cancelarReservasVencidas() {
        List<Reserva> vencidas = reservaRepository.findByEstadoAndFechaLimiteConfirmacionLessThanEqual(
                EstadoReserva.PENDIENTE,
                LocalDateTime.now(ZonaHoraria.URUGUAY)
        );

        if (!vencidas.isEmpty()) {
            vencidas.forEach(Reserva::cancelar);
            reservaRepository.saveAll(vencidas);
        }

        return vencidas.size() + " reservas canceladas automáticamente";
    }
}
package com.cipolflo.server.reservas.service;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.dto.RegistroPagoReservaRequestDto;
import com.cipolflo.server.reservas.dto.ReservaFinalizacionCheckResponseDto;
import com.cipolflo.server.reservas.dto.ReservaFinalizacionRequestDto;
import com.cipolflo.server.reservas.exception.ReservaCodigoError;
import com.cipolflo.server.reservas.exception.ReservaNotFoundException;
import com.cipolflo.server.reservas.exception.ReservaValidacionException;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.reservas.validators.FinalizacionReservaValidationContext;
import com.cipolflo.server.reservas.validators.FinalizacionReservaValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinalizacionReservaService implements IFinalizacionReservaService {

    private final ReservaRepository reservaRepository;
    private final IPagoReservaService pagoReservaService;
    private final FinalizacionReservaValidator finalizacionReservaValidator;

    public FinalizacionReservaService(
            ReservaRepository reservaRepository,
            IPagoReservaService pagoReservaService,
            FinalizacionReservaValidator finalizacionReservaValidator
    ) {
        this.reservaRepository = reservaRepository;
        this.pagoReservaService = pagoReservaService;
        this.finalizacionReservaValidator = finalizacionReservaValidator;
    }

    @Transactional(readOnly = true)
    @Override
    public ReservaFinalizacionCheckResponseDto verificarFinalizacion(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));

        if (reserva.getEstado() != EstadoReserva.EN_CURSO) {
            throw new ReservaValidacionException(
                    ReservaCodigoError.RESERVA_NO_FINALIZABLE,
                    "Solo se pueden finalizar reservas en curso"
            );
        }

        return new ReservaFinalizacionCheckResponseDto(
                reserva.estaPaga(),
                reserva.getMontoImpago()
        );
    }

    @Override
    @Transactional
    public void finalizar(Long reservaId, ReservaFinalizacionRequestDto dto) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));

        FinalizacionReservaValidationContext contexto = new FinalizacionReservaValidationContext(
                dto,
                reserva.getEstado(),
                reserva.estaPaga()
        );

        finalizacionReservaValidator.validar(contexto);

        if (!reserva.estaPaga() && Boolean.TRUE.equals(dto.getCompletarPago())) {
            RegistroPagoReservaRequestDto pagoDto = new RegistroPagoReservaRequestDto();
            pagoDto.setImporte(reserva.getMontoImpago());
            pagoDto.setEsPagoTotal(true);
            pagoDto.setFormaPago(dto.getFormaPago());
            pagoDto.setNotas(dto.getNotas());

            pagoReservaService.registrarPago(reservaId, pagoDto);
        }

        reserva.cambiarEstado(EstadoReserva.FINALIZADA);
        reservaRepository.save(reserva);
    }
}

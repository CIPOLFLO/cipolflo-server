package com.cipolflo.server.reservas.service;

import com.cipolflo.server.finanzas.service.IConsultaPagosAsociadosReserva;
import com.cipolflo.server.finanzas.service.IFinanzaService;
import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.dto.PagoAsociadoReservaDto;
import com.cipolflo.server.reservas.dto.ReservaCancelacionCheckResponseDto;
import com.cipolflo.server.reservas.dto.ReservaCancelacionRequestDto;
import com.cipolflo.server.reservas.exception.ReservaCodigoError;
import com.cipolflo.server.reservas.exception.ReservaNotFoundException;
import com.cipolflo.server.reservas.exception.ReservaValidacionException;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.reservas.validators.CancelacionReservaValidator;
import com.cipolflo.server.reservas.validators.contexto.CancelacionReservaValidationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CancelacionReservaService implements ICancelacionReservaService {

    private final ReservaRepository reservaRepository;
    private final IConsultaPagosAsociadosReserva consultaPagosAsociadosReserva;
    private final IFinanzaService finanzaService;
    private final CancelacionReservaValidator cancelacionReservaValidator;

    public CancelacionReservaService(
            ReservaRepository reservaRepository,
            IConsultaPagosAsociadosReserva consultaPagosAsociadosReserva,
            IFinanzaService finanzaService,
            CancelacionReservaValidator cancelacionReservaValidator
    ) {
        this.reservaRepository = reservaRepository;
        this.consultaPagosAsociadosReserva = consultaPagosAsociadosReserva;
        this.finanzaService = finanzaService;
        this.cancelacionReservaValidator = cancelacionReservaValidator;
    }

    @Transactional(readOnly = true)
    @Override
    public ReservaCancelacionCheckResponseDto verificarCancelacion(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));

        if (!reserva.esCancelable()) {
            throw new ReservaValidacionException(
                    ReservaCodigoError.RESERVA_NO_CANCELABLE,
                    "No se puede cancelar una reserva en este estado"
            );
        }

        List<PagoAsociadoReservaDto> pagos = consultaPagosAsociadosReserva.getPagosAsociados(reservaId);

        BigDecimal importeTotalPagos = pagos.stream()
                .map(PagoAsociadoReservaDto::importe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ReservaCancelacionCheckResponseDto(
                pagos.isEmpty(),
                pagos,
                importeTotalPagos
        );
    }

    @Override
    @Transactional
    public void cancelar(Long reservaId, ReservaCancelacionRequestDto dto) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));

        List<PagoAsociadoReservaDto> pagos = consultaPagosAsociadosReserva.getPagosAsociados(reservaId);

        BigDecimal importeTotalPagos = pagos.stream()
                .map(PagoAsociadoReservaDto::importe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CancelacionReservaValidationContext contexto = new CancelacionReservaValidationContext(
                dto,
                reserva,
                pagos,
                importeTotalPagos
        );

        cancelacionReservaValidator.validar(contexto);

        boolean debeGenerarDevolucion = !pagos.isEmpty()
                && Boolean.TRUE.equals(dto.getGenerarDevolucion());

        if (debeGenerarDevolucion) {
            finanzaService.registrarDevolucionPorCancelacionReserva(
                    reservaId,
                    dto.getImporteDevolucion(),
                    dto.getFormaPago(),
                    reserva.getProcedencia()
            );
        }

        reserva.cancelar();
        reservaRepository.save(reserva);
    }
}

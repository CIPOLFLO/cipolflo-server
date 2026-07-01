package com.cipolflo.server.reservas.service;

import com.cipolflo.server.finanzas.domain.enums.Concepto;
import com.cipolflo.server.finanzas.domain.enums.TipoMovimiento;
import com.cipolflo.server.finanzas.dto.FinanzaCrearRequestDto;
import com.cipolflo.server.finanzas.service.IFinanzaService;
import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.dto.RegistroPagoReservaRequestDto;
import com.cipolflo.server.reservas.exception.ReservaNotFoundException;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.reservas.validators.PagoReservaValidator;
import com.cipolflo.server.reservas.validators.contexto.PagoReservaValidationContext;
import com.cipolflo.server.shared.ZonaHoraria;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class PagoReservaService implements IPagoReservaService {

    private final ReservaRepository reservaRepository;
    private final IFinanzaService finanzaService;
    private final PagoReservaValidator pagoReservaValidator;

    public PagoReservaService(
            ReservaRepository reservaRepository,
            IFinanzaService finanzaService,
            PagoReservaValidator pagoReservaValidator
    ){
        this.reservaRepository = reservaRepository;
        this.finanzaService = finanzaService;
        this.pagoReservaValidator = pagoReservaValidator;
    }

    @Transactional
    @Override
    public void registrarPago(Long reservaId, RegistroPagoReservaRequestDto dto) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));
        PagoReservaValidationContext contexto = new PagoReservaValidationContext(
                dto,
                reserva.getEstado(),
                reserva.getTipoReserva(),
                reserva.getMontoImpago()
        );
        pagoReservaValidator.validar(contexto);

        FinanzaCrearRequestDto dtoIngreso =  new FinanzaCrearRequestDto();

        dtoIngreso.setTipoMovimiento(TipoMovimiento.INGRESO);
        dtoIngreso.setProcedencia(reserva.getProcedencia());
        dtoIngreso.setConcepto(Concepto.PAGO_RESERVA);
        dtoIngreso.setFecha(LocalDate.now(ZonaHoraria.URUGUAY));
        dtoIngreso.setImporte(dto.getImporte());
        dtoIngreso.setFormaPago(dto.getFormaPago());
        dtoIngreso.setNotas(dto.getNotas());
        dtoIngreso.setReservaId(reservaId);

        finanzaService.registrarPagoReserva(dtoIngreso);

        reserva.registrarPago(dto.getImporte(), dto.getEsPagoTotal());
    }
}

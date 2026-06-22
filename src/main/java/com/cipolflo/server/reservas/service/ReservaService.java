package com.cipolflo.server.reservas.service;

import com.cipolflo.server.clientes.dto.ClienteResponseDto;
import com.cipolflo.server.clientes.dto.RegistroParticularRequestDto;
import com.cipolflo.server.clientes.service.IRegistroParticularService;
import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.domain.enums.EstadoReserva;
import com.cipolflo.server.reservas.domain.enums.TipoReserva;
import com.cipolflo.server.reservas.dto.ReservaCreacionRequestDto;
import com.cipolflo.server.reservas.dto.ReservaCreacionResponseDto;
import com.cipolflo.server.reservas.repository.ReservaRepository;
import com.cipolflo.server.reservas.validators.ReservaCreacionValidator;
import com.cipolflo.server.servicios.service.IServicioRequiereDocumentacion;
import com.cipolflo.server.shared.ZonaHoraria;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservaService implements IReservaService {
    private static final int DIAS_VENTANA_RESERVAS_PROXIMAS = 60;

    private static final List<EstadoReserva> ESTADOS_OCUPANTES = List.of(
            EstadoReserva.PENDIENTE,
            EstadoReserva.CONFIRMADA,
            EstadoReserva.EN_CURSO
    );

    private final ReservaRepository reservaRepository;
    private final IRegistroParticularService registroParticularService;
    private final ReservaCreacionValidator reservaCreacionValidator;
    private final IServicioRequiereDocumentacion servicioRequiereDocumentacion;

    public ReservaService(
            ReservaRepository reservaRepository,
            IRegistroParticularService registroParticularService,
            ReservaCreacionValidator reservaCreacionValidator,
            IServicioRequiereDocumentacion servicioRequiereDocumentacion
    ) {
        this.reservaRepository = reservaRepository;
        this.registroParticularService = registroParticularService;
        this.reservaCreacionValidator = reservaCreacionValidator;
        this.servicioRequiereDocumentacion = servicioRequiereDocumentacion;
    }

    @Override
    public List<Reserva> obtenerProximasPorServicioEnRango(Long servicioId) {
        LocalDate desde = LocalDate.now(ZonaHoraria.URUGUAY);
        LocalDate hasta = desde.plusDays(DIAS_VENTANA_RESERVAS_PROXIMAS);

        return reservaRepository.findByServicioIdAndFechaEntradaBetweenAndEstadoIn(
                servicioId,
                desde,
                hasta,
                List.of(
                        EstadoReserva.PENDIENTE,
                        EstadoReserva.CONFIRMADA
                )
        );
    }

    @Override
    public List<Reserva> obtenerOcupacionPorServicioEnRango(Long servicioId, LocalDate desde, LocalDate hasta) {
        return reservaRepository
                .findByServicioIdAndEstadoInAndFechaEntradaLessThanEqualAndFechaSalidaGreaterThanEqual(
                        servicioId,
                        ESTADOS_OCUPANTES,
                        hasta,
                        desde
                );
    }

    @Override
    public void cancelarTodas(List<Reserva> reservas) {
        reservas.forEach(Reserva::cancelar);
        reservaRepository.saveAll(reservas);
    }

    @Override
    public void cancelarReservasFuturasPorCliente(Long clienteId) {
        List<Reserva> reservas = reservaRepository.findByClienteIdAndFechaEntradaAfterAndEstadoIn(
                clienteId,
                LocalDate.now(ZonaHoraria.URUGUAY),
                List.of(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA)
        );
        cancelarTodas(reservas);
    }

    @Override
    @Transactional
    public ReservaCreacionResponseDto registrar(ReservaCreacionRequestDto dto) {
        reservaCreacionValidator.validar(dto);

        // TODO: usar dto.getTipoCliente() para diferenciar el cálculo de costo según tipo de cliente (ticket pendiente)
        Long clienteId;
        if (Boolean.TRUE.equals(dto.getCrearCliente())) {
            RegistroParticularRequestDto nuevoCliente = new RegistroParticularRequestDto();
            nuevoCliente.setNombre(dto.getNombre());
            nuevoCliente.setCedula(dto.getCedula());
            nuevoCliente.setCelular(dto.getCelular());
            nuevoCliente.setMail(dto.getEmail());

            ClienteResponseDto clienteCreado = registroParticularService.registrarParticular(nuevoCliente);
            clienteId = clienteCreado.getId();
        } else {
            clienteId = dto.getClienteId();
        }

        boolean requiereDocumentacion = servicioRequiereDocumentacion.requiereDocumentacion(dto.getServicioId());

        Reserva reserva = Reserva.crear(
                dto.getTipoReserva(),
                clienteId,
                dto.getServicioId(),
                dto.getProcedencia(),
                dto.getFechaInicio(),
                dto.getFechaFin(),
                dto.getCantidadTotal(),
                dto.getCantidadMenores(),
                dto.getCantidad(),
                dto.getRut(),
                dto.getNotas(),
                requiereDocumentacion
        );

        // TODO: calcular y asignar importe llamando a ServicioCalculoImporte antes de guardar (ticket pendiente)
        Reserva guardada = reservaRepository.save(reserva);

        return new ReservaCreacionResponseDto(guardada.getId());
    }
}

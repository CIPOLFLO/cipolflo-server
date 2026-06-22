package com.cipolflo.server.reservas.service;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.dto.ReservaCreacionRequestDto;
import com.cipolflo.server.reservas.dto.ReservaCreacionResponseDto;
import com.cipolflo.server.reservas.dto.ReservaDetalleResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface IReservaService {

    List<Reserva> obtenerProximasPorServicioEnRango(Long servicioId);

    List<Reserva> obtenerOcupacionPorServicioEnRango(Long servicioId, LocalDate desde, LocalDate hasta);

    void cancelarTodas(List<Reserva> reservas);

    void cancelarReservasFuturasPorCliente(Long clienteId);

    ReservaCreacionResponseDto registrar(ReservaCreacionRequestDto dto);

    ReservaDetalleResponseDto getDetalle(Long id);
}

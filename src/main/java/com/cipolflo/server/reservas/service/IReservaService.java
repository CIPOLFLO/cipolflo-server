package com.cipolflo.server.reservas.service;

import com.cipolflo.server.reservas.domain.Reserva;
import com.cipolflo.server.reservas.dto.*;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

public interface IReservaService {

    CalculoCostoResponseDto calcularCosto(CalculoCostoRequestDto request);

    List<Reserva> obtenerProximasPorServicioEnRango(Long servicioId);

    List<Reserva> obtenerOcupacionPorServicioEnRango(Long servicioId, LocalDate desde, LocalDate hasta);

    void cancelarTodas(List<Reserva> reservas);

    void cancelarReservasFuturasPorCliente(Long clienteId);

    ReservaCreacionResponseDto registrar(ReservaCreacionRequestDto dto);

    ReservaDetalleResponseDto getDetalle(Long id);

    PageResponse<ListadoReservasResponseDto> getListadoReservas(ListadoReservasRequestDto filtros,PageRequestDto pageRequest);

    ReservaModificacionResponseDto modificar(Long id, ReservaModificacionRequestDto dto);
}

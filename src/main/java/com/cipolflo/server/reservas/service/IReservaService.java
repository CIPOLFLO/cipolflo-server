package com.cipolflo.server.reservas.service;

import com.cipolflo.server.reservas.domain.Reserva;

import java.util.List;

public interface IReservaService {

    List<Reserva> obtenerProximasPorServicioEnRango(Long servicioId);

    void cancelarTodas(List<Reserva> reservas);

    void cancelarReservasFuturasPorCliente(Long clienteId);
}

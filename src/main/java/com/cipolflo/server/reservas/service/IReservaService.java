package com.cipolflo.server.reservas.service;

import com.cipolflo.server.reservas.domain.Reserva;

import java.util.List;

public interface IReservaService {

    List<Reserva> obtenerProximasPorServicio(Long servicioId);

    List<Reserva> obtenerPorIdsYServicio(List<Long> ids, Long servicioId);

    void cancelarTodas(List<Reserva> reservas);
}

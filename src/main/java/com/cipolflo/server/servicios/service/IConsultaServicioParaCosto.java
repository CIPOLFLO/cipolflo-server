package com.cipolflo.server.servicios.service;

import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.domain.TarifaServicio;

import java.util.List;

public interface IConsultaServicioParaCosto {
    Servicio obtenerServicio(Long id);

    List<TarifaServicio> obtenerTarifas(Long servicioId);
}

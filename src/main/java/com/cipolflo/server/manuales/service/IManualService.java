package com.cipolflo.server.manuales.service;

import com.cipolflo.server.manuales.domain.CategoriaManual;
import com.cipolflo.server.manuales.dto.ManualResponseDto;

import java.util.List;

public interface IManualService {

    /**
     * Lista el catálogo de manuales.
     *
     * @param categoria filtro opcional; {@code null} devuelve todas las categorías
     */
    List<ManualResponseDto> listarManuales(CategoriaManual categoria);

    /**
     * Resuelve el PDF de un manual publicado.
     *
     * @throws com.cipolflo.server.manuales.exception.ManualNoEncontradoException
     *         si la clave no está en el catálogo
     * @throws com.cipolflo.server.manuales.exception.ManualNoDisponibleException
     *         si el manual está en el catálogo pero su PDF no fue publicado
     */
    ManualDescarga descargarManual(String clave);
}

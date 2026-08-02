package com.cipolflo.server.manuales.dto;

import com.cipolflo.server.manuales.domain.CategoriaManual;
import com.cipolflo.server.shared.dto.ResponseDto;

import java.time.LocalDate;

/**
 * Entrada del listado de manuales.
 *
 * <p>{@code disponible} en {@code false} significa que el manual está previsto pero su
 * PDF todavía no fue publicado; en ese caso {@code version} y {@code fechaActualizacion}
 * vienen en {@code null} y el front puede mostrarlo deshabilitado.</p>
 */
public record ManualResponseDto(
        String clave,
        String titulo,
        CategoriaManual categoria,
        boolean disponible,
        String version,
        LocalDate fechaActualizacion
) implements ResponseDto {}

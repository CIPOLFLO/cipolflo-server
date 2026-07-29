package com.cipolflo.server.ajustes.service;

import com.cipolflo.server.ajustes.dto.DestinatarioNotificacionEmailResponseDto;
import com.cipolflo.server.ajustes.dto.HabilitacionDestinatarioNotificacionEmailRequestDto;
import com.cipolflo.server.ajustes.dto.ListadoDestinatarioNotificacionEmailResponseDto;
import com.cipolflo.server.ajustes.dto.ListadoDestinatariosNotificacionEmailRequestDto;
import com.cipolflo.server.ajustes.dto.ModificacionDestinatarioNotificacionEmailRequestDto;
import com.cipolflo.server.ajustes.dto.RegistroDestinatarioNotificacionEmailRequestDto;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;

public interface IDestinatarioNotificacionEmailService {

    PageResponse<ListadoDestinatarioNotificacionEmailResponseDto> getListado(
            ListadoDestinatariosNotificacionEmailRequestDto filtros, PageRequestDto pageRequest);

    DestinatarioNotificacionEmailResponseDto getDetalle(Long id);

    DestinatarioNotificacionEmailResponseDto registrar(RegistroDestinatarioNotificacionEmailRequestDto dto);

    DestinatarioNotificacionEmailResponseDto modificar(Long id, ModificacionDestinatarioNotificacionEmailRequestDto dto);

    DestinatarioNotificacionEmailResponseDto cambiarHabilitacion(
            Long id, HabilitacionDestinatarioNotificacionEmailRequestDto dto);

    void eliminar(Long id);
}

package com.cipolflo.server.ajustes.service;

import com.cipolflo.server.ajustes.dto.ClienteTelegramResponseDto;
import com.cipolflo.server.ajustes.dto.HabilitacionClienteTelegramRequestDto;
import com.cipolflo.server.ajustes.dto.ListadoClienteTelegramResponseDto;
import com.cipolflo.server.ajustes.dto.ListadoClientesTelegramRequestDto;
import com.cipolflo.server.ajustes.dto.ModificacionClienteTelegramRequestDto;
import com.cipolflo.server.ajustes.dto.RegistroClienteTelegramRequestDto;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;

public interface IClienteTelegramService {

    PageResponse<ListadoClienteTelegramResponseDto> getListado(
            ListadoClientesTelegramRequestDto filtros, PageRequestDto pageRequest);

    ClienteTelegramResponseDto getDetalle(Long id);

    ClienteTelegramResponseDto registrar(RegistroClienteTelegramRequestDto dto);

    ClienteTelegramResponseDto modificar(Long id, ModificacionClienteTelegramRequestDto dto);

    ClienteTelegramResponseDto cambiarHabilitacion(Long id, HabilitacionClienteTelegramRequestDto dto);

    void eliminar(Long id);
}

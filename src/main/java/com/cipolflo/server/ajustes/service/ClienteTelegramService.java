package com.cipolflo.server.ajustes.service;

import com.cipolflo.server.ajustes.dto.ClienteTelegramResponseDto;
import com.cipolflo.server.ajustes.dto.HabilitacionClienteTelegramRequestDto;
import com.cipolflo.server.ajustes.dto.ListadoClienteTelegramResponseDto;
import com.cipolflo.server.ajustes.dto.ListadoClientesTelegramRequestDto;
import com.cipolflo.server.ajustes.dto.ModificacionClienteTelegramRequestDto;
import com.cipolflo.server.ajustes.dto.RegistroClienteTelegramRequestDto;
import com.cipolflo.server.ajustes.mapper.ClienteTelegramMapper;
import com.cipolflo.server.integraciones.telegram.domain.TelegramChatAutorizado;
import com.cipolflo.server.integraciones.telegram.exception.TelegramChatNoEncontradoException;
import com.cipolflo.server.integraciones.telegram.exception.TelegramCodigoError;
import com.cipolflo.server.integraciones.telegram.exception.TelegramValidacionException;
import com.cipolflo.server.integraciones.telegram.repository.TelegramChatAutorizadoRepository;
import com.cipolflo.server.integraciones.telegram.repository.TelegramChatAutorizadoSpecification;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import com.cipolflo.server.shared.pagination.PaginationMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteTelegramService implements IClienteTelegramService {

    private final TelegramChatAutorizadoRepository telegramChatAutorizadoRepository;

    public ClienteTelegramService(TelegramChatAutorizadoRepository telegramChatAutorizadoRepository) {
        this.telegramChatAutorizadoRepository = telegramChatAutorizadoRepository;
    }

    @Override
    public PageResponse<ListadoClienteTelegramResponseDto> getListado(
            ListadoClientesTelegramRequestDto filtros, PageRequestDto pageRequest) {
        Specification<TelegramChatAutorizado> spec = TelegramChatAutorizadoSpecification
                .conAlias(filtros.alias())
                .and(TelegramChatAutorizadoSpecification.conActivo(filtros.activo()));
        Page<ListadoClienteTelegramResponseDto> page = telegramChatAutorizadoRepository
                .findAll(spec, pageRequest.toPageable())
                .map(ClienteTelegramMapper::toListadoResponseDto);
        return PaginationMapper.toPageResponse(page);
    }

    @Override
    public ClienteTelegramResponseDto getDetalle(Long id) {
        return ClienteTelegramMapper.toResponseDto(buscarPorId(id));
    }

    @Override
    @Transactional
    public ClienteTelegramResponseDto registrar(RegistroClienteTelegramRequestDto dto) {
        TelegramChatAutorizado chat =
                TelegramChatAutorizado.registrar(dto.chatId(), dto.alias(), dto.recibeNotificaciones());
        try {
            return ClienteTelegramMapper.toResponseDto(telegramChatAutorizadoRepository.saveAndFlush(chat));
        } catch (DataIntegrityViolationException e) {
            throw new TelegramValidacionException(
                    TelegramCodigoError.CHAT_ID_DUPLICADO.name(),
                    "Ya existe un cliente autorizado de Telegram con ese chatId"
            );
        }
    }

    @Override
    @Transactional
    public ClienteTelegramResponseDto modificar(Long id, ModificacionClienteTelegramRequestDto dto) {
        TelegramChatAutorizado chat = buscarPorId(id);
        chat.modificar(dto.alias(), dto.recibeNotificaciones());
        return ClienteTelegramMapper.toResponseDto(telegramChatAutorizadoRepository.save(chat));
    }

    @Override
    @Transactional
    public ClienteTelegramResponseDto cambiarHabilitacion(Long id, HabilitacionClienteTelegramRequestDto dto) {
        TelegramChatAutorizado chat = buscarPorId(id);
        if (Boolean.TRUE.equals(dto.activo())) {
            chat.activar();
        } else {
            chat.desactivar();
        }
        return ClienteTelegramMapper.toResponseDto(telegramChatAutorizadoRepository.save(chat));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        telegramChatAutorizadoRepository.delete(buscarPorId(id));
    }

    private TelegramChatAutorizado buscarPorId(Long id) {
        return telegramChatAutorizadoRepository.findById(id)
                .orElseThrow(() -> new TelegramChatNoEncontradoException(id));
    }
}

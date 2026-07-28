package com.cipolflo.server.ajustes.service;

import com.cipolflo.server.ajustes.dto.DestinatarioNotificacionEmailResponseDto;
import com.cipolflo.server.ajustes.dto.HabilitacionDestinatarioNotificacionEmailRequestDto;
import com.cipolflo.server.ajustes.dto.ListadoDestinatarioNotificacionEmailResponseDto;
import com.cipolflo.server.ajustes.dto.ListadoDestinatariosNotificacionEmailRequestDto;
import com.cipolflo.server.ajustes.dto.ModificacionDestinatarioNotificacionEmailRequestDto;
import com.cipolflo.server.ajustes.dto.RegistroDestinatarioNotificacionEmailRequestDto;
import com.cipolflo.server.ajustes.mapper.DestinatarioNotificacionEmailMapper;
import com.cipolflo.server.shared.email.domain.DestinatarioNotificacionEmail;
import com.cipolflo.server.shared.email.exception.DestinatarioNotificacionEmailCodigoError;
import com.cipolflo.server.shared.email.exception.DestinatarioNotificacionEmailNoEncontradoException;
import com.cipolflo.server.shared.email.exception.DestinatarioNotificacionEmailValidacionException;
import com.cipolflo.server.shared.email.repository.DestinatarioNotificacionEmailRepository;
import com.cipolflo.server.shared.email.repository.DestinatarioNotificacionEmailSpecification;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import com.cipolflo.server.shared.pagination.PaginationMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DestinatarioNotificacionEmailService implements IDestinatarioNotificacionEmailService {

    private final DestinatarioNotificacionEmailRepository destinatarioNotificacionEmailRepository;

    public DestinatarioNotificacionEmailService(
            DestinatarioNotificacionEmailRepository destinatarioNotificacionEmailRepository) {
        this.destinatarioNotificacionEmailRepository = destinatarioNotificacionEmailRepository;
    }

    @Override
    public PageResponse<ListadoDestinatarioNotificacionEmailResponseDto> getListado(
            ListadoDestinatariosNotificacionEmailRequestDto filtros, PageRequestDto pageRequest) {
        Specification<DestinatarioNotificacionEmail> spec = DestinatarioNotificacionEmailSpecification
                .conAlias(filtros.alias())
                .and(DestinatarioNotificacionEmailSpecification.conActivo(filtros.activo()));
        Page<ListadoDestinatarioNotificacionEmailResponseDto> page = destinatarioNotificacionEmailRepository
                .findAll(spec, pageRequest.toPageable())
                .map(DestinatarioNotificacionEmailMapper::toListadoResponseDto);
        return PaginationMapper.toPageResponse(page);
    }

    @Override
    public DestinatarioNotificacionEmailResponseDto getDetalle(Long id) {
        return DestinatarioNotificacionEmailMapper.toResponseDto(buscarPorId(id));
    }

    @Override
    @Transactional
    public DestinatarioNotificacionEmailResponseDto registrar(RegistroDestinatarioNotificacionEmailRequestDto dto) {
        DestinatarioNotificacionEmail destinatario =
                DestinatarioNotificacionEmail.registrar(dto.email(), dto.alias());
        try {
            return DestinatarioNotificacionEmailMapper.toResponseDto(
                    destinatarioNotificacionEmailRepository.saveAndFlush(destinatario));
        } catch (DataIntegrityViolationException e) {
            throw new DestinatarioNotificacionEmailValidacionException(
                    DestinatarioNotificacionEmailCodigoError.EMAIL_DUPLICADO.name(),
                    "Ya existe un destinatario de notificaciones con ese email"
            );
        }
    }

    @Override
    @Transactional
    public DestinatarioNotificacionEmailResponseDto modificar(
            Long id, ModificacionDestinatarioNotificacionEmailRequestDto dto) {
        DestinatarioNotificacionEmail destinatario = buscarPorId(id);
        destinatario.modificar(dto.alias());
        return DestinatarioNotificacionEmailMapper.toResponseDto(
                destinatarioNotificacionEmailRepository.save(destinatario));
    }

    @Override
    @Transactional
    public DestinatarioNotificacionEmailResponseDto cambiarHabilitacion(
            Long id, HabilitacionDestinatarioNotificacionEmailRequestDto dto) {
        DestinatarioNotificacionEmail destinatario = buscarPorId(id);
        if (Boolean.TRUE.equals(dto.activo())) {
            destinatario.activar();
        } else {
            destinatario.desactivar();
        }
        return DestinatarioNotificacionEmailMapper.toResponseDto(
                destinatarioNotificacionEmailRepository.save(destinatario));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        destinatarioNotificacionEmailRepository.delete(buscarPorId(id));
    }

    private DestinatarioNotificacionEmail buscarPorId(Long id) {
        return destinatarioNotificacionEmailRepository.findById(id)
                .orElseThrow(() -> new DestinatarioNotificacionEmailNoEncontradoException(id));
    }
}

package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.Particular;
import com.cipolflo.server.clientes.dto.ClienteResponseDto;
import com.cipolflo.server.clientes.dto.RegistroParticularRequestDto;
import com.cipolflo.server.clientes.exception.ClienteCodigoError;
import com.cipolflo.server.clientes.exception.ClienteValidacionException;
import com.cipolflo.server.clientes.mapper.ClienteMapper;
import com.cipolflo.server.clientes.repository.ClienteRepository;
import com.cipolflo.server.clientes.utils.CedulaNormalizador;
import com.cipolflo.server.clientes.validator.RegistroParticularValidator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistroParticularService implements IRegistroParticularService {

    private final ClienteRepository clienteRepository;
    private final RegistroParticularValidator registroParticularValidator;

    public RegistroParticularService(
            ClienteRepository clienteRepository,
            RegistroParticularValidator registroParticularValidator
    ) {
        this.clienteRepository = clienteRepository;
        this.registroParticularValidator = registroParticularValidator;
    }

    @Override
    @Transactional
    public ClienteResponseDto registrarParticular(RegistroParticularRequestDto dto) {
        String cedulaNormalizada = CedulaNormalizador.normalizar(dto.getCedula());
        String mailNormalizado = dto.getMail() != null ? dto.getMail().trim() : null;
        String nombreNormalizado = dto.getNombre().trim();
        String celularNormalizado = dto.getCelular().trim();

        registroParticularValidator.validar(dto, cedulaNormalizada);

        Particular particular = Particular.registrar(
                cedulaNormalizada,
                nombreNormalizado,
                celularNormalizado,
                mailNormalizado,
                null
        );

        try {
            return ClienteMapper.toDetalleResponseDto(clienteRepository.saveAndFlush(particular), null);
        } catch (DataIntegrityViolationException e) {
            throw new ClienteValidacionException(
                    ClienteCodigoError.CEDULA_DUPLICADA.name(),
                    "Ya existe un cliente con esa cédula"
            );
        }
    }
}

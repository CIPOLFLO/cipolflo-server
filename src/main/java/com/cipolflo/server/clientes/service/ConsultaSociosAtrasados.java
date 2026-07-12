package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.dto.SocioAtrasadoDto;
import com.cipolflo.server.clientes.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsultaSociosAtrasados implements IConsultaSociosAtrasados {

    private static final List<EstadoSocio> ESTADOS_ACCIONABLES = List.of(EstadoSocio.ACTIVO, EstadoSocio.INACTIVO);
    private static final int MESES_MINIMO = 1;

    private final ClienteRepository clienteRepository;

    public ConsultaSociosAtrasados(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public List<SocioAtrasadoDto> buscarAtrasados() {
        return clienteRepository
                .findByEstadoInAndMesesSinPagarGreaterThanEqual(ESTADOS_ACCIONABLES, MESES_MINIMO).stream()
                .map(this::aDto)
                .toList();
    }

    private SocioAtrasadoDto aDto(Socio socio) {
        return new SocioAtrasadoDto(
                socio.getId(), socio.getNombreCompleto(), socio.getCedula(), socio.getMesesSinPagar(), socio.getEstado());
    }
}

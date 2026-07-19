package com.cipolflo.server.clientes.service;

import com.cipolflo.server.clientes.domain.Socio;
import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.clientes.domain.enums.TipoCliente;
import com.cipolflo.server.clientes.dto.SocioAtrasadoDto;
import com.cipolflo.server.clientes.repository.ClienteRepository;
import com.cipolflo.server.clientes.repository.ClienteSpecification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsultaSociosAtrasados implements IConsultaSociosAtrasados {

    private static final List<EstadoSocio> ESTADOS_ACCIONABLES = List.of(EstadoSocio.ACTIVO, EstadoSocio.INACTIVO);
    private static final int MESES_MINIMO = 1;

    private final ClienteRepository clienteRepository;
    private final IPagoCuotaService pagoCuotaService;

    public ConsultaSociosAtrasados(ClienteRepository clienteRepository, IPagoCuotaService pagoCuotaService) {
        this.clienteRepository = clienteRepository;
        this.pagoCuotaService = pagoCuotaService;
    }

    @Override
    public List<SocioAtrasadoDto> buscarAtrasados() {
        return clienteRepository.findAll(ClienteSpecification.conTipoCliente(TipoCliente.SOCIO)).stream()
                .filter(Socio.class::isInstance)
                .map(Socio.class::cast)
                .filter(socio -> ESTADOS_ACCIONABLES.contains(socio.getEstado()))
                .map(this::aDto)
                .filter(dto -> dto.mesesSinPagar() >= MESES_MINIMO)
                .toList();
    }

    private SocioAtrasadoDto aDto(Socio socio) {
        return new SocioAtrasadoDto(
                socio.getId(), socio.getNombreCompleto(), socio.getCedula(),
                pagoCuotaService.calcularMesesAdeudados(socio), socio.getEstado());
    }
}

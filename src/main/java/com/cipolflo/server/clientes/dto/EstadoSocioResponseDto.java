package com.cipolflo.server.clientes.dto;
import lombok.Getter;
import com.cipolflo.server.clientes.domain.enums.EstadoSocio;
import com.cipolflo.server.shared.dto.ResponseDto;
@Getter
public class EstadoSocioResponseDto implements ResponseDto {

    private final Long id;
    private final EstadoSocio estado;
    private final Integer numeroSocio;

    public EstadoSocioResponseDto(Long id, EstadoSocio estado, Integer numeroSocio) {
        this.id = id;
        this.estado = estado;
        this.numeroSocio = numeroSocio;
    }
}

package com.cipolflo.server.servicios.service;

import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.dto.ListadoServiciosRequestDto;
import com.cipolflo.server.servicios.dto.ListadoServiciosResponseDto;
import com.cipolflo.server.servicios.dto.ServicioResponseDto;
import com.cipolflo.server.servicios.exception.ServicioNotFoundException;
import com.cipolflo.server.servicios.mapper.ServicioMapper;
import com.cipolflo.server.servicios.repository.ServicioRepository;
import com.cipolflo.server.servicios.repository.ServicioSpecification;
import com.cipolflo.server.shared.pagination.PageRequestDto;
import com.cipolflo.server.shared.pagination.PageResponse;
import com.cipolflo.server.shared.pagination.PaginationMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ServicioService implements IServicioService {
    private final ServicioRepository servicioRepository;

    public ServicioService(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    public ServicioResponseDto getDetalleServicio(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ServicioNotFoundException(id));
        return new ServicioResponseDto(
                servicio.getId(),
                servicio.getNombre(),
                servicio.getProcedencia(),
                servicio.getCantidad(),
                servicio.getPrecioSocio(),
                servicio.getPrecioParticular(),
                servicio.getCapacidad(),
                servicio.getHabilitado(),
                servicio.getModalidadPrecio()
        );
    }

    @Override
    public PageResponse<ListadoServiciosResponseDto> getListadoServicios(
            ListadoServiciosRequestDto filtros, PageRequestDto pageRequest) {

        Boolean habilitado = filtros.estado() != null ? filtros.estado().toBoolean() : null;

        Specification<Servicio> spec = ServicioSpecification.conNombre(filtros.nombre())
                .and(ServicioSpecification.conProcedencia(filtros.procedencia()))
                .and(ServicioSpecification.conHabilitado(habilitado));

        Page<ListadoServiciosResponseDto> page = servicioRepository
                .findAll(spec, pageRequest.toPageable())
                .map(ServicioMapper::toListadoResponseDto);

        return PaginationMapper.toPageResponse(page);
    }
}

package com.cipolflo.server.servicios.service;

import com.cipolflo.server.servicios.domain.Servicio;
import com.cipolflo.server.servicios.domain.TarifaServicio;
import com.cipolflo.server.servicios.domain.enums.ModalidadPrecio;
import com.cipolflo.server.servicios.domain.enums.TipoClienteTarifa;
import com.cipolflo.server.servicios.dto.TarifaServicioRequestDto;
import com.cipolflo.server.servicios.dto.TarifaServicioResponseDto;
import com.cipolflo.server.servicios.exception.ServicioValidacionException;
import com.cipolflo.server.servicios.exception.TarifaServicioNotFoundException;
import com.cipolflo.server.servicios.repository.TarifaServicioRepository;
import com.cipolflo.server.servicios.validator.TarifaServicioReglasValidator;
import com.cipolflo.server.shared.exception.ServicioCodigoError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TarifaServicioServiceTest {

    @Mock
    private TarifaServicioRepository tarifaServicioRepository;

    private TarifaServicioService tarifaServicioService;

    @BeforeEach
    void setUp() {
        tarifaServicioService = new TarifaServicioService(
                tarifaServicioRepository,
                new TarifaServicioReglasValidator()
        );
    }

    private Servicio crearServicio(Long id) {
        Servicio servicio = new Servicio();
        servicio.setId(id);
        return servicio;
    }

    private TarifaServicio crearTarifaExistente(
            Long id,
            Servicio servicio,
            TipoClienteTarifa tipoCliente,
            Integer antiguedadMinima,
            Integer antiguedadMaxima
    ) {
        TarifaServicio tarifa = TarifaServicio.registrar(
                servicio,
                tipoCliente,
                BigDecimal.valueOf(1000),
                ModalidadPrecio.POR_DIA,
                antiguedadMinima,
                antiguedadMaxima
        );
        ReflectionTestUtils.setField(tarifa, "id", id);
        return tarifa;
    }

    private TarifaServicioRequestDto crearDto(
            Long id,
            TipoClienteTarifa tipoCliente,
            Integer antiguedadMinima,
            Integer antiguedadMaxima
    ) {
        TarifaServicioRequestDto dto = new TarifaServicioRequestDto();
        dto.setId(id);
        dto.setTipoCliente(tipoCliente);
        dto.setPrecio(BigDecimal.valueOf(1000));
        dto.setModalidadPrecio(ModalidadPrecio.POR_DIA);
        dto.setAntiguedadMinima(antiguedadMinima);
        dto.setAntiguedadMaxima(antiguedadMaxima);
        return dto;
    }

    @Test
    void deberiaRegistrarTarifasInicialesExitosamente() {
        Servicio servicio = crearServicio(1L);

        List<TarifaServicioRequestDto> tarifasDto = List.of(
                crearDto(null, TipoClienteTarifa.PARTICULAR, null, null),
                crearDto(null, TipoClienteTarifa.SOCIO_COMUN, null, null)
        );

        when(tarifaServicioRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<TarifaServicioResponseDto> resultado =
                tarifaServicioService.registrarTarifas(servicio, tarifasDto);

        assertEquals(2, resultado.size());
        verify(tarifaServicioRepository).saveAll(anyList());
    }

    @Test
    void registrarTarifasDebeRechazarCuandoFaltaTarifaObligatoria() {
        Servicio servicio = crearServicio(1L);

        List<TarifaServicioRequestDto> tarifasDto = List.of(
                crearDto(null, TipoClienteTarifa.SOCIO_COMUN, null, null)
        );

        ServicioValidacionException exception = assertThrows(
                ServicioValidacionException.class,
                () -> tarifaServicioService.registrarTarifas(servicio, tarifasDto)
        );

        assertEquals(
                ServicioCodigoError.TARIFAS_OBLIGATORIAS_FALTANTES.name(),
                exception.getCodigo()
        );

        verify(tarifaServicioRepository, never()).saveAll(anyList());
    }

    @Test
    void modificarTarifasDebeActualizarExistentesYCrearNuevasSinTocarLasNoMencionadas() {
        Servicio servicio = crearServicio(1L);

        TarifaServicio particularExistente =
                crearTarifaExistente(10L, servicio, TipoClienteTarifa.PARTICULAR, null, null);
        TarifaServicio socioPoliciaExistente =
                crearTarifaExistente(11L, servicio, TipoClienteTarifa.SOCIO_POLICIA, 0, 5);

        when(tarifaServicioRepository.findByServicioId(1L))
                .thenReturn(List.of(particularExistente, socioPoliciaExistente));
        when(tarifaServicioRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<TarifaServicioRequestDto> tarifasDto = List.of(
                crearDto(10L, TipoClienteTarifa.PARTICULAR, null, null),
                crearDto(null, TipoClienteTarifa.SOCIO_COMUN, null, null)
        );

        List<TarifaServicioResponseDto> resultado =
                tarifaServicioService.modificarTarifas(servicio, tarifasDto);

        assertEquals(3, resultado.size());
        assertTrue(resultado.stream().anyMatch(t -> t.getTipoCliente() == TipoClienteTarifa.SOCIO_POLICIA));
        assertTrue(resultado.stream().anyMatch(t -> t.getTipoCliente() == TipoClienteTarifa.SOCIO_COMUN));

        verify(tarifaServicioRepository, never()).delete(any());
        verify(tarifaServicioRepository, never()).deleteAll(anyList());
    }

    @Test
    void modificarTarifasDebeLanzar404CuandoElIdNoPerteneceAlServicio() {
        Servicio servicio = crearServicio(1L);

        when(tarifaServicioRepository.findByServicioId(1L))
                .thenReturn(List.of());

        List<TarifaServicioRequestDto> tarifasDto = List.of(
                crearDto(99L, TipoClienteTarifa.PARTICULAR, null, null),
                crearDto(null, TipoClienteTarifa.SOCIO_COMUN, null, null)
        );

        TarifaServicioNotFoundException exception = assertThrows(
                TarifaServicioNotFoundException.class,
                () -> tarifaServicioService.modificarTarifas(servicio, tarifasDto)
        );

        assertEquals(
                "Tarifa no encontrada con id: 99 para el servicio 1",
                exception.getMessage()
        );
    }

    @Test
    void modificarTarifasDebeRechazarCuandoElEstadoResultanteQuedaSinTarifaObligatoria() {
        Servicio servicio = crearServicio(1L);

        TarifaServicio particularExistente =
                crearTarifaExistente(10L, servicio, TipoClienteTarifa.PARTICULAR, null, null);
        TarifaServicio socioComunExistente =
                crearTarifaExistente(11L, servicio, TipoClienteTarifa.SOCIO_COMUN, null, null);

        when(tarifaServicioRepository.findByServicioId(1L))
                .thenReturn(List.of(particularExistente, socioComunExistente));

        List<TarifaServicioRequestDto> tarifasDto = List.of(
                crearDto(10L, TipoClienteTarifa.SOCIO_POLICIA, null, null),
                crearDto(11L, TipoClienteTarifa.SOCIO_POLICIA_RETIRADO, null, null)
        );

        ServicioValidacionException exception = assertThrows(
                ServicioValidacionException.class,
                () -> tarifaServicioService.modificarTarifas(servicio, tarifasDto)
        );

        assertEquals(
                ServicioCodigoError.TARIFAS_OBLIGATORIAS_FALTANTES.name(),
                exception.getCodigo()
        );

        verify(tarifaServicioRepository, never()).saveAll(anyList());
    }

    @Test
    void deberiaEliminarTarifaExitosamente() {
        Servicio servicio = crearServicio(1L);

        TarifaServicio particular =
                crearTarifaExistente(10L, servicio, TipoClienteTarifa.PARTICULAR, null, null);
        TarifaServicio socioComun =
                crearTarifaExistente(11L, servicio, TipoClienteTarifa.SOCIO_COMUN, null, null);
        TarifaServicio socioPolicia =
                crearTarifaExistente(12L, servicio, TipoClienteTarifa.SOCIO_POLICIA, null, null);

        when(tarifaServicioRepository.findByIdAndServicioId(12L, 1L))
                .thenReturn(java.util.Optional.of(socioPolicia));
        when(tarifaServicioRepository.findByServicioId(1L))
                .thenReturn(List.of(particular, socioComun, socioPolicia));

        tarifaServicioService.eliminarTarifa(1L, 12L);

        verify(tarifaServicioRepository).delete(socioPolicia);
    }

    @Test
    void eliminarTarifaDebeLanzar404CuandoNoExisteONoPerteneceAlServicio() {
        when(tarifaServicioRepository.findByIdAndServicioId(99L, 1L))
                .thenReturn(java.util.Optional.empty());

        TarifaServicioNotFoundException exception = assertThrows(
                TarifaServicioNotFoundException.class,
                () -> tarifaServicioService.eliminarTarifa(1L, 99L)
        );

        assertEquals(
                "Tarifa no encontrada con id: 99 para el servicio 1",
                exception.getMessage()
        );

        verify(tarifaServicioRepository, never()).delete(any());
    }

    @Test
    void eliminarTarifaDebeRechazarCuandoEsLaUltimaParticular() {
        Servicio servicio = crearServicio(1L);

        TarifaServicio particular =
                crearTarifaExistente(10L, servicio, TipoClienteTarifa.PARTICULAR, null, null);
        TarifaServicio socioComun =
                crearTarifaExistente(11L, servicio, TipoClienteTarifa.SOCIO_COMUN, null, null);

        when(tarifaServicioRepository.findByIdAndServicioId(10L, 1L))
                .thenReturn(java.util.Optional.of(particular));
        when(tarifaServicioRepository.findByServicioId(1L))
                .thenReturn(List.of(particular, socioComun));

        ServicioValidacionException exception = assertThrows(
                ServicioValidacionException.class,
                () -> tarifaServicioService.eliminarTarifa(1L, 10L)
        );

        assertEquals(
                ServicioCodigoError.TARIFA_OBLIGATORIA_NO_ELIMINABLE.name(),
                exception.getCodigo()
        );

        verify(tarifaServicioRepository, never()).delete(any());
    }

    @Test
    void eliminarTarifaDebeRechazarCuandoEsLaUltimaSocioComun() {
        Servicio servicio = crearServicio(1L);

        TarifaServicio particular =
                crearTarifaExistente(10L, servicio, TipoClienteTarifa.PARTICULAR, null, null);
        TarifaServicio socioComun =
                crearTarifaExistente(11L, servicio, TipoClienteTarifa.SOCIO_COMUN, null, null);

        when(tarifaServicioRepository.findByIdAndServicioId(11L, 1L))
                .thenReturn(java.util.Optional.of(socioComun));
        when(tarifaServicioRepository.findByServicioId(1L))
                .thenReturn(List.of(particular, socioComun));

        ServicioValidacionException exception = assertThrows(
                ServicioValidacionException.class,
                () -> tarifaServicioService.eliminarTarifa(1L, 11L)
        );

        assertEquals(
                ServicioCodigoError.TARIFA_OBLIGATORIA_NO_ELIMINABLE.name(),
                exception.getCodigo()
        );

        verify(tarifaServicioRepository, never()).delete(any());
    }

    @Test
    void obtenerTarifasPorServicioDebeMapearLasEntidadesEncontradas() {
        Servicio servicio = crearServicio(1L);
        TarifaServicio tarifa =
                crearTarifaExistente(10L, servicio, TipoClienteTarifa.PARTICULAR, null, null);

        when(tarifaServicioRepository.findByServicioId(1L))
                .thenReturn(List.of(tarifa));

        List<TarifaServicioResponseDto> resultado =
                tarifaServicioService.obtenerTarifasPorServicio(1L);

        assertEquals(1, resultado.size());
        assertEquals(TipoClienteTarifa.PARTICULAR, resultado.get(0).getTipoCliente());
    }
}

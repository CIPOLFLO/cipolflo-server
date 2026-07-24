package com.cipolflo.server.ajustes.service;

import com.cipolflo.server.ajustes.domain.CostoCuotaSocio;
import com.cipolflo.server.ajustes.dto.CostoCuotaRequestDto;
import com.cipolflo.server.ajustes.dto.CostoCuotaResponseDto;
import com.cipolflo.server.ajustes.repository.CostoCuotaSocioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CostoCuotaServiceTest {

    @Mock
    private CostoCuotaSocioRepository costoCuotaSocioRepository;

    @InjectMocks
    private CostoCuotaService costoCuotaService;

    @Test
    void obtenerCostoCuota_devuelveElValorVigente() {
        CostoCuotaSocio costoCuota = new CostoCuotaSocio(new BigDecimal("200.00"));
        when(costoCuotaSocioRepository.findById(CostoCuotaSocio.ID_FIJO)).thenReturn(Optional.of(costoCuota));

        CostoCuotaResponseDto response = costoCuotaService.obtenerCostoCuota();

        assertEquals(new BigDecimal("200.00"), response.monto());
    }

    @Test
    void obtenerCostoCuota_sinFilaSembrada_lanzaIllegalStateException() {
        when(costoCuotaSocioRepository.findById(CostoCuotaSocio.ID_FIJO)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> costoCuotaService.obtenerCostoCuota());
    }

    @Test
    void actualizarCostoCuota_conMontoValido_actualizaYPersiste() {
        CostoCuotaSocio costoCuota = new CostoCuotaSocio(new BigDecimal("200.00"));
        when(costoCuotaSocioRepository.findById(CostoCuotaSocio.ID_FIJO)).thenReturn(Optional.of(costoCuota));
        when(costoCuotaSocioRepository.save(any(CostoCuotaSocio.class))).thenAnswer(inv -> inv.getArgument(0));

        CostoCuotaResponseDto response = costoCuotaService.actualizarCostoCuota(
                new CostoCuotaRequestDto(new BigDecimal("300.00")));

        assertEquals(new BigDecimal("300.00"), response.monto());
        verify(costoCuotaSocioRepository).save(costoCuota);
    }

    @Test
    void actualizarCostoCuota_conMontoInvalido_lanzaExcepcionYNoPersiste() {
        CostoCuotaSocio costoCuota = new CostoCuotaSocio(new BigDecimal("200.00"));
        when(costoCuotaSocioRepository.findById(CostoCuotaSocio.ID_FIJO)).thenReturn(Optional.of(costoCuota));

        assertThrows(IllegalArgumentException.class, () ->
                costoCuotaService.actualizarCostoCuota(new CostoCuotaRequestDto(BigDecimal.ZERO)));
    }
}

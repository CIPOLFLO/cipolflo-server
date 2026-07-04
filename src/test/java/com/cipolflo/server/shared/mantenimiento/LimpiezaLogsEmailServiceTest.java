package com.cipolflo.server.shared.mantenimiento;

import com.cipolflo.server.shared.email.EnvioEmailLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LimpiezaLogsEmailServiceTest {

    @Mock
    private EnvioEmailLogRepository envioEmailLogRepository;

    private LimpiezaLogsEmailService servicio(int retencionDias) {
        LimpiezaLogsEmailProperties props =
                new LimpiezaLogsEmailProperties("0 0 5 * * SUN", "America/Montevideo", retencionDias);
        return new LimpiezaLogsEmailService(envioEmailLogRepository, props);
    }

    @Test
    void purgarLogsVencidos_borraAnterioresAlLimiteYReportaLaCantidad() {
        when(envioEmailLogRepository.deleteByCreatedAtBefore(any())).thenReturn(5);

        String resumen = servicio(90).purgarLogsVencidos();

        ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
        verify(envioEmailLogRepository).deleteByCreatedAtBefore(captor.capture());

        Instant esperado = Instant.now().minus(Duration.ofDays(90));
        long desvioSegundos = Math.abs(Duration.between(esperado, captor.getValue()).getSeconds());
        assertTrue(desvioSegundos < 60, "el límite debe ser ~90 días atrás");

        assertTrue(resumen.contains("5"), "el resumen debe informar cuántos se borraron");
        assertTrue(resumen.contains("90"), "el resumen debe informar la ventana de retención");
    }

    @Test
    void purgarLogsVencidos_sinVencidos_reportaCero() {
        when(envioEmailLogRepository.deleteByCreatedAtBefore(any())).thenReturn(0);

        String resumen = servicio(90).purgarLogsVencidos();

        assertTrue(resumen.contains("0"));
    }

    @Test
    void purgarLogsVencidos_retencionNoPositiva_abortaSinBorrar() {
        assertThrows(IllegalArgumentException.class, () -> servicio(0).purgarLogsVencidos());
        verify(envioEmailLogRepository, never()).deleteByCreatedAtBefore(any());
    }
}

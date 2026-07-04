package com.cipolflo.server.shared.scheduling;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EjecutorTareaProgramadaTest {

    @Mock
    private LogTareaProgramadaRegistrar registrar;

    @InjectMocks
    private EjecutorTareaProgramada ejecutor;

    @Test
    void ejecutar_tareaOk_registraExitoConResumen() {
        ejecutor.ejecutar(TipoTareaProgramada.LIMPIEZA_LOGS_EMAIL, () -> "3 registros eliminados");

        ArgumentCaptor<Instant> inicio = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> fin = ArgumentCaptor.forClass(Instant.class);
        verify(registrar).registrar(
                eq(TipoTareaProgramada.LIMPIEZA_LOGS_EMAIL),
                eq(EstadoEjecucionTarea.EXITO),
                inicio.capture(),
                fin.capture(),
                eq("3 registros eliminados"),
                isNull());

        assertFalse(fin.getValue().isBefore(inicio.getValue()), "fin no puede ser anterior al inicio");
    }

    @Test
    void ejecutar_tareaFalla_registraFallidoYNoPropaga() {
        assertDoesNotThrow(() -> ejecutor.ejecutar(
                TipoTareaProgramada.REPORTE_SEMANAL_RESERVAS,
                () -> { throw new IllegalStateException("boom"); }));

        ArgumentCaptor<String> error = ArgumentCaptor.forClass(String.class);
        verify(registrar).registrar(
                eq(TipoTareaProgramada.REPORTE_SEMANAL_RESERVAS),
                eq(EstadoEjecucionTarea.FALLIDO),
                any(Instant.class),
                any(Instant.class),
                isNull(),
                error.capture());

        assertTrue(error.getValue().contains("boom"), "el detalle debe incluir el mensaje de error");
        assertTrue(error.getValue().contains("IllegalStateException"), "el detalle debe incluir el tipo de excepción");
    }
}

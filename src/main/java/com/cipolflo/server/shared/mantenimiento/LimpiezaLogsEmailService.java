package com.cipolflo.server.shared.mantenimiento;

import com.cipolflo.server.shared.email.EnvioEmailLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/**
 * Purga los registros de envío de emails ({@code envio_emails_logs}) más antiguos que la
 * ventana de retención configurada ({@code cipolflo.tareas.limpieza-logs-email.retencion-dias},
 * default 90 días). Es idempotente: si no hay registros vencidos, no borra nada.
 */
@Service
public class LimpiezaLogsEmailService implements ILimpiezaLogsEmailService {

    private static final Logger log = LoggerFactory.getLogger(LimpiezaLogsEmailService.class);

    private final EnvioEmailLogRepository envioEmailLogRepository;
    private final LimpiezaLogsEmailProperties properties;

    public LimpiezaLogsEmailService(EnvioEmailLogRepository envioEmailLogRepository,
                                    LimpiezaLogsEmailProperties properties) {
        this.envioEmailLogRepository = envioEmailLogRepository;
        this.properties = properties;
    }

    @Override
    @Transactional
    public String purgarLogsVencidos() {
        int dias = properties.retencionDias();
        Instant limite = Instant.now().minus(Duration.ofDays(dias));
        long borrados = envioEmailLogRepository.deleteByCreatedAtBefore(limite);
        log.info("Purga de logs de email: {} registros con más de {} días eliminados (anteriores a {}).",
                borrados, dias, limite);
        return "%d logs de email eliminados (más de %d días, anteriores a %s).".formatted(borrados, dias, limite);
    }
}

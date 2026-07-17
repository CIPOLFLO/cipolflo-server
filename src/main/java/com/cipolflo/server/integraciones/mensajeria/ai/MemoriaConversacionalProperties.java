package com.cipolflo.server.integraciones.mensajeria.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuración de la memoria conversacional del asistente. Se bindea desde
 * {@code cipolflo.mensajeria.memoria.*} en application.properties.
 */
@ConfigurationProperties(prefix = "cipolflo.mensajeria.memoria")
public record MemoriaConversacionalProperties(
        Integer maxMensajes,
        Duration ttlInactividad
) {
}

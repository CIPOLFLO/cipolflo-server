package com.cipolflo.server.shared.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Propiedades de configuración para Azure Document Intelligence.
 * Los valores se leen desde application.properties bajo el prefijo azure.document-intelligence.
 */

@ConfigurationProperties(prefix = "azure.document-intelligence")
@Validated
public record AzureDocumentIntelligenceProperties(
        @NotBlank String endpoint,
        @NotBlank String key
) {
}



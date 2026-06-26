package com.cipolflo.server.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de configuración para Azure Document Intelligence.
 * Los valores se leen desde application.properties bajo el prefijo azure.document-intelligence.
 */

@ConfigurationProperties(prefix = "azure.document-intelligence")
public record AzureDocumentIntelligenceProperties(

        String endpoint,
        String key
) {}



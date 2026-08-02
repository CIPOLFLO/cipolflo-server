package com.cipolflo.server.manuales.service;

import com.cipolflo.server.manuales.domain.CategoriaManual;
import com.cipolflo.server.manuales.domain.Manual;
import com.cipolflo.server.manuales.dto.ManualResponseDto;
import com.cipolflo.server.manuales.exception.ManualNoDisponibleException;
import com.cipolflo.server.manuales.exception.ManualNoEncontradoException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Sirve los manuales estáticos que viajan dentro del JAR.
 */
@Slf4j
@Service
public class ManualService implements IManualService {

    private final ResourceLoader resourceLoader;

    public ManualService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public List<ManualResponseDto> listarManuales(CategoriaManual categoria) {
        return Manual.porCategoria(categoria).stream()
                .map(this::aDto)
                .toList();
    }

    @Override
    public ManualDescarga descargarManual(String clave) {
        Manual manual = Manual.porClave(clave)
                .orElseThrow(() -> new ManualNoEncontradoException(clave));

        Resource contenido = recurso(manual);

        if (!contenido.exists()) {
            log.warn("Se solicitó el manual '{}' pero el archivo {} no está publicado",
                    manual.getClave(), manual.getRutaClasspath());
            throw new ManualNoDisponibleException(manual.getTitulo());
        }

        return new ManualDescarga(manual, contenido);
    }

    /**
     * Deja constancia al arrancar de qué manuales todavía no tienen PDF publicado.
     *
     * <p>Sirve para detectar en el log de despliegue un manual que quedó declarado en el
     * catálogo pero cuyo archivo no llegó al build.</p>
     */
    @PostConstruct
    void registrarManualesPendientes() {
        List<String> pendientes = Arrays.stream(Manual.values())
                .filter(manual -> !recurso(manual).exists())
                .map(Manual::getClave)
                .toList();

        if (pendientes.isEmpty()) {
            log.info("Catálogo de manuales completo: {} manuales publicados", Manual.values().length);
        } else {
            log.info("Manuales declarados sin PDF publicado ({}): {}", pendientes.size(), pendientes);
        }
    }

    private Resource recurso(Manual manual) {
        return resourceLoader.getResource(ResourceLoader.CLASSPATH_URL_PREFIX + manual.getRutaClasspath());
    }

    private ManualResponseDto aDto(Manual manual) {
        boolean disponible = recurso(manual).exists();

        return new ManualResponseDto(
                manual.getClave(),
                manual.getTitulo(),
                manual.getCategoria(),
                disponible,
                manual.getVersion(),
                manual.getFechaActualizacion()
        );
    }
}

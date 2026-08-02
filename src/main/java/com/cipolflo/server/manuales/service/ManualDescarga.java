package com.cipolflo.server.manuales.service;

import com.cipolflo.server.manuales.domain.Manual;
import org.springframework.core.io.Resource;

/**
 * PDF listo para ser enviado al cliente, junto con el manual del catálogo que lo describe.
 *
 * <p>Se transporta como {@link Resource} y no como {@code byte[]} para que Spring lo
 * streamee desde el classpath: un manual con imágenes puede pesar varios MB y no tiene
 * sentido cargarlo entero en memoria en cada request.</p>
 */
public record ManualDescarga(Manual manual, Resource contenido) {}

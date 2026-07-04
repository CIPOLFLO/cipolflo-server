package com.cipolflo.server.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita las tareas programadas ({@code @Scheduled}) de la aplicación.
 *
 * Los schedulers (ver {@code *.scheduled}) son cáscaras delgadas: solo definen el
 * "cuándo" (cron) y delegan la lógica en un service. Los cron y zonas horarias se
 * configuran en application.properties para poder ajustarlos sin recompilar.
 *
 * Nota: por defecto Spring ejecuta todas las tareas {@code @Scheduled} en un único
 * hilo. Con el volumen actual (un reporte semanal + una limpieza) alcanza; si se
 * agregan tareas pesadas o concurrentes, exponer aquí un {@code TaskScheduler} con pool.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}

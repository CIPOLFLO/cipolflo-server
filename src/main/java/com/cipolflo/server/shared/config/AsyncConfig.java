package com.cipolflo.server.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Habilita el procesamiento asíncrono y expone pools dedicados por tipo de tarea.
 * Los listeners de eventos que mandan correos usan {@code @Async("mailExecutor")}
 * para no bloquear el hilo de la request mientras se conecta al SMTP. El procesador
 * de mensajes de Telegram usa {@code @Async("telegramExecutor")} por la misma razón:
 * que el 200 del webhook no espere a la latencia del modelo de IA.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "mailExecutor")
    public Executor mailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("mail-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "telegramExecutor")
    public Executor telegramExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("telegram-");
        executor.initialize();
        return executor;
    }
}

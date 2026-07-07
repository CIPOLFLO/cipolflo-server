package com.cipolflo.server.shared.config;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AsyncConfigTest {

    @Test
    void mailExecutor_seCreaConfigurado() {
        Executor executor = new AsyncConfig().mailExecutor();

        assertNotNull(executor);
        ThreadPoolTaskExecutor pool = assertInstanceOf(ThreadPoolTaskExecutor.class, executor);
        assertNotNull(pool.getThreadPoolExecutor());
    }
}

package com.industrieit.ledger.contract.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ServiceConfig {
    /**
     * @return a single thread executor to run on
     */
    @Bean
    public ExecutorService executorService() {
        return Executors.newSingleThreadExecutor();
    }

    /**
     * @return {@link Logger} for standardized logging
     */
    @Bean
    public Logger logger() {
        return LoggerFactory.getLogger("com.industrieit.ledger.contract");
    }
}

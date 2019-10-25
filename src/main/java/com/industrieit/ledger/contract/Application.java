package com.industrieit.ledger.contract;

import com.industrieit.ledger.security.consumer.config.ApiSecurityConfiguration;
import com.industrieit.ledger.security.consumer.model.WellKnownJsonUrl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ComponentScan(basePackages = {"com.industrieit.ledger.contract", "com.industrieit.ledger.security"})
@Import(ApiSecurityConfiguration.class)
public class Application implements CommandLineRunner {
    @Value(value = "${auth.domain}")
    String authDomain;

    @Override
    public void run(String... arg0) {
        if (arg0.length > 0 && arg0[0].equals("exitcode")) {
            throw new ExitException();
        }
    }

    /**
     * @return {@link WellKnownJsonUrl} on the Auth Server. Ledger is protected by same JWT-based security as is other micro-services.
     */
    @Bean
    public WellKnownJsonUrl wellKnownJsonUrl() {
        return new WellKnownJsonUrl(authDomain + "/.well-known/jwks.json");
    }

    /**
     * Exit Exception on command line args of exitcode
     */
    public static class ExitException extends RuntimeException implements ExitCodeGenerator {
        private static final long serialVersionUID = 1L;

        @Override
        public int getExitCode() {
            return 10;
        }

    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

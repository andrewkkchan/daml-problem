package com.industrieit.ledger.security.consumer.config;

import com.google.common.collect.ImmutableList;
import com.industrieit.ledger.security.consumer.filter.JWTAuthorizationFilter;
import com.industrieit.ledger.security.consumer.helper.ConsumerAlgorithmProvider;
import com.industrieit.ledger.security.consumer.helper.JWKSetLoader;
import com.industrieit.ledger.security.consumer.helper.JWTDecoder;
import com.industrieit.ledger.security.consumer.model.WellKnownJsonUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ApiSecurityConfiguration extends WebSecurityConfigurerAdapter {
    private final WellKnownJsonUrl wellKnownJsonUrl;
    private final JWKSetLoader jwkSetLoader;
    private final ConsumerAlgorithmProvider consumerAlgorithmProvider;
    private final JWTDecoder jwtDecoder;

    @Autowired
    public ApiSecurityConfiguration(WellKnownJsonUrl wellKnownJsonUrl, JWKSetLoader jwkSetLoader, ConsumerAlgorithmProvider consumerAlgorithmProvider, JWTDecoder jwtDecoder) {
        this.wellKnownJsonUrl = wellKnownJsonUrl;
        this.jwkSetLoader = jwkSetLoader;
        this.consumerAlgorithmProvider = consumerAlgorithmProvider;
        this.jwtDecoder = jwtDecoder;
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and()
                .csrf().disable()
                .authorizeRequests()
                .mvcMatchers( "/health", "/credentials").permitAll()
                .antMatchers("/api-docs", "/configuration/ui", "/swagger-resources", "/configuration/security",
                        "/swagger-ui.html", "/webjars/**","/swagger-resources/configuration/ui","/swagger-ui.html").permitAll()
                .mvcMatchers(HttpMethod.POST, "/user").permitAll()
                .mvcMatchers(HttpMethod.POST, "/password").permitAll()
                .mvcMatchers(HttpMethod.POST, "/password/recovery").permitAll()
                .mvcMatchers(HttpMethod.GET, "/.well-known/jwks.json").permitAll()
                .mvcMatchers(HttpMethod.POST, "/user/login").permitAll()
                .mvcMatchers(HttpMethod.POST, "/sign-up").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilter(new JWTAuthorizationFilter(authenticationManager(), wellKnownJsonUrl, jwkSetLoader, consumerAlgorithmProvider, jwtDecoder))
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        ;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(ImmutableList.of("*"));
        configuration.setAllowedMethods(ImmutableList.of("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(ImmutableList.of("Authorization", "Cache-Control", "Content-Type"));
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}

package com.industrieit.ledger.security.consumer.filter;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.industrieit.ledger.security.consumer.constant.SecurityConstants;
import com.industrieit.ledger.security.consumer.helper.ConsumerAlgorithmProvider;
import com.industrieit.ledger.security.consumer.helper.JWKSetLoader;
import com.industrieit.ledger.security.consumer.helper.JWTDecoder;
import com.industrieit.ledger.security.consumer.model.WellKnownJsonUrl;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {
    private final WellKnownJsonUrl wellKnownJsonUrl;
    private final JWKSetLoader jwkSetLoader;
    private final ConsumerAlgorithmProvider consumerAlgorithmProvider;
    private final JWTDecoder jwtDecoder;


    public JWTAuthorizationFilter(AuthenticationManager authenticationManager, WellKnownJsonUrl wellKnownJsonUrl, JWKSetLoader jwkSetLoader, ConsumerAlgorithmProvider consumerAlgorithmProvider, JWTDecoder jwtDecoder) {
        super(authenticationManager);
        this.wellKnownJsonUrl = wellKnownJsonUrl;
        this.jwkSetLoader = jwkSetLoader;
        this.consumerAlgorithmProvider = consumerAlgorithmProvider;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String token = request.getHeader(SecurityConstants.HEADER_STRING);
        if (token == null || !token.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }
        UsernamePasswordAuthenticationToken authentication;
        authentication = getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(@NonNull String token) throws IOException {
        // parse the token.
        Algorithm algorithm;
        try {
            JWKSet jwkSet = jwkSetLoader.load(wellKnownJsonUrl);
            algorithm = consumerAlgorithmProvider.provide(jwkSet);
        } catch (ParseException | JOSEException e) {
            throw new AuthenticationCredentialsNotFoundException("Fail to get Authentication");
        }
        DecodedJWT decodedJWT = jwtDecoder.decode(token, algorithm);

        String principal = decodedJWT.getSubject();
        List<SimpleGrantedAuthority> simpleGrantedAuthorities = null;
        if (decodedJWT.getClaim(SecurityConstants.AUTHORITIES) != null) {
            simpleGrantedAuthorities = decodedJWT.getClaim(SecurityConstants.AUTHORITIES).asList(String.class) == null ? null : decodedJWT.getClaim(SecurityConstants.AUTHORITIES).asList(String.class).stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        }
        if (principal != null) {
            return new UsernamePasswordAuthenticationToken(principal, null, simpleGrantedAuthorities);
        } else {
            throw new AuthenticationCredentialsNotFoundException("Fail to parse the principal from the token");
        }
    }
}


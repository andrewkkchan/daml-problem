package com.industrieit.ledger.security.consumer.helper;

import com.auth0.jwt.algorithms.Algorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPublicKey;

import static com.auth0.jwt.algorithms.Algorithm.RSA256;

@Component
public class ConsumerAlgorithmProvider {
    public Algorithm provide(JWKSet jwkSet) throws JOSEException {
        if (jwkSet.getKeys().isEmpty() || !(jwkSet.getKeys().get(0) instanceof RSAKey)) {
            throw new AuthenticationCredentialsNotFoundException("Fail to get JWKS");
        }
        RSAKey rsaKey = (RSAKey) jwkSet.getKeys().get(0);
        RSAPublicKey rsaPublicKey = rsaKey.toRSAPublicKey();
        return RSA256(rsaPublicKey, null);
    }
}

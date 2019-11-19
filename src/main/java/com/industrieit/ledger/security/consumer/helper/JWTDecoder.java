package com.industrieit.ledger.security.consumer.helper;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.industrieit.ledger.security.consumer.constant.SecurityConstants;
import org.springframework.stereotype.Component;

@Component
public class JWTDecoder {
    public DecodedJWT decode(String token, Algorithm algorithm){
        return JWT.require(algorithm)
                .build()
                .verify(token.replace(SecurityConstants.TOKEN_PREFIX, ""));
    }
}

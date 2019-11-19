package com.industrieit.ledger.security.consumer.helper;

import com.industrieit.ledger.security.consumer.model.WellKnownJsonUrl;
import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

@Component
public class JWKSetLoader {
    public JWKSet load(WellKnownJsonUrl wellKnownJsonUrl) throws IOException, ParseException {
        return JWKSet.load(new URL(wellKnownJsonUrl.getUrl()));
    }
}

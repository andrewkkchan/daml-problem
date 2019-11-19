package com.industrieit.ledger.security.consumer.service.impl;

import com.industrieit.ledger.security.consumer.service.ConsumerTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ConsumerTokenServiceImpl implements ConsumerTokenService {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Override
    public String getCurrentUserName() {
        if (SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getPrincipal() != null
                && !SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString().isEmpty()) {
            String principal = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            return principal;
        }
        LOGGER.warn("No user slug found in security context");
        return null;
    }
}

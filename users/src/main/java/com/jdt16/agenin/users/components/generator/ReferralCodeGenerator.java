package com.jdt16.agenin.users.components.generator;

import org.springframework.stereotype.Component;
import org.apache.commons.lang3.RandomStringUtils;

@Component
public class ReferralCodeGenerator {
    public String generateReferralCode() {
        return RandomStringUtils.secure().nextAlphanumeric(10).toUpperCase();
    }
}

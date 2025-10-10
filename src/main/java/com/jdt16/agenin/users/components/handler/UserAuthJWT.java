package com.jdt16.agenin.users.components.handler;

import com.jdt16.agenin.users.dto.entity.UserEntityDTO;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserAuthJWT {

    private static final byte[] SECRET;
    static {
        SECRET = new byte[32];
        new SecureRandom().nextBytes(SECRET);
        // log.debug("JWT SECRET (base64) = {}", java.util.Base64.getEncoder().encodeToString(SECRET)); // HATI-HATI: hanya untuk debug
    }

    public String generateAuthToken(UserEntityDTO user, long expiresInSeconds) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }

        // Pastikan ada subject (fallback: ID -> email -> phone -> random)
        String subject = resolveSubject(user);

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expiresInSeconds);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .jwtID(UUID.randomUUID().toString())
                .claim("USER_ID", user.getUserEntityDTOId() != null ? user.getUserEntityDTOId().toString() : null)
                .claim("USER_EMAIL", user.getUserEntityDTOEmail())
                .claim("USER_NAME", user.getUserEntityDTOFullName())
                .claim("USER_IS_ADMIN", Boolean.TRUE.equals(user.getUserEntityDTOIsAdmin()))
                .build();

        try {
            SignedJWT signed = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            signed.sign(new MACSigner(SECRET));
            return signed.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to sign JWT", e);
        }
    }

    private String resolveSubject(UserEntityDTO u) {
        if (u.getUserEntityDTOId() != null) return u.getUserEntityDTOId().toString();
        if (u.getUserEntityDTOEmail() != null && !u.getUserEntityDTOEmail().isBlank()) return u.getUserEntityDTOEmail();
        if (u.getUserEntityDTOPhoneNumber() != null && !u.getUserEntityDTOPhoneNumber().isBlank()) return u.getUserEntityDTOPhoneNumber();
        log.warn("User has no ID/email/phone – generating temporary subject.");
        return "temp-" + UUID.randomUUID();
    }
}

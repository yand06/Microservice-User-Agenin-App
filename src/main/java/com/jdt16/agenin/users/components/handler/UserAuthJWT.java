package com.jdt16.agenin.users.components.handler;

import com.jdt16.agenin.users.dto.entity.UserEntityDTO;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserAuthJWT {

    public String generateAuthToken(UserEntityDTO userEntityDTO, int TTL) {
        try {

            Instant ISSUED_AT = Instant.now();
            Instant EXPIRED_AT = ISSUED_AT.plusSeconds(TTL);

            RSAPublicKey publicKey = UserAuthJWTUtility.loadPublicKey("certificate/public-key.pem");

            JWTClaimsSet.Builder claimsSetBuilder = new JWTClaimsSet.Builder();

            claimsSetBuilder.subject(userEntityDTO.getUserEntityDTOId().toString());
            claimsSetBuilder.issuer(userEntityDTO.getUserEntityDTOEmail());
            claimsSetBuilder.issueTime(Date.from(ISSUED_AT));
            claimsSetBuilder.expirationTime(Date.from(EXPIRED_AT));
            claimsSetBuilder.claim("USER_ID", userEntityDTO.getUserEntityDTOId());

            JWTClaimsSet jwtClaimsSet = claimsSetBuilder.build();

            JWEHeader header = new JWEHeader.Builder(
                    JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM
            ).contentType("JWT")
                    .build();


            EncryptedJWT encryptedJWT = new EncryptedJWT(header, jwtClaimsSet);

            RSAEncrypter encrypter = new RSAEncrypter(publicKey);
            encryptedJWT.encrypt(encrypter);

            return encryptedJWT.serialize();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

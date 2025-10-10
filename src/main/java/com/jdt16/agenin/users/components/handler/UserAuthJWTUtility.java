package com.jdt16.agenin.users.components.handler;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


@Configuration
public class UserAuthJWTUtility {


    @Bean
    public JwtEncoder jwtEncoder() throws GeneralSecurityException, IOException {
        // Load RSA private and public keys from the resources directory
        RSAPrivateKey privateKey = loadPrivateKey("certificate/private-key.pem");
        RSAPublicKey publicKey = loadPublicKey("certificate/public-key.pem");

        // Create RSA key representation for JWK
        JWK jwk = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder() throws GeneralSecurityException, IOException {
        // Use the public key to create the JwtDecoder
        RSAPublicKey publicKey = loadPublicKey("certificate/public-key.pem");
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }


    /**
     * Load and parse RSA private key from a PEM file in the resource folder.
     *
     * @param path Path to the private key PEM file.
     * @return RSAPrivateKey instance.
     */
    public static RSAPrivateKey loadPrivateKey(String path) {
        try {
            // Load PEM file from resources
            String key = readKeyFromFile(path);

            // Remove PEM file markers
            key = key.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            // Decode base64-encoded key
            byte[] keyBytes = Base64.getDecoder().decode(key);

            // Generate RSA private key
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load private key from path: " + path, e);
        }
    }

    /**
     * Load and parse RSA public key from a PEM file in the resources folder.
     *
     * @param path Path to the public key PEM file.
     * @return RSAPublicKey instance.
     */
    public static RSAPublicKey loadPublicKey(String path) throws IOException, GeneralSecurityException {
        try {
            // Load PEM file from resources
            String key = readKeyFromFile(path);

            // Remove PEM file markers
            key = key.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            // Decode base64-encoded key
            byte[] keyBytes = Base64.getDecoder().decode(key);

            // Generate RSA public key
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load public key from path: " + path, e);
        }
    }

    /**
     * Helper method to read the key file from the resources folder and return its content as a string.
     *
     * @param path The path to the PEM file in the resources folder.
     * @return The key content as a single string.
     * @throws IOException If the file could not be read.
     */
    private static String readKeyFromFile(String path) throws Exception {
        Resource resource = new ClassPathResource(path);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder keyBuilder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                keyBuilder.append(line).append("\n");
            }
            return keyBuilder.toString();
        }
    }
}

package com.entrusting.backend.ars.security;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.*;
import java.util.Base64;

/**
 * RSA 키 제공자
 * ARS PIN 암호화/복호화를 위한 RSA 키페어 관리
 */
@Service
@Getter
@Slf4j
@EnableScheduling
public class RsaKeyProvider {

    private KeyPair keyPair;
    private String kid;

    @PostConstruct
    public void init() {
        rotateKey();
    }

    @Scheduled(fixedRate = 3600000) // 1 Hour
    public void rotateKey() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            this.keyPair = keyGen.generateKeyPair();
            this.kid = java.util.UUID.randomUUID().toString();
            log.info("[Issuer] RSA Key Rotated. New KID: {}", this.kid);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("RSA Key Generation Failed", e);
        }
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    public String getKid() {
        return kid;
    }

    public String getPublicKeyAsString() {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }
}

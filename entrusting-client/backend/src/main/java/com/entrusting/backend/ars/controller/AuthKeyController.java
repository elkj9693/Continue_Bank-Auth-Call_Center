package com.entrusting.backend.ars.controller;

import com.entrusting.backend.ars.security.RsaKeyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 인증 관련 엔드포인트 (공개키 제공)
 */
@RestController
@RequestMapping("/issuer/auth")
@RequiredArgsConstructor
public class AuthKeyController {

    private final RsaKeyProvider rsaKeyProvider;

    /**
     * RSA 공개키 및 KID 제공
     */
    @GetMapping("/public-key")
    public Map<String, String> getPublicKey() {
        return Map.of(
                "publicKey", rsaKeyProvider.getPublicKeyAsString(),
                "kid", rsaKeyProvider.getKid());
    }
}

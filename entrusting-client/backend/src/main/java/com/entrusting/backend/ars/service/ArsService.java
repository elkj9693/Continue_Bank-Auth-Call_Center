package com.entrusting.backend.ars.service;

import com.entrusting.backend.ars.entity.Card;
import com.entrusting.backend.ars.repository.CardRepository;
import com.entrusting.backend.ars.security.RsaKeyProvider;
import com.entrusting.backend.user.repository.UserRepository;
import com.entrusting.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.entrusting.backend.util.EncryptionUtils;
import lombok.extern.slf4j.Slf4j;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;
import java.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.security.spec.MGF1ParameterSpec;

/**
 * ARS 서비스
 * PIN 복호화, 검증, 카드 조회 등
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ArsService {

    private final RsaKeyProvider rsaKeyProvider;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    /**
     * 1. Identify Customer by Phone (Simulated ANI)
     */
    public Map<String, Object> identifyCustomer(String phoneNumber) {
        log.info("[ARS-DEBUG] Raw input phone: [{}]", phoneNumber);

        if (phoneNumber == null) {
            log.warn("[ARS-DEBUG] Phone number is NULL");
            return Map.of("found", false);
        }

        // [Security] Normalizing phone number (removing hyphens, spaces, etc.)
        String normalizedPhone = phoneNumber.replaceAll("\\D", "");
        log.info("[ARS-DEBUG] Normalized phone: [{}]", normalizedPhone);

        // [Report Check] As per Step 678, Phone number is NOT an encryption target for
        // identification.
        // We search by PLAIN normalized phone.
        List<User> users = userRepository.findByPhoneNumber(normalizedPhone);

        if (users.isEmpty()) {
            // Fallback for transition: check encrypted as well
            log.info("[ARS-DEBUG] Not found in plain, checking encrypted...");
            users = userRepository.findByPhoneNumber(EncryptionUtils.encrypt(normalizedPhone));
        }

        if (!users.isEmpty()) {
            User user = users.get(0);
            String decryptedName = EncryptionUtils.decrypt(user.getName());
            log.info("[ARS-DEBUG] Customer found: {} (ID: {})", decryptedName, user.getId());
            return Map.of(
                    "found", true,
                    "name", decryptedName,
                    "customerRef", user.getId().toString());
        } else {
            log.warn("[ARS-DEBUG] Customer NOT found in DB. Total Users: {}", userRepository.count());
            return Map.of("found", false);
        }
    }

    /**
     * 2. Verify PIN and Get Cards
     */
    public Map<String, Object> verifyPinAndGetCards(Map<String, Object> request) {
        String customerRef = (String) request.get("customerRef");
        String inputKid = (String) request.get("kid");
        String ciphertext = (String) request.get("ciphertext");

        log.info("[ARS-DEBUG] PIN Verification started for customerRef: {}", customerRef);

        // Validate Key ID
        if (inputKid == null || !inputKid.equals(rsaKeyProvider.getKid())) {
            log.error("[ARS-DEBUG] Invalid Key ID. Input: {}, Actual: {}", inputKid, rsaKeyProvider.getKid());
            return Map.of("success", false, "status", "LOCKED", "message", "Invalid Key ID");
        }

        // Decrypt PIN (OAEP)
        String pin;
        try {
            pin = decrypt(ciphertext);
            log.info("[ARS-DEBUG] PIN Decrypted successfully");
        } catch (Exception e) {
            log.error("[ARS-DEBUG] PIN Decryption Failed: {}", e.getMessage());
            return Map.of("success", false, "status", "FAIL", "message", "Decryption Failed");
        }

        String inputHash = hash(pin);
        List<Card> cards = cardRepository.findByCustomerRefOrderByIdAsc(customerRef);
        log.info("[ARS-DEBUG] Found {} cards for customer", cards.size());

        if (cards.isEmpty()) {
            log.error("[ARS-DEBUG] No cards found for customer ID: {}", customerRef);
            return Map.of("success", false, "status", "FAIL", "message", "No cards found");
        }

        boolean pinMatched = cards.stream()
                .anyMatch(c -> inputHash.equals(c.getPinHash()));

        if (pinMatched) {
            log.info("[ARS-DEBUG] PIN Matched!");
            List<Map<String, String>> cardList = cards.stream()
                    .map(c -> Map.of(
                            "cardRef", c.getCardRef(),
                            "cardNo", c.getCardNo(),
                            "status", c.getStatus()))
                    .collect(Collectors.toList());

            return Map.of(
                    "success", true,
                    "status", "SUCCESS",
                    "cards", cardList);
        } else {
            log.error("[ARS-DEBUG] PIN Mismatch");
            return Map.of("success", false, "status", "FAIL", "message", "Invalid PIN");
        }
    }

    private String decrypt(String ciphertext) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        // [Security] explicitly set MGF1 to SHA-256 to match CallCenter's encryption
        // Spec
        OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"),
                PSource.PSpecified.DEFAULT);
        cipher.init(Cipher.DECRYPT_MODE, rsaKeyProvider.getPrivateKey(), oaepParams);
        return new String(cipher.doFinal(Base64.getDecoder().decode(ciphertext)));
    }

    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}

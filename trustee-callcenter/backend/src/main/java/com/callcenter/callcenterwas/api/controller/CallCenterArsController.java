package com.callcenter.callcenterwas.api.controller;

import com.callcenter.callcenterwas.common.util.SecurityUtil;
import com.callcenter.callcenterwas.infrastructure.client.IssuerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/callcenter/ars")
@RequiredArgsConstructor
@Slf4j
public class CallCenterArsController {

    private final IssuerClient issuerClient;

    /**
     * Identify Customer by Phone (ANI Detection)
     */
    @PostMapping("/identify")
    public Map<String, Object> identifyCustomer(@RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        log.info("[ARS] Identifying customer with ANI: {}", phoneNumber);
        return issuerClient.identifyCustomer(phoneNumber);
    }

    /**
     * Verify PIN with 3-Tier Security (Encryption)
     */
    @PostMapping("/verify-pin")
    public Map<String, Object> verifyPin(@RequestBody Map<String, String> request) {
        String customerRef = request.get("customerRef");
        String pin = request.get("pin");

        log.info("[ARS] Verifying PIN for customerRef: {} using 3-Tier Security", customerRef);

        try {
            // STEP 1: Get Public Key from Bank (Issuer)
            Map<String, Object> keyResponse = issuerClient.getPublicKey();
            String kid = (String) keyResponse.get("kid");
            String publicKeyStr = (String) keyResponse.get("publicKey");

            // STEP 2: Convert to PublicKey Object
            byte[] publicBytes = Base64.getDecoder().decode(publicKeyStr);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            // STEP 3: Encrypt PIN using OAEP
            String ciphertext = SecurityUtil.encrypt(pin, publicKey);

            // STEP 4: Send Encrypted PIN to Bank for Verification
            Map<String, Object> result = issuerClient.verifyPin(customerRef, kid, ciphertext);

            log.info("[ARS] PIN Verification Result: {}", result.get("status"));
            return result;

        } catch (Exception e) {
            log.error("[ARS] PIN Encryption or Verification Error", e);
            return Map.of("success", false, "status", "ERROR", "message", e.getMessage());
        }
    }

    /**
     * Report Card Loss
     */
    @PostMapping("/report-loss")
    public Map<String, Object> reportLoss(@RequestBody Map<String, Object> request) {
        String customerRef = (String) request.get("customerRef");
        @SuppressWarnings("unchecked")
        List<String> selectedCardRefs = (List<String>) request.get("selectedCardRefs");

        log.info("[ARS] Reporting loss for customerRef: {}, cardRefs: {}", customerRef, selectedCardRefs);

        Map<String, Object> result = issuerClient.reportCardLoss(customerRef, selectedCardRefs);

        // Audit Logging
        issuerClient.sendAuditEvent("CARD_LOSS_REPORT", (boolean) result.get("success") ? "SUCCESS" : "FAIL",
                "ARS_SYSTEM", "Card loss reported via ARS");

        return result;
    }
}

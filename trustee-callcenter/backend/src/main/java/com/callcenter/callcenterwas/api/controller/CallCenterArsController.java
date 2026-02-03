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
@RequestMapping("/api/v1/ars")
@RequiredArgsConstructor
@Slf4j
public class CallCenterArsController {

    private final IssuerClient issuerClient;
    private final com.callcenter.callcenterwas.domain.consultation.service.ConsultationService consultationService;
    private final com.callcenter.callcenterwas.domain.log.service.LogService logService;

    /**
     * Identify Customer by Phone (ANI Detection)
     */
    @PostMapping("/identify")
    public Map<String, Object> identifyCustomer(@RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        log.info("[ARS] Identifying customer with ANI: {}", phoneNumber);

        // API 호출 및 로그 기록 (은행은 'found' 키를 사용함)
        Map<String, Object> result = issuerClient.identifyCustomer(phoneNumber);
        boolean found = Boolean.TRUE.equals(result.get("found"));

        logService.logIntegration("N/A", "/api/v1/ars/identify", "POST",
                found ? 200 : 404,
                "Customer Identification Result: " + found);

        return result;
    }

    /**
     * Verify PIN with 3-Tier Security (Encryption)
     */
    @PostMapping("/verify-pin")
    public Map<String, Object> verifyPin(@RequestBody Map<String, String> request) {
        String customerRef = request.get("customerRef");
        // [MODIFIED] ARS does not send customerName, so we start with anonymous
        String pin = request.get("pin");

        log.info("[ARS] Verifying PIN for customerRef: {} using 3-Tier Security", customerRef);

        // 1. 상담 케이스 생성 (ARS - LOSS_REPORT)
        // Initially masked name is unknown ("익명")
        com.callcenter.callcenterwas.domain.consultation.entity.ConsultationCase consultationCase = consultationService
                .createCase("ARS", "LOSS_REPORT", customerRef, null, "SYSTEM");

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
            boolean success = (boolean) result.get("success");

            // [NEW] Update Customer Name if returned by Bank
            if (success && result.containsKey("customerName")) {
                String fetchedName = (String) result.get("customerName");
                consultationService.updateCustomerName(consultationCase.getId(), fetchedName);
                log.info("[ARS] Updated Case {} with Customer Name: {}", consultationCase.getId(), fetchedName);
            }

            // 2. 인증 로그 기록
            logService.logArsAuth(consultationCase.getId(), customerRef, "ARS_PIN",
                    success ? "SUCCESS" : "FAIL", success ? "" : (String) result.get("message"), 0);

            // 3. 연동 로그 기록
            logService.logIntegration(kid, "/api/v1/ars/verify-pin", "POST",
                    success ? 200 : 401, "PIN Verification " + success);

            log.info("[ARS] PIN Verification Result: {}", result.get("status"));

            // 4. 본인 확인 실패 시 케이스 종료
            if (!success) {
                consultationService.closeCase(consultationCase.getId(), "본인 확인 실패 (비밀번호 불일치)");
            }

            // 결과에 Case ID 포함 (추후 정지 시 사용)
            Map<String, Object> response = new java.util.HashMap<>(result);
            response.put("caseId", consultationCase.getId());
            return response;

        } catch (Exception e) {
            log.error("[ARS] PIN Encryption or Verification Error", e);
            logService.logArsAuth(consultationCase.getId(), customerRef, "ARS_PIN", "ERROR", e.getMessage(), 1);
            consultationService.closeCase(consultationCase.getId(), "시스템 에러: " + e.getMessage());
            return Map.of("success", false, "status", "ERROR", "message", e.getMessage());
        }
    }

    /**
     * Explicitly close a consultation case (for early hangup or cancellation)
     */
    @PostMapping("/close-case")
    public Map<String, Object> closeCase(@RequestBody Map<String, String> request) {
        Long caseId = Long.valueOf(request.get("caseId"));
        String note = request.getOrDefault("note", "상담 종료");

        log.info("[ARS] Explicitly closing case: {}, Note: {}", caseId, note);
        consultationService.closeCase(caseId, note);

        return Map.of("success", true);
    }

    /**
     * Report Card Loss
     */
    @PostMapping("/report-loss")
    public Map<String, Object> reportLoss(@RequestBody Map<String, Object> request) {
        String customerRef = (String) request.get("customerRef");
        Long caseId = Long.valueOf(request.get("caseId").toString());
        @SuppressWarnings("unchecked")
        List<String> selectedCardRefs = (List<String>) request.get("selectedCardRefs");

        log.info("[ARS] Reporting loss for customerRef: {}, cardRefs: {}, caseId: {}", customerRef, selectedCardRefs,
                caseId);

        // API 호출
        Map<String, Object> result = issuerClient.reportCardLoss(customerRef, selectedCardRefs);
        boolean success = Boolean.TRUE.equals(result.get("success"));

        // 1. 연동 로그 기록
        logService.logIntegration("N/A", "/api/v1/ars/report-loss", "POST",
                success ? 200 : 500, "Card Loss Report " + success);

        // 2. 감시 로그 기록
        logService.logAudit("SYSTEM", "CARD_LOSS_REPORT", caseId.toString(), "CASE",
                "Reported loss for ids: " + selectedCardRefs);

        // 3. 케이스 종료 (상세 결과 메시지 포함)
        if (success) {
            consultationService.closeCase(caseId, "분실 신고 완료 (" + selectedCardRefs.size() + "건)");
        } else {
            consultationService.closeCase(caseId, "분실 신고 실패: " + result.get("message"));
        }

        return result;
    }
}

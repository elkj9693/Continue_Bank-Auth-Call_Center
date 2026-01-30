package com.entrusting.backend.user.controller;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/compliance")
public class ComplianceController {

    private final com.entrusting.backend.user.repository.UserRepository userRepository;
    private final com.entrusting.backend.user.repository.AccessLogRepository accessLogRepository;

    public ComplianceController(com.entrusting.backend.user.repository.UserRepository userRepository,
                                com.entrusting.backend.user.repository.AccessLogRepository accessLogRepository) {
        this.userRepository = userRepository;
        this.accessLogRepository = accessLogRepository;
    }

    @PostMapping("/marketing-consent")
    public Map<String, Object> registerMarketingConsent(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String productName = request.get("productName");
        
        com.entrusting.backend.user.entity.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Update Consent: Only Affiliate TM Center Provision
        user.setSsapProvisionAgreed(true);

        userRepository.save(user);

        // Audit Log
        accessLogRepository.save(new com.entrusting.backend.user.entity.AccessLog(
                user.getId(),
                "SELF",
                user.getUsername(),
                "MARKETING_CONSENT",
                "이벤트 참여 동의 (Product: " + productName + ")",
                "WEB"
        ));
        
        UUID requestId = UUID.randomUUID();
        LocalDate retentionUntil = LocalDate.now().plusMonths(3);
        
        return Map.of(
            "status", "SUCCESS",
            "requestId", requestId.toString(),
            "retentionUntil", retentionUntil.toString(),
            "message", "마케팅 활용 동의가 성공적으로 기록되었습니다. (보유기한: 3개월)"
        );
    }

    @GetMapping("/marketing-consented-users")
    public java.util.List<Map<String, Object>> getMarketingConsentedUsers() {
        return userRepository.findByMarketingAgreedTrue().stream()
            .map(user -> Map.<String, Object>of(
                "id", user.getId(),
                "name", com.entrusting.backend.util.EncryptionUtils.decrypt(user.getName()), // Decrypt for display
                "phone", com.entrusting.backend.util.EncryptionUtils.decrypt(user.getPhoneNumber()),
                "type", "신용카드 권유",
                "purpose", "상품 소개 및 권유",
                "retentionUntil", LocalDate.now().plusMonths(3).toString()
            ))
            .collect(java.util.stream.Collectors.toList());
    }
    @GetMapping("/my-consent")
    public Map<String, Object> getMyConsentStatus(@RequestParam String username) {
        com.entrusting.backend.user.entity.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        return Map.of(
            "marketingAgreed", user.getMarketingAgreed(), // Fixed naming convention
            "marketingSms", user.getMarketingSms(),
            "ssapProvisionAgreed", user.getSsapProvisionAgreed(), // 3rd party
            "thirdPartyProvisionAgreed", user.getThirdPartyProvisionAgreed() != null ? user.getThirdPartyProvisionAgreed() : false
        );
    }

    @PostMapping("/update-consent")
    public Map<String, Object> updateConsent(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        String consentType = (String) request.get("consentType"); // marketing, ssap, thirdParty, personal
        Boolean agreed = (Boolean) request.get("agreed");

        com.entrusting.backend.user.entity.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        String logDetail = "";

        switch (consentType) {
            case "MARKETING":
                user.setMarketingAgreed(agreed);
                if (agreed) user.setMarketingSms(true);
                else user.setMarketingSms(false);
                logDetail = "마케팅 정보 수신 동의 변경: " + agreed;
                break;
            case "SSAP_PROVISION": // Affiliate TM
                user.setSsapProvisionAgreed(agreed);
                logDetail = "제휴 TM 센터(Continue Call) 제공 동의 변경: " + agreed;
                break;
            case "THIRD_PARTY": // General Third Party
                user.setThirdPartyProvisionAgreed(agreed);
                logDetail = "제3자 정보 제공 동의 변경: " + agreed;
                break;

            default:
                throw new IllegalArgumentException("Unknown consent type: " + consentType);
        }
        
        userRepository.save(user);

        // Audit Log
        accessLogRepository.save(new com.entrusting.backend.user.entity.AccessLog(
                user.getId(),
                "SELF",
                user.getUsername(),
                "UPDATE_CONSENT",
                logDetail,
                "WEB"
        ));

        return Map.of(
            "status", "SUCCESS",
            "message", "동의 상태가 변경되었습니다.",
            "updatedAt", java.time.LocalDateTime.now().toString()
        );
    }

    @PostMapping("/withdraw-consent")
    public Map<String, Object> withdrawConsent(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String consentType = request.get("consentType"); // e.g., "ALL", "3RD_PARTY"

        com.entrusting.backend.user.entity.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Revoke Consents
        user.setMarketingAgreed(false);
        user.setMarketingSms(false);
        user.setSsapProvisionAgreed(false);
        user.setThirdPartyProvisionAgreed(false);

        userRepository.save(user);

        // Audit Log for Compliance
        accessLogRepository.save(new com.entrusting.backend.user.entity.AccessLog(
                user.getId(),
                "SELF",
                user.getUsername(),
                "WITHDRAW_CONSENT",
                "마케팅 동의 철회 (Type: " + consentType + ")",
                "WEB"
        ));

        return Map.of(
            "status", "SUCCESS",
            "message", "마케팅 동의가 성공적으로 철회되었습니다.",
            "withdrawnAt", java.time.LocalDateTime.now().toString()
        );
    }
}

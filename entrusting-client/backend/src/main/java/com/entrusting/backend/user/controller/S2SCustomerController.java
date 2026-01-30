package com.entrusting.backend.user.controller;

import com.entrusting.backend.user.entity.Account;
import com.entrusting.backend.user.entity.User;
import com.entrusting.backend.user.repository.AccountRepository;
import com.entrusting.backend.user.repository.UserRepository;
import com.entrusting.backend.util.EncryptionUtils;
import com.entrusting.backend.user.repository.AccessLogRepository;
import com.entrusting.backend.user.entity.AccessLog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/s2s")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5175", "http://localhost:5173"}) // Allow Call Center & Self
public class S2SCustomerController {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AccessLogRepository accessLogRepository;

    // S2S token validation (simplified for demo)
    private static final String VALID_S2S_TOKEN = "callcenter-service-token-2026";

    /**
     * Phase 1: 발신번호로 회원 조회
     */
    @PostMapping("/members/lookup")
    public ResponseEntity<?> lookupMember(@RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        if (phoneNumber == null) {
            return ResponseEntity.badRequest().body("phoneNumber is required");
        }

        String cleanPhone = phoneNumber.replaceAll("\\D", "");
        String encryptedPhone = EncryptionUtils.encrypt(cleanPhone);
        
        java.util.List<User> users = userRepository.findByPhoneNumber(encryptedPhone);
        
        Map<String, Object> response = new HashMap<>();
        if (!users.isEmpty()) {
            User user = users.get(0);
            response.put("isExist", true);
            response.put("memberId", user.getUsername()); 
            
            String fullName = EncryptionUtils.decrypt(user.getName());
            String maskedName = maskName(fullName);
            response.put("maskedName", maskedName);

            // [COMPLIANCE] 조회 로그 (존재하는 회원인 경우만)
            accessLogRepository.save(new AccessLog(
                user.getId(),
                "TM_AGENT",
                "unknown-agent",
                "SEARCH_MEMBER",
                "발신번호로 회원 검색: " + maskedName,
                "S2S-API"
            ));
        } else {
            response.put("isExist", false);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Phase 3: 고객 360 View 조회
     */
    @GetMapping("/customers/{memberId}")
    public ResponseEntity<?> getCustomerDetail(@PathVariable String memberId) {
        User user = userRepository.findByUsername(memberId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Account> accounts = accountRepository.findByUserId(user.getId());

        // [COMPLIANCE] 개인정보 조회 로그 기록
        accessLogRepository.save(new AccessLog(
            user.getId(),
            "TM_AGENT",
            "unknown-agent", // 실제로는 헤더에서 상담원 ID를 받아와야 함
            "VIEW_360",
            "고객 360 상세 정보 조회",
            "S2S-API"
        ));

        Map<String, Object> response = new HashMap<>();
        
        Map<String, Object> basic = new HashMap<>();
        basic.put("name", EncryptionUtils.decrypt(user.getName()));
        basic.put("phone", EncryptionUtils.decrypt(user.getPhoneNumber()));
        basic.put("email", user.getUsername() + "@continue.com");
        basic.put("address", "서울시 강남구 테헤란로 123");
        
        // [COMPLIANCE] 제3자 제공 동의 상태 및 보유기간 전달
        basic.put("thirdPartyProvisionAgreed", user.getSsapProvisionAgreed());
        basic.put("retentionUntil", user.getThirdPartyProvisionRetentionUntil());
        
        response.put("basic", basic);

        List<Map<String, Object>> cards = accounts.stream().map(acc -> {
            Map<String, Object> card = new HashMap<>();
            card.put("cardId", acc.getId().toString());
            card.put("cardName", "Continue " + acc.getAccountType());
            card.put("cardNumber", acc.getAccountNumber());
            card.put("status", "정상");
            card.put("creditLimit", 10000000);
            card.put("usedAmount", acc.getBalance().longValue());
            return card;
        }).collect(Collectors.toList());
        response.put("cards", cards);

        response.put("recentHistory", new ArrayList<>());
        response.put("consultHistory", new ArrayList<>());

        return ResponseEntity.ok(response);
    }

    /**
     * Phase 4: 마케팅 동의 고객 목록 조회 (Outbound용)
     */
    @PostMapping("/marketing-consented")
    public ResponseEntity<?> getMarketingConsentedUsers(
            @RequestHeader(value = "X-Service-Token", required = false) String serviceToken) {

        if (serviceToken == null || !VALID_S2S_TOKEN.equals(serviceToken)) {
            return ResponseEntity.status(403).body(Map.of("error", "INVALID_TOKEN"));
        }

        List<Map<String, Object>> candidates = userRepository.findByMarketingAgreedTrue().stream()
                .map(user -> {
                    String decryptedName = EncryptionUtils.decrypt(user.getName());
                    String decryptedPhone = EncryptionUtils.decrypt(user.getPhoneNumber());

                    Map<String, Object> map = new HashMap<>();
                    map.put("customerRef", "CU" + user.getId());
                    map.put("name", maskName(decryptedName));
                    map.put("phone", maskPhone(decryptedPhone));
                    map.put("marketingAgreedAt", user.getTermsAgreedAt());
                    return map;
                })
                .collect(Collectors.toList());

        accessLogRepository.save(new AccessLog(
                0L, // System
                "CALLCENTER",
                "callcenter-was",
                "BATCH_RETRIEVE",
                "마케팅 동의 고객 목록 조회 (Outbound)",
                "S2S-API"
        ));

        return ResponseEntity.ok(Map.of(
            "count", candidates.size(),
            "candidates", candidates
        ));
    }

    /**
     * Phase 5: 감사 로그 조회 (Admin Dashboard용)
     */
    @GetMapping("/audit/logs")
    public ResponseEntity<?> getAuditLogs(
            @RequestHeader(value = "X-Service-Token", required = false) String serviceToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        // In real world, strictly validate token
        // if (serviceToken == null || !VALID_S2S_TOKEN.equals(serviceToken)) { ... }

        List<AccessLog> logs = accessLogRepository.findAll(
            org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "accessedAt"))
        ).getContent();
        
        return ResponseEntity.ok(logs);
    }

    private String maskName(String name) {
        if (name == null || name.length() < 2) return name;
        if (name.length() == 2) return name.charAt(0) + "*";
        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 10) return "***-****-****";
        String normalized = phone.replaceAll("\\D", "");
        if (normalized.length() == 11) {
            return normalized.substring(0, 3) + "-****-" + normalized.substring(7);
        } else if (normalized.length() == 10) {
            return normalized.substring(0, 3) + "-***-" + normalized.substring(6);
        }
        return "***-****-****";
    }
}

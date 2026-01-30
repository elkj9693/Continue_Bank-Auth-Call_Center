package com.entrusting.backend.user.controller;

import com.entrusting.backend.user.entity.User;
import com.entrusting.backend.user.entity.AccessLog;
import com.entrusting.backend.user.repository.UserRepository;
import com.entrusting.backend.user.repository.AccessLogRepository;
import com.entrusting.backend.util.EncryptionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * [COMPLIANCE] 동의 관리 컨트롤러
 * 개인정보보호법 제37조 - 정보주체의 동의 철회권
 */
@RestController
@RequestMapping("/api/consent")
@CrossOrigin(origins = {"http://localhost:5175", "http://localhost:5173"})
public class ConsentController {

    private final UserRepository userRepository;
    private final AccessLogRepository accessLogRepository;

    public ConsentController(UserRepository userRepository, AccessLogRepository accessLogRepository) {
        this.userRepository = userRepository;
        this.accessLogRepository = accessLogRepository;
    }

    /**
     * 현재 동의 상태 조회
     */
    @GetMapping("/status")
    public ResponseEntity<?> getConsentStatus(@RequestParam Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        return ResponseEntity.ok(Map.of(
            "marketingAgreed", user.getMarketingAgreed() != null && user.getMarketingAgreed(),
            "marketingSms", user.getMarketingSms() != null && user.getMarketingSms(),
            "marketingEmail", user.getMarketingEmail() != null && user.getMarketingEmail(),
            "marketingPush", user.getMarketingPush() != null && user.getMarketingPush(),
            "ssapProvisionAgreed", user.getSsapProvisionAgreed() != null && user.getSsapProvisionAgreed()
        ));
    }

    /**
     * 마케팅 동의 변경
     * [COMPLIANCE] 동의 철회 시 즉시 TM 대상에서 제외됨
     */
    @PutMapping("/marketing")
    public ResponseEntity<?> updateMarketingConsent(
            @RequestParam Long userId,
            @RequestBody Map<String, Boolean> request) {
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        
        if (request.containsKey("marketingAgreed")) {
            user.setMarketingAgreed(request.get("marketingAgreed"));
        }
        if (request.containsKey("marketingSms")) {
            user.setMarketingSms(request.get("marketingSms"));
        }
        if (request.containsKey("marketingEmail")) {
            user.setMarketingEmail(request.get("marketingEmail"));
        }
        if (request.containsKey("marketingPush")) {
            user.setMarketingPush(request.get("marketingPush"));
        }

        userRepository.save(user);

        // [COMPLIANCE] 동의 변경 이력 기록
        accessLogRepository.save(new AccessLog(
            user.getId(),
            "SELF",
            user.getUsername(),
            "UPDATE_CONSENT",
            "마케팅 동의 변경: " + request,
            "WEB"
        ));

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "동의 설정이 변경되었습니다."
        ));
    }

    /**
     * 제3자 제공 동의 (TM센터) 변경
     */
    @PutMapping("/third-party")
    public ResponseEntity<?> updateThirdPartyConsent(
            @RequestParam Long userId,
            @RequestBody Map<String, Boolean> request) {
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        
        if (request.containsKey("ssapProvisionAgreed")) {
            boolean agreed = request.get("ssapProvisionAgreed");
            user.setSsapProvisionAgreed(agreed);
            
            if (agreed) {
                // [COMPLIANCE] 동의 시 3개월 보관 기간 설정
                user.setThirdPartyProvisionRetentionUntil(java.time.LocalDateTime.now().plusMonths(3));
            } else {
                // [COMPLIANCE] 동의 철회 시 즉시 파기 (또는 null 처리)
                user.setThirdPartyProvisionRetentionUntil(null);
            }
        }

        userRepository.save(user);

        // [COMPLIANCE] 동의 변경 이력 기록
        accessLogRepository.save(new AccessLog(
            user.getId(),
            "SELF",
            user.getUsername(),
            "UPDATE_CONSENT",
            "제3자 제공 동의 변경: " + request,
            "WEB"
        ));

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "제3자 제공 동의가 변경되었습니다."
        ));
    }
}

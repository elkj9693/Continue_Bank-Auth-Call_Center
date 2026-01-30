package com.entrusting.backend.user.controller;

import com.entrusting.backend.user.dto.AuthCallbackRequest;
import com.entrusting.backend.user.dto.LoginRequest;
import com.entrusting.backend.user.dto.RegisterRequest;
import com.entrusting.backend.user.entity.User;
import com.entrusting.backend.user.service.UserService;
import com.entrusting.backend.user.service.S2SAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class UserController {

    private final UserService userService;
    private final S2SAuthService s2sAuthService;

    @org.springframework.beans.factory.annotation.Value("${trustee.api.base-url}")
    private String trusteeApiBaseUrl;

    public UserController(UserService userService, S2SAuthService s2sAuthService) {
        this.userService = userService;
        this.s2sAuthService = s2sAuthService;
    }

    /**
     * 회원가입
     * 
     * [S2S 토큰 소비 로직]
     * 1. 토큰 존재 여부 확인
     * 2. 수탁사에 토큰 소비 요청 (POST /consume/{tokenId})
     * 3. 토큰이 USED 상태로 변경됨 (재사용 불가)
     * 4. 인증된 정보와 가입 정보 일치 여부 확인
     * 5. 회원가입 진행
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        try {
            // [1] 토큰 존재 여부 확인
            String tokenId = request.getTokenId();
            if (tokenId == null || tokenId.isEmpty()) {
                throw new IllegalArgumentException("본인인증 토큰이 누락되었습니다. 다시 시도해 주세요.");
            }

            // [1.5] 약관 동의 여부 선검증 제거 (사용자 요청)
            // if (request.getTermsAgreement() == null || !request.getTermsAgreement().isAllRequiredAgreed()) {
            //    throw new IllegalArgumentException("필수 약관에 모두 동의해야 합니다.");
            // }

            // [2] S2S 토큰 소비 (일회성 사용)
            // consume API는 토큰을 USED 상태로 변경하여 재사용 방지
            java.util.Map<String, Object> verification;
            try {
                verification = s2sAuthService.consumeTokenWithTrustee(tokenId);
            } catch (S2SAuthService.S2SAuthException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
            
            if (verification == null) {
                throw new IllegalArgumentException("본인인증 검증에 실패했습니다. 다시 시도해 주세요.");
            }
            
            // [3] 토큰 상태 확인 (USED 또는 COMPLETED)
            String status = String.valueOf(verification.get("status"));
            if (!"USED".equals(status) && !"COMPLETED".equals(status)) {
                throw new IllegalArgumentException("본인인증이 완료되지 않았습니다. (상태: " + status + ")");
            }

            // [4] 인증된 정보와 가입 정보 일치 여부 확인
            String verifiedName = (String) verification.get("name");
            String verifiedPhone = (String) verification.get("phoneNumber");
            
            if (verifiedName != null && !verifiedName.equals(request.getName())) {
                System.err.println("[ENTRUSTING-SEC] Name mismatch! Verified: " + verifiedName + 
                                 ", Request: " + request.getName());
                throw new IllegalArgumentException("본인인증 성명과 가입 성명이 일치하지 않습니다.");
            }
            
            // [선택적] 전화번호 일치 확인
            if (verifiedPhone != null) {
                String cleanVerifiedPhone = verifiedPhone.replaceAll("\\D", "");
                String cleanRequestPhone = request.getPhoneNumber().replaceAll("\\D", "");
                if (!cleanVerifiedPhone.equals(cleanRequestPhone)) {
                    System.err.println("[ENTRUSTING-SEC] Phone mismatch! Verified: " + cleanVerifiedPhone + 
                                     ", Request: " + cleanRequestPhone);
                    throw new IllegalArgumentException("본인인증 전화번호와 가입 전화번호가 일치하지 않습니다.");
                }
            }

            // [5] 회원가입 진행
            request.setVerified(true);
            userService.registerUser(request);
            
            System.out.println("[ENTRUSTING] Register SUCCESS - Name: " + request.getName() + 
                             ", Username: " + request.getUsername());
            return ResponseEntity.ok("User registered successfully");
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("[ENTRUSTING-ERROR] Register failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body("서버 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.loginUser(request);
            java.util.Map<String, String> response = new java.util.HashMap<>();
            response.put("status", "success");
            response.put("username", user.getUsername());
            response.put("name", com.entrusting.backend.util.EncryptionUtils.decrypt(user.getName()));
            response.put("phoneNumber", com.entrusting.backend.util.EncryptionUtils.decrypt(user.getPhoneNumber()));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/callback")
    public org.springframework.http.ResponseEntity<?> authCallback(@RequestBody AuthCallbackRequest request) {
        System.out.println("[ENTRUSTING-DEBUG] Callback Request Received - Phone: " + request.getPhoneNumber()
                + ", Token: " + request.getTokenId());
        java.util.Map<String, String> response = new java.util.HashMap<>();
        try {
            // [보안 강화] S2SAuthService를 사용하여 수탁사 서버에 토큰 상태 확인
            java.util.Map<String, Object> statusResponse = s2sAuthService.verifyTokenWithTrustee(String.valueOf(request.getTokenId()));
            
            if (statusResponse == null || !"COMPLETED".equals(String.valueOf(statusResponse.get("status")))) {
                System.err.println("[ENTRUSTING-DEBUG] Verification Incomplete - Status: " + 
                                 (statusResponse != null ? statusResponse.get("status") : "null"));
                throw new Exception("인증이 완료되지 않았거나 토큰이 유효하지 않습니다.");
            }

            userService.updateUserVerifiedStatus(request.getPhoneNumber(), true);
            System.out.println("[ENTRUSTING-DEBUG] Callback Success for: " + request.getPhoneNumber());
            response.put("status", "success");
            response.put("message", "verified successfully");

            // 수탁사에서 받은 실제 이름을 사용
            String verifiedName = (String) statusResponse.get("name");
            response.put("name", verifiedName != null ? verifiedName : "인증완료");

            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("[ENTRUSTING-DEBUG] Callback Error: " + e.getMessage());
            response.put("status", "error");
            response.put("message", "Verification failed: " + e.getMessage());
            return org.springframework.http.ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/find-id")
    public ResponseEntity<String> findId(@RequestParam String phoneNumber, @RequestParam String name) {
        try {
            String username = userService.findUsernameByPhoneNumber(phoneNumber, name);
            return ResponseEntity.ok(username);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String username, @RequestParam String newPassword,
            @RequestParam String phoneNumber, @RequestParam String name) {
        try {
            userService.resetPassword(username, newPassword, phoneNumber, name);
            return ResponseEntity.ok("Password reset successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

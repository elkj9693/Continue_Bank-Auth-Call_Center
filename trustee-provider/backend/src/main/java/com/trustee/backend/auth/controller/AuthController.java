package com.trustee.backend.auth.controller;

import com.trustee.backend.auth.dto.AuthConfirmRequest;
import com.trustee.backend.auth.dto.AuthInitRequest;
import com.trustee.backend.auth.dto.AuthInitResponse;
import com.trustee.backend.auth.dto.AuthOtpRequest;
import com.trustee.backend.auth.dto.AuthStatusResponse;
import com.trustee.backend.auth.dto.AuthVerificationResponse;
import com.trustee.backend.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/init")
    public ResponseEntity<AuthInitResponse> initAuth(@RequestBody AuthInitRequest request) {
        AuthInitResponse response = authService.initAuth(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{tokenId}")
    public ResponseEntity<AuthStatusResponse> getAuthStatus(@PathVariable UUID tokenId) {
        AuthStatusResponse response = authService.getAuthStatus(tokenId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(@RequestBody AuthOtpRequest request) {
        System.out.println("[TRUSTEE-API] requestOtp CALLED - TokenId: " + request.getTokenId() + ", Phone: " + request.getPhoneNumber());
        try {
            AuthInitResponse response = authService.requestOtp(request);
            System.out.println("[TRUSTEE-API] requestOtp SUCCESS - OTP Generated: " + response.getOtp());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            System.err.println("[TRUSTEE-API] requestOtp FAILED - " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/confirm")
    public ResponseEntity<Void> confirmAuth(@RequestBody AuthConfirmRequest request) {
        authService.confirmAuth(request);
        return ResponseEntity.noContent().build();
    }

    /**
     * [S2S API] 토큰 상태 조회 (읽기 전용)
     * - 토큰 상태만 확인, 상태 변경 없음
     */
    @GetMapping("/verify/{tokenId}")
    public ResponseEntity<AuthVerificationResponse> verifyToken(@PathVariable UUID tokenId) {
        AuthVerificationResponse response = authService.verifyToken(tokenId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * [S2S API] 토큰 소비 (일회성 사용)
     * 
     * 위탁사 백엔드가 수탁사에게 "이 토큰 진짜 니네가 인증해준 거 맞아?" 확인하고
     * 확인되면 토큰을 USED 상태로 변경하여 재사용 방지
     * 
     * [중요] 이 API는 위탁사 백엔드에서만 호출해야 함 (S2S 통신)
     * 프론트엔드에서 직접 호출하면 안 됨
     * 
     * @param tokenId 소비할 토큰 ID
     * @return 인증된 사용자 정보
     */
    @PostMapping("/consume/{tokenId}")
    public ResponseEntity<?> consumeToken(@PathVariable UUID tokenId) {
        try {
            AuthVerificationResponse response = authService.consumeToken(tokenId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            System.err.println("[TRUSTEE-S2S] Consume failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
}

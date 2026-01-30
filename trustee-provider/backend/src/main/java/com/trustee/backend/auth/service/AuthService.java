package com.trustee.backend.auth.service;

import com.trustee.vpass.util.CryptoUtils;

import com.trustee.backend.auth.dto.AuthConfirmRequest;
import com.trustee.backend.auth.dto.AuthInitRequest;
import com.trustee.backend.auth.dto.AuthInitResponse;
import com.trustee.backend.auth.dto.AuthStatusResponse;
import com.trustee.backend.auth.dto.AuthVerificationResponse;
import com.trustee.backend.auth.entity.AuthStatus;
import com.trustee.backend.auth.entity.AuthToken;
import com.trustee.backend.auth.repository.AuthTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 본인인증 서비스
 * 
 * [2026 컴플라이언스 대응]
 * - 신용정보법: 본인확인 기록 2년 보관
 * - 개인정보보호법: 최소 수집, 목적 달성 시 파기
 * - 금융위 가이드라인: 인증 유효시간 3분 제한
 */
@Service
public class AuthService {

    private final AuthTokenRepository authTokenRepository;
    private final MockCarrierDatabase mockCarrierDatabase;
    private final SmsService smsService;
    
    // [보안] SecureRandom 사용 (Random 대신 암호학적으로 안전한 난수 생성)
    private final SecureRandom secureRandom = new SecureRandom();
    
    // OTP 유효 시간 (분)
    @Value("${auth.otp.validity-minutes:3}")
    private int otpValidityMinutes;
    
    // 테스트 모드 여부 (true면 응답에 OTP 포함)
    @Value("${auth.test-mode:true}")
    private boolean testMode;

    public AuthService(AuthTokenRepository authTokenRepository, 
                       MockCarrierDatabase mockCarrierDatabase,
                       SmsService smsService) {
        this.authTokenRepository = authTokenRepository;
        this.mockCarrierDatabase = mockCarrierDatabase;
        this.smsService = smsService;
    }
    
    /**
     * 6자리 OTP 생성 (암호학적으로 안전한 난수)
     */
    private String generateOtp() {
        return String.format("%06d", secureRandom.nextInt(1000000));
    }

    @Transactional
    public AuthInitResponse initAuth(AuthInitRequest request) {
        UUID tokenId = UUID.randomUUID();
        // [보안] SecureRandom을 사용한 6자리 OTP 생성
        String otp = generateOtp();

        String receivedName = request.getName();
        String rawPhone = request.getClientData();
        String cleanPhone = (rawPhone != null) ? rawPhone.replaceAll("\\D", "") : "";
        
        System.out.println("[TRUSTEE] initAuth - Name: [" + receivedName + "], Phone: [" + cleanPhone + "]");

        // [보안] 인증 세션 생성 (전화번호는 숫자로만 정제하여 저장)
        AuthToken authToken = new AuthToken(tokenId, request.getAuthRequestId(), cleanPhone, receivedName, request.getCarrier(),
                otp,
                AuthStatus.PENDING,
                LocalDateTime.now());
        authTokenRepository.save(authToken);
        
        // ============================================================
        // [SMS 발송] AWS SNS 연동 시 실제 발송됨
        // 현재는 sms.enabled=false로 콘솔 로그만 출력
        // ============================================================
        boolean smsSent = smsService.sendOtp(cleanPhone, otp);
        if (!smsSent) {
            System.err.println("[TRUSTEE-ERROR] SMS 발송 실패 - Phone: " + cleanPhone);
            // 발송 실패해도 세션은 유지 (재발송 가능)
        }
        
        // ============================================================
        // [테스트 모드] OTP를 응답에 포함 (운영 시 제거)
        // application.properties: auth.test-mode=false 로 변경
        // ============================================================
        if (testMode) {
            System.out.println("[TRUSTEE-TEST] 테스트 모드 - OTP 응답 포함: " + otp);
            return new AuthInitResponse(tokenId, otp);
        }
        
        // [운영 모드] OTP는 SMS로만 전달, 응답에는 미포함
        return new AuthInitResponse(tokenId, null);
    }

    @Transactional(readOnly = true)
    public AuthStatusResponse getAuthStatus(UUID tokenId) {
        AuthToken authToken = authTokenRepository.findById(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("AuthToken not found with id: " + tokenId));
        
        String name = authToken.getName();
        String phone = authToken.getClientData();
        
        // [보안] 데이터가 암호화된 상태라면 상태(status)와 관계없이 무조건 복호화 시도하여 평문 보장
        try {
            if (name != null && name.length() > 15) { 
                String decrypted = CryptoUtils.decryptAES256(name);
                System.out.println("[TRUSTEE-DEBUG] getAuthStatus DECRYPT Name: " + name.substring(0, Math.min(10, name.length())) + "... -> " + decrypted);
                name = decrypted;
            }
            if (phone != null && phone.length() > 15) {
                String decrypted = CryptoUtils.decryptAES256(phone);
                System.out.println("[TRUSTEE-DEBUG] getAuthStatus DECRYPT Phone: " + phone.substring(0, Math.min(10, phone.length())) + "... -> " + decrypted);
                phone = decrypted;
            }
        } catch (Exception e) {
            // 복호화 실패 시 원본 데이터(평문일 가능성 높음) 유지
            System.err.println("[TRUSTEE-ERROR] getAuthStatus Decryption failed: " + e.getMessage() + " (Raw Name: " + (name != null && name.length() > 20 ? name.substring(0, 20) + "..." : name) + ")");
        }
        
        return new AuthStatusResponse(authToken.getTokenId(), authToken.getStatus(), name, formatPhoneNumberWithHyphens(phone));
    }

    @Transactional
    public void confirmAuth(AuthConfirmRequest request) {
        UUID requestedTokenId = request.getTokenId();
        String requestedOtp = request.getOtp();

        System.out.println("[TRUSTEE-DEBUG] Verification Attempt - TokenID: " + requestedTokenId + ", OTP: ["
                + requestedOtp + "]");

        if (requestedTokenId == null || requestedOtp == null || requestedOtp.trim().isEmpty()) {
            System.err.println("[TRUSTEE-DEBUG] Validation Failed: TokenID or OTP is null/empty");
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "인증 토큰 또는 번호가 누락되었습니다.");
        }

        AuthToken authToken = authTokenRepository.findById(requestedTokenId)
                .orElseThrow(() -> {
                    System.err.println("[TRUSTEE-DEBUG] Token NOT FOUND in DB: " + requestedTokenId);
                    return new org.springframework.web.server.ResponseStatusException(
                            org.springframework.http.HttpStatus.NOT_FOUND, "인증 세션이 만료되었거나 존재하지 않습니다. 다시 시도해 주세요.");
                });

        // [컴플라이언스] OTP 유효 시간 검증 (기본 3분, 설정 가능)
        if (LocalDateTime.now().isAfter(authToken.getCreatedAt().plusMinutes(otpValidityMinutes))) {
            System.err.println("[TRUSTEE-ERROR] Token Expired: " + requestedTokenId);
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.GONE, "인증 유효 시간이 만료되었습니다. 다시 시작해 주세요.");
        }

        // [보안] 인증 시도 횟수 제한 (5회)
        if (authToken.getRetryCount() >= 5) {
            System.err.println("[TRUSTEE-ERROR] Retry Limit Exceeded: " + requestedTokenId);
            authToken.setStatus(AuthStatus.EXPIRED);
            authTokenRepository.save(authToken);
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.TOO_MANY_REQUESTS, "인증 시도 횟수(5회)를 초과하였습니다. 처음부터 다시 시작해 주세요.");
        }

        String storedOtp = authToken.getOtp().trim();
        String sentOtp = requestedOtp.trim();

        System.out.println("[TRUSTEE-DEBUG] Comparing OTPs - Stored: [" + storedOtp + "], Received: [" + sentOtp + "]");

        if (!storedOtp.equals(sentOtp)) {
            authToken.incrementRetryCount();
            authTokenRepository.save(authToken);
            
            System.err.println("[TRUSTEE-DEBUG] MISMATH !! Stored: [" + storedOtp + "] != Received: [" + sentOtp + "]");
            int remaining = 5 - authToken.getRetryCount();
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "인증번호가 일치하지 않습니다. (남은 횟수: " + remaining + "회)");
        }

        // [핵심] 이미 완료된 경우 중복 암호화 방지
        if (authToken.getStatus() == AuthStatus.COMPLETED || authToken.getStatus() == AuthStatus.USED) {
            System.out.println("[TRUSTEE-DEBUG] Already COMPLETED/USED. Skipping re-encryption.");
            return;
        }

        System.out.println("[TRUSTEE-DEBUG] OTP Match SUCCESS. Generating CI/DI and Encrypting...");
        String cleanPhone = authToken.getClientData().replaceAll("\\D", "").trim();

        // [핵심] 인증번호가 맞더라도, 실제 통신사 정보와 일치하는지 최종 단계에서 검증
        if (!mockCarrierDatabase.verifyIdentity(cleanPhone, authToken.getName(), authToken.getCarrier(), authToken.getResidentFront())) {
            authToken.incrementRetryCount();
            authTokenRepository.save(authToken);
            
            System.err
                    .println("[TRUSTEE-ERROR] Identity Disclosure Mismatch at FINAL step for: " + authToken.getName()
                            + " (" + authToken.getCarrier() + ")");
            int remaining = 5 - authToken.getRetryCount();
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "정보 불일치: 입력하신 정보와 통신사 명의 정보가 일치하지 않습니다. (남은 횟수: " + remaining + "회)");
        }

        // [COMPLIANCE] CI/DI 생성
        // 실제로는 통신사로부터 받은 실명/주민번호 정보로 생성
        // 교육용으로 이름과 전화번호 기반 생성
        String ci = CryptoUtils.generateCI(authToken.getName(), cleanPhone);
        String di = CryptoUtils.generateDI(ci, "ENTRUSTING-BANK"); // 위탁사 코드
        
        authToken.setCi(ci);
        authToken.setDi(di);
        authToken.setStatus(AuthStatus.COMPLETED);
        
        // [COMPLIANCE] 개인정보 암호화 저장
        authToken.setName(CryptoUtils.encryptAES256(authToken.getName()));
        authToken.setClientData(CryptoUtils.encryptAES256(authToken.getClientData()));
        
        authTokenRepository.save(authToken);
        System.out.println("[TRUSTEE-DEBUG] SUCCESS !! Verification Completed for Token: " + requestedTokenId);
        System.out.println("[COMPLIANCE] CI/DI Generated - CI: " + ci.substring(0, 20) + "...");
    }

    @Transactional
    public AuthInitResponse requestOtp(com.trustee.backend.auth.dto.AuthOtpRequest request) {
        AuthToken authToken = authTokenRepository.findById(request.getTokenId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 세션입니다. (Token NOT FOUND)"));

        // [컴플라이언스] 유효 시간 검증
        if (LocalDateTime.now().isAfter(authToken.getCreatedAt().plusMinutes(otpValidityMinutes))) {
            throw new IllegalArgumentException("인증 유효 시간이 만료되었습니다. 처음부터 다시 진행해 주세요.");
        }

        // [핵심] 위탁사에서 등록한 정보와 현재 입력한 정보가 일치하는지 검증
        String expectedPhone = authToken.getClientData().replaceAll("\\D", "").trim();
        String inputPhone = request.getPhoneNumber().replaceAll("\\D", "").trim();
        String expectedName = (authToken.getName() != null) ? authToken.getName().trim() : "";
        String inputName = (request.getName() != null) ? request.getName().trim() : "";

        System.out.println("[TRUSTEE-SEC] Validating Identity - Expected: [" + expectedName + ", " + expectedPhone
                + "], Input: [" + inputName + ", " + inputPhone + "]");

        if (!expectedPhone.equals(inputPhone) || (!expectedName.equals(inputName))) {
            throw new IllegalArgumentException("정보 불일치: 위탁사에 등록된 정보와 일치하지 않습니다.");
        }

        // [핵심] 재전송 시에도 통신사 실명 대조
        if (!mockCarrierDatabase.verifyIdentity(inputPhone, inputName, request.getCarrier(), request.getResidentFront())) {
            throw new IllegalArgumentException("정보 불일치: 통신사 명의 정보와 일치하지 않습니다.");
        }

        // [추가] 선택한 통신사 및 주민번호 정보를 세션에 업데이트 (최종 검증 시 사용됨)
        authToken.setCarrier(request.getCarrier());
        authToken.setResidentFront(request.getResidentFront());

        // [보안] 새로운 OTP 생성 및 세션 업데이트
        String newOtp = generateOtp();
        authToken.setOtp(newOtp);
        // [중요] OTP 재발송 시 유효시간도 갱신
        authToken.setCreatedAt(LocalDateTime.now());
        authTokenRepository.save(authToken);

        // [SMS 발송]
        String cleanPhone = authToken.getClientData().replaceAll("\\D", "");
        boolean smsSent = smsService.sendOtp(cleanPhone, newOtp);
        if (!smsSent) {
            System.err.println("[TRUSTEE-ERROR] SMS 재발송 실패 - Phone: " + cleanPhone);
        }
        
        System.out.println("[TRUSTEE-SEC] OTP Regenerated - Token: " + authToken.getTokenId());
        
        // [테스트/운영 모드 분기]
        if (testMode) {
            return new AuthInitResponse(authToken.getTokenId(), newOtp);
        }
        return new AuthInitResponse(authToken.getTokenId(), null);
    }

    /**
     * [S2S API] 위탁사에서 호출하는 토큰 검증 (읽기 전용)
     * - 토큰 상태만 확인, 상태 변경 없음
     * - 상태 조회용으로 사용
     */
    @Transactional(readOnly = true)
    public AuthVerificationResponse verifyToken(UUID tokenId) {
        AuthToken authToken = authTokenRepository.findById(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token ID"));

        // [COMPLIANCE] 암호화된 데이터 복호화
        String decryptedName = authToken.getName();
        String decryptedPhone = authToken.getClientData();
        
        // COMPLETED 또는 USED 상태인 경우 복호화 시도
        if ((authToken.getStatus() == AuthStatus.COMPLETED || authToken.getStatus() == AuthStatus.USED) 
            && authToken.getCi() != null) {
            try {
                System.out.println("[TRUSTEE-DEBUG] verifyToken - Decrypting Name: [" + decryptedName + "]");
                if (decryptedName != null && decryptedName.length() > 10) {
                    decryptedName = CryptoUtils.decryptAES256(decryptedName);
                }
                if (decryptedPhone != null && decryptedPhone.length() > 10) {
                    decryptedPhone = CryptoUtils.decryptAES256(decryptedPhone);
                }
            } catch (Exception e) {
                System.err.println("[COMPLIANCE] verifyToken Decryption failed: " + e.getMessage());
            }
        }

        return new AuthVerificationResponse(
                authToken.getStatus(),
                decryptedName,
                formatPhoneNumberWithHyphens(decryptedPhone),
                authToken.getCi(),
                authToken.getDi());
    }
    
    /**
     * [S2S API] 위탁사에서 호출하는 토큰 소비 (일회성 사용)
     * 
     * 위탁사가 "이 토큰 진짜 니네가 인증해준 거 맞아?" 확인 후
     * 토큰을 USED 상태로 변경하여 재사용 방지
     * 
     * [보안 흐름]
     * 1. 토큰 존재 여부 확인
     * 2. 토큰 상태가 COMPLETED인지 확인 (인증 완료 상태)
     * 3. 토큰 유효시간 확인 (인증 완료 후 일정 시간 내 사용)
     * 4. 토큰 상태를 USED로 변경 (일회성 보장)
     * 5. 인증된 정보 반환
     * 
     * @param tokenId 검증할 토큰 ID
     * @return 인증된 사용자 정보 (상태, 이름, 전화번호)
     * @throws IllegalArgumentException 토큰이 유효하지 않은 경우
     */
    @Transactional
    public AuthVerificationResponse consumeToken(UUID tokenId) {
        AuthToken authToken = authTokenRepository.findById(tokenId)
                .orElseThrow(() -> {
                    System.err.println("[TRUSTEE-S2S] Token NOT FOUND: " + tokenId);
                    return new IllegalArgumentException("유효하지 않은 인증 토큰입니다.");
                });
        
        // [검증 1] 이미 사용된 토큰인지 확인
        if (authToken.getStatus() == AuthStatus.USED) {
            System.err.println("[TRUSTEE-S2S] Token ALREADY USED: " + tokenId);
            throw new IllegalArgumentException("이미 사용된 인증 토큰입니다. (재사용 불가)");
        }
        
        // [검증 2] 만료된 토큰인지 확인
        if (authToken.getStatus() == AuthStatus.EXPIRED) {
            System.err.println("[TRUSTEE-S2S] Token EXPIRED: " + tokenId);
            throw new IllegalArgumentException("만료된 인증 토큰입니다.");
        }
        
        // [검증 3] 인증이 완료되지 않은 토큰인지 확인
        if (authToken.getStatus() != AuthStatus.COMPLETED) {
            System.err.println("[TRUSTEE-S2S] Token NOT COMPLETED: " + tokenId + ", Status: " + authToken.getStatus());
            throw new IllegalArgumentException("본인인증이 완료되지 않은 토큰입니다.");
        }
        
        // [검증 4] 인증 완료 후 유효시간 확인 (인증 완료 후 10분 이내 사용 권장)
        // 이 시간은 운영 정책에 따라 조정 가능
        int tokenUsageValidityMinutes = 10;
        if (LocalDateTime.now().isAfter(authToken.getCreatedAt().plusMinutes(tokenUsageValidityMinutes))) {
            System.err.println("[TRUSTEE-S2S] Token USAGE EXPIRED: " + tokenId);
            authToken.setStatus(AuthStatus.EXPIRED);
            authTokenRepository.save(authToken);
            throw new IllegalArgumentException("인증 토큰 사용 유효시간이 만료되었습니다. 다시 인증해 주세요.");
        }
        
        // [COMPLIANCE] 암호화된 데이터 복호화
        String decryptedName = authToken.getName();
        String decryptedPhone = authToken.getClientData();
        
        try {
            if (authToken.getCi() != null) {
                System.out.println("[TRUSTEE-DEBUG] consumeToken - Decrypting Name: [" + decryptedName + "]");
                if (decryptedName != null && decryptedName.length() > 10) {
                    decryptedName = CryptoUtils.decryptAES256(decryptedName);
                }
                if (decryptedPhone != null && decryptedPhone.length() > 10) {
                    decryptedPhone = CryptoUtils.decryptAES256(decryptedPhone);
                }
            }
        } catch (Exception e) {
            System.err.println("[COMPLIANCE] Decryption failed in consumeToken, using raw data: " + e.getMessage());
        }
        
        // [핵심] 토큰 상태를 USED로 변경 (일회성 보장)
        authToken.setStatus(AuthStatus.USED);
        authTokenRepository.save(authToken);
        
        System.out.println("[TRUSTEE-S2S] Token CONSUMED successfully: " + tokenId + 
                          ", Name: " + CryptoUtils.mask(decryptedName, CryptoUtils.MaskType.NAME) + 
                          ", Phone: " + CryptoUtils.mask(decryptedPhone, CryptoUtils.MaskType.PHONE));
        
        // [컴플라이언스 로그] 본인인증 사용 이력 기록
        logTokenUsage(tokenId, decryptedName, decryptedPhone);
        
        return new AuthVerificationResponse(
                AuthStatus.USED,  // 소비된 상태로 반환
                decryptedName,
                formatPhoneNumberWithHyphens(decryptedPhone),
                authToken.getCi(),
                authToken.getDi());
    }
    
    /**
     * [보안] 데이터 암호화 여부 체크 (Base64 형식 및 길이 기반)
     */
    private boolean isEncrypted(String text) {
        if (text == null) return false;
        // AES-256 Base64 결과물은 보통 24자 이상 (padding 포함)
        return text.length() >= 24 && !text.contains(" ") && !text.contains("-");
    }

    /**
     * [개인정보보호] 전화번호 하이픈 포맷팅 (01012345678 -> 010-1234-5678)
     */
    private String formatPhoneNumberWithHyphens(String phone) {
        if (phone == null) return null;
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() == 11) {
            return digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
        } else if (digits.length() == 10) {
            return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
        }
        return digits;
    }

    /**
     * [개인정보보호] 전화번호 마스킹
     */
    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 8) return "***";
        String cleaned = phone.replaceAll("\\D", "");
        return cleaned.substring(0, 3) + "****" + cleaned.substring(cleaned.length() - 4);
    }
    
    /**
     * [컴플라이언스] 토큰 사용 이력 로깅
     * TODO: 실제 운영 시 별도 감사 로그 테이블 또는 외부 로깅 시스템 연동
     */
    private void logTokenUsage(UUID tokenId, String name, String phone) {
        // [신용정보법] 본인확인 기록 2년 보관 필요
        System.out.println(String.format(
            "[COMPLIANCE-AUDIT] TOKEN_USED | tokenId=%s | name=%s | phone=%s | timestamp=%s",
            tokenId, name, maskPhoneNumber(phone), LocalDateTime.now()
        ));
    }
}

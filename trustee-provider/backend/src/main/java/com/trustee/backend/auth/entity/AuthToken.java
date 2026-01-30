package com.trustee.backend.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 본인인증 토큰 엔티티
 * 
 * [JWT Flow 구조]
 * - tokenId (jti): 수탁사 내부 고유 토큰 ID
 * - authRequestId: 위탁사가 생성한 요청 ID (거래 매핑용)
 */
@Entity
public class AuthToken {

    /** 수탁사 내부 고유 토큰 ID (JWT의 jti로 사용) */
    @Id
    private UUID tokenId;
    
    /** 위탁사가 생성한 요청 ID (JWT claim으로 포함, 거래 매핑용) */
    private String authRequestId;

    // [COMPLIANCE] 개인정보는 암호화하여 저장 (AES-256)
    @Column(length = 500)
    private String clientData; // 전화번호 (암호화)
    
    @Column(length = 500)
    private String name; // 이름 (암호화)
    
    @Column(length = 10)
    private String residentFront; // 주민등록번호 앞자리
    
    private String carrier;
    
    @Column(length = 100)
    private String otp; // OTP는 해시로 저장
    
    // [COMPLIANCE] CI/DI - 본인인증 완료 시 생성
    @Column(length = 120)
    private String ci; // 연계정보
    
    @Column(length = 90)
    private String di; // 중복가입확인정보

    @Enumerated(EnumType.STRING)
    private AuthStatus status;

    @Column(name = "retry_count")
    private int retryCount = 0; // 인증 시도 횟수

    private LocalDateTime createdAt;

    public AuthToken() {
    }

    public AuthToken(UUID tokenId, String authRequestId, String clientData, String name, String carrier, String otp, AuthStatus status,
            LocalDateTime createdAt) {
        this.tokenId = tokenId;
        this.authRequestId = authRequestId;
        this.clientData = clientData;
        this.name = name;
        this.carrier = carrier;
        this.otp = otp;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getTokenId() {
        return tokenId;
    }

    public void setTokenId(UUID tokenId) {
        this.tokenId = tokenId;
    }

    public String getClientData() {
        return clientData;
    }

    public void setClientData(String clientData) {
        this.clientData = clientData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public AuthStatus getStatus() {
        return status;
    }

    public void setStatus(AuthStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getAuthRequestId() {
        return authRequestId;
    }
    
    public void setAuthRequestId(String authRequestId) {
        this.authRequestId = authRequestId;
    }
    
    // CI/DI Getters and Setters
    public String getCi() {
        return ci;
    }
    
    public void setCi(String ci) {
        this.ci = ci;
    }
    
    public String getDi() {
        return di;
    }
    
    public void setDi(String di) {
        this.di = di;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public String getResidentFront() {
        return residentFront;
    }

    public void setResidentFront(String residentFront) {
        this.residentFront = residentFront;
    }
}

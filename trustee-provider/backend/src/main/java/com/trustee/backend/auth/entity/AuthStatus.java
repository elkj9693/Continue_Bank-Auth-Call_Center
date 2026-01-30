package com.trustee.backend.auth.entity;

/**
 * 본인인증 토큰 상태
 * 
 * [상태 흐름]
 * PENDING → COMPLETED → USED
 *    ↓          ↓
 * EXPIRED   EXPIRED
 */
public enum AuthStatus {
    /** 인증 대기 중 (OTP 발송됨, 미확인) */
    PENDING,
    
    /** 인증 완료 (OTP 확인됨, 위탁사 검증 대기) */
    COMPLETED,
    
    /** 사용 완료 (위탁사에서 S2S 검증 완료, 재사용 불가) */
    USED,
    
    /** 만료됨 (유효시간 초과 또는 수동 만료) */
    EXPIRED
}

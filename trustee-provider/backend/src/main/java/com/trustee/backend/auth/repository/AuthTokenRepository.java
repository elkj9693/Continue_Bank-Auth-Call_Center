package com.trustee.backend.auth.repository;

import com.trustee.backend.auth.entity.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AuthTokenRepository extends JpaRepository<AuthToken, UUID> {
    
    /**
     * [COMPLIANCE] 생성 시간 기준으로 만료된 토큰 조회
     * 개인정보보호법: 목적 달성 후 즉시 파기
     */
    List<AuthToken> findByCreatedAtBefore(LocalDateTime dateTime);
    
    /**
     * CI값으로 기존 인증 이력 조회 (중복 가입 방지)
     */
    List<AuthToken> findByCi(String ci);
}

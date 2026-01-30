package com.entrusting.backend.user.repository;

import com.entrusting.backend.user.entity.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
    
    // 사용자별 접근 기록 조회 (최신순)
    List<AccessLog> findByUserIdOrderByAccessedAtDesc(Long userId);
    
    // 접근자별 조회
    List<AccessLog> findByAccessorIdOrderByAccessedAtDesc(String accessorId);
}

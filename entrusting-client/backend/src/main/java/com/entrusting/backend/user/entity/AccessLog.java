package com.entrusting.backend.user.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * [COMPLIANCE] 접근 로그 엔티티
 * 개인정보보호법 제29조에 따른 접근 기록 보관
 */
@Entity
@Table(name = "access_logs")
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 정보주체 (조회 대상)
    @Column(name = "user_id")
    private Long userId;

    // 접근자 유형: CALLCENTER, ADMIN, SELF
    @Column(name = "accessor_type")
    private String accessorType;

    // 접근자 식별자
    @Column(name = "accessor_id")
    private String accessorId;

    // 조회 행위: SEARCH, VIEW, EXPORT, UPDATE
    private String action;

    // 접근 상세 정보
    private String details;

    // 접근자 IP
    @Column(name = "ip_address")
    private String ipAddress;

    // 접근 시간
    @Column(name = "accessed_at")
    private LocalDateTime accessedAt;

    public AccessLog() {}

    public AccessLog(Long userId, String accessorType, String accessorId, 
                     String action, String details, String ipAddress) {
        this.userId = userId;
        this.accessorType = accessorType;
        this.accessorId = accessorId;
        this.action = action;
        this.details = details;
        this.ipAddress = ipAddress;
        this.accessedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getAccessorType() { return accessorType; }
    public void setAccessorType(String accessorType) { this.accessorType = accessorType; }

    public String getAccessorId() { return accessorId; }
    public void setAccessorId(String accessorId) { this.accessorId = accessorId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getAccessedAt() { return accessedAt; }
    public void setAccessedAt(LocalDateTime accessedAt) { this.accessedAt = accessedAt; }
}

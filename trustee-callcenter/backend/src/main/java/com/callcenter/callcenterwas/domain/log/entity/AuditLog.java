package com.callcenter.callcenterwas.domain.log.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 행위자: AGENT, SYSTEM
    @Column(nullable = false)
    private String actorId;

    // 동작: SEARCH, UPDATE, REQUEST, STATUS_CHANGE
    @Column(nullable = false)
    private String action;

    // 대상: CASE_ID, CUSTOMER_REF 등
    private String targetId;

    private String targetType;

    @CreationTimestamp
    private LocalDateTime timestamp;

    private String clientIp;

    @Column(length = 1000)
    private String description;
}

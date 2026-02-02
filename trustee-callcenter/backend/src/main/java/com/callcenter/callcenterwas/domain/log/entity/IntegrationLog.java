package com.callcenter.callcenterwas.domain.log.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "integration_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegrationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String bankRequestId;

    private String apiPath;

    private String requestMethod;

    private Integer responseCode;

    // PII 유출 방지를 위해 Body 대신 요약 정보만 저장
    private String responseSummary;

    @CreationTimestamp
    private LocalDateTime timestamp;

    private String idempotencyKey;

    private Integer retryCount;
}

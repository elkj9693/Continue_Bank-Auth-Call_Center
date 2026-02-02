package com.callcenter.callcenterwas.domain.consent.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "marketing_consents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketingConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerRef;

    // 동의 상태: OPT_IN, OPT_OUT, WITHDRAWN
    @Column(nullable = false)
    private String consentStatus;

    @CreationTimestamp
    private LocalDateTime consentAt;

    private String channel; // PHONE, OUTBOUND 등

    private String campaignId;

    // S3 녹취 파일 참조 키 (위탁사 S3에 저장됨)
    private String consentEvidenceKey;
}

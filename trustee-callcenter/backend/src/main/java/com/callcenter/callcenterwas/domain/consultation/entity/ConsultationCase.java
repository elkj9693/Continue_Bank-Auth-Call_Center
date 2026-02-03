package com.callcenter.callcenterwas.domain.consultation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultation_cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 채널: INBOUND, ARS, OUTBOUND
    @Column(nullable = false)
    private String channel;

    // 서비스 유형: CARD_LOSS, MARKETING, GENERAL_INQUIRY 등
    private String serviceType;

    // 상태: OPEN, CLOSED, ESCALATED, FAILED
    @Column(nullable = false)
    private String status;

    // 위탁사 고객 식별 토큰 (가명 처리된 참조값, 실명/번호 아님)
    @Column(nullable = false)
    private String customerRef;

    // 고객 마스킹 이름 (예: 홍*동) - 상담 편의를 위한 최소 식별 정보
    private String customerName;

    // 상담원 ID (ARS인 경우 'SYSTEM')
    private String agentId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime closedAt;

    // 위탁사 연동 시 사용된 최종 Request ID (IntegrationLog와 연계 가능)
    private String lastBankRequestId;

    // 구체적인 비즈니스 결과 메시지 (예: "분실 신고 완료", "마케팅 동의 거절" 등)
    private String resultNote;
}

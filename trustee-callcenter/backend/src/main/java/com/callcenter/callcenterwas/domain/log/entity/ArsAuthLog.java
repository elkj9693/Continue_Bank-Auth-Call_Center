package com.callcenter.callcenterwas.domain.log.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ars_auth_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArsAuthLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long caseId; // ConsultationCase FK

    @Column(nullable = false)
    private String customerRef;

    private String authMethod; // ARS_PIN

    private String authResult; // SUCCESS, FAIL

    private String failReason;

    private Integer failCount;

    @CreationTimestamp
    private LocalDateTime authAt;
}

package com.entrusting.backend.ars.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String leadId;

    @Column(nullable = false)
    private String customerRef;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String requestedProductType; // LOAN, CARD, DEPOSIT 등

    @Column(nullable = false)
    private String status; // PENDING, CONTACTED, COMPLETED, REJECTED

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime contactedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }
}

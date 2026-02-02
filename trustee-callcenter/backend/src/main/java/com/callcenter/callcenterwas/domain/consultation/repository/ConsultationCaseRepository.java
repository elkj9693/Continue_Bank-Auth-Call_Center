package com.callcenter.callcenterwas.domain.consultation.repository;

import com.callcenter.callcenterwas.domain.consultation.entity.ConsultationCase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationCaseRepository extends JpaRepository<ConsultationCase, Long> {
}

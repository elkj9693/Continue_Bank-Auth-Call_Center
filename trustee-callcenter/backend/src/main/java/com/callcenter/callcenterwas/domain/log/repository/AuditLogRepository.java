package com.callcenter.callcenterwas.domain.log.repository;

import com.callcenter.callcenterwas.domain.log.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}

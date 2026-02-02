package com.callcenter.callcenterwas.domain.log.repository;

import com.callcenter.callcenterwas.domain.log.entity.IntegrationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntegrationLogRepository extends JpaRepository<IntegrationLog, Long> {
}

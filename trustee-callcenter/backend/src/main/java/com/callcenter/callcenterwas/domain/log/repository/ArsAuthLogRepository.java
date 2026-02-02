package com.callcenter.callcenterwas.domain.log.repository;

import com.callcenter.callcenterwas.domain.log.entity.ArsAuthLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArsAuthLogRepository extends JpaRepository<ArsAuthLog, Long> {
}

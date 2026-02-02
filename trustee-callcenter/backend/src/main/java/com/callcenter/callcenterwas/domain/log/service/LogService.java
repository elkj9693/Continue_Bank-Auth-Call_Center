package com.callcenter.callcenterwas.domain.log.service;

import com.callcenter.callcenterwas.domain.log.entity.ArsAuthLog;
import com.callcenter.callcenterwas.domain.log.entity.AuditLog;
import com.callcenter.callcenterwas.domain.log.entity.IntegrationLog;
import com.callcenter.callcenterwas.domain.log.repository.ArsAuthLogRepository;
import com.callcenter.callcenterwas.domain.log.repository.AuditLogRepository;
import com.callcenter.callcenterwas.domain.log.repository.IntegrationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogService {

    private final IntegrationLogRepository integrationLogRepository;
    private final ArsAuthLogRepository arsAuthLogRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void logIntegration(String bankRequestId, String path, String method, Integer code, String summary) {
        IntegrationLog logEntity = IntegrationLog.builder()
                .bankRequestId(bankRequestId)
                .apiPath(path)
                .requestMethod(method)
                .responseCode(code)
                .responseSummary(summary)
                .build();
        integrationLogRepository.save(logEntity);
    }

    @Transactional
    public void logArsAuth(Long caseId, String customerRef, String method, String result, String reason,
            Integer failCount) {
        ArsAuthLog logEntity = ArsAuthLog.builder()
                .caseId(caseId)
                .customerRef(customerRef)
                .authMethod(method)
                .authResult(result)
                .failReason(reason)
                .failCount(failCount)
                .build();
        arsAuthLogRepository.save(logEntity);
    }

    @Transactional
    public void logAudit(String actorId, String action, String targetId, String targetType, String description) {
        AuditLog logEntity = AuditLog.builder()
                .actorId(actorId)
                .action(action)
                .targetId(targetId)
                .targetType(targetType)
                .description(description)
                .build();
        auditLogRepository.save(logEntity);
    }
}

package com.entrusting.backend.ars.controller;

import com.entrusting.backend.ars.entity.AuditEvent;
import com.entrusting.backend.ars.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 감사 로그 엔드포인트
 */
@RestController
@RequestMapping("/issuer/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditEventRepository auditEventRepository;

    /**
     * 감사 이벤트 저장
     */
    @PostMapping("/events")
    public Map<String, Object> saveEvent(@RequestBody Map<String, String> request) {
        AuditEvent event = AuditEvent.builder()
                .eventType(request.get("eventType"))
                .resultCode(request.get("resultCode"))
                .operatorId(request.get("operatorId"))
                .notes(request.get("notes"))
                .build();

        auditEventRepository.save(event);

        return Map.of("success", true);
    }
}

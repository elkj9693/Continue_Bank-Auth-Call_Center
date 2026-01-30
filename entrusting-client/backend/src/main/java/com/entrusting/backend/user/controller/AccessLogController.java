package com.entrusting.backend.user.controller;

import com.entrusting.backend.user.entity.AccessLog;
import com.entrusting.backend.user.repository.AccessLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * [COMPLIANCE] 접근 로그 조회 컨트롤러
 * 개인정보보호법 제35조 - 정보주체의 열람권
 */
@RestController
@RequestMapping("/api/my")
@CrossOrigin(origins = {"http://localhost:5175", "http://localhost:5173"})
public class AccessLogController {

    private final AccessLogRepository accessLogRepository;

    public AccessLogController(AccessLogRepository accessLogRepository) {
        this.accessLogRepository = accessLogRepository;
    }

    /**
     * 내 정보 접근 기록 조회
     * [COMPLIANCE] 정보주체는 자신의 정보가 언제, 누구에 의해 조회되었는지 확인할 수 있음
     */
    @GetMapping("/access-logs")
    public ResponseEntity<?> getMyAccessLogs(@RequestParam Long userId) {
        List<AccessLog> logs = accessLogRepository.findByUserIdOrderByAccessedAtDesc(userId);
        
        List<Map<String, Object>> response = logs.stream()
            .map(log -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", log.getId());
                item.put("accessorType", log.getAccessorType());
                item.put("accessorId", maskAccessorId(log.getAccessorId()));
                item.put("action", log.getAction());
                item.put("details", log.getDetails());
                item.put("accessedAt", log.getAccessedAt().toString());
                return item;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
            "totalCount", logs.size(),
            "logs", response
        ));
    }

    /**
     * 접근자 ID 마스킹
     */
    private String maskAccessorId(String accessorId) {
        if (accessorId == null || accessorId.length() < 3) {
            return "***";
        }
        return accessorId.substring(0, 2) + "***" + accessorId.substring(accessorId.length() - 1);
    }
}

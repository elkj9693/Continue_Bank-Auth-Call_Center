package com.entrusting.backend.ars.controller;

import com.entrusting.backend.ars.entity.Lead;
import com.entrusting.backend.ars.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lead 관리 엔드포인트
 */
@RestController
@RequestMapping("/api/v1/leads")
@RequiredArgsConstructor
@Slf4j
public class LeadController {

    private final LeadRepository leadRepository;

    /**
     * Lead 생성문
     */
    @PostMapping
    public Map<String, Object> createLead(@RequestBody Map<String, String> request) {
        Lead lead = Lead.builder()
                .customerRef(request.get("customerRef"))
                .name(request.get("name"))
                .phone(request.get("phone"))
                .requestedProductType(request.get("productType"))
                .status("PENDING")
                .build();

        Lead saved = leadRepository.save(lead);

        return Map.of(
                "success", true,
                "leadId", saved.getLeadId(),
                "message", "상담 신청이 완료되었습니다.");
    }

    /**
     * 상담 가능한 Lead 목록 조회
     */
    @GetMapping("/eligible")
    public List<Lead> getEligibleLeads() {
        return leadRepository.findByStatus("PENDING");
    }

    /**
     * 상담 이력 조회 (완료된 건들)
     */
    @GetMapping("/history")
    public List<Lead> getHistoryLeads() {
        return leadRepository.findByStatusNot("PENDING");
    }

    /**
     * 상담 결과 업데이트
     */
    @PostMapping("/{leadId}/result")
    public Map<String, Object> updateResult(
            @PathVariable String leadId,
            @RequestBody Map<String, String> request) {

        String outcome = request.get("outcome");
        log.info("[LEAD-DEBUG] Updating lead {} with outcome: {}", leadId, outcome);

        Optional<Lead> leadOptional = leadRepository.findById(leadId);

        if (leadOptional.isPresent()) {
            Lead lead = leadOptional.get();
            if (outcome != null) {
                lead.setStatus(outcome); // "COMPLETED", "REJECTED", etc.
                lead.setContactedAt(LocalDateTime.now());
                leadRepository.save(lead);
                log.info("[LEAD-DEBUG] Lead {} updated successfully to status {}", leadId, outcome);
                return Map.of("success", true);
            } else {
                log.error("[LEAD-DEBUG] Outcome is null for lead {}", leadId);
                return Map.of("success", false, "message", "Outcome cannot be null");
            }
        } else {
            log.warn("[LEAD-DEBUG] Lead with ID {} not found for update", leadId);
            return Map.of("success", false, "message", "Lead not found");
        }
    }
}

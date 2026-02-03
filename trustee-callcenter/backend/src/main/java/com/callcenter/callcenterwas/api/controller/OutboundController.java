package com.callcenter.callcenterwas.api.controller;

import com.callcenter.callcenterwas.infrastructure.client.IssuerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/outbound")
@RequiredArgsConstructor
@Slf4j
public class OutboundController {

    private final IssuerClient issuerClient;
    private final com.callcenter.callcenterwas.domain.consultation.service.ConsultationService consultationService;
    private final com.callcenter.callcenterwas.domain.log.service.LogService logService;
    private final com.callcenter.callcenterwas.domain.consent.repository.MarketingConsentRepository marketingConsentRepository;
    private final com.callcenter.callcenterwas.domain.consultation.repository.ConsultationCaseRepository consultationCaseRepository;

    /**
     * Get target leads for outbound calls (Data Masking Applied)
     */
    @GetMapping("/targets")
    public List<Map<String, Object>> getTargets() {
        log.info("[Outbound] Fetching leads from issuer");
        List<Map<String, Object>> leads = issuerClient.getOutboundLeads();

        // Data Masking Simulation (PII Protection)
        return maskLeads(leads);
    }

    /**
     * Get consultation history (NOW FROM LOCAL DB)
     */
    @GetMapping("/history")
    public List<com.callcenter.callcenterwas.domain.consultation.entity.ConsultationCase> getHistory() {
        log.info("[Outbound] Fetching consultation history from LOCAL RDS");
        return consultationCaseRepository.findAll();
    }

    private List<Map<String, Object>> maskLeads(List<Map<String, Object>> leads) {
        return leads.stream().map(lead -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> leadMap = new java.util.HashMap<>(lead);
            String name = (String) leadMap.get("name");
            String phone = (String) leadMap.get("phone");

            leadMap.put("name", maskName(name));
            leadMap.put("phone", maskPhone(phone));
            return leadMap;
        }).collect(Collectors.toList());
    }

    /**
     * Submit outbound call result
     */
    @PostMapping("/result")
    public Map<String, Object> submitResult(@RequestBody Map<String, Object> request) {
        // leadId를 customerRef로 간주 (데모 시나리오)
        String customerRef = request.get("leadId").toString();
        String status = (String) request.get("status");
        String agentId = (String) request.get("agentId");

        log.info("[Outbound] Submitting result for customerRef: {}, status: {}", customerRef, status);

        // 1. 로컬 상담 케이스 생성 (OUTBOUND - MARKETING)
        com.callcenter.callcenterwas.domain.consultation.entity.ConsultationCase consultationCase = consultationService
                .createCase("OUTBOUND", "MARKETING", customerRef, agentId);

        // 2. 마케팅 동의 정보 저장 (COMPLETED인 경우 -> 관심 있음)
        if ("COMPLETED".equals(status)) {
            com.callcenter.callcenterwas.domain.consent.entity.MarketingConsent consent = com.callcenter.callcenterwas.domain.consent.entity.MarketingConsent
                    .builder()
                    .customerRef(customerRef)
                    .consentStatus("AGREED")
                    .channel("OUTBOUND")
                    .campaignId("CAMP_2024_PROMO")
                    .consentEvidenceKey("REC_" + System.currentTimeMillis() + ".mp3") // Pseudonymous Key
                    .build();
            marketingConsentRepository.save(consent);
        }

        // 3. 위탁사(Bank)로 결과 전송 (동기화)
        issuerClient.updateLeadResult(customerRef, status);

        // 4. 연동 로그 및 감사 로그 기록
        logService.logIntegration("N/A", "/api/v1/outbound/result", "POST",
                200, "Outbound Result Sync: " + status);

        logService.logAudit(agentId, "SUBMIT_OUTBOUND_RESULT", consultationCase.getId().toString(), "CASE",
                "Status: " + status);

        // 5. 케이스 종료 (사용자 친화적인 메시지 포함)
        String resultNote;
        if ("COMPLETED".equals(status)) {
            resultNote = "상담 완료 (관심 있음)";
        } else if ("NO_ANSWER".equals(status)) {
            resultNote = "상담 실패 (부재중)";
        } else {
            resultNote = "상담 완료 (거절함)";
        }
        consultationService.closeCase(consultationCase.getId(), resultNote);

        return Map.of("success", true);
    }

    private String maskName(String name) {
        if (name == null || name.length() < 2)
            return name;
        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 10)
            return phone;
        return phone.substring(0, 3) + "-****-" + phone.substring(phone.length() - 4);
    }
}

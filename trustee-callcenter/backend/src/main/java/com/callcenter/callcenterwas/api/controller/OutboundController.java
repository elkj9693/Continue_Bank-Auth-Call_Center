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

    /**
     * Get target leads for outbound calls (Data Masking Applied)
     */
    @GetMapping("/targets")
    public List<Map<String, Object>> getTargets() {
        log.info("[Outbound] Fetching leads from issuer");
        List<Map<String, Object>> leads = issuerClient.getOutboundLeads();

        // Data Masking Simulation
        return maskLeads(leads);
    }

    /**
     * Get consultation history
     */
    @GetMapping("/history")
    public List<Map<String, Object>> getHistory() {
        log.info("[Outbound] Fetching history from issuer");
        List<Map<String, Object>> leads = issuerClient.getOutboundHistory();
        return maskLeads(leads);
    }

    private List<Map<String, Object>> maskLeads(List<Map<String, Object>> leads) {
        return leads.stream().map(lead -> {
            String name = (String) lead.get("name");
            String phone = (String) lead.get("phone");

            lead.put("name", maskName(name));
            lead.put("phone", maskPhone(phone));
            return lead;
        }).collect(Collectors.toList());
    }

    /**
     * Submit outbound call result
     */
    @PostMapping("/result")
    public Map<String, Object> submitResult(@RequestBody Map<String, Object> request) {
        String leadId = request.get("leadId").toString();
        String status = (String) request.get("status");
        String agentId = (String) request.get("agentId");

        log.info("[Outbound] Updating lead {} result to {} by agent {}", leadId, status, agentId);

        issuerClient.updateLeadResult(leadId, status);

        // Audit Logging
        issuerClient.sendAuditEvent("OUTBOUND_COUNSEL", "SUCCESS", agentId, "Consultation result: " + status);

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

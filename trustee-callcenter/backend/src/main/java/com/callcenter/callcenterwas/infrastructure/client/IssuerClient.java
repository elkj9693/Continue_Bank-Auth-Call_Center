package com.callcenter.callcenterwas.infrastructure.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class IssuerClient {

        private final RestClient restClient;

        public IssuerClient(@Value("${issuer.api.url}") String issuerUrl) {
                this.restClient = RestClient.builder()
                                .baseUrl(issuerUrl)
                                .build();
        }

        // ARS: Get Public Key for RSA
        public Map<String, Object> getPublicKey() {
                return restClient.get()
                                .uri("/issuer/auth/public-key")
                                .retrieve()
                                .body(Map.class);
        }

        // ARS: Identify Customer by ANI
        public Map<String, Object> identifyCustomer(String phoneNumber) {
                return restClient.post()
                                .uri("/issuer/ars/identify")
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(Map.of("phoneNumber", phoneNumber))
                                .retrieve()
                                .body(Map.class);
        }

        // ARS: Verify PIN (Encrypted)
        public Map<String, Object> verifyPin(String customerRef, String kid, String ciphertext) {
                return restClient.post()
                                .uri("/issuer/ars/verify-pin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(Map.of(
                                                "customerRef", customerRef,
                                                "kid", kid,
                                                "ciphertext", ciphertext))
                                .retrieve()
                                .body(Map.class);
        }

        // ARS: Finalize Loss Report
        public Map<String, Object> reportCardLoss(String customerRef, List<String> selectedCardRefs) {
                return restClient.post()
                                .uri("/issuer/card/loss")
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(Map.of(
                                                "customerRef", customerRef,
                                                "selectedCardRefs", selectedCardRefs))
                                .retrieve()
                                .body(Map.class);
        }

        // Outbound: Fetch Lead Targets
        public List<Map<String, Object>> getOutboundLeads() {
                return restClient.get()
                                .uri("/api/v1/leads/eligible")
                                .retrieve()
                                .body(List.class);
        }

        // Outbound: Fetch Lead History
        public List<Map<String, Object>> getOutboundHistory() {
                return restClient.get()
                                .uri("/api/v1/leads/history")
                                .retrieve()
                                .body(List.class);
        }

        // Outbound: Update Lead Result
        public void updateLeadResult(String leadId, String status) {
                restClient.post()
                                .uri("/api/v1/leads/{id}/result", leadId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(Map.of("outcome", status))
                                .retrieve()
                                .toBodilessEntity();
        }

        // Audit: Log Call Center Event to Bank
        public void sendAuditEvent(String eventType, String resultCode, String operatorId, String notes) {
                restClient.post()
                                .uri("/issuer/audit/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(Map.of(
                                                "eventType", eventType,
                                                "resultCode", resultCode,
                                                "operatorId", operatorId,
                                                "notes", notes))
                                .retrieve()
                                .toBodilessEntity();
        }
}

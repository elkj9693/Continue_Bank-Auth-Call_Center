package com.callcenter.callcenterwas.domain.consultation.service;

import com.callcenter.callcenterwas.domain.consultation.entity.ConsultationCase;
import com.callcenter.callcenterwas.domain.consultation.repository.ConsultationCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationService {

    private final ConsultationCaseRepository consultationCaseRepository;

    @Transactional
    public ConsultationCase createCase(String channel, String serviceType, String customerRef, String customerName,
            String agentId) {
        ConsultationCase consultationCase = ConsultationCase.builder()
                .channel(channel)
                .serviceType(serviceType)
                .status("OPEN")
                .customerRef(customerRef)
                .customerName(customerName != null ? customerName : "익명")
                .agentId(agentId)
                .build();
        return consultationCaseRepository.save(consultationCase);
    }

    @Transactional
    public void closeCase(Long caseId) {
        closeCase(caseId, "처리 완료");
    }

    @Transactional
    public void closeCase(Long caseId, String resultNote) {
        consultationCaseRepository.findById(caseId).ifPresent(c -> {
            c.setStatus("CLOSED");
            c.setResultNote(resultNote);
            consultationCaseRepository.save(c);
        });
    }

    @Transactional
    public void updateLastBankRequestId(Long caseId, String bankRequestId) {
        consultationCaseRepository.findById(caseId).ifPresent(c -> {
            c.setLastBankRequestId(bankRequestId);
            consultationCaseRepository.save(c);
        });
    }

    @Transactional
    public void updateCustomerName(Long caseId, String customerName) {
        consultationCaseRepository.findById(caseId).ifPresent(c -> {
            c.setCustomerName(customerName);
            consultationCaseRepository.save(c);
        });
    }
}

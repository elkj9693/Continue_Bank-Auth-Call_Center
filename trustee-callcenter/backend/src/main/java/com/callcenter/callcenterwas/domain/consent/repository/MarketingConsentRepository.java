package com.callcenter.callcenterwas.domain.consent.repository;

import com.callcenter.callcenterwas.domain.consent.entity.MarketingConsent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketingConsentRepository extends JpaRepository<MarketingConsent, Long> {
}

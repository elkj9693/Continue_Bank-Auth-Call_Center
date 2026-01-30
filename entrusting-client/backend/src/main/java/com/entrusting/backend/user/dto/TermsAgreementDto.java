package com.entrusting.backend.user.dto;

import java.util.Map;

public class TermsAgreementDto {
    private Map<String, Boolean> agreements;
    private Map<String, Boolean> marketingChannels;

    public boolean isAllRequiredAgreed() {
        if (agreements == null) {
            System.err.println("[ENTRUSTING-DEBUG] Agreements Map is NULL");
            return false;
        }
        System.out.println("[ENTRUSTING-DEBUG] Agreements Map: " + agreements);

        boolean age = agreements.getOrDefault("age", false);
        boolean terms = agreements.getOrDefault("terms", false);
        boolean privacy = agreements.getOrDefault("privacy", false);
        boolean uniqueId = agreements.getOrDefault("uniqueId", false);
        boolean creditInfo = agreements.getOrDefault("creditInfo", false);
        boolean carrierAuth = agreements.getOrDefault("carrierAuth", false);
        // ssapProvision is now OPTIONAL
        // boolean ssapProvision = agreements.getOrDefault("ssapProvision", false);
        boolean electronicFinance = agreements.getOrDefault("electronicFinance", false);
        boolean monitoring = agreements.getOrDefault("monitoring", false);

        if (!age || !terms || !privacy || !uniqueId || !creditInfo || !carrierAuth || !electronicFinance || !monitoring) {
            System.err.println("[ENTRUSTING-DEBUG] Validation FAILED - Missing fields:");
            if (!age) System.err.println(" - age is false/missing");
            if (!terms) System.err.println(" - terms is false/missing");
            if (!privacy) System.err.println(" - privacy is false/missing");
            if (!uniqueId) System.err.println(" - uniqueId is false/missing");
            if (!creditInfo) System.err.println(" - creditInfo is false/missing");
            if (!carrierAuth) System.err.println(" - carrierAuth is false/missing");
            // if (!ssapProvision) System.err.println(" - ssapProvision is false/missing");
            if (!electronicFinance) System.err.println(" - electronicFinance is false/missing");
            if (!monitoring) System.err.println(" - monitoring is false/missing");
            return false;
        }
        return true;
    }

    public Map<String, Boolean> getAgreements() {
        return agreements;
    }

    public void setAgreements(Map<String, Boolean> agreements) {
        this.agreements = agreements;
    }

    public Map<String, Boolean> getMarketingChannels() {
        return marketingChannels;
    }

    public void setMarketingChannels(Map<String, Boolean> marketingChannels) {
        this.marketingChannels = marketingChannels;
    }
}

package com.entrusting.backend.user.dto;

public class RegisterRequest {
    private String name;
    private String username;
    private String password;
    private String phoneNumber;
    private String tokenId;
    private boolean isVerified;
    private TermsAgreementDto termsAgreement; // 약관 동의 정보

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public TermsAgreementDto getTermsAgreement() {
        return termsAgreement;
    }

    public void setTermsAgreement(TermsAgreementDto termsAgreement) {
        this.termsAgreement = termsAgreement;
    }
}

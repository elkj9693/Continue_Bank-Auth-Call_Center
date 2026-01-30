package com.trustee.backend.auth.dto;

import java.util.UUID;

public class AuthConfirmRequest {
    private UUID tokenId;
    private String otp;

    public UUID getTokenId() {
        return tokenId;
    }

    public void setTokenId(UUID tokenId) {
        this.tokenId = tokenId;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}

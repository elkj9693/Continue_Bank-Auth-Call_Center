package com.entrusting.backend.user.dto;

import java.util.UUID;

public class AuthCallbackRequest {
    private UUID tokenId;
    private String phoneNumber; // 인증 성공 후 사용자 정보 업데이트를 위해 필요

    public UUID getTokenId() {
        return tokenId;
    }

    public void setTokenId(UUID tokenId) {
        this.tokenId = tokenId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}

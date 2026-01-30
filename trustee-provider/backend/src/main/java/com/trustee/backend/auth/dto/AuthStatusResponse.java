package com.trustee.backend.auth.dto;

import com.trustee.backend.auth.entity.AuthStatus;
import java.util.UUID;

public class AuthStatusResponse {
    private UUID tokenId;
    private AuthStatus status;
    private String name;
    private String phoneNumber;

    public AuthStatusResponse(UUID tokenId, AuthStatus status, String name, String phoneNumber) {
        this.tokenId = tokenId;
        this.status = status;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public UUID getTokenId() {
        return tokenId;
    }

    public void setTokenId(UUID tokenId) {
        this.tokenId = tokenId;
    }

    public AuthStatus getStatus() {
        return status;
    }

    public void setStatus(AuthStatus status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}

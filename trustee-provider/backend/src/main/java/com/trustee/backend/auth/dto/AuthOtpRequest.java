package com.trustee.backend.auth.dto;

import java.util.UUID;

public class AuthOtpRequest {
    private UUID tokenId;
    private String name;
    private String phoneNumber;
    private String residentFront;
    private String carrier;

    public UUID getTokenId() {
        return tokenId;
    }

    public void setTokenId(UUID tokenId) {
        this.tokenId = tokenId;
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

    public String getResidentFront() {
        return residentFront;
    }

    public void setResidentFront(String residentFront) {
        this.residentFront = residentFront;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }
}

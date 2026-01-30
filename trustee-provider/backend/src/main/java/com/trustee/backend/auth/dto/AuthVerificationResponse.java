package com.trustee.backend.auth.dto;

import com.trustee.backend.auth.entity.AuthStatus;

public class AuthVerificationResponse {
    private AuthStatus status;
    private String name;
    private String phoneNumber;
    
    // [COMPLIANCE] CI/DI 추가
    private String ci; // 연계정보
    private String di; // 중복가입확인정보

    public AuthVerificationResponse() {
    }

    public AuthVerificationResponse(AuthStatus status, String name, String phoneNumber) {
        this.status = status;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }
    
    public AuthVerificationResponse(AuthStatus status, String name, String phoneNumber, String ci, String di) {
        this.status = status;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.ci = ci;
        this.di = di;
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

    public String getCi() {
        return ci;
    }

    public void setCi(String ci) {
        this.ci = ci;
    }

    public String getDi() {
        return di;
    }

    public void setDi(String di) {
        this.di = di;
    }
}

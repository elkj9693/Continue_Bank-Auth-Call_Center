package com.trustee.backend.auth.dto;

/**
 * 인증 초기화 요청 DTO
 * 
 * [JWT Flow]
 * 위탁사가 auth_request_id를 생성하여 함께 전송
 */
public class AuthInitRequest {
    /** 위탁사가 생성한 요청 ID (거래 매핑용, JWT claim으로 포함됨) */
    private String authRequestId;
    
    private String clientData;
    private String name;
    private String carrier;

    public String getAuthRequestId() {
        return authRequestId;
    }

    public void setAuthRequestId(String authRequestId) {
        this.authRequestId = authRequestId;
    }

    public String getClientData() {
        return clientData;
    }

    public void setClientData(String clientData) {
        this.clientData = clientData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }
}

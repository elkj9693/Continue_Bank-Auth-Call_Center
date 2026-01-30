package com.entrusting.backend.user.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "site_users") // 'user', 'users'는 H2/SQL 예약어와 충돌할 가능성이 높음
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // [COMPLIANCE] 개인정보는 암호화하여 저장
    @Column(length = 500)
    private String name; // 사용자 이름 (AES-256 암호화)
    
    private String username;
    private String password;
    
    @Column(length = 500)
    private String phoneNumber; // 휴대폰 번호 (AES-256 암호화)
    
    // [COMPLIANCE] CI/DI - 금융권 본인인증 표준 식별자
    @Column(name = "ci", unique = true, length = 120)
    private String ci; // 연계정보 - 모든 금융기관 공통 식별자 (88byte Base64)
    
    @Column(name = "di", length = 90)
    private String di; // 중복가입확인정보 - 사이트별 고유값 (64byte Base64)
    
    private boolean isVerified;
    
    // [COMPLIANCE] 개인정보 수집/이용 동의 일시
    @Column(name = "privacy_agreed_at")
    private java.time.LocalDateTime privacyAgreedAt;
    
    // [COMPLIANCE] 데이터 보관 만료일 (개인정보보호법 - 보유기간)
    @Column(name = "data_expire_at")
    private java.time.LocalDateTime dataExpireAt;
    
    // [COMPLIANCE] 약관 동의 정보
    @Column(name = "terms_agreed")
    private Boolean termsAgreed = false; // 이용약관
    
    @Column(name = "privacy_agreed")  
    private Boolean privacyAgreed = false; // 개인정보 수집·이용
    
    @Column(name = "unique_id_agreed")
    private Boolean uniqueIdAgreed = false; // 고유식별정보 처리
    
    @Column(name = "credit_info_agreed")
    private Boolean creditInfoAgreed = false; // 신용정보 조회·제공
    
    @Column(name = "carrier_auth_agreed")
    private Boolean carrierAuthAgreed = false; // 본인확인서비스
    
    @Column(name = "ssap_provision_agreed")
    private Boolean ssapProvisionAgreed = false; // SSAP 정보 제공 동의 (제휴 TM 센터)

    @Column(name = "third_party_provision_agreed")
    private Boolean thirdPartyProvisionAgreed = false; // 제3자 정보 제공 동의
    
    @Column(name = "electronic_finance_agreed")
    private Boolean electronicFinanceAgreed = false; // 전자금융거래 기본약관
    
    @Column(name = "monitoring_agreed")
    private Boolean monitoringAgreed = false; // 금융거래 모니터링/AML
    
    @Column(name = "marketing_personal_agreed")
    private Boolean marketingPersonalAgreed = false; // 개인맞춤형 상품 추천 (선택)
    
    // [COMPLIANCE] 마케팅 동의 상세 항목
    @Column(name = "marketing_agreed")
    private Boolean marketingAgreed = false; // 마케팅 정보 수신 동의
    
    @Column(name = "marketing_sms")
    private Boolean marketingSms = false; // SMS 마케팅 동의
    
    @Column(name = "marketing_email")
    private Boolean marketingEmail = false; // 이메일 마케팅 동의
    
    @Column(name = "marketing_push")
    private Boolean marketingPush = false; // 푸시 알림 마케팅 동의
    
    // [COMPLIANCE] 제3자 제공 동의 보유기간 (상담 완료 후 3개월)
    @Column(name = "third_party_retention_until")
    private java.time.LocalDateTime thirdPartyProvisionRetentionUntil;

    @Column(name = "terms_agreed_at")
    private java.time.LocalDateTime termsAgreedAt; // 약관 동의 일시

    public User() {
    }

    public User(String name, String username, String password, String phoneNumber, boolean isVerified) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.isVerified = isVerified;
    }
    
    // CI/DI를 포함한 생성자
    public User(String name, String username, String password, String phoneNumber, 
                String ci, String di, boolean isVerified) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.ci = ci;
        this.di = di;
        this.isVerified = isVerified;
        this.privacyAgreedAt = java.time.LocalDateTime.now();
        // 개인정보 보관 기간 5년 (금융거래 기록)
        this.dataExpireAt = java.time.LocalDateTime.now().plusYears(5);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    // CI/DI Getters and Setters
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

    public java.time.LocalDateTime getPrivacyAgreedAt() {
        return privacyAgreedAt;
    }

    public void setPrivacyAgreedAt(java.time.LocalDateTime privacyAgreedAt) {
        this.privacyAgreedAt = privacyAgreedAt;
    }

    public java.time.LocalDateTime getDataExpireAt() {
        return dataExpireAt;
    }

    public void setDataExpireAt(java.time.LocalDateTime dataExpireAt) {
        this.dataExpireAt = dataExpireAt;
    }

    // Terms Agreement Getters and Setters
    public Boolean getTermsAgreed() {
        return termsAgreed;
    }

    public void setTermsAgreed(Boolean termsAgreed) {
        this.termsAgreed = termsAgreed;
    }

    public Boolean getPrivacyAgreed() {
        return privacyAgreed;
    }

    public void setPrivacyAgreed(Boolean privacyAgreed) {
        this.privacyAgreed = privacyAgreed;
    }

    public Boolean getUniqueIdAgreed() {
        return uniqueIdAgreed;
    }

    public void setUniqueIdAgreed(Boolean uniqueIdAgreed) {
        this.uniqueIdAgreed = uniqueIdAgreed;
    }

    public Boolean getCreditInfoAgreed() {
        return creditInfoAgreed;
    }

    public void setCreditInfoAgreed(Boolean creditInfoAgreed) {
        this.creditInfoAgreed = creditInfoAgreed;
    }

    public Boolean getCarrierAuthAgreed() {
        return carrierAuthAgreed;
    }

    public void setCarrierAuthAgreed(Boolean carrierAuthAgreed) {
        this.carrierAuthAgreed = carrierAuthAgreed;
    }

    public Boolean getMarketingAgreed() {
        return marketingAgreed;
    }

    public void setMarketingAgreed(Boolean marketingAgreed) {
        this.marketingAgreed = marketingAgreed;
    }

    public Boolean getMarketingSms() {
        return marketingSms;
    }

    public void setMarketingSms(Boolean marketingSms) {
        this.marketingSms = marketingSms;
    }

    public Boolean getMarketingEmail() {
        return marketingEmail;
    }

    public void setMarketingEmail(Boolean marketingEmail) {
        this.marketingEmail = marketingEmail;
    }

    public Boolean getMarketingPush() {
        return marketingPush;
    }

    public void setMarketingPush(Boolean marketingPush) {
        this.marketingPush = marketingPush;
    }

    public Boolean getSsapProvisionAgreed() {
        return ssapProvisionAgreed;
    }
    public void setSsapProvisionAgreed(Boolean ssapProvisionAgreed) {
        this.ssapProvisionAgreed = ssapProvisionAgreed;
    }
    public Boolean getThirdPartyProvisionAgreed() {
        return thirdPartyProvisionAgreed;
    }
    public void setThirdPartyProvisionAgreed(Boolean thirdPartyProvisionAgreed) {
        this.thirdPartyProvisionAgreed = thirdPartyProvisionAgreed;
    }
    public Boolean getElectronicFinanceAgreed() {
        return electronicFinanceAgreed;
    }
    public void setElectronicFinanceAgreed(Boolean electronicFinanceAgreed) {
        this.electronicFinanceAgreed = electronicFinanceAgreed;
    }
    public Boolean getMonitoringAgreed() {
        return monitoringAgreed;
    }
    public void setMonitoringAgreed(Boolean monitoringAgreed) {
        this.monitoringAgreed = monitoringAgreed;
    }
    public Boolean getMarketingPersonalAgreed() {
        return marketingPersonalAgreed;
    }
    public void setMarketingPersonalAgreed(Boolean marketingPersonalAgreed) {
        this.marketingPersonalAgreed = marketingPersonalAgreed;
    }
    public java.time.LocalDateTime getTermsAgreedAt() {
        return termsAgreedAt;
    }
    public void setTermsAgreedAt(java.time.LocalDateTime termsAgreedAt) {
        this.termsAgreedAt = termsAgreedAt;
    }

    public java.time.LocalDateTime getThirdPartyProvisionRetentionUntil() {
        return thirdPartyProvisionRetentionUntil;
    }

    public void setThirdPartyProvisionRetentionUntil(java.time.LocalDateTime thirdPartyProvisionRetentionUntil) {
        this.thirdPartyProvisionRetentionUntil = thirdPartyProvisionRetentionUntil;
    }
}

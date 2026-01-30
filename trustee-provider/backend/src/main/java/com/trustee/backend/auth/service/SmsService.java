package com.trustee.backend.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * [SMS-TMP] SMS 발송 서비스 (임시 구현)
 * 
 * ====================================================================
 * 현재 상태: 테스트용 난수 OTP 생성 + 콘솔 출력
 * 향후 계획: AWS SNS 연동을 통한 실제 SMS 발송
 * ====================================================================
 * 
 * [개발 단계] 현재는 콘솔 로그로 OTP 출력 (테스트용)
 * [운영 단계] AWS SNS를 통한 실제 SMS 발송으로 전환
 * 
 * AWS SNS 설정 시 필요한 의존성 (pom.xml):
 * <dependency>
 *     <groupId>software.amazon.awssdk</groupId>
 *     <artifactId>sns</artifactId>
 *     <version>2.20.0</version>
 * </dependency>
 * 
 * application.properties 설정:
 * aws.sns.region=ap-northeast-2
 * aws.sns.access-key=${AWS_ACCESS_KEY}
 * aws.sns.secret-key=${AWS_SECRET_KEY}
 * sms.sender-id=SSAP
 */
@Service
public class SmsService {

    // ============================================================
    // [SMS-TMP] 현재는 AWS SNS 미연동 상태
    // [운영 전환 시 주석 해제] AWS SNS Client 설정
    // ============================================================
    // private final SnsClient snsClient;
    
    @Value("${sms.enabled:false}")
    private boolean smsEnabled;
    
    @Value("${sms.sender-id:SSAP}")
    private String senderId;
    
    // ============================================================
    // [운영 전환 시 주석 해제] AWS SNS 클라이언트 초기화
    // ============================================================
    /*
    @Value("${aws.sns.region:ap-northeast-2}")
    private String awsRegion;
    
    @Value("${aws.sns.access-key}")
    private String awsAccessKey;
    
    @Value("${aws.sns.secret-key}")
    private String awsSecretKey;
    
    @PostConstruct
    public void init() {
        if (smsEnabled) {
            this.snsClient = SnsClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(awsAccessKey, awsSecretKey)
                ))
                .build();
            System.out.println("[SMS-TMP] AWS SNS Client initialized for region: " + awsRegion);
        }
    }
    */

    /**
     * [SMS-TMP] OTP 인증번호 SMS 발송
     * 
     * 현재: 콘솔에 OTP 출력 (테스트용)
     * 향후: AWS SNS를 통한 실제 SMS 발송
     * 
     * @param phoneNumber 수신자 전화번호 (숫자만, 예: 01012345678)
     * @param otp 6자리 인증번호
     * @return 발송 성공 여부
     */
    public boolean sendOtp(String phoneNumber, String otp) {
        String formattedPhone = formatPhoneNumber(phoneNumber);
        String message = buildOtpMessage(otp);
        
        // ============================================================
        // [SMS-TMP] 개발 모드 - 콘솔 로그 출력 (테스트용 난수 OTP)
        // 실제 SMS는 AWS SNS 연동 후 활성화됩니다
        // ============================================================
        if (!smsEnabled) {
            System.out.println("========================================");
            System.out.println("[SMS-TMP] 개발 모드 - SMS 발송 시뮬레이션");
            System.out.println("[SMS-TMP] ※ 현재 OTP는 테스트용 난수입니다");
            System.out.println("[SMS-TMP] ※ AWS SNS 연동 예정");
            System.out.println("[SMS-TMP] 수신번호: " + formattedPhone);
            System.out.println("[SMS-TMP] 발신자ID: " + senderId);
            System.out.println("[SMS-TMP] 메시지 내용:");
            System.out.println(message);
            System.out.println("========================================");
            return true;
        }

        
        // ============================================================
        // [운영 전환 시 주석 해제] AWS SNS를 통한 실제 SMS 발송
        // ============================================================
        /*
        try {
            // 한국 번호 형식으로 변환 (+82)
            String e164Phone = convertToE164(formattedPhone);
            
            // SMS 메시지 속성 설정
            Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
            messageAttributes.put("AWS.SNS.SMS.SenderID", MessageAttributeValue.builder()
                .stringValue(senderId)
                .dataType("String")
                .build());
            messageAttributes.put("AWS.SNS.SMS.SMSType", MessageAttributeValue.builder()
                .stringValue("Transactional")  // 트랜잭션 메시지 (높은 신뢰도)
                .dataType("String")
                .build());
            
            // SMS 발송 요청
            PublishRequest request = PublishRequest.builder()
                .phoneNumber(e164Phone)
                .message(message)
                .messageAttributes(messageAttributes)
                .build();
            
            PublishResponse response = snsClient.publish(request);
            
            System.out.println("[SMS-TMP] SMS 발송 성공 - MessageId: " + response.messageId());
            System.out.println("[SMS-TMP] 수신번호: " + e164Phone);
            
            // [컴플라이언스] SMS 발송 이력 로깅 (개인정보 마스킹)
            logSmsSent(maskPhoneNumber(formattedPhone), response.messageId());
            
            return true;
            
        } catch (SnsException e) {
            System.err.println("[SMS-TMP] AWS SNS 발송 실패: " + e.getMessage());
            // [컴플라이언스] 발송 실패 로깅
            logSmsFailure(maskPhoneNumber(formattedPhone), e.getMessage());
            return false;
        }
        */
        
        return true;
    }
    
    /**
     * OTP 메시지 템플릿 생성
     * [컴플라이언스] 금융위원회 가이드라인 준수 문구
     */
    private String buildOtpMessage(String otp) {
        return String.format(
            "[SSAP 본인인증]\n" +
            "인증번호: %s\n" +
            "타인에게 절대 알려주지 마세요.\n" +
            "유효시간: 3분",
            otp
        );
    }
    
    /**
     * 전화번호 형식 정리 (숫자만 추출)
     */
    private String formatPhoneNumber(String phone) {
        if (phone == null) return "";
        return phone.replaceAll("\\D", "");
    }
    
    /**
     * E.164 국제 전화번호 형식으로 변환
     * 예: 01012345678 → +821012345678
     */
    private String convertToE164(String phone) {
        String cleaned = formatPhoneNumber(phone);
        if (cleaned.startsWith("0")) {
            return "+82" + cleaned.substring(1);
        }
        return "+82" + cleaned;
    }
    
    /**
     * [개인정보보호] 전화번호 마스킹
     * 예: 01012345678 → 010****5678
     */
    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 8) return "***";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
    
    // ============================================================
    // [운영 전환 시 주석 해제] 컴플라이언스 로깅 메서드
    // ============================================================
    /*
    private void logSmsSent(String maskedPhone, String messageId) {
        // TODO: 실제 로깅 시스템 연동 (ELK, CloudWatch 등)
        // [신용정보법] SMS 발송 이력 2년 보관 필요
        System.out.println(String.format(
            "[COMPLIANCE-LOG] SMS_SENT | phone=%s | messageId=%s | timestamp=%s",
            maskedPhone, messageId, java.time.Instant.now()
        ));
    }
    
    private void logSmsFailure(String maskedPhone, String errorMessage) {
        System.err.println(String.format(
            "[COMPLIANCE-LOG] SMS_FAILED | phone=%s | error=%s | timestamp=%s",
            maskedPhone, errorMessage, java.time.Instant.now()
        ));
    }
    */
}

package com.entrusting.backend.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import java.util.Map;

/**
 * Server-to-Server 인증 서비스
 * 
 * 위탁사 백엔드 → 수탁사 백엔드 직접 통신
 * "이 토큰 진짜 니네가 인증해준 거 맞아?" 검증
 * 
 * [보안 원칙]
 * 1. 프론트엔드가 아닌 백엔드 간 통신
 * 2. 토큰 일회성 소비 (USED 상태 처리)
 * 3. 검증 실패 시 명확한 에러 반환
 */
@Service
public class S2SAuthService {

    private final RestTemplate restTemplate;

    @Value("${trustee.api.base-url}")
    private String trusteeBaseUrl;

    public S2SAuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * [읽기 전용] 수탁사 토큰 상태 조회
     * - 상태만 확인, 토큰 소비하지 않음
     * - 콜백 등에서 단순 상태 확인용
     */
    public Map<String, Object> verifyTokenWithTrustee(String tokenId) {
        String url = trusteeBaseUrl + "/api/v1/auth/verify/" + tokenId;
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.getForEntity(url,
                    (Class<Map<String, Object>>) (Class<?>) Map.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("[ENTRUSTING-S2S] Token verified (read-only): " + tokenId);
                return response.getBody();
            }
        } catch (HttpClientErrorException e) {
            System.err.println("[ENTRUSTING-S2S] Verify failed - Status: " + e.getStatusCode() + 
                             ", Body: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("[ENTRUSTING-S2S] Verify connection failed: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * [일회성 소비] 수탁사 토큰 검증 및 소비
     * 
     * 회원가입, 계좌개설 등 중요 작업 시 사용
     * 토큰을 USED 상태로 변경하여 재사용 방지
     * 
     * [흐름]
     * 1. 위탁사 → 수탁사: POST /api/v1/auth/consume/{tokenId}
     * 2. 수탁사: 토큰 검증 후 USED 상태로 변경
     * 3. 수탁사 → 위탁사: 인증된 사용자 정보 반환
     * 
     * @param tokenId 소비할 토큰 ID
     * @return 인증된 사용자 정보 (status, name, phoneNumber)
     * @throws S2SAuthException 검증 실패 시
     */
    public Map<String, Object> consumeTokenWithTrustee(String tokenId) {
        String url = trusteeBaseUrl + "/api/v1/auth/consume/" + tokenId;
        
        try {
            System.out.println("[ENTRUSTING-S2S] Consuming token: " + tokenId);
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                url, 
                null,  // 요청 본문 없음
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();
                System.out.println("[ENTRUSTING-S2S] Token consumed successfully: " + tokenId);
                System.out.println("[ENTRUSTING-S2S] Response: status=" + body.get("status") + 
                                 ", name=" + body.get("name"));
                return body;
            }
            
        } catch (HttpClientErrorException e) {
            // 수탁사에서 반환한 에러 메시지 파싱
            String errorBody = e.getResponseBodyAsString();
            System.err.println("[ENTRUSTING-S2S] Consume failed - Status: " + e.getStatusCode() + 
                             ", Body: " + errorBody);
            
            // 에러 메시지 추출 시도
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> errorMap = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(errorBody, Map.class);
                String message = (String) errorMap.get("message");
                throw new S2SAuthException(message != null ? message : "토큰 검증 실패");
            } catch (S2SAuthException se) {
                throw se;
            } catch (Exception parseEx) {
                throw new S2SAuthException("토큰 검증 실패: " + e.getStatusCode());
            }
            
        } catch (S2SAuthException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("[ENTRUSTING-S2S] Consume connection failed: " + e.getMessage());
            throw new S2SAuthException("수탁사 서버 연결 실패: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * S2S 인증 예외
     */
    public static class S2SAuthException extends RuntimeException {
        public S2SAuthException(String message) {
            super(message);
        }
    }
}

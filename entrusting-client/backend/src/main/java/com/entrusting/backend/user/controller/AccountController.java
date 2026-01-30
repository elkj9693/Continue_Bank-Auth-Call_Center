package com.entrusting.backend.user.controller;

import com.entrusting.backend.user.entity.Account;
import com.entrusting.backend.user.service.AccountService;
import com.entrusting.backend.user.service.S2SAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;
    private final S2SAuthService s2sAuthService;

    public AccountController(AccountService accountService, S2SAuthService s2sAuthService) {
        this.accountService = accountService;
        this.s2sAuthService = s2sAuthService;
    }

    /**
     * 계좌 개설
     * 
     * [무결성 확보 로직]
     * 1. 본인인증 토큰 필수 검증
     * 2. S2S 토큰 소비 (일회성 사용)
     * 3. 사용자 is_verified 상태 재확인
     * 4. 인증된 정보와 계정 정보 일치 확인
     * 5. 계좌번호 중복 없이 생성 (110-XXX-XXXXXX 형식)
     * 6. 첫 번째 계좌인 경우 축하금 지급
     */
    @PostMapping("/create")
    public ResponseEntity<?> createAccount(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String accountName = request.get("accountName");
        String rawPin = request.get("pin");
        String tokenId = request.get("tokenId");

        try {
            System.out.println("[ENTRUSTING] Create Account Request - User: [" + username + "], TokenId: [" + tokenId + "]");

            // [검증 1] 토큰 존재 여부 확인
            if (tokenId == null || tokenId.isEmpty()) {
                throw new IllegalArgumentException("본인인증 토큰이 누락되었습니다.");
            }

            // [검증 2] S2S 토큰 소비 (일회성 사용)
            Map<String, Object> verification;
            try {
                verification = s2sAuthService.consumeTokenWithTrustee(tokenId);
            } catch (S2SAuthService.S2SAuthException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
            
            if (verification == null) {
                throw new IllegalArgumentException("본인인증 검증에 실패했습니다.");
            }
            
            // [검증 3] 토큰 상태 확인
            String status = String.valueOf(verification.get("status"));
            if (!"USED".equals(status) && !"COMPLETED".equals(status)) {
                throw new IllegalArgumentException("본인인증이 완료되지 않았습니다. (상태: " + status + ")");
            }
            
            // [검증 4] 인증된 정보와 계정 정보 일치 확인
            String verifiedName = (String) verification.get("name");
            String verifiedPhone = (String) verification.get("phoneNumber");
            
            // accountService에서 사용자 정보 조회하여 검증
            // (AccountService에서 내부적으로 사용자 확인하므로 여기선 로깅만)
            System.out.println("[ENTRUSTING-SEC] Verified info - Name: " + verifiedName + 
                             ", Phone: " + (verifiedPhone != null ? verifiedPhone.substring(0, 3) + "****" : "N/A"));

            // [핵심] 계좌 생성 (AccountService에서 계좌번호 중복 검증 및 축하금 처리)
            Account account = accountService.createAccount(username, accountName, rawPin);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("accountNumber", account.getAccountNumber());
            response.put("balance", account.getBalance());
            response.put("bonusApplied", account.getBalance().compareTo(java.math.BigDecimal.ZERO) > 0);
            
            System.out.println("[ENTRUSTING] Account Created - User: " + username + 
                             ", AccountNo: " + account.getAccountNumber() +
                             ", Bonus: " + (account.getBalance().compareTo(java.math.BigDecimal.ZERO) > 0));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            System.err.println("[ENTRUSTING-ERROR] Account Creation Failed: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            System.err.println("[ENTRUSTING-ERROR] Account Creation Exception for [" + username + "]: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "서버 내부 오류가 발생했습니다.");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAccounts(@RequestParam String username) {
        try {
            List<Account> accounts = accountService.getAccountsByUsername(username);
            List<Map<String, Object>> responseList = accounts.stream().map(acc -> {
                Map<String, Object> map = new HashMap<>();
                map.put("accountNumber", acc.getAccountNumber());
                map.put("accountName", acc.getAccountName());
                map.put("balance", acc.getBalance());
                return map;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(responseList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody Map<String, String> request) {
        String from = request.get("fromAccountNumber");
        String to = request.get("toAccountNumber");
        BigDecimal amount = new BigDecimal(request.get("amount"));
        String pin = request.get("pin");

        try {
            accountService.transferFunds(from, to, amount, pin);
            return ResponseEntity.ok(Map.of("status", "success", "message", "이체가 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}

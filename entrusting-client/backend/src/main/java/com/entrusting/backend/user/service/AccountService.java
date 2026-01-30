package com.entrusting.backend.user.service;

import com.entrusting.backend.user.entity.Account;
import com.entrusting.backend.user.entity.Transaction;
import com.entrusting.backend.user.entity.User;
import com.entrusting.backend.user.repository.AccountRepository;
import com.entrusting.backend.user.repository.TransactionRepository;
import com.entrusting.backend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 계좌 서비스
 * 
 * [2026 컴플라이언스 대응]
 * - 금융실명법: 실명 확인 후 계좌 개설
 * - 전자금융거래법: 거래 기록 5년 보관
 * - 개인정보보호법: 계좌번호 등 금융정보 암호화 저장
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    
    // [보안] SecureRandom 사용 (암호학적으로 안전한 난수)
    private final SecureRandom secureRandom = new SecureRandom();
    
    // 가입 축하금 금액 (설정 가능)
    @Value("${account.welcome-bonus:10000}")
    private BigDecimal welcomeBonus;
    
    // 계좌번호 생성 최대 재시도 횟수
    private static final int MAX_ACCOUNT_NUMBER_RETRY = 10;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository,
            TransactionRepository transactionRepository, BCryptPasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
        this.welcomeBonus = new BigDecimal("10000"); // 기본값 설정
    }
    
    /**
     * 고유한 계좌번호 생성
     * 형식: 110-XXX-XXXXXX (은행코드-지점코드-일련번호)
     * 
     * [무결성] 중복 방지를 위해 생성 후 DB 조회로 유일성 확인
     */
    private String generateUniqueAccountNumber() {
        for (int i = 0; i < MAX_ACCOUNT_NUMBER_RETRY; i++) {
            // 형식: 110-XXX-XXXXXX
            String accountNumber = String.format("110-%03d-%06d", 
                secureRandom.nextInt(1000),      // 지점코드 (000-999)
                secureRandom.nextInt(1000000)    // 일련번호 (000000-999999)
            );
            
            // [중복 검증] DB에서 해당 계좌번호가 이미 존재하는지 확인
            if (accountRepository.findByAccountNumber(accountNumber).isEmpty()) {
                System.out.println("[ACCOUNT] Generated unique account number: " + accountNumber);
                return accountNumber;
            }
            
            System.out.println("[ACCOUNT] Account number collision detected, retrying... (" + (i + 1) + "/" + MAX_ACCOUNT_NUMBER_RETRY + ")");
        }
        
        // 최대 재시도 초과 시 예외 (매우 드문 경우)
        throw new IllegalStateException("계좌번호 생성에 실패했습니다. 잠시 후 다시 시도해 주세요.");
    }

    /**
     * 계좌 개설
     * 
     * [무결성 검증 로직]
     * 1. 사용자 존재 여부 확인
     * 2. 사용자 본인인증 완료 여부 확인 (is_verified)
     * 3. 계좌 비밀번호 유효성 검증
     * 4. 고유 계좌번호 생성 (중복 방지)
     * 5. 첫 번째 계좌인 경우 축하금 지급 및 거래내역 기록
     * 
     * @param username 사용자 ID
     * @param accountName 계좌명
     * @param rawPin 계좌 비밀번호 (4자리)
     * @return 생성된 계좌
     */
    @Transactional
    public Account createAccount(String username, String accountName, String rawPin) {
        System.out.println("[ACCOUNT] Creating account for user: " + username);
        
        // [검증 1] 사용자 존재 여부 확인
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.err.println("[ACCOUNT-ERROR] User NOT FOUND: " + username);
                    return new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username);
                });

        // [검증 2] 본인인증 완료 여부 확인 (컴플라이언스 필수)
        if (!user.isVerified()) {
            System.err.println("[ACCOUNT-ERROR] User NOT VERIFIED: " + username);
            throw new IllegalArgumentException("본인인증이 완료되지 않은 사용자입니다. 본인인증을 먼저 진행해 주세요.");
        }
        
        // [검증 3] 계좌 비밀번호 유효성 검증
        if (rawPin == null || rawPin.length() != 4 || !rawPin.matches("\\d{4}")) {
            throw new IllegalArgumentException("계좌 비밀번호는 숫자 4자리여야 합니다.");
        }
        
        // [핵심] 고유 계좌번호 생성 (중복 검증 포함)
        String accountNumber = generateUniqueAccountNumber();

        // [보안] PIN 해싱 (Salt + BCrypt)
        String salt = UUID.randomUUID().toString().substring(0, 8);
        String hashedPin = passwordEncoder.encode(rawPin + salt);

        // [비즈니스 로직] 첫 번째 계좌 여부 확인
        List<Account> existingAccounts = accountRepository.findByUser(user);
        boolean isFirstAccount = existingAccounts.isEmpty();
        BigDecimal initialBalance = isFirstAccount ? welcomeBonus : BigDecimal.ZERO;

        System.out.println("[ACCOUNT] Creating account - First: " + isFirstAccount + 
                         ", Initial balance: " + initialBalance);

        // [계좌 생성]
        Account newAccount = new Account(accountNumber, accountName, initialBalance, user, hashedPin, salt);
        Account savedAccount = accountRepository.save(newAccount);

        // [거래내역 기록] 첫 번째 계좌인 경우 축하금 입금 내역 기록
        if (isFirstAccount && initialBalance.compareTo(BigDecimal.ZERO) > 0) {
            Transaction welcomeTransaction = new Transaction(
                null,                    // 출금 계좌 (없음 - 시스템 입금)
                savedAccount,            // 입금 계좌
                initialBalance,          // 금액
                "DEPOSIT",               // 거래 유형
                "가입 축하금"             // 적요
            );
            transactionRepository.save(welcomeTransaction);
            
            // [컴플라이언스 로그] 거래 기록 (전자금융거래법 준수)
            System.out.println(String.format(
                "[COMPLIANCE-TX] WELCOME_BONUS | account=%s | amount=%s | timestamp=%s",
                accountNumber, initialBalance, LocalDateTime.now()
            ));
        }

        System.out.println("[ACCOUNT] Account created successfully - Number: " + accountNumber + 
                         ", User: " + username);
        return savedAccount;
    }

    @Transactional
    public void transferFunds(String fromAccountNumber, String toAccountNumber, BigDecimal amount, String pin) {
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("출금 계좌를 찾을 수 없습니다."));

        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("입금 계좌를 찾을 수 없습니다."));

        // [추가] 비밀번호 오류 횟수 및 상태 확인
        if ("SUSPENDED".equals(fromAccount.getStatus()) || fromAccount.getPinFailCount() >= 5) {
            throw new IllegalArgumentException("비밀번호 5회 오류로 인해 계좌가 정지되었습니다. 고객센터에 문의하세요.");
        }

        if (!passwordEncoder.matches(pin + fromAccount.getSalt(), fromAccount.getAccountPin())) {
            // PIN 불일치 - 카운트 증가
            int newCount = fromAccount.getPinFailCount() + 1;
            fromAccount.setPinFailCount(newCount);
            if (newCount >= 5) {
                fromAccount.setStatus("SUSPENDED");
            }
            accountRepository.save(fromAccount);

            String errorMsg = "계좌 비밀번호가 일치하지 않습니다.";
            if (newCount >= 5) {
                errorMsg = "비밀번호 5회 연속 불일치로 계좌가 정지되었습니다.";
            } else {
                errorMsg += " (현재 " + newCount + "회 연속 오류)";
            }
            throw new IllegalArgumentException(errorMsg);
        }

        // 성공 시 오류 카운트 초기화
        fromAccount.setPinFailCount(0);

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("잔액이 부족합니다.");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction transaction = new Transaction(fromAccount, toAccount, amount, "TRANSFER", "계좌 이체");
        transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public List<Account> getAccountsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
        return accountRepository.findByUser(user);
    }
}

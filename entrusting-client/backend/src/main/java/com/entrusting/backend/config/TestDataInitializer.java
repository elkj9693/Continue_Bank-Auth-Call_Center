package com.entrusting.backend.config;

import com.entrusting.backend.ars.entity.Card;
import com.entrusting.backend.ars.entity.Lead;
import com.entrusting.backend.ars.repository.CardRepository;
import com.entrusting.backend.ars.repository.LeadRepository;
import com.entrusting.backend.user.entity.User;
import com.entrusting.backend.user.repository.UserRepository;
import com.entrusting.backend.util.EncryptionUtils;
import org.springframework.boot.CommandLineRunner;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Seed test data for development.
 * 테스트용 고객 데이터를 초기화합니다.
 */
@Configuration
public class TestDataInitializer {

    @Bean
    CommandLineRunner initTestData(UserRepository userRepository, CardRepository cardRepository,
            LeadRepository leadRepository, BCryptPasswordEncoder passwordEncoder) {
        return args -> {
            // 테스트 고객 리스트
            String[][] testUsers = {
                    { "홍길동", "hong", "01000000000" },
                    { "홍길순", "hong2", "01000000001" },
                    { "고길동", "go", "01000000002" },
                    { "차은우", "cha", "01010041004" },
                    { "일영이", "one", "01011111111" },
                    { "테스트", "test", "01012345678" },
                    { "ARS테스터1", "ars1", "01099991111" },
                    { "ARS테스터2", "ars2", "01099992222" }
            };

            for (String[] u : testUsers) {
                String plainPhone = u[2];
                // 1. 사용자 조회 (평문 또는 암호화)
                List<User> users = userRepository.findByPhoneNumber(plainPhone);
                if (users.isEmpty()) {
                    users = userRepository.findByPhoneNumber(EncryptionUtils.encrypt(plainPhone));
                }

                User user;
                if (users.isEmpty()) {
                    // 사용자 없으면 생성
                    user = new User(
                            EncryptionUtils.encrypt(u[0]),
                            u[1],
                            passwordEncoder.encode("test1234"),
                            plainPhone,
                            true);
                    user.setTermsAgreed(true);
                    user.setPrivacyAgreed(true);
                    user.setUniqueIdAgreed(true);
                    user.setMarketingAgreed(true);
                    user.setMarketingSms(true);
                    user.setTermsAgreedAt(java.time.LocalDateTime.now());
                    user = userRepository.save(user);
                    System.out.println("[TEST-DATA] Added missing user: " + u[0] + " (" + u[2] + ")");
                } else {
                    user = users.get(0);
                }

                // 2. 카드 생성 로직 (ARS 테스터는 3장, 나머지는 1장)
                if (cardRepository.findByCustomerRefOrderByIdAsc(user.getId().toString()).isEmpty()) {
                    int cardCount = (u[1].startsWith("ars")) ? 3 : 1;

                    for (int i = 0; i < cardCount; i++) {
                        Card card = Card.builder()
                                .cardRef(java.util.UUID.randomUUID().toString())
                                .customerRef(user.getId().toString())
                                .cardNo(generateCardNo(i)) // 순차적 카드 번호 생성
                                .pinHash(sha256Hash("1234"))
                                .status("ACTIVE")
                                .build();
                        cardRepository.save(card);
                    }
                    System.out.println(
                            "[TEST-DATA] Created " + cardCount + " cards for ID: " + user.getId() + " (" + u[0] + ")");
                }

                // 3. 상담 신청(Lead) 데이터가 없으면 생성 (Outbound 테스트용)
                if (leadRepository.findByCustomerRef(user.getId().toString()).isEmpty()) {
                    Lead lead = Lead.builder()
                            .customerRef(user.getId().toString())
                            .name(u[0])
                            .phone(plainPhone)
                            .requestedProductType("신용카드")
                            .status("PENDING")
                            .build();
                    leadRepository.save(lead);
                    System.out.println("[TEST-DATA] Created lead for outbound test: " + u[0]);
                }
            }
        };
    }

    private String generateCardNo(int seed) {
        String prefix = String.format("%04d", 1000 + seed * 1111);
        return prefix + "-1111-2222-3333";
    }

    private String sha256Hash(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
